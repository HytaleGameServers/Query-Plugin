package net.hytalegameservers.query.constants;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Constants {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static final String API_URL = "https://hytalegameservers.net/api/plugin/query/%s";

    public static final String PLUGIN_REDACTED_MESSAGE = "[PLUGIN_REDACTED]";
}