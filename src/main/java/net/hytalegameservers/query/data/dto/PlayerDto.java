package net.hytalegameservers.query.data.dto;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class PlayerDto {

    private UUID id;
    private String username, usernameLower;

    public PlayerDto(final PlayerRef playerRef) {
        this.id = playerRef.getUuid();
        this.username = playerRef.getUsername();
        this.usernameLower = playerRef.getUsername().toLowerCase(Locale.ROOT);
    }
}