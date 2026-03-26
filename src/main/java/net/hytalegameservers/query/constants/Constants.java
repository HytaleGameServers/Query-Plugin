package net.hytalegameservers.query.constants;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Shared constants used across the plugin.
 */
public class Constants {

    /**
     * Jackson ObjectMapper configured to ignore unknown properties, preventing
     * deserialization failures if the API adds new response fields in the future.
     */
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Base URL for the HGS plugin query API. Use with {@code String.formatted(endpoint)}.
     */
    public static final String API_URL = "https://hytalegameservers.net/api/plugin/query/%s";

    /**
     * Placeholder value used when a privacy-redacted field is sent in the payload.
     */
    public static final String PLUGIN_REDACTED_MESSAGE = "[PLUGIN_REDACTED]";
}
