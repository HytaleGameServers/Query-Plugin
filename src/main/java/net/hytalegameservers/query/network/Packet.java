package net.hytalegameservers.query.network;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hytalegameservers.query.network.enums.PacketType;

/**
 * TCP message envelope exchanged between network nodes.
 *
 * <p>Serialized as a single JSON line (newline-delimited) over the socket.
 * The {@code payload} field is only used for {@link PacketType#DATA} packets,
 * which contain a JSON-encoded {@link NodeSnapshot}.</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Packet {

    private PacketType type;
    private String nodeId, payload;

    /**
     * Creates a heartbeat packet (expects a HEARTBEAT_ACK response).
     */
    public static Packet heartbeat(final String nodeId) {
        return new Packet(PacketType.HEARTBEAT, nodeId, null);
    }

    /**
     * Creates a heartbeat acknowledgement.
     */
    public static Packet heartbeatAck(final String nodeId) {
        return new Packet(PacketType.HEARTBEAT_ACK, nodeId, null);
    }

    /**
     * Creates a snapshot request — sent by the controller to all peers.
     */
    public static Packet requestData(final String nodeId) {
        return new Packet(PacketType.REQUEST_DATA, nodeId, null);
    }

    /**
     * Creates a data response containing a serialized {@link NodeSnapshot}.
     */
    public static Packet data(final String nodeId, final String snapshotJson) {
        return new Packet(PacketType.DATA, nodeId, snapshotJson);
    }

    /**
     * Creates a shutdown notification — informs peers this node is leaving.
     */
    public static Packet shutdown(final String nodeId) {
        return new Packet(PacketType.SHUTDOWN, nodeId, null);
    }
}
