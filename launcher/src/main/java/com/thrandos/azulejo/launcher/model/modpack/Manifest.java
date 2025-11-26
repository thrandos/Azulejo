/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.thrandos.azulejo.launcher.Instance;
import com.thrandos.azulejo.launcher.LauncherUtils;
import com.thrandos.azulejo.launcher.install.Installer;
import com.thrandos.azulejo.launcher.model.loader.LoaderManifest;
import com.thrandos.azulejo.launcher.model.minecraft.VersionManifest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class Manifest extends BaseManifest {

    public static final int MIN_PROTOCOL_VERSION = 3;

    private int minimumVersion;
    private URL baseUrl;
    private String librariesLocation;
    private String objectsLocation;
    private String gameVersion;
    @JsonProperty("launch")
    private LaunchModifier launchModifier;
    private List<Feature> features = new ArrayList<Feature>();
    @JsonManagedReference("manifest")
    private List<ManifestEntry> tasks = new ArrayList<ManifestEntry>();
    @Getter @Setter @JsonIgnore
    private Installer installer;
    private VersionManifest versionManifest;
    private Map<String, LoaderManifest> loaders = new HashMap<String, LoaderManifest>();

    @JsonIgnore
    public URL getLibrariesUrl() {
        if (Strings.nullToEmpty(getLibrariesLocation()) == null) {
            return null;
        }

        try {
            return LauncherUtils.concat(baseUrl, Strings.nullToEmpty(getLibrariesLocation()) + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonIgnore
    public URL getObjectsUrl() {
        if (Strings.nullToEmpty(getObjectsLocation()) == null) {
            return baseUrl;
        }

        try {
            return LauncherUtils.concat(baseUrl, Strings.nullToEmpty(getObjectsLocation()) + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateName(String name) {
        if (name != null) {
            setName(name);
        }
    }

    public void updateTitle(String title) {
        if (title != null) {
            setTitle(title);
        }
    }

    public void updateGameVersion(String gameVersion) {
        if (gameVersion != null) {
            setGameVersion(gameVersion);
        }
    }

    public void update(Instance instance) {
        instance.setLaunchModifier(getLaunchModifier());
    }
}
