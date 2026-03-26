package net.hytalegameservers.query.config;

import io.github.trae.di.configuration.annotations.Comment;
import io.github.trae.di.configuration.annotations.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * API authentication configuration.
 *
 * <p>Contains the server's unique identifier and API token, both obtained
 * from the server dashboard on the website. The plugin will refuse
 * to start if either value is still set to its placeholder.</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Configuration("Api")
public class ApiConfig {

    public static final String SERVER_ID_PLACEHOLDER = "YOUR_SERVER_ID_HERE";
    public static final String API_TOKEN_PLACEHOLDER = "YOUR_API_TOKEN_HERE";

    @Comment({"Your unique server identifier.", "Obtain this from your server's settings page in the dashboard."})
    private String serverId = "YOUR_SERVER_ID_HERE";

    @Comment({"Your server's API authentication token.", "Obtain this from your server's settings page in the dashboard.", "Keep this value private — do not share it publicly."})
    private String apiToken = "YOUR_API_TOKEN_HERE";
}
