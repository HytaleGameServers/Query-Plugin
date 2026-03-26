package net.hytalegameservers.query.data.dto;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PluginDto {

    private String group, name, version;

    public PluginDto(final PluginBase pluginBase) {
        final PluginManifest pluginManifest = pluginBase.getManifest();

        this.group = pluginManifest.getGroup();
        this.name = pluginManifest.getName();
        this.version = pluginManifest.getVersion() != null ? pluginManifest.getVersion().toString() : "Unknown";
    }
}