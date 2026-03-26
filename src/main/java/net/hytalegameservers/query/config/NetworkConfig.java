package net.hytalegameservers.query.config;

import io.github.trae.di.configuration.annotations.Comment;
import io.github.trae.di.configuration.annotations.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hytalegameservers.query.config.interfaces.INetworkConfig;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Configuration("Network")
public class NetworkConfig implements INetworkConfig {

    @Comment({"Whether network mode is enabled.", "Enable this for multi-server setups where multiple nodes report as one server listing."})
    private boolean enabled = false;

    @Comment({"The unique identifier for this node.", "This is displayed in the server directory and used for controller election.", "Examples: LOBBY, LOBBY-1, FACTIONS, SURVIVAL, MINIGAMES-1"})
    private String nodeId = "DEFAULT";

    @Comment("The TCP port used for inter-node communication.")
    private int port = 9800;

    @Comment({"List of peer node addresses to connect to in host:port format.", "Do not include this node's own address.", "Leave empty for standalone mode.", "Example: [\"192.168.1.10:9800\", \"192.168.1.11:9800\"]"})
    private List<String> nodes = new ArrayList<>();

    @Override
    public boolean isNetworkMode() {
        return this.isEnabled() && !(this.getNodes().isEmpty());
    }
}