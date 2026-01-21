/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Splitter;
import com.thrandos.azulejo.launcher.model.loader.SidedData;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionManifest {

    private String id;
    private Date time;
    private Date releaseTime;
    private String assets;
    private AssetIndex assetIndex;
    private String type;
    private MinecraftArguments arguments;
    private String mainClass;
    private int minimumLauncherVersion;
    private LinkedHashSet<Library> libraries;
    private JavaVersion javaVersion;
    private SidedData<LoggingConfig> logging;
    private Map<String, Artifact> downloads = new HashMap<String, Artifact>();

    public String getAssetId() {
        return getAssetIndex() != null
                ? getAssetIndex().getId()
                : "legacy";
    }

    public void setMinecraftArguments(String minecraftArguments) {
        MinecraftArguments result = new MinecraftArguments();

        for (String arg : Splitter.on(' ').split(minecraftArguments)) {
            result.getGameArguments().add(new GameArgument(arg));
        }

        setArguments(result);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Artifact {
        private String id;
        private String url;
        private int size;

        @JsonProperty("sha1")
        private String hash;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssetIndex {
        private String id;
        private String url;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoggingConfig {
        private String argument;
        private Artifact file;
    }
}
