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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Version {

    @Getter
    @Setter
    @NonNull
    private String id;

    @Getter
    @Setter
    @NonNull
    private String url;

    public Version() {
    }

    public Version(@NonNull String id) {
        this.id = id;
    }

    @JsonIgnore
    public String getName() {
        return id;
    }

    @Override
    public String toString() {
        return getName();
    }

    boolean thisEquals(Version other) {
        return getId().equals(other.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;
        return thisEquals(version) && version.thisEquals(this);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
