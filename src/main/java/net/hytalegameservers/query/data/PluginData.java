package net.hytalegameservers.query.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hytalegameservers.query.QueryPlugin;
import net.hytalegameservers.query.config.Config;
import net.hytalegameservers.query.data.dto.PluginDto;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class PluginData {

    private PluginDto selfPlugin;
    private List<PluginDto> allPlugins;

    public PluginData(final QueryPlugin plugin, final Config config) {
        this.selfPlugin = new PluginDto(plugin);
        this.allPlugins = config.isSendPluginInfo() ? QueryPlugin.HYTALE_SERVER.getPluginManager().getPlugins().stream().map(PluginDto::new).toList() : Collections.emptyList();
    }
}