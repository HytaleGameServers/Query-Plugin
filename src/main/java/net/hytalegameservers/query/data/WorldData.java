package net.hytalegameservers.query.data;

import com.hypixel.hytale.server.core.universe.world.World;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hytalegameservers.query.QueryPlugin;

import java.util.List;
import java.util.Optional;

/**
 * Snapshot of world information on this server instance.
 *
 * <p>Includes the default world name, total world count, and a list of
 * all world names currently loaded.</p>
 */
@NoArgsConstructor
@Getter
@Setter
public class WorldData {

    private String defaultWorldName;
    private int worldCount;
    private List<String> worldNameList;

    public static WorldData create() {
        final WorldData worldData = new WorldData();
        worldData.setDefaultWorldName(Optional.ofNullable(QueryPlugin.UNIVERSE.getDefaultWorld()).map(World::getName).orElse(null));
        worldData.setWorldCount(QueryPlugin.UNIVERSE.getWorlds().size());
        worldData.setWorldNameList(QueryPlugin.UNIVERSE.getWorlds().values().stream().map(World::getName).toList());
        return worldData;
    }
}
