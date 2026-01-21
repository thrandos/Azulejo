/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.swing;

import javax.swing.*;
import java.awt.*;

public class HeaderPanel extends JPanel {

    public HeaderPanel() {
        setBackground(new Color(0xDB5036));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 60);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }
}
