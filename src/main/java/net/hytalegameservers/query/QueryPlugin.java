package net.hytalegameservers.query;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import io.github.trae.di.annotations.type.Application;
import io.github.trae.hytale.framework.HytalePlugin;

import javax.annotation.Nonnull;

@Application
public class QueryPlugin extends HytalePlugin {

    public static final long BOOT_START_AT = System.currentTimeMillis();

    public static final HytaleServer HYTALE_SERVER = HytaleServer.get();
    public static final HytaleServerConfig HYTALE_SERVER_CONFIG = HYTALE_SERVER.getConfig();
    public static final Universe UNIVERSE = Universe.get();
    public static final ServerManager SERVER_MANAGER = ServerManager.get();

    public QueryPlugin(@Nonnull final JavaPluginInit javaPluginInit) {
        super(javaPluginInit);
    }

    @Override
    protected void setup() {
        this.initializePlugin();
    }

    @Override
    protected void shutdown() {
        this.shutdownPlugin();
    }
}
