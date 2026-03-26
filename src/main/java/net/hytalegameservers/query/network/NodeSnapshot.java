package net.hytalegameservers.query.network;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hytalegameservers.query.QueryPlugin;
import net.hytalegameservers.query.config.Config;
import net.hytalegameservers.query.config.NetworkConfig;
import net.hytalegameservers.query.data.*;

/**
 * A point-in-time data snapshot from a single node.
 *
 * <p>In standalone mode, one snapshot is created per update cycle. In network
 * mode, the controller collects snapshots from all peers and aggregates them
 * into a single API payload.</p>
 *
 * <p>Serialized as JSON over TCP between nodes (inside {@link Packet#getPayload()})
 * and also used locally by {@link net.hytalegameservers.query.api.ApiManager}
 * when building the update payload.</p>
 */
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

    /**
     * Creates a snapshot from the current state of this server node.
     */
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
