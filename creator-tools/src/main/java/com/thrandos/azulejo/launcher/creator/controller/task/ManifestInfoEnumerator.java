/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.creator.controller.task;

import com.thrandos.azulejo.launcher.creator.model.creator.ManifestEntry;
import com.thrandos.azulejo.launcher.model.modpack.Manifest;
import com.thrandos.azulejo.launcher.model.modpack.ManifestInfo;
import com.thrandos.azulejo.launcher.persistence.Persistence;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class ManifestInfoEnumerator implements Function<List<ManifestEntry>, List<ManifestEntry>> {

    private final File searchDir;

    public ManifestInfoEnumerator(File searchDir) {
        this.searchDir = searchDir;
    }

    @Override
    public List<ManifestEntry> apply(List<ManifestEntry> entries) {
        File[] files = searchDir.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".json") && !f.getName().startsWith("packages."));

        if (files != null) {
            for (File file : files) {
                String location = file.getName();
                Manifest manifest = Persistence.read(file, Manifest.class, true);

                if (manifest != null) {
                    ManifestInfo info = new ManifestInfo();
                    info.setName(manifest.getName());
                    info.setTitle(manifest.getTitle());
                    info.setVersion(manifest.getVersion());
                    info.setPriority(0);
                    info.setLocation(location);

                    boolean found = false;

                    for (ManifestEntry entry : entries) {
                        if (entry.getManifestInfo().getLocation().equals(location)) {
                            info.setPriority(entry.getManifestInfo().getPriority());
                            entry.setManifestInfo(info);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        ManifestEntry entry = new ManifestEntry();
                        entry.setManifestInfo(info);
                        entries.add(entry);
                    }
                }
            }
        }

        return entries;
    }

}
