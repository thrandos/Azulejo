/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class MorePaths {

    private MorePaths() {
    }

    public static boolean isSamePath(File a, File b) throws IOException {
        return a.getCanonicalPath().equals(b.getCanonicalPath());
    }

    public static boolean isSubDirectory(File base, File child) throws IOException {
        base = base.getCanonicalFile();
        child = child.getCanonicalFile();

        File parentFile = child;
        while (parentFile != null) {
            if (base.equals(parentFile)) {
                return true;
            }

            parentFile = parentFile.getParentFile();
        }

        return false;
    }

    public static String relativize(File base, File child) {
        Path basePath = Path.of(base.getAbsolutePath());
        Path childPath = Path.of(child.getAbsolutePath());
        return basePath.relativize(childPath).toString();
    }

}
