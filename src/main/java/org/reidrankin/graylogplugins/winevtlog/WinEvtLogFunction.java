package org.reidrankin.graylogplugins.winevtlog;

import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.expressions.Expression;
import org.graylog.plugins.pipelineprocessor.ast.functions.AbstractFunction;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;
import com.google.inject.TypeLiteral;
import java.util.Collections;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
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

    private static final ArrayList<Pattern> winEvtLogPatterns = new ArrayList<Pattern>();
    
    @Override
    public Map<String, String> evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
        final String input = inputParam.required(functionArgs, evaluationContext);
        final String prefix = prefixParam.optional(functionArgs, evaluationContext).orElse("");

        if (input == null) {
            return Collections.emptyMap();
        }

        final Map<String, String> map = new LinkedHashMap<>();
        try {
          for (int i = 0; true; i++) {
            Pattern pattern = ((winEvtLogPatterns.size() > i) ? winEvtLogPatterns.get(i) : null);
            if (pattern == null) {
              pattern = Pattern.compile("^((?:.(?!    ))*?.)(?:    (?: *(?:((?:[^\\s]+? )*?[^\\s]+?):   )?((?:[^\\s]+? )*?[^\\s]+?):  ?((?:(?:.(?!  ))*).)  ){" + Integer.toString(i + 1) + "}) *(.*)$");
              winEvtLogPatterns.add(pattern);
            }
            Matcher matcher = pattern.matcher(input);
            if (!matcher.find()) break;
            if (matcher.group(3) == null || matcher.group(4) == null) break;
            
            String fieldName = prefix;
            if ((matcher.group(2) != null) && (matcher.group(2).length() > 0)) fieldName += matcher.group(2) + "__";
            fieldName += matcher.group(3);
            fieldName = fieldName.replace(" ", "_");
            map.put(fieldName, matcher.group(4));
          }
        } catch (Exception e) {
          LOG.error("Error while parsing WinEvtLog message: {}", input, e);
        }
        return map;
    }

    @Override
    public FunctionDescriptor<Map<String, String>> descriptor() {
        return FunctionDescriptor.<Map<String, String>>builder()
                .name(NAME)
                .description("Parses WinEvtLog output.")
                .params(inputParam, prefixParam)
                .returnType((Class<? extends Map<String, String>>) new TypeLiteral<Map<String, String>>() {}.getRawType())
                .build();
    }
}
