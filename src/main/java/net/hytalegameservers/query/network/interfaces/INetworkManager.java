package net.hytalegameservers.query.network.interfaces;

import net.hytalegameservers.query.network.NodeSnapshot;

import java.util.List;
import java.util.Map;

public interface INetworkManager {

    boolean isController();

    List<String> getAliveNodeIds();

    Map<String, NodeSnapshot> requestSnapshots(final long timeoutMs);

    void broadcastShutdown();
}
