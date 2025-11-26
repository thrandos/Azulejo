/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.creator.swing;

import com.thrandos.azulejo.launcher.creator.model.creator.Workspace;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class PackDirectoryFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f.isDirectory() && !f.getName().equals(Workspace.DIR_NAME);
    }

    @Override
    public String getDescription() {
        return "Directories";
    }

}
