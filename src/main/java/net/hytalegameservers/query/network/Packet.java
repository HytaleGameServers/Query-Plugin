package net.hytalegameservers.query.network;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hytalegameservers.query.network.enums.PacketType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Packet {

    private PacketType type;
    private String nodeId, payload;

    public static Packet heartbeat(final String nodeId) {
        return new Packet(PacketType.HEARTBEAT, nodeId, null);
    }

    public static Packet heartbeatAck(final String nodeId) {
        return new Packet(PacketType.HEARTBEAT_ACK, nodeId, null);
    }

    public static Packet requestData(final String nodeId) {
        return new Packet(PacketType.REQUEST_DATA, nodeId, null);
    }

    public static Packet data(final String nodeId, final String snapshotJson) {
        return new Packet(PacketType.DATA, nodeId, snapshotJson);
    }

    public static Packet shutdown(final String nodeId) {
        return new Packet(PacketType.SHUTDOWN, nodeId, null);
    }
}