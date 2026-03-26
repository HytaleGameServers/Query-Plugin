package net.hytalegameservers.query.config;

import io.github.trae.di.configuration.annotations.Comment;
import io.github.trae.di.configuration.annotations.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * General plugin configuration.
 *
 * <p>Controls the master enable/disable toggle and privacy settings that
 * determine what data is included in API update payloads.</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Configuration("Config")
public class Config {

    @Comment("Whether the plugin is enabled. Set to false to completely disable all API communication.")
    private boolean enabled = true;

    @Comment({"Whether to include the operating system name in the update payload.", "Set to false to redact this information for privacy."})
    private boolean sendSystemEnvironmentName = true;

    @Comment({"Whether to include online player usernames in the update payload.", "Disabling this will still send player counts, but not individual usernames."})
    private boolean sendOnlinePlayerUsernames = true;

    @Comment({"Whether to include a list of installed plugins in the update payload.", "Only sends the group, name, and version of each plugin.", "Disabling this will hide your server's plugin list from the directory."})
    private boolean sendPluginInfo = true;
}
