package com.thrandos.azulejo.launcher.bootstrap.fx;

import com.thrandos.azulejo.launcher.Bootstrap;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BootstrapFxApp extends Application {
    private static Bootstrap bootstrapRef;

    public static void launchWith(Bootstrap bootstrap) {
        bootstrapRef = bootstrap;
        Application.launch(BootstrapFxApp.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/skcraft/launcher/bootstrap/fx/bootstrap.fxml"));
        Parent root = loader.load();
        BootstrapFxController controller = loader.getController();
        controller.init(bootstrapRef, stage);

        Scene scene = new Scene(root);
        // Reuse the launcher's stylesheet for consistent look if present
        String css = getClass().getResource("/com/skcraft/launcher/fx/launcher.css") != null
                ? "/com/skcraft/launcher/fx/launcher.css"
                : null;
        if (css != null) {
            scene.getStylesheets().add(getClass().getResource(css).toExternalForm());
        }

        stage.setTitle("Azulejo Bootstrapper");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}
