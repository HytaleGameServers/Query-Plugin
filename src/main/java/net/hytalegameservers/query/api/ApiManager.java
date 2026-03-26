package net.hytalegameservers.query.api;

import io.github.trae.di.annotations.method.ApplicationReady;
import io.github.trae.di.annotations.method.PreDestroy;
import io.github.trae.di.annotations.type.component.Service;
import io.github.trae.hf.Manager;
import io.github.trae.hytale.framework.utility.UtilTask;
import io.github.trae.utilities.UtilHttp;
import io.github.trae.utilities.UtilJava;
import io.github.trae.utilities.UtilLogger;
import io.github.trae.utilities.enums.HttpMethod;
import lombok.RequiredArgsConstructor;
import net.hytalegameservers.query.QueryPlugin;
import net.hytalegameservers.query.api.dto.QueryUpdateResponse;
import net.hytalegameservers.query.api.enums.State;
import net.hytalegameservers.query.api.interfaces.IApiManager;
import net.hytalegameservers.query.config.ApiConfig;
import net.hytalegameservers.query.config.Config;
import net.hytalegameservers.query.config.NetworkConfig;
import net.hytalegameservers.query.constants.Constants;
import net.hytalegameservers.query.network.NetworkManager;
import net.hytalegameservers.query.network.NodeSnapshot;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class ApiManager implements Manager<QueryPlugin>, IApiManager {

    private static final long DEFAULT_INTERVAL_UPDATE_IN_SECONDS = Duration.ofSeconds(30).toSeconds();

    private static final long SNAPSHOT_REQUEST_TIMEOUT_MS = 3000L;

    private final Config config;
    private final ApiConfig apiConfig;
    private final NetworkConfig networkConfig;

    private final NetworkManager networkManager;

    private long currentIntervalUpdateInSeconds = DEFAULT_INTERVAL_UPDATE_IN_SECONDS;
    private long allowedIntervalUpdateInSeconds = DEFAULT_INTERVAL_UPDATE_IN_SECONDS;

    private State state = State.NONE;

    @ApplicationReady
    public void onApplicationReady() {
        if (!(this.config.isEnabled())) {
            UtilLogger.info("Plugin is disabled, skipping scheduler initialization.");
            return;
        }

        if (this.apiConfig.getServerId().equals(ApiConfig.SERVER_ID_PLACEHOLDER)) {
            throw new IllegalStateException("Server Id has not been configured. Please update your API Config with your server id from the dashboard.");
        }

        if (this.apiConfig.getApiToken().equals(ApiConfig.API_TOKEN_PLACEHOLDER)) {
            throw new IllegalStateException("API token has not been configured. Please update your API Config with your API token from the dashboard.");
        }

        this.startScheduler();
    }

    @PreDestroy
    public void onPreDestroy() {
        if (!(this.config.isEnabled())) {
            return;
        }

        if (this.networkConfig.isNetworkMode()) {
            this.networkManager.broadcastShutdown();

            try {
                Thread.sleep(java.util.concurrent.ThreadLocalRandom.current().nextLong(1000, 5000));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (this.networkManager.getAliveNodeIds().size() > 1) {
                return;
            }
        }

        this.executeShutdown();
    }

    @Override
    public void executeUpdate() {
        if (this.state == State.NONE) {
            this.state = State.VERIFYING;
            UtilLogger.info("<yellow>Initiating verification with remote server.");
        }

        if (this.state != State.VERIFYING && this.state != State.VERIFIED) {
            return;
        }

        // In network mode, only the controller sends to API.
        if (this.networkConfig.isNetworkMode() && !(this.networkManager.isController())) {
            return;
        }

        final NodeSnapshot localSnapshot = NodeSnapshot.create(this.getPlugin(), this.networkConfig, this.config);

        // Collect remote snapshots (empty map if standalone).
        final Map<String, NodeSnapshot> remoteSnapshots = this.networkConfig.isNetworkMode() ? this.networkManager.requestSnapshots(SNAPSHOT_REQUEST_TIMEOUT_MS) : Map.of();

        this.sendUpdate(localSnapshot, remoteSnapshots);
    }

    @Override
    public void executeShutdown() {
        if (this.state != State.VERIFYING && this.state != State.VERIFIED) {
            return;
        }

        this.state = State.SHUTTING_DOWN;

        UtilLogger.info("<yellow>Sending shutdown signal to remote server.");

        try {
            final LinkedHashMap<String, Object> dataMap = UtilJava.createMap(new LinkedHashMap<>(), map -> {
                map.put("serverId", this.apiConfig.getServerId());
            });

            final String jsonBody = Constants.OBJECT_MAPPER.writeValueAsString(dataMap);

            final HttpResponse<String> httpResponse = UtilHttp.supply(HttpMethod.POST, Constants.API_URL.formatted("shutdown"), jsonBody, "application/json", Map.of("Authorization", "Bearer %s".formatted(this.apiConfig.getApiToken())));
            if (UtilHttp.isSuccess(httpResponse)) {
                UtilLogger.info("<green>Shutdown acknowledged by remote server.");
            } else {
                UtilLogger.warning("Shutdown request rejected with status %d.".formatted(httpResponse.statusCode()));
            }
        } catch (final Exception e) {
            UtilLogger.severe("Shutdown request failed: %s".formatted(e.getMessage()));
        }
    }

    // -----------------------------------------------------------------------
    // Unified update — always sends instances array
    // -----------------------------------------------------------------------

    private void sendUpdate(final NodeSnapshot localSnapshot, final Map<String, NodeSnapshot> remoteSnapshots) {
        final List<Map<String, Object>> instanceList = UtilJava.createCollection(new ArrayList<>(), list -> {
            list.add(this.buildInstanceEntry(localSnapshot));

            for (final NodeSnapshot nodeSnapshot : remoteSnapshots.values()) {
                list.add(this.buildInstanceEntry(nodeSnapshot));
            }
        });

        final LinkedHashMap<String, Object> dataMap = UtilJava.createMap(new LinkedHashMap<>(), map -> {
            map.put("serverId", this.apiConfig.getServerId());
            map.put("currentUpdateIntervalInSeconds", this.currentIntervalUpdateInSeconds);
            map.put("instances", instanceList);
        });

        this.sendToApi(dataMap);
    }

    private LinkedHashMap<String, Object> buildInstanceEntry(final NodeSnapshot nodeSnapshot) {
        return UtilJava.createMap(new LinkedHashMap<>(), map -> {
            map.put("nodeId", nodeSnapshot.getNodeId());
            map.put("systemData", nodeSnapshot.getSystemData());
            map.put("pluginData", nodeSnapshot.getPluginData());
            map.put("serverData", nodeSnapshot.getServerData());
            map.put("worldData", nodeSnapshot.getWorldData());
            map.put("playerData", nodeSnapshot.getPlayerData());
        });
    }

    // -----------------------------------------------------------------------
    // HTTP
    // -----------------------------------------------------------------------

    private void sendToApi(final LinkedHashMap<String, Object> dataMap) {
        try {
            final String jsonBody = Constants.OBJECT_MAPPER.writeValueAsString(dataMap);

            final HttpResponse<String> httpResponse = UtilHttp.supply(HttpMethod.POST, Constants.API_URL.formatted("update"), jsonBody, "application/json", Map.of("Authorization", "Bearer %s".formatted(this.apiConfig.getApiToken())));
            if (UtilHttp.isSuccess(httpResponse)) {
                if (httpResponse.statusCode() == 200) {
                    final QueryUpdateResponse queryUpdateResponse = Constants.OBJECT_MAPPER.readValue(httpResponse.body(), QueryUpdateResponse.class);
                    if (queryUpdateResponse != null) {
                        this.allowedIntervalUpdateInSeconds = queryUpdateResponse.getAllowedUpdateIntervalInSeconds();
                    }
                }

                UtilLogger.info("<green>Update dispatched successfully.");
                this.state = State.VERIFIED;
            } else {
                if (httpResponse.statusCode() == 833) {
                    this.allowedIntervalUpdateInSeconds = Long.parseLong(httpResponse.body());
                    return;
                }
                this.state = State.NONE;
                UtilLogger.warning("Update rejected with status %d: %s".formatted(httpResponse.statusCode(), httpResponse.body()));
            }
        } catch (final Exception e) {
            this.state = State.NONE;
            UtilLogger.severe("Update request failed: %s".formatted(e.getMessage()));
        }
    }

    private void startScheduler() {
        UtilTask.scheduleAsynchronous(this::executeUpdate, 0, (int) this.currentIntervalUpdateInSeconds, TimeUnit.SECONDS, this::shouldRestart, true);
    }

    private boolean shouldRestart() {
        if (this.currentIntervalUpdateInSeconds != this.allowedIntervalUpdateInSeconds) {
            UtilLogger.info("<aqua>Update interval changed from %ds to %ds, restarting scheduler.".formatted(this.currentIntervalUpdateInSeconds, this.allowedIntervalUpdateInSeconds));

            this.currentIntervalUpdateInSeconds = this.allowedIntervalUpdateInSeconds;

            this.startScheduler();
            return true;
        }

        return false;
    }
}