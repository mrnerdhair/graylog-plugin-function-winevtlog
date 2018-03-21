package org.reidrankin.graylogplugins.winevtlog;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class WinEvtLogMetaData implements PluginMetaData {
    private static final String PLUGIN_PROPERTIES = "org.reidrankin.graylogplugins.graylog-plugin-function-winevtlog/graylog-plugin.properties";

    @Override
    public String getUniqueId() {
        return "org.reidrankin.graylogplugins.winevtlog.WinEvtLog";
    }

    @Override
    public String getName() {
        return "WinEvtLogPlugin";
    }

    @Override
    public String getAuthor() {
        return "Reid Rankin <reidrankin@gmail.com>";
    }

    @Override
    public URI getURL() {
        return URI.create("https://github.com/reidrankin/graylog-plugin-function-winevtlog");
    }

    @Override
    public Version getVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "version", Version.from(0, 0, 0, "unknown"));
    }

    @Override
    public String getDescription() {
        return "Pipeline function that parses WinEvtLog messages.";
    }

    @Override
    public Version getRequiredVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "graylog.version", Version.from(2, 3, 4));
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
