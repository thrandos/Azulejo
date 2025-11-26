/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.selfupdate;

import com.thrandos.azulejo.launcher.Launcher;
import com.thrandos.azulejo.launcher.LauncherException;
import com.thrandos.azulejo.launcher.util.HttpRequest;
import com.thrandos.azulejo.launcher.util.SharedLocale;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.net.URL;
import java.util.concurrent.Callable;

/**
 * A worker that checks for an update to the launcher. A URL is returned
 * if there is an update to be downloaded.
 */
@Log
public class UpdateChecker implements Callable<LatestVersionInfo> {

    private final Launcher launcher;

    public UpdateChecker(@NonNull Launcher launcher) {
        this.launcher = launcher;
    }

    @Override
    public LatestVersionInfo call() throws Exception {
        try {
            UpdateChecker.log.info("Checking for update...");

            String selfUpdateUrl = launcher.getProperties().getProperty("selfUpdateUrl");
            if (selfUpdateUrl == null || selfUpdateUrl.trim().isEmpty()) {
                UpdateChecker.log.info("No self-update URL configured - skipping update check");
                return null;
            }

            URL url = HttpRequest.url(selfUpdateUrl);

            LatestVersionInfo versionInfo = HttpRequest.get(url)
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asJson(LatestVersionInfo.class);

            ComparableVersion current = new ComparableVersion(launcher.getVersion());
            ComparableVersion latest = new ComparableVersion(versionInfo.getVersion());

            UpdateChecker.log.info("Latest version is " + latest + ", while current is " + current);

            if (latest.compareTo(current) >= 1) {
                if (versionInfo.getUrl() != null) {
                    UpdateChecker.log.info("Update available at " + versionInfo.getUrl());
                    return versionInfo;
                } else {
                    UpdateChecker.log.info("Update available but no download URL provided - ignoring");
                    return null;
                }
            } else {
                UpdateChecker.log.info("No update required.");
                return null;
            }
        } catch (Exception e) {
            throw new LauncherException(e, SharedLocale.tr("errors.selfUpdateCheckError"));
        }
    }

}
