package net.hytalegameservers.query.network;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hytalegameservers.query.QueryPlugin;
import net.hytalegameservers.query.config.Config;
import net.hytalegameservers.query.config.NetworkConfig;
import net.hytalegameservers.query.data.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NodeSnapshot {

    private String nodeId;
    private long timestamp;
    private SystemData systemData;
    private PluginData pluginData;
    private ServerData serverData;
    private WorldData worldData;
    private PlayerData playerData;

    public static NodeSnapshot create(final QueryPlugin plugin, final NetworkConfig networkConfig, final Config config) {
        return new NodeSnapshot(
                networkConfig.getNodeId(),
                System.currentTimeMillis(),
                SystemData.create(config),
                new PluginData(plugin, config),
                ServerData.create(),
                WorldData.create(),
                new PlayerData(config)
        );
    }
}
