package net.hytalegameservers.query.network;

import io.github.trae.di.annotations.method.ApplicationReady;
import io.github.trae.di.annotations.method.PreDestroy;
import io.github.trae.di.annotations.type.component.Service;
import io.github.trae.hf.Manager;
import io.github.trae.hytale.framework.utility.UtilTask;
import io.github.trae.utilities.UtilLogger;
import lombok.RequiredArgsConstructor;
import net.hytalegameservers.query.QueryPlugin;
import net.hytalegameservers.query.config.Config;
import net.hytalegameservers.query.config.NetworkConfig;
import net.hytalegameservers.query.constants.Constants;
import net.hytalegameservers.query.network.enums.PacketType;
import net.hytalegameservers.query.network.interfaces.INetworkManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Manages inter-node TCP communication for network mode.
 *
 * <p>Each node runs a TCP server and connects to all peers as a client.
 * Heartbeats track liveness. The node with the lowest alive {@code nodeId}
 * (lexicographic) is the controller.</p>
 *
 * <p>When the controller is about to send an update to HGS, it calls
 * {@link #requestSnapshots(long)} which broadcasts {@link PacketType#REQUEST_DATA}
 * to all peers. Each peer responds with a {@link PacketType#DATA} packet containing
 * their current snapshot. The controller collects responses within the timeout
 * and aggregates them into the update payload.</p>
 */
@RequiredArgsConstructor
@Service
public class NetworkManager implements Manager<QueryPlugin>, INetworkManager {

    private static final long HEARTBEAT_INTERVAL_SECONDS = 5L;
    private static final long NODE_TIMEOUT_MS = 15_000L;
    private static final int SOCKET_TIMEOUT_MS = 30_000;

    private final Config config;
    private final NetworkConfig networkConfig;

    /**
     * Tracks last heartbeat time per remote nodeId.
     */
    private final Map<String, Long> lastHeartbeatMap = new ConcurrentHashMap<>();

    /**
     * Active outbound connections to peers.
     */
    private final List<PeerConnection> peerConnections = new CopyOnWriteArrayList<>();

    /**
     * Pending snapshot request — when the controller sends REQUEST_DATA, it creates
     * a latch and a collection map. Peers' DATA responses populate the map and count
     * down the latch.
     */
    private volatile CountDownLatch snapshotLatch;
    private final Map<String, NodeSnapshot> pendingSnapshots = new ConcurrentHashMap<>();

    private volatile boolean running = false;
    private volatile String lastControllerId = null;
    private ServerSocket serverSocket;

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @ApplicationReady
    public void onApplicationReady() {
        if (!(this.config.isEnabled())) {
            return;
        }

        if (!(this.networkConfig.isNetworkMode())) {
            return;
        }

        this.running = true;

        UtilLogger.info("<yellow>Starting network node %s on port %s.".formatted(this.networkConfig.getNodeId(), this.networkConfig.getPort()));

        UtilTask.executeAsynchronous(this::runServer);

        for (final String node : this.networkConfig.getNodes()) {
            UtilTask.executeAsynchronous(() -> this.connectToPeer(node));
        }

        UtilTask.scheduleAsynchronous(this::sendHeartbeats, (int) HEARTBEAT_INTERVAL_SECONDS, (int) HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void onPreDestroy() {
        if (!(this.config.isEnabled())) {
            return;
        }

        this.running = false;

        for (final PeerConnection peerConnection : this.peerConnections) {
            peerConnection.close();
        }

        this.peerConnections.clear();

        try {
            if (this.serverSocket != null && !this.serverSocket.isClosed()) {
                this.serverSocket.close();
            }
        } catch (final Exception e) {
            UtilLogger.severe("Failed to close server socket: %s".formatted(e.getMessage()));
        }

        UtilLogger.info("<green>Network node shut down.");
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    @Override
    public boolean isController() {
        final String controllerId = this.resolveControllerId();

        if (this.lastControllerId == null || !(this.lastControllerId.equals(controllerId))) {
            UtilLogger.info("<aqua>Controller elected: %s".formatted(controllerId + (controllerId.equals(this.networkConfig.getNodeId()) ? " (this node)" : "")));
            this.lastControllerId = controllerId;
        }

        return this.networkConfig.getNodeId().equals(controllerId);
    }

    @Override
    public List<String> getAliveNodeIds() {
        final List<String> alive = new ArrayList<>(this.lastHeartbeatMap.entrySet().stream().filter(entry -> System.currentTimeMillis() - entry.getValue() <= NODE_TIMEOUT_MS).map(Map.Entry::getKey).toList());

        alive.add(this.networkConfig.getNodeId());

        Collections.sort(alive);

        return alive;
    }

    @Override
    public Map<String, NodeSnapshot> requestSnapshots(final long timeoutMs) {
        final List<String> aliveNodes = this.getAliveNodeIds();
        // Exclude self
        final int expectedResponses = (int) aliveNodes.stream().filter(id -> !id.equals(this.networkConfig.getNodeId())).count();

        if (expectedResponses == 0) {
            return Map.of();
        }

        this.pendingSnapshots.clear();
        this.snapshotLatch = new CountDownLatch(expectedResponses);

        // Broadcast REQUEST_DATA to all peers
        try {
            final String line = Constants.OBJECT_MAPPER.writeValueAsString(Packet.requestData(this.networkConfig.getNodeId()));

            for (final PeerConnection peerConnection : this.peerConnections) {
                peerConnection.send(line);
            }
        } catch (final Exception e) {
            UtilLogger.severe("Failed to broadcast snapshot request: %s".formatted(e.getMessage()));
            return Map.of();
        }

        // Wait for responses
        try {
            this.snapshotLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return Map.copyOf(this.pendingSnapshots);
    }

    @Override
    public void broadcastShutdown() {
        try {
            final String line = Constants.OBJECT_MAPPER.writeValueAsString(Packet.shutdown(this.networkConfig.getNodeId()));

            for (final PeerConnection peerConnection : this.peerConnections) {
                peerConnection.send(line);
            }
        } catch (final Exception e) {
            UtilLogger.severe("Failed to broadcast shutdown: %s".formatted(e.getMessage()));
        }
    }

    // -----------------------------------------------------------------------
    // TCP Server
    // -----------------------------------------------------------------------

    private void runServer() {
        try {
            this.serverSocket = new ServerSocket(this.networkConfig.getPort());

            while (this.running) {
                final Socket clientSocket = this.serverSocket.accept();
                UtilTask.executeAsynchronous(() -> this.handleInbound(clientSocket));
            }
        } catch (final Exception e) {
            if (this.running) {
                UtilLogger.severe("Server socket error: %s".formatted(e.getMessage()));
            }
        }
    }

    private void handleInbound(final Socket socket) {
        try {
            socket.setSoTimeout(SOCKET_TIMEOUT_MS);
        } catch (final Exception e) {
            UtilLogger.warning("Failed to set socket timeout: %s".formatted(e.getMessage()));
        }

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             final PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line;
            while (this.running && (line = reader.readLine()) != null) {
                this.handlePacket(line, writer);
            }
        } catch (final SocketTimeoutException e) {
            if (this.running) {
                UtilLogger.warning("Inbound connection timed out: %s".formatted(e.getMessage()));
            }
        } catch (final Exception e) {
            if (this.running) {
                UtilLogger.warning("Inbound connection error: %s".formatted(e.getMessage()));
            }
        } finally {
            try {
                socket.close();
            } catch (final Exception ignored) {
            }
        }
    }

    // -----------------------------------------------------------------------
    // TCP Client
    // -----------------------------------------------------------------------

    private void connectToPeer(final String hostPort) {
        final String[] parts = hostPort.split(":");
        if (parts.length != 2) {
            UtilLogger.severe("Invalid node address: <red>%s</red>".formatted(hostPort));
            return;
        }

        final String address = parts[0];
        final int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (final NumberFormatException e) {
            UtilLogger.severe("Invalid port in node address: <red>%s</red>".formatted(hostPort));
            return;
        }

        while (this.running) {
            try {
                final Socket socket = new Socket(address, port);
                socket.setSoTimeout(SOCKET_TIMEOUT_MS);
                final PeerConnection peerConnection = new PeerConnection(hostPort, socket);
                this.peerConnections.add(peerConnection);

                UtilLogger.info("<green>Connected to peer %s".formatted(hostPort));

                final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                String line;
                while (this.running) {
                    try {
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        this.handlePacket(line, peerConnection.getPrintWriter());
                    } catch (final SocketTimeoutException ignored) {
                        // Timeout is expected — re-evaluate running flag
                    }
                }
            } catch (final Exception e) {
                if (this.running) {
                    UtilLogger.warning("Failed to connect to <red>%s</red>, retrying in 5s...".formatted(hostPort));
                }
            } finally {
                this.peerConnections.removeIf(p -> p.getAddress().equals(hostPort));
            }

            if (this.running) {
                try {
                    Thread.sleep(5000);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Packet handling
    // -----------------------------------------------------------------------

    private void handlePacket(final String line, final PrintWriter responseWriter) {
        try {
            final Packet packet = Constants.OBJECT_MAPPER.readValue(line, Packet.class);

            switch (packet.getType()) {
                case HEARTBEAT -> {
                    this.lastHeartbeatMap.put(packet.getNodeId(), System.currentTimeMillis());
                    final String ack = Constants.OBJECT_MAPPER.writeValueAsString(Packet.heartbeatAck(this.networkConfig.getNodeId()));
                    responseWriter.println(ack);
                }
                case HEARTBEAT_ACK -> {
                    this.lastHeartbeatMap.put(packet.getNodeId(), System.currentTimeMillis());
                }
                case REQUEST_DATA -> {
                    this.lastHeartbeatMap.put(packet.getNodeId(), System.currentTimeMillis());

                    // Build local snapshot and send it back immediately.
                    final NodeSnapshot nodeSnapshot = NodeSnapshot.create(this.getPlugin(), this.networkConfig, this.config);

                    final String json = Constants.OBJECT_MAPPER.writeValueAsString(nodeSnapshot);
                    final String response = Constants.OBJECT_MAPPER.writeValueAsString(Packet.data(this.networkConfig.getNodeId(), json));

                    responseWriter.println(response);
                }
                case DATA -> {
                    this.lastHeartbeatMap.put(packet.getNodeId(), System.currentTimeMillis());

                    if (packet.getPayload() != null) {
                        final NodeSnapshot snapshot = Constants.OBJECT_MAPPER.readValue(packet.getPayload(), NodeSnapshot.class);
                        this.pendingSnapshots.put(packet.getNodeId(), snapshot);

                        if (this.snapshotLatch != null) {
                            this.snapshotLatch.countDown();
                        }
                    }
                }
                case SHUTDOWN -> {
                    this.lastHeartbeatMap.remove(packet.getNodeId());
                    UtilLogger.info("<yellow>Node %s has shut down.".formatted(packet.getNodeId()));
                }
            }
        } catch (final Exception e) {
            UtilLogger.warning("Failed to handle packet: %s".formatted(e.getMessage()));
        }
    }

    // -----------------------------------------------------------------------
    // Heartbeats & Election
    // -----------------------------------------------------------------------

    private void sendHeartbeats() {
        try {
            final String line = Constants.OBJECT_MAPPER.writeValueAsString(Packet.heartbeat(this.networkConfig.getNodeId()));

            for (final PeerConnection peerConnection : this.peerConnections) {
                peerConnection.send(line);
            }
        } catch (final Exception e) {
            UtilLogger.warning("Failed to send heartbeats: %s".formatted(e.getMessage()));
        }
    }

    private String resolveControllerId() {
        final List<String> alive = this.getAliveNodeIds();

        return alive.isEmpty() ? this.networkConfig.getNodeId() : alive.getFirst();
    }
}