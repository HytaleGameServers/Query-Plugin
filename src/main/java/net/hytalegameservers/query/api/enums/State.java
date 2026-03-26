package net.hytalegameservers.query.api.enums;

/**
 * Represents the lifecycle state of the plugin's API communication.
 *
 * <p>State transitions: {@code NONE → VERIFYING → VERIFIED → SHUTTING_DOWN}</p>
 */
public enum State {

    /**
     * Initial state — no communication has been attempted yet.
     */
    NONE,

    /**
     * First update sent, awaiting successful acknowledgement from the API.
     */
    VERIFYING,

    /**
     * API has acknowledged at least one successful update.
     */
    VERIFIED,

    /**
     * Shutdown signal has been dispatched to the API. Terminal state.
     */
    SHUTTING_DOWN
}
