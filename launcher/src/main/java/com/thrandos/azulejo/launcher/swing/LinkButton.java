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
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LinkButton extends JButton {

    private static final Color LINK_COLOR = Color.blue;
    private static final Border LINK_BORDER = BorderFactory.createEmptyBorder(0, 0, 1, 0); 
    private static final Border HOVER_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 0, LINK_COLOR);
    
    public LinkButton() {
        super();
        setupLink();
    }
    
    public LinkButton(Action a) {
        super(a);
        setupLink();
    }
    
    public LinkButton(Icon icon) {
        super(icon);
        setupLink();
    }
    
    public LinkButton(String text, Icon icon) {
        super(text, icon);
        setupLink();
    }
    
    public LinkButton(String text) {
        super(text);
        setupLink();
    }
    
    public void setupLink() {
        setBorder(LINK_BORDER); 
        setForeground(LINK_COLOR); 
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 
        setFocusPainted(false); 
        setRequestFocusEnabled(false); 
        setContentAreaFilled(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ((JComponent) e.getComponent()).setBorder(HOVER_BORDER);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ((JComponent) e.getComponent()).setBorder(LINK_BORDER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((JComponent) e.getComponent()).setBorder(LINK_BORDER);
            }
        });
    }

}
