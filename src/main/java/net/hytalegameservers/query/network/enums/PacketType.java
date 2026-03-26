package net.hytalegameservers.query.network.enums;

/**
 * Types of packets exchanged between network nodes over TCP.
 */
public enum PacketType {

    /**
     * Periodic liveness check — expects a {@link #HEARTBEAT_ACK} response.
     */
    HEARTBEAT,

    /**
     * Response to a {@link #HEARTBEAT}.
     */
    HEARTBEAT_ACK,

    /**
     * Sent by the controller to request a data snapshot from each peer.
     */
    REQUEST_DATA,

    /**
     * A peer's response to {@link #REQUEST_DATA}, containing a serialized snapshot.
     */
    DATA,

    /**
     * Broadcast when a node is shutting down — peers remove it from the alive list.
     */
    SHUTDOWN
}
