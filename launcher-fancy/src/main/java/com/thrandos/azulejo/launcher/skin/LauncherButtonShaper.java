/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.skin;

import org.pushingpixels.substance.api.shaper.ClassicButtonShaper;

import javax.swing.*;
import java.awt.*;

public class LauncherButtonShaper extends ClassicButtonShaper {

    public Dimension getPreferredSize(AbstractButton button, Dimension uiPreferredSize) {
        Dimension size = super.getPreferredSize(button, uiPreferredSize);
        return new Dimension(size.width + 5, size.height + 4);
    }

}
