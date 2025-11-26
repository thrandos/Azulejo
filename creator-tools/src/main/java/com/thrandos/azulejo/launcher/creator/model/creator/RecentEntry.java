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

import lombok.Data;

import java.io.File;

@Data
public class RecentEntry {

    private File path;

    public void setPath(File path) {
        this.path = path != null ? path : new File(".");
    }

    @Override
    public String toString() {
        return path.getAbsolutePath();
    }

}
