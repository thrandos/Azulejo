/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/


package com.thrandos.azulejo.launcher.creator.controller;

import com.thrandos.azulejo.launcher.creator.dialog.ManifestEntryDialog;
import com.thrandos.azulejo.launcher.creator.model.creator.ManifestEntry;
import com.thrandos.azulejo.launcher.swing.SwingHelper;

public class ManifestEntryController {

    private final ManifestEntryDialog dialog;
    private final ManifestEntry manifestEntry;

    private boolean save;

    public ManifestEntryController(ManifestEntryDialog dialog, ManifestEntry manifestEntry) {
        this.dialog = dialog;
        this.manifestEntry = manifestEntry;

        initListeners();
        copyFrom();
    }

    private void copyFrom() {
        dialog.getIncludeCheck().setSelected(manifestEntry.isSelected());
        dialog.getPrioritySpinner().setValue(manifestEntry.getManifestInfo().getPriority());
        SwingHelper.setTextAndResetCaret(dialog.getGameKeysText(), SwingHelper.listToLines(manifestEntry.getGameKeys()));
    }

    private void copyTo() {
        manifestEntry.setSelected(dialog.getIncludeCheck().isSelected());
        manifestEntry.getManifestInfo().setPriority((Integer) dialog.getPrioritySpinner().getValue());
        manifestEntry.setGameKeys(SwingHelper.linesToList(dialog.getGameKeysText().getText()));
    }

    public boolean show() {
        dialog.setVisible(true);
        return save;
    }

    private void initListeners() {
        dialog.getOkButton().addActionListener(e -> {
            copyTo();
            save = true;
            dialog.dispose();
        });

        dialog.getCancelButton().addActionListener(e -> dialog.dispose());
    }

}
