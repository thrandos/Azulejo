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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.thrandos.azulejo.concurrency.Deferred;
import com.thrandos.azulejo.concurrency.Deferreds;
import com.thrandos.azulejo.concurrency.SettableProgress;
import com.thrandos.azulejo.launcher.creator.dialog.GenerateListingDialog;
import com.thrandos.azulejo.launcher.creator.dialog.ManifestEntryDialog;
import com.thrandos.azulejo.launcher.creator.model.creator.ManifestEntry;
import com.thrandos.azulejo.launcher.creator.model.creator.Workspace;
import com.thrandos.azulejo.launcher.creator.model.swing.ListingType;
import com.thrandos.azulejo.launcher.creator.model.swing.ManifestEntryTableModel;
import com.thrandos.azulejo.launcher.dialog.ProgressDialog;
import com.thrandos.azulejo.launcher.persistence.Persistence;
import com.thrandos.azulejo.launcher.swing.SwingHelper;
import com.thrandos.azulejo.launcher.util.SwingExecutor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateListingController {

    private final GenerateListingDialog dialog;
    private final Workspace workspace;
    private final List<ManifestEntry> manifestEntries;
    private final ListeningExecutorService executor;
    private final ManifestEntryTableModel manifestTableModel;

    public GenerateListingController(GenerateListingDialog dialog, Workspace workspace, List<ManifestEntry> manifestEntries, ListeningExecutorService executor) {
        this.dialog = dialog;
        this.workspace = workspace;
        this.manifestEntries = manifestEntries;
        this.executor = executor;

        this.manifestTableModel = new ManifestEntryTableModel(manifestEntries);
        dialog.getManifestsTable().setModel(manifestTableModel);
        dialog.getManifestsTableAdjuster().adjustColumns();

        initListeners();

        setListingType(workspace.getPackageListingType());
    }

    public void setOutputDir(File dir) {
        dialog.getDestDirField().setPath(dir.getAbsolutePath());
    }

    public void setListingType(ListingType type) {
        dialog.getListingTypeCombo().setSelectedItem(type);
    }

    public void show() {
        dialog.setVisible(true);
    }

    public Optional<ManifestEntry> getManifestFromIndex(int selectedIndex) {
        if (selectedIndex >= 0) {
            ManifestEntry manifest = manifestEntries.get(selectedIndex);
            if (manifest != null) {
                return Optional.fromNullable(manifest);
            }
        }
        return Optional.absent();
    }

    public Optional<ManifestEntry> getSelectedManifest() {
        JTable table = dialog.getManifestsTable();
        int selectedIndex = table.getSelectedRow();
        if (selectedIndex >= 0) {
            selectedIndex = table.convertRowIndexToModel(selectedIndex);
            ManifestEntry manifest = manifestEntries.get(selectedIndex);
            if (manifest != null) {
                return Optional.fromNullable(manifest);
            }
        }

        SwingHelper.showErrorDialog(dialog, "Please select a modpack from the list.", "Error");
        return Optional.absent();
    }

    private void updateManifestEntryInTable(ManifestEntry manifestEntry) {
        int index = manifestEntries.indexOf(manifestEntry);
        if (index >= 0) {
            manifestTableModel.fireTableRowsUpdated(index, index);
        }
    }

    private void initListeners() {
        dialog.getManifestsTable().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable table = (JTable) e.getSource();
                    Point point = e.getPoint();
                    int selectedIndex = table.rowAtPoint(point);
                    if (selectedIndex >= 0) {
                        selectedIndex = table.convertRowIndexToModel(selectedIndex);
                        Optional<ManifestEntry> optional = getManifestFromIndex(selectedIndex);
                        if (optional.isPresent()) {
                            if (showModifyDialog(optional.get())) {
                                updateManifestEntryInTable(optional.get());
                            }
                        }
                    }
                }
            }
        });

        dialog.getListingTypeCombo().addItemListener(e -> {
            ListingType type = (ListingType) e.getItem();
            dialog.getGameKeyWarning().setVisible(!type.isGameKeyCompatible());
        });

        dialog.getEditManifestButton().addActionListener(e -> {
            Optional<ManifestEntry> optional = getSelectedManifest();
            if (optional.isPresent()) {
                if (showModifyDialog(optional.get())) {
                    updateManifestEntryInTable(optional.get());
                }
            }
        });

        dialog.getGenerateButton().addActionListener(e -> tryGenerate());

        dialog.getCancelButton().addActionListener(e -> dialog.dispose());
    }

    private boolean showModifyDialog(ManifestEntry manifestEntry) {
        ManifestEntryDialog modifyDialog = new ManifestEntryDialog(dialog);
        modifyDialog.setTitle("Modify " + manifestEntry.getManifestInfo().getLocation());
        ManifestEntryController controller = new ManifestEntryController(modifyDialog, manifestEntry);
        return controller.show();
    }

    private boolean tryGenerate() {
        String path = dialog.getDestDirField().getPath().trim();

        if (path.isEmpty()) {
            SwingHelper.showErrorDialog(dialog, "A directory must be entered.", "Error");
            return false;
        }

        List<ManifestEntry> selected = manifestEntries.stream()
                .filter(ManifestEntry::isSelected)
                .sorted()
                .collect(Collectors.toCollection(Lists::newArrayList));

        if (selected.isEmpty()) {
            SwingHelper.showErrorDialog(dialog, "At least one modpack must be selected to appear in the package list.", "Error");
            return false;
        }

        ListingType listingType = (ListingType) dialog.getListingTypeCombo().getSelectedItem();
        File destDir = new File(path);
        destDir.mkdirs();
        File file = new File(destDir, listingType.getFilename());

        workspace.setPackageListingEntries(selected);
        workspace.setPackageListingType(listingType);
        Persistence.commitAndForget(workspace);

        SettableProgress progress = new SettableProgress("Generating package listing...", -1);

        Deferred<?> deferred = Deferreds.makeDeferred(executor.submit(() -> listingType.generate(selected)))
                .thenTap(() -> progress.set("Deleting older package listing files...", -1))
                .thenApply(input -> {
                    for (ListingType otherListingType : ListingType.values()) {
                        File f = new File(destDir, otherListingType.getFilename());
                        if (f.exists()) {
                            f.delete();
                        }
                    }

                    return input;
                })
                .thenTap(() -> progress.set("Writing package listing to disk...", -1))
                .thenApply(input -> {
                    try {
                        Files.write(input, file, Charset.forName("UTF-8"));
                        return file;
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to write package listing file to disk", e);
                    }
                })
                .handleAsync(v -> {
                    if (listingType.isGameKeyCompatible()) {
                        SwingHelper.showMessageDialog(dialog, "Successfully generated package listing.", "Success", null, JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        SwingHelper.showMessageDialog(dialog, """
                                Successfully generated package listing.
                                
                                Note that any modpacks with game keys set were not added.""",
                                "Success", null, JOptionPane.INFORMATION_MESSAGE);
                    }
                    dialog.dispose();
                    SwingHelper.browseDir(destDir, dialog);
                }, ex -> {}, SwingExecutor.INSTANCE);

        ProgressDialog.showProgress(dialog, deferred, progress, "Writing package listing...", "Writing package listing...");
        SwingHelper.addErrorDialogCallback(dialog, deferred);

        return true;
    }

}
