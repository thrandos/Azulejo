/*
  ====================================================================
  AZULEJO

  Built for the Coastline server network
  Copyright (C) 2025-2026
  Some base code copyright (C) 2010-2014 Albert Pham and contributors
  Please see LICENSE.txt for more information.
  ====================================================================
*/

package com.thrandos.azulejo.launcher.fx;

import com.thrandos.azulejo.launcher.Instance;
import com.thrandos.azulejo.launcher.InstanceList;
import com.thrandos.azulejo.launcher.Launcher;
import com.thrandos.azulejo.launcher.auth.OfflineSession;
import com.thrandos.azulejo.launcher.auth.Session;

import com.thrandos.azulejo.launcher.dialog.AboutDialog;
import com.thrandos.azulejo.launcher.dialog.ConfigurationDialog;
import com.thrandos.azulejo.launcher.launch.LaunchOptions;
import com.thrandos.azulejo.launcher.launch.LaunchOptions.UpdatePolicy;
import com.thrandos.azulejo.launcher.launch.LaunchSupervisor;
import com.thrandos.azulejo.launcher.model.modpack.PackageList;
import com.thrandos.azulejo.launcher.persistence.Persistence;
import com.thrandos.azulejo.launcher.swing.InstanceTable;
import com.thrandos.azulejo.launcher.swing.InstanceTableModel;
import com.thrandos.azulejo.launcher.util.SharedLocale;
import com.thrandos.azulejo.concurrency.ObservableFuture;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import lombok.extern.java.Log;

import javax.swing.*;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * JavaFX Controller for the main launcher window.
 */
@Log
public class LauncherController implements Initializable {
    
    @FXML private TableView<Instance> instanceTable;
    @FXML private TableColumn<Instance, String> nameColumn;
    @FXML private TableColumn<Instance, String> versionColumn;
    @FXML private Button launchButton;
    @FXML private CheckBox updateCheck;
    @FXML private WebView newsWebView;
    @FXML private MenuItem optionsMenuItem;
    @FXML private MenuItem aboutMenuItem;
    @FXML private MenuItem refreshMenuItem;
    
    private Launcher launcher;
    private ObservableList<Instance> instances = FXCollections.observableArrayList();
    private ExecutorService executor = Executors.newCachedThreadPool();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup table columns
        nameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        versionColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getVersion()));
        
        instanceTable.setItems(instances);
        
        // Setup event handlers
        launchButton.setOnAction(event -> launch());
        optionsMenuItem.setOnAction(event -> showOptions());
        aboutMenuItem.setOnAction(event -> showAbout());
        refreshMenuItem.setOnAction(event -> refreshInstances());
        
        // Enable/disable launch button based on selection
        instanceTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                launchButton.setDisable(newSelection == null);
            });
        
        launchButton.setDisable(true);
        
        // Initialize update checkbox to be selected by default (like Swing version)
        updateCheck.setSelected(true);
    }
    
    /**
     * Set the launcher instance.
     * @param launcher the launcher
     */
    public void setLauncher(Launcher launcher) {
        this.launcher = launcher;
        log.info("JavaFX controller initialized with launcher");
        
        // Load instances and news after UI is fully initialized
        Platform.runLater(() -> {
            log.info("Loading instances and news in JavaFX UI thread");
            loadInstances();
            loadNews();
        });
    }
    
    /**
     * Load instances from the launcher.
     */
    private void loadInstances() {
        if (launcher == null) {
            log.warning("Cannot load instances: launcher is null");
            return;
        }
        
        log.info("Loading instances in JavaFX controller");
        
        // Start by showing any cached instances immediately
        try {
            instances.clear();
            if (launcher.getInstances() != null && launcher.getInstances().size() > 0) {
                log.info("Found " + launcher.getInstances().size() + " cached instances");
                for (int i = 0; i < launcher.getInstances().size(); i++) {
                    instances.add(launcher.getInstances().get(i));
                }
                
                if (!instances.isEmpty()) {
                    instanceTable.getSelectionModel().selectFirst();
                    log.info("Selected first instance in table");
                }
            } else {
                log.info("No cached instances found, table will be empty initially");
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to load cached instances", e);
        }
        
        // Then refresh from remote asynchronously
        log.info("Starting refresh of instances from remote");
        refreshInstances();
    }
    
    /**
     * Refresh instances manually.
     */
    private void refreshInstances() {
        if (launcher == null) return;
        
        log.info("Starting to refresh instances from remote sources");
        
        Task<Void> refreshTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Load instances in background with timeout
                log.info("Attempting to reload instances with 30-second timeout");
                try {
                    launcher.getInstanceTasks().reloadInstances(null).get(30, TimeUnit.SECONDS);
                    log.info("Successfully reloaded instances from remote");
                } catch (TimeoutException e) {
                    log.warning("Instance refresh timed out after 30 seconds");
                    throw new RuntimeException("Request timed out after 30 seconds. Please check your internet connection.", e);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to reload instances", e);
                    throw new RuntimeException("Failed to load modpacks: " + e.getMessage(), e);
                }
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    instances.clear();
                    // Convert InstanceList to regular collection
                    for (int i = 0; i < launcher.getInstances().size(); i++) {
                        instances.add(launcher.getInstances().get(i));
                    }
                    
                    // Select first instance if available
                    if (!instances.isEmpty()) {
                        instanceTable.getSelectionModel().selectFirst();
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    Throwable exception = getException();
                    log.log(Level.WARNING, "Failed to refresh instances", exception);
                    
                    String userMessage;
                    if (exception.getMessage().contains("timed out")) {
                        userMessage = "Unable to connect to modpack server. Please check your internet connection and try again.";
                    } else if (exception.getMessage().contains("Connection refused") || exception.getMessage().contains("No route to host")) {
                        userMessage = "Modpack server is unreachable. Please try again later.";
                    } else {
                        userMessage = "Failed to load modpacks. " + exception.getMessage();
                    }
                    
                    showError("Unable to Load Modpacks", userMessage);
                });
            }
        };
        
        executor.submit(refreshTask);
    }
    
    /**
     * Load news in the web view.
     */
    private void loadNews() {
        if (launcher == null || newsWebView == null) return;
        
        URL newsUrl = launcher.getNewsURL();
        if (newsUrl != null) {
            newsWebView.getEngine().load(newsUrl.toString());
        }
    }
    
    /**
     * Launch the selected instance.
     */
    private void launch() {
        Instance selected = instanceTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        // Create a temporary offline session for launching
        // Use a session that can be considered online for updates
        Session session = new OfflineSession("Player") {
            @Override
            public boolean isOnline() {
                return true;  // Treat as online to allow updates
            }
        };
        
        Task<Void> launchTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                LaunchOptions options = new LaunchOptions.Builder()
                    .setInstance(selected)
                    .setSession(session)
                    .setUpdatePolicy(UpdatePolicy.UPDATE_IF_SESSION_ONLINE)  // Allow updates when online
                    .build();
                
                launcher.getLaunchSupervisor().launch(options);
                return null;
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    log.log(Level.WARNING, "Failed to launch", getException());
                    showError("Launch Failed", getException().getMessage());
                });
            }
        };
        
        executor.submit(launchTask);
    }
    
    /**
     * Show the options dialog.
     */
    private void showOptions() {
        // For now, use Swing dialog - will convert later
        SwingUtilities.invokeLater(() -> {
            ConfigurationDialog dialog = new ConfigurationDialog(null, launcher);
            dialog.setVisible(true);
        });
    }
    
    /**
     * Show the about dialog.
     */
    private void showAbout() {
        // For now, use Swing dialog - will convert later
        SwingUtilities.invokeLater(() -> {
            AboutDialog dialog = new AboutDialog(null);
            dialog.setVisible(true);
        });
    }
    
    /**
     * Show an error dialog.
     * @param title the title
     * @param message the message
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}