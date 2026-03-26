package net.hytalegameservers.query.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hytalegameservers.query.QueryPlugin;
import net.hytalegameservers.query.config.Config;
import net.hytalegameservers.query.data.dto.PlayerDto;

import java.util.Collections;
import java.util.List;

/**
 * Snapshot of current player information on this server instance.
 *
 * <p>Player usernames are only included if {@link Config#isSendOnlinePlayerUsernames()}
 * is enabled. Player counts are always sent regardless of that setting.</p>
 */
@NoArgsConstructor
@Getter
@Setter
public class PlayerData {

    private int onlinePlayerCount, maxPlayerCount;
    private List<PlayerDto> playerList;

    public PlayerData(final Config config) {
        this.onlinePlayerCount = QueryPlugin.UNIVERSE.getPlayerCount();
        this.maxPlayerCount = QueryPlugin.HYTALE_SERVER_CONFIG.getMaxPlayers();
        this.playerList = config.isSendOnlinePlayerUsernames() ? QueryPlugin.UNIVERSE.getPlayers().stream().map(PlayerDto::new).toList() : Collections.emptyList();
    }
}
