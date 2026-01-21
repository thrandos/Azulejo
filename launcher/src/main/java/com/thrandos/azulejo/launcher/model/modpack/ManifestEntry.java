/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.thrandos.azulejo.launcher.install.InstallExtras;
import com.thrandos.azulejo.launcher.install.InstallLog;
import com.thrandos.azulejo.launcher.install.Installer;
import com.thrandos.azulejo.launcher.install.UpdateCache;
import com.thrandos.azulejo.launcher.model.loader.ProcessorEntry;
import lombok.Data;
import lombok.ToString;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = FileInstall.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FileInstall.class, name = "file"),
        @JsonSubTypes.Type(value = ProcessorEntry.class, name = "process")
})
@Data
@ToString(exclude = "manifest")
public abstract class ManifestEntry {

    @JsonBackReference("manifest")
    private Manifest manifest;
    private Condition when;

    public abstract void install(Installer installer, InstallLog log, UpdateCache cache, InstallExtras extras) throws Exception;

}
