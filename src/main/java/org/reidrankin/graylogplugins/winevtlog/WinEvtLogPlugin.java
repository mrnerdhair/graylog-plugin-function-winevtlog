package org.reidrankin.graylogplugins.winevtlog;

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;

import java.util.Collection;
import java.util.Collections;

public class WinEvtLogPlugin implements Plugin {
    @Override
    public PluginMetaData metadata() {
        return new WinEvtLogMetaData();
    }

    @Override
    public Collection<PluginModule> modules() {
        return Collections.singleton(new WinEvtLogModule());
    }
}
