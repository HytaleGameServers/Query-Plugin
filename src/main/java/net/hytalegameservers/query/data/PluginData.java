package net.hytalegameservers.query.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hytalegameservers.query.QueryPlugin;
import net.hytalegameservers.query.config.Config;
import net.hytalegameservers.query.data.dto.PluginDto;

import java.util.Collections;
import java.util.List;

/**
 * Snapshot of installed plugin information on this server instance.
 *
 * <p>Always includes the query plugin itself ({@code selfPlugin}). The full
 * plugin list is only included if {@link Config#isSendPluginInfo()} is enabled.</p>
 */
@NoArgsConstructor
@Getter
@Setter
public class PluginData {

    /**
     * This plugin's own metadata (always sent).
     */
    private PluginDto selfPlugin;

    /**
     * All installed plugins on the server (empty if redacted by config).
     */
    private List<PluginDto> allPlugins;

    public PluginData(final QueryPlugin plugin, final Config config) {
        this.selfPlugin = new PluginDto(plugin);
        this.allPlugins = config.isSendPluginInfo() ? QueryPlugin.HYTALE_SERVER.getPluginManager().getPlugins().stream().map(PluginDto::new).toList() : Collections.emptyList();
    }
}
