package com.thrandos.azulejo.launcher.bootstrap.fx;

import com.thrandos.azulejo.launcher.Bootstrap;
import com.thrandos.azulejo.launcher.bootstrap.BootstrapUtils;
import com.thrandos.azulejo.launcher.bootstrap.HttpRequest;
import com.thrandos.azulejo.launcher.bootstrap.LauncherBinary;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import static com.thrandos.azulejo.launcher.bootstrap.SharedLocale.tr;

public class BootstrapFxController {
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button cancelButton;

    private Bootstrap bootstrap;
    private final java.util.concurrent.atomic.AtomicReference<Thread> worker = new java.util.concurrent.atomic.AtomicReference<>();
    private final java.util.concurrent.atomic.AtomicReference<HttpRequest> httpRef = new java.util.concurrent.atomic.AtomicReference<>();

    public void init(Bootstrap bootstrap, Stage stage) throws IOException {
        this.bootstrap = bootstrap;
        this.statusLabel.setText(tr("downloader.pleaseWait"));
        this.progressBar.setProgress(-1);

        cancelButton.setOnAction(e -> cancel());

        Thread t = new Thread(this::runDownload, "bootstrap-download");
        worker.set(t);
        t.start();
    }

    private void runDownload() {
        try {
            doDownload();
        } catch (InterruptedException ex) {
            // user canceled
            Thread.currentThread().interrupt();
            Platform.exit();
        } catch (Exception t) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to download launcher", t);
            // Fallback: close FX and let Swing error dialog be used by existing code path if needed
            Platform.runLater(() -> {
                Stage st = (Stage) cancelButton.getScene().getWindow();
                if (st != null) st.close();
                Platform.exit();
            });
        }
    }

    private void doDownload() throws Exception {
        Properties props = BootstrapUtils.loadProperties(Bootstrap.class, "bootstrap.properties");
        URL updateUrl = HttpRequest.url(props.getProperty("latestUrl"));

        updateStatus(tr("downloader.status"));

        String data = HttpRequest
                .get(updateUrl)
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asString("UTF-8");

        Object object = JSONValue.parse(data);
        URL url;

        if (object instanceof JSONObject nObject) {
            String rawUrl = String.valueOf(nObject.get("url"));
            if (rawUrl != null) {
                url = HttpRequest.url(rawUrl.trim());
            } else {
                throw new IOException("Update URL did not return a valid result");
            }
        } else {
            throw new IOException("Update URL did not return a valid result");
        }

        File finalFile = new File(bootstrap.getBinariesDir(), System.currentTimeMillis() + ".jar");
        File tempFile = new File(finalFile.getParentFile(), finalFile.getName() + ".tmp");

        updateStatus(tr("downloader.progressStatus").formatted(0));
        progressIndeterminate(true);

        HttpRequest httpRequest = HttpRequest.get(url);
        this.httpRef.set(httpRequest);
        httpRequest
                .execute()
                .expectResponseCode(200)
                .saveContent(tempFile);

        // Update progress periodically while downloading
        // Note: HttpRequest implementation updates progress internally that can be polled
        new Thread(() -> {
            try {
                while (true) {
                    Thread w = worker.get();
                    HttpRequest h = httpRef.get();
                    if (w == null || !w.isAlive() || h == null) break;
                    double p = h.getProgress();
                    if (p >= 0) {
                        progressIndeterminate(false);
                        updateProgress(p);
                        updateStatus(tr("downloader.progressStatus").formatted(p * 100));
                    }
                    Thread.sleep(200);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }, "bootstrap-progress-updater").start();

        if (Thread.interrupted()) throw new InterruptedException();

        try {
            java.nio.file.Files.deleteIfExists(finalFile.toPath());
            java.nio.file.Files.move(tempFile.toPath(), finalFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException io) {
            throw io;
        }

        List<LauncherBinary> binaries = new ArrayList<>();
        binaries.add(new LauncherBinary(finalFile));

        Platform.runLater(() -> {
            try {
                Stage st = (Stage) cancelButton.getScene().getWindow();
                if (st != null) st.close();
            } finally {
                Platform.exit();
            }
        });

        // hand over to the main bootstrap logic to load and execute
        bootstrap.launchExisting(binaries, false);
    }

    private void cancel() {
        Thread w = worker.get();
        if (w != null) w.interrupt();
    }

    private void updateStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }

    private void progressIndeterminate(boolean indeterminate) {
        Platform.runLater(() -> progressBar.setProgress(indeterminate ? -1 : 0));
    }

    private void updateProgress(double value) {
        Platform.runLater(() -> progressBar.setProgress(value));
    }
}
