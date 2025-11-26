/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.creator.model.creator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.File;
import java.net.URL;
import java.util.regex.Pattern;

@Data
public class ModFile {

    private File file;
    private String modId;
    private String name;
    private String gameVersion;
    private String version;
    private String latestVersion;
    private String latestDevVersion;
    private URL url;

    @JsonIgnore
    public String getCleanVersion() {
        String version = getVersion();
        return version != null ?
                version
                        .replaceAll("^" + Pattern.quote(gameVersion) + "\\-", "")
                        .replaceAll("\\-" + Pattern.quote(gameVersion) + "$", "")
                : null;
    }

}
