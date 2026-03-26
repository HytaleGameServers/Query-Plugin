package net.hytalegameservers.query.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hytalegameservers.query.config.Config;
import net.hytalegameservers.query.constants.Constants;

@NoArgsConstructor
@Getter
@Setter
public class SystemData {

    private String environment;

    public static SystemData create(final Config config) {
        final SystemData systemData = new SystemData();
        systemData.setEnvironment(config.isSendSystemEnvironmentName() ? System.getProperty("os.name", "Unknown") : Constants.PLUGIN_REDACTED_MESSAGE);
        return systemData;
    }
}