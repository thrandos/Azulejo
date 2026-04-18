package com.thrandos.azulejo.app;

import lombok.extern.java.Log;

@Log // first time using lombok!
public final class Azulejo {

    private Azulejo() {
        // static entrypoint class
    }

    public static void main(String[] args) {
        try {
            boot(args); // boots with args
        } catch (Exception ex) {
            log.severe("Startup failed: " + ex.getMessage()); // prints why startup failed
            ex.printStackTrace();
            System.exit(1); 
        }
    }

    private static void boot(String[] args) {
        log.info("Starting Azulejo...");

        diag();
        update();
        login();
        bootstrap();
        presentGUI(args);
    }

    // ---- all the things found in boot() ----------------------------------------------------------------------------------------------------------
    
    // diag
    private static void diag() {
        log.info("Running diagnostics...");

        // TODO below
        // checks Azulejo folder in Program Files
            // If missing folder(s), adds folder(s)
        // checks Azulejo folder in AppData
            // If missing folder(s), adds folder(s)
        // validate & repair incorrect settings

    }


    // Checks for updates       
    private static void update() {
        log.info("Checking for updates...");

        // TODO below
        // If older version, update
    }


    // Gets Microsoft login     
    private static void login() {
        log.info("Checking for Microsoft sign-in...");

        boolean needsSignin = false;

        // TODO below
        // If missing, mark needsSignin as true, triggers signin flow

        if (needsSignin) {
            log.info("User sign-in required.");
        }
    }


    // Checks if bootstrapper is enabled
    private static void bootstrap() {
        log.info("Checking if bootstrapper is enabled...");

        boolean bootstrapperEnabled = false;
        boolean bootstrapperRunning = false;

        // TODO below
        // If yes, check if it's on
            // If not on, turn it on
        // If no, do nothing

        if (bootstrapperEnabled && !bootstrapperRunning) {
            log.info("Bootstrapper is enabled but not running; startup action required.");
        }
    }

    // 

    // Opens CLI
    private static void presentGUI(String[] args) {
        log.info("Opening CLI...");
        log.fine("CLI args: " + String.join(", ", args));

        // TODO below
        // instantiate launcher core dependencies
        // open first-launch CLI if required
        // otherwise open normal CLI
        // relay any launch args to CLI behavior
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------

    // At this point everything is pretty much handed off to LauncherCLI to handle.

}