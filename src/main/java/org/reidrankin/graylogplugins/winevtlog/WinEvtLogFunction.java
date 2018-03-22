package org.reidrankin.graylogplugins.winevtlog;

import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.functions.AbstractFunction;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;
import com.google.inject.TypeLiteral;
import java.util.Collections;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WinEvtLogFunction extends AbstractFunction<Map<String, String>> {
    private static final Logger LOG = LoggerFactory.getLogger(WinEvtLogFunction.class);
    public static final String NAME = "parse_winevtlog";
    private static final String INPUTPARAM = "input";
    private static final String PREFIXPARAM = "field_prefix";

    private final ParameterDescriptor<String, String> inputParam = ParameterDescriptor
            .string(INPUTPARAM)
            .description("The input string.")
            .build();
    
    private final ParameterDescriptor<String, String> prefixParam = ParameterDescriptor
            .string(PREFIXPARAM)
            .description("Prefix for returned keys.")
            .build();

    private static final Pattern masterPattern = Pattern.compile("^((?:.(?!    ))*?.)    (?: *(?:((?:[^\\s]+? )*?[^\\s]+?):   )?((?:[^\\s]+? )*?[^\\s]+?):  ?((?:(?:.(?!  ))*).)  ){1,} *(.*)$");
    private static final Pattern detailPattern = Pattern.compile(" *(?:((?:[^\\s]+? )*?[^\\s]+?):   )?((?:[^\\s]+? )*?[^\\s]+?):  ?((?:(?:.(?!  ))*).)  ");
    @Override
    public Map<String, String> evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
        final String input = inputParam.required(functionArgs, evaluationContext);
        final String prefix = prefixParam.optional(functionArgs, evaluationContext).orElse("");

        if (input == null) {
            return Collections.emptyMap();
        }

        final Map<String, String> map = new LinkedHashMap<>();
        try {
          final Matcher masterMatcher = masterPattern.matcher(input);
          if (masterMatcher.matches()) {
            map.put(prefix + "message", masterMatcher.group(1));
            map.put(prefix + "description", masterMatcher.group(5));

            final Matcher detailMatcher = detailPattern.matcher(input);
            detailMatcher.region(masterMatcher.end(1), input.length());

            while (detailMatcher.find()) {
              if (detailMatcher.group(2) == null || detailMatcher.group(3) == null) continue;

              String fieldName = prefix;
              if (detailMatcher.group(1) != null && detailMatcher.group(1).length() > 0) {
                fieldName += detailMatcher.group(1) + "__";
              }
              fieldName += detailMatcher.group(2);
              fieldName = fieldName.replace(" ", "_");

              map.put(fieldName, detailMatcher.group(3));
            }
          }
        } catch (Exception e) {
          LOG.error("Error while parsing WinEvtLog message: {}", input, e);
        }
        return map;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FunctionDescriptor<Map<String, String>> descriptor() {
        return FunctionDescriptor.<Map<String, String>>builder()
                .name(NAME)
                .description("Parses WinEvtLog output.")
                .params(inputParam, prefixParam)
                .returnType((Class<? extends Map<String, String>>) new TypeLiteral<Map<String, String>>() {}.getRawType())
                .build();
    }
}
