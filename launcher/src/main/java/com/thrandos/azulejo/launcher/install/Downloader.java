/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.install;

import com.thrandos.azulejo.concurrency.ProgressObservable;

import java.io.File;
import java.net.URL;
import java.util.List;


public interface Downloader extends ProgressObservable {

    File download(List<URL> urls, String key, long size, String name);

    File download(URL url, String key, long size, String name);
}
