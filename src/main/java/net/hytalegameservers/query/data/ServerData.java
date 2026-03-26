package net.hytalegameservers.query.data;

import com.hypixel.hytale.server.core.HytaleServer;
import io.github.trae.utilities.UtilString;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hytalegameservers.query.QueryPlugin;

import java.net.InetSocketAddress;

/**
 * Snapshot of core server metadata.
 *
 * <p>Includes connection details, server name, MOTD, version, password
 * protection status, and boot timestamp. Address and port are resolved
 * from the server's non-loopback binding.</p>
 */
@NoArgsConstructor
@Getter
@Setter
public class ServerData {

    private String address, name, version, motd;
    private Integer port;
    private Boolean passwordProtected;
    private Long bootStartAt;

    /**
     * Creates a snapshot from the current server state.
     */
    public static ServerData create() {
        final ServerData serverData = new ServerData();
        serverData.setAddress(serverData.buildAddress());
        serverData.setName(QueryPlugin.HYTALE_SERVER_CONFIG.getServerName());
        serverData.setVersion(HytaleServer.class.getPackage().getImplementationVersion());
        serverData.setMotd(QueryPlugin.HYTALE_SERVER_CONFIG.getMotd());
        serverData.setPort(serverData.buildPort());
        serverData.setPasswordProtected(!(UtilString.isEmpty(QueryPlugin.HYTALE_SERVER_CONFIG.getPassword())));
        serverData.setBootStartAt(QueryPlugin.BOOT_START_AT);
        return serverData;
    }

    /**
     * Resolves the server's non-loopback address, or {@code null} if unavailable.
     */
    private String buildAddress() {
        try {
            final InetSocketAddress nonLoopbackAddress = QueryPlugin.SERVER_MANAGER.getNonLoopbackAddress();
            if (nonLoopbackAddress != null) {
                return nonLoopbackAddress.getHostString();
            }
        } catch (final Exception ignored) {
        }

        return null;
    }

    /**
     * Resolves the server's non-loopback port, or {@code null} if unavailable.
     */
    private Integer buildPort() {
        try {
            final InetSocketAddress nonLoopbackAddress = QueryPlugin.SERVER_MANAGER.getNonLoopbackAddress();
            if (nonLoopbackAddress != null) {
                return nonLoopbackAddress.getPort();
            }
        } catch (final Exception ignored) {
        }

        return null;
    }
}
