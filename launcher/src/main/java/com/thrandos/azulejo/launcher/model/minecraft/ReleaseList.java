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
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReleaseList {

    private LatestReleases latest;
    private List<Version> versions;

    /**
     * Get a release with the given ID.
     *
     * @param id the ID
     * @return the release
     */
    public Version find(@NonNull String id) {
        for (Version version : getVersions()) {
            if (version.getId().equals(id)) {
                return version;
            }
        }
        return null;
    }

    @Data
    public static class LatestReleases {
        private String snapshot;
        private String release;
    }

}
