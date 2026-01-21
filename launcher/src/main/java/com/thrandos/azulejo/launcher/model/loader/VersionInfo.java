/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.model.loader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Splitter;
import com.thrandos.azulejo.launcher.model.minecraft.GameArgument;
import com.thrandos.azulejo.launcher.model.minecraft.Library;
import com.thrandos.azulejo.launcher.model.minecraft.MinecraftArguments;
import com.thrandos.azulejo.launcher.model.minecraft.VersionManifest;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionInfo {
    private String id;
    private MinecraftArguments arguments;
    private String mainClass;
    private List<Library> libraries;
    private SidedData<VersionManifest.LoggingConfig> logging;

    @JsonIgnore private transient boolean overridingArguments;

    public void setMinecraftArguments(String argumentString) {
        MinecraftArguments minecraftArguments = new MinecraftArguments();

        for (String arg : Splitter.on(' ').split(argumentString)) {
            minecraftArguments.getGameArguments().add(new GameArgument(arg));
        }

        setArguments(minecraftArguments);
        setOverridingArguments(true);
    }
}
