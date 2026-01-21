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

import javax.xml.bind.annotation.XmlEnumValue;

// Indicates the platform.
public enum Platform {
    @XmlEnumValue("windows") WINDOWS,
    @XmlEnumValue("mac_os_x") MAC_OS_X,
    @XmlEnumValue("linux") LINUX,
    @XmlEnumValue("solaris") SOLARIS,
    @XmlEnumValue("unknown") UNKNOWN
}