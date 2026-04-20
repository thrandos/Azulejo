package com.thrandos.azulejo.ui.cli;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Logger;

/*

This is the CLI UI for Azulejo. 
I plan to keep it once we switch to JavaFX as a cool little thing you can
still use if you want.
FYI I have never worked with CLI or ANSI codes before so good luck to me!

*/

public final class LauncherCLI {
    private static final Logger LOGGER = Logger.getLogger(LauncherCLI.class.getName());
    private static final String DEFAULT_INSTANCE_NAME = "default"; // lmao this is probably malpractice
    private final LauncherBridge launcher;
    private final Scanner scanner = new Scanner(System.in);
    private String activeInstanceTitle = DEFAULT_INSTANCE_NAME; // this too
    
    // ANSI color codes & other useful things =========================

    // special
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String DIM = "\u001B[2m";
    
    // colors
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String WHITE = "\u001B[37m";
    
    // light variants or at least the ones I'm using
    private static final String LIGHT_CYAN = "\u001B[96m";
    private static final String LIGHT_GREEN = "\u001B[92m";
    private static final String LIGHT_YELLOW = "\u001B[93m";
    private static final String LIGHT_RED = "\u001B[91m";
    
    // and bright white (idk why this one is 'bright')
    private static final String BRIGHT_WHITE = "\u001B[97m";

    // spinner characters yay
    private static final char[] SPINNER = {'/', '-', '\\', '|'};

    // ================================================================

    public LauncherCLI(LauncherBridge launcher) {
        this.launcher = launcher;
    }

    public interface LauncherBridge {
        String getVersion();
        InstanceListRef getInstances();
        AccountStore getAccounts();
        LoginServiceRef getLoginService(String type);
        LaunchSupervisorRef getLaunchSupervisor();
        ConfigurationRef getConfig();
        PersistenceBridge getPersistence();
    }

    public interface InstanceRef {
        boolean isInstalled();
        String getTitle();
        File getContentDir();
    }

    public interface InstanceListRef {
        int size();
        InstanceRef get(int index);
        EnumeratorRef createEnumerator();
    }

    public interface EnumeratorRef {
        void call() throws Exception;
    }

    public interface AccountStore {
        int getSize();
        SavedSessionRef getElementAt(int index);
        void remove(SavedSessionRef session);
    }

    public interface SavedSessionRef {
        String getType();
        String getUsername();
    }

    public interface SessionRef {
        String getName();
    }

    public interface LoginServiceRef {
        SessionRef restore(SavedSessionRef session) throws Exception;
    }

    public interface LaunchListenerRef {
        void instancesUpdated();
        void gameStarted();
        void gameClosed();
    }

    public interface LaunchSupervisorRef {
        void launch(LaunchCommand command) throws Exception;
    }

    public interface ConfigurationRef {
        int getMaxMemory();
        void setMaxMemory(int value);
        int getMinMemory();
        void setMinMemory(int value);
        String getJvmArgs();
        void setJvmArgs(String args);
        boolean isOfflineEnabled();
        void setOfflineEnabled(boolean enabled);
    }

    public interface PersistenceBridge {
        void commitAndForget(Object value) throws Exception;
    }

    public static final class LaunchCommand {
        public enum UpdatePolicy {
            UPDATE_IF_SESSION_ONLINE
        }

        private final InstanceRef instance;
        private final SessionRef session;
        private final LaunchListenerRef listener;
        private final UpdatePolicy updatePolicy;

        private LaunchCommand(Builder builder) {
            this.instance = builder.instance;
            this.session = builder.session;
            this.listener = builder.listener;
            this.updatePolicy = builder.updatePolicy;
        }

        public InstanceRef getInstance() {
            return instance;
        }

        public SessionRef getSession() {
            return session;
        }

        public LaunchListenerRef getListener() {
            return listener;
        }

        public UpdatePolicy getUpdatePolicy() {
            return updatePolicy;
        }

        public static final class Builder {
            private InstanceRef instance;
            private SessionRef session;
            private LaunchListenerRef listener;
            private UpdatePolicy updatePolicy = UpdatePolicy.UPDATE_IF_SESSION_ONLINE;

            public Builder setInstance(InstanceRef instance) {
                this.instance = instance;
                return this;
            }

            public Builder setSession(SessionRef session) {
                this.session = session;
                return this;
            }

            public Builder setListener(LaunchListenerRef listener) {
                this.listener = listener;
                return this;
            }

            public Builder setUpdatePolicy(UpdatePolicy updatePolicy) {
                this.updatePolicy = updatePolicy;
                return this;
            }

            public LaunchCommand build() {
                return new LaunchCommand(this);
            }
        }
    }

    public void run() {
        // Enable ANSI on Windows
        enableAnsiSupport();

        // Default instance selection is automatic and can be changed in settings.
        initializeActiveInstanceSelection();
        
        // Show splash screen
        printGUI();

        while (true) {
            String input = scanner.nextLine().trim().toLowerCase();
            
            switch (input) {
                case "", "enter" -> launchGame(); //launches game
                case "m" -> openModsFolder(); //opens mods folder for selected instance
                case "s" -> settings(); // opens settings
                case "h" -> showHelp(); // shows help options
                case "c" -> printGUI(); // clears screen and shows splash again
                case "r" -> // refreshes instances list
                { 
                    refreshInstances(); 
                    printGUI(); 
                }
                case "q" -> // quits Azulejo
                {
                    System.out.println(WHITE + "\nGoodbye!" + RESET);
                    LOGGER.info("Launcher closed by user");
                    scanner.close();
                    System.exit(0);
                }
                default -> // what was that?
                {
                    System.out.println(LIGHT_RED + "Entry not valid. Press H for a list of commands." + RESET);
                    LOGGER.fine("Unknown command entered: " + input);
                    printPrompt();
                }
            }
        }
    }
    
    private void enableAnsiSupport() {
        // enables ansi (needs to be enabled even though win 10 & 11 support ansi natively)
        // MacOS and most Linux distros don't need to enable this, for them it's on by default
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) { // checks to see if OS name has "win" in it and enables it if it does
                new ProcessBuilder("cmd", "/c", "").inheritIO().start().waitFor(); 
            }
        } catch (Exception ignored) {
            // colors won't work if it fails (but that's all)
            LOGGER.warning("Failed to enable ANSI support. If you are using an old version of Windows, the black-and-white interface should fit right in.");
        }
    }
    // TODO do this
    private void printGUI() {
        clearScreen();
        
        System.out.println();
        System.out.println(DIM + " Welcome to" + RESET + BRIGHT_WHITE + " Coastline" + RESET);
        System.out.println();
        
        // logo
        System.out.println(BLUE + "     ___              __       _     " + RESET);
        System.out.println(BLUE + "    /   |____  __  __/ /__    (_)___ " + RESET);
        System.out.println(BLUE + "   / /| /_  / / / / / / _ \\  / / __ \\" + RESET);
        System.out.println(BLUE + "  / ___ |/ /_/ /_/ / /  __/ / / /_/ /" + RESET);
        System.out.println(BLUE + " /_/  |_/___/\\__,_/_/\\___/_/ /\\____/  " + CYAN + "v" + launcher.getVersion() + RESET);
        System.out.println(BLUE + "                        /___/        " + RESET);
        System.out.println();
        
        System.out.println(DIM + " Made by Thrandos. Copyright 2025-26." + RESET);
        System.out.println(DIM + " A Java-based Minecraft launcher for the Coastline Server Network" + RESET);
        System.out.println(DIM + " Press " + RESET + WHITE + "h + enter" + RESET + DIM + " for help" + RESET);
        System.out.println();
        
        System.out.println(LIGHT_CYAN + "  (i) " + WHITE + "Azulejo is still in beta. A complete UI is coming soon." + RESET);
        System.out.println();
        
        printMainOptions();
        printPrompt();
    }
    
    // menu options are printMainOptions
    private void printMainOptions() {
        System.out.println(LIGHT_GREEN + " ENTER" + RESET + DIM + "      ->  Launch Coastline" + RESET);
        System.out.println(WHITE + " m + enter" + RESET + DIM + "  ->  Open mods folder" + RESET);
        System.out.println(WHITE + " s + enter" + RESET + DIM + "  ->  Settings" + RESET);
        System.out.println(WHITE + " q + enter" + RESET + DIM + "  ->  Quit" + RESET);
        System.out.println(DIM + " Active instance: " + BRIGHT_WHITE + activeInstanceTitle + RESET);
        System.out.println();
    }
    
    // the silly little prompt thing
    private void printPrompt() {
        System.out.print(BRIGHT_WHITE + " > " + RESET);
    }
    
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    // the help menu
    private void showHelp() {
        System.out.println();
        System.out.println();
        System.out.println(BRIGHT_WHITE + "Shortcuts:" + RESET);
        System.out.println(WHITE + "  R" + RESET + DIM + "  ->  Reload/refresh instances" + RESET);
        System.out.println(WHITE + "  B" + RESET + DIM + "  ->  Open bug report form in browser" + RESET);
        System.out.println(WHITE + "  A" + RESET + DIM + "  ->  Manage accounts" + RESET);
        System.out.println(WHITE + "  C" + RESET + DIM + "  ->  Clear console" + RESET);
        System.out.println();
        printMainOptions();
        printPrompt();
    }
    
    // fun part
    private void launchGame() {
        InstanceRef instanceToLaunch = getActiveInstance();
        if (instanceToLaunch == null) {
            printPrompt();
            return;
        }
        
        // check for account
        AccountStore accounts = launcher.getAccounts();
        if (accounts.getSize() == 0) {
            System.out.println(RED + "\nNo accounts configured!" + RESET);
            System.out.println(YELLOW + "Please add a Microsoft account first." + RESET);
            manageAccounts();
            return;
        }
        
        // launch with progress meter
        launchWithProgress(instanceToLaunch);
    }
    
    // pick instance!
    private InstanceRef selectInstance(InstanceListRef instances) {
        System.out.println();
        System.out.println(LIGHT_CYAN + "=== CHOOSE AN INSTANCE ===" + RESET);
        
        for (int i = 0; i < instances.size(); i++) {
            InstanceRef inst = instances.get(i);
            String status = inst.isInstalled() ? GREEN + "[Ready]" : YELLOW + "[Installation Required]";
            System.out.printf("%s  %d. %s%-25s %s%s%n", WHITE, i + 1, BRIGHT_WHITE, inst.getTitle(), status, RESET); // ?
        }
        System.out.println(DIM + "  0. Cancel" + RESET);
        System.out.print(BRIGHT_WHITE + " \n> " + RESET);
        
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice == 0) return null;
            if (choice < 1 || choice > instances.size()) {
                System.out.println(RED + "Invalid selection." + RESET);
                return null;
            }
            return instances.get(choice - 1);
        } catch (NumberFormatException e) {
            System.out.println(RED + "Invalid input." + RESET);
            return null;
        }
    }
    
    // this is the launch with progress seen above
    private void launchWithProgress(InstanceRef instance) {
        SavedSessionRef savedSession = launcher.getAccounts().getElementAt(0);
        
        System.out.println();
        System.out.println(BRIGHT_WHITE + "||       Coastline is booting up.       ||" + RESET);
        System.out.println();
        
        // Step 1: Restore session (authenticate)
        System.out.println(DIM + "Authenticating..." + RESET);
        SessionRef session;
        try {
            LoginServiceRef loginService = launcher.getLoginService(savedSession.getType());
            session = loginService.restore(savedSession);
            System.out.println(GREEN + "Logged in as " + session.getName() + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Authentication failed. " + e.getMessage() + RESET);
            System.out.println(YELLOW + "(Try removing and re-adding your account)" + RESET);
            printPrompt();
            return;
        }
        
        // Step 2: Build launch options
        LaunchCommand options = new LaunchCommand.Builder()
                .setInstance(instance)
                .setSession(session)
                .setListener(new CLILaunchListener())
            .setUpdatePolicy(LaunchCommand.UpdatePolicy.UPDATE_IF_SESSION_ONLINE)
                .build();
        
        // Step 3: Launch game
        System.out.println(DIM + "Preparing to launch..." + RESET);
        System.out.println();
        
        try {
            launcher.getLaunchSupervisor().launch(options);
            System.out.println(GREEN + "Done! Game launched." + RESET); // Done! Game launched in [time] seconds. 
        } catch (Exception e) {
            System.out.println(RED + "Launch failed: " + e.getMessage() + RESET);
            LOGGER.severe("Launch error: " + e.getMessage());
            printPrompt();
        }
    }
    
    // launch listener, prints status updates to console

    private class CLILaunchListener implements LaunchListenerRef {
        @Override
        public void instancesUpdated() {
            System.out.println(DIM + "Instances updated." + RESET);
        }
        
        @Override
        public void gameStarted() {
            System.out.println(GREEN + "\n=== GAME STARTED ==="  + RESET);
        }
        
        @Override
        public void gameClosed() {
            System.out.println(YELLOW + "\nGame closed." + RESET);
        }
    }
    
    private void printProgressBar(int percent, String message) {
        int barWidth = 30;
        int filled = (int) ((percent / 100.0) * barWidth);
        
        StringBuilder bar = new StringBuilder();
        bar.append(CYAN + "[");
        for (int i = 0; i < barWidth; i++) {
            if (i < filled) {
                bar.append(GREEN + "|");
            } else if (i == barWidth / 2 - 2) {
                bar.append(String.format(BRIGHT_WHITE + "%02d%%", percent));
                i += 3;
            } else {
                bar.append(DIM + "-");
            }
        }
        bar.append(CYAN + "]" + RESET);
        
        // yay spinny thingy I love these
        char spin = SPINNER[(percent / 5) % SPINNER.length];
        
        System.out.print("\rProgress: " + bar.toString() + " " + YELLOW + spin + RESET + "  ");
        System.out.println();
        System.out.println(DIM + message + RESET);
    }
    
    private void openModsFolder() {
        InstanceRef instance = getActiveInstance();
        if (instance == null) {
            printPrompt();
            return;
        }
        
        File modsDir = new File(instance.getContentDir(), "mods");
        if (!modsDir.exists()) {
            modsDir.mkdirs();
        }
        
        try {
            Desktop.getDesktop().open(modsDir);
            System.out.println(GREEN + "\nOpened mods folder: " + modsDir.getAbsolutePath() + RESET);
        } catch (IOException e) {
            System.out.println(RED + "\nCouldn't open folder: " + e.getMessage() + RESET);
        }
        
        printPrompt();
    }
    
    private void refreshInstances() {
        System.out.println(YELLOW + "\nRefreshing instances..." + RESET);
        try {
            EnumeratorRef enumerator = launcher.getInstances().createEnumerator();
            enumerator.call();
            keepActiveInstanceValid(launcher.getInstances());
            System.out.println(GREEN + "Instances refreshed successfully!" + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Failed to refresh: " + e.getMessage() + RESET);
        }
    }
    
    private void manageAccounts() {
        boolean inMenu = true;
        
        while (inMenu) {
            AccountStore accounts = launcher.getAccounts();
            
            System.out.println();
            System.out.println(LIGHT_CYAN + "=== ACCOUNT MANAGEMENT ===" + RESET);
            System.out.println();
            
            if (accounts.getSize() == 0) {
                System.out.println(DIM + "  No accounts configured" + RESET);
            } else {
                for (int i = 0; i < accounts.getSize(); i++) {
                    SavedSessionRef session = accounts.getElementAt(i);
                    System.out.println(WHITE + "  " + (i + 1) + ". " + BRIGHT_WHITE + session.getUsername() + RESET);
                }
            }
            
            System.out.println();
            System.out.println(CYAN + "  1" + RESET + DIM + " -> Add Account (Microsoft)" + RESET);
            System.out.println(CYAN + "  2" + RESET + DIM + " -> Remove Account" + RESET);
            System.out.println(DIM + "  0" + RESET + DIM + " -> Back" + RESET);
            System.out.print(BRIGHT_WHITE + " > " + RESET);
            
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addAccount();
                case "2" -> removeAccount();
                case "0", "" -> inMenu = false;
                default -> System.out.println(RED + "Invalid option." + RESET);
            }
        }
        
        printGUI();
    }
    
    private void addAccount() {
        System.out.println();
        System.out.println(YELLOW + "To add a Microsoft account:" + RESET);
        System.out.println(BRIGHT_WHITE + "A browser window will open for authentication." + RESET);
        System.out.println(DIM + "(Full Microsoft OAuth flow integration pending)" + RESET);
        
        try {
            Desktop.getDesktop().browse(new URI("https://login.live.com/oauth20_authorize.srf"));
        } catch (Exception e) {
            System.out.println(RED + "Couldn't open browser: " + e.getMessage() + RESET);
        }
    }
    
    private void removeAccount() {
        AccountStore accounts = launcher.getAccounts();
        if (accounts.getSize() == 0) {
            System.out.println(RED + "\nNo accounts to remove." + RESET);
            return;
        }
        
        System.out.print(BRIGHT_WHITE + "Account number to remove (0 to cancel): " + RESET);
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice == 0) return;
            if (choice < 1 || choice > accounts.getSize()) {
                System.out.println(RED + "Invalid selection." + RESET);
                return;
            }
            
            SavedSessionRef session = accounts.getElementAt(choice - 1);
            accounts.remove(session);
            try {
                launcher.getPersistence().commitAndForget(accounts);
                System.out.println(GREEN + "Removed " + session.getUsername() + RESET);
            } catch (Exception e) {
                System.out.println(RED + "Removed account " + e.getMessage() + ", but failed to persist changes." + RESET);
            }
        } catch (NumberFormatException e) {
            System.out.println(RED + "Invalid input." + RESET);
        }
    }
    
    private void settings() {
        boolean inMenu = true;
        
        while (inMenu) {
            ConfigurationRef config = launcher.getConfig();
            
            System.out.println();
            System.out.println(LIGHT_CYAN + "=== SETTINGS ===" + RESET);
            System.out.println();
            
            System.out.println(DIM + "Current Configuration:" + RESET);
            System.out.printf(WHITE + "  Max Memory:   " + BRIGHT_WHITE + "%d MB%n" + RESET, config.getMaxMemory());
            System.out.printf(WHITE + "  Min Memory:   " + BRIGHT_WHITE + "%d MB%n" + RESET, config.getMinMemory());
            System.out.printf(WHITE + "  JVM Args:     " + BRIGHT_WHITE + "%s%n" + RESET, 
                config.getJvmArgs() != null ? config.getJvmArgs() : "(none)");
            System.out.printf(WHITE + "  Offline Mode: " + BRIGHT_WHITE + "%s%n" + RESET,
                config.isOfflineEnabled() ? "Enabled" : "Disabled");
            
            System.out.println();
            System.out.println(CYAN + "  1" + RESET + "  ->  Configure Memory");
            System.out.println(CYAN + "  2" + RESET + "  ->  Configure Launch Args");
            System.out.println(CYAN + "  3" + RESET + "  ->  Reset to Defaults");
            System.out.println(CYAN + "  4" + RESET + "  ->  Switch Active Instance");
            System.out.println(DIM + "  0" + RESET + "  ->  Back (saves automatically)");
            System.out.print(BRIGHT_WHITE + "\nChoice: " + RESET);
            
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> configureMemory(config);
                case "2" -> configureLaunchArgs(config);
                case "3" -> resetSettings(config);
                case "4" -> switchActiveInstance();
                case "0", "" -> {
                    saveSettings(config);
                    inMenu = false;
                }
                default -> System.out.println(RED + "Invalid option." + RESET);
            }
        }
        
        printGUI();
    }

    // The memory configuration screen.
    private void configureMemory(ConfigurationRef config) {
        System.out.println();
        System.out.println(BRIGHT_WHITE + "Memory Configuration" + RESET);
        
        System.out.print(WHITE + "Max Memory (MB) (current: " + config.getMaxMemory() + "): " + RESET);
        try {
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                int mem = Integer.parseInt(input);
                if (mem < 512) {
                    System.out.println(YELLOW + "Warning: Less than 512 MB may cause issues." + RESET);
                }
                config.setMaxMemory(mem);
                System.out.println(GREEN + "Max memory set to " + mem + " MB" + RESET);
            }
        } catch (NumberFormatException e) {
            System.out.println(RED + "Please enter a valid number." + RESET);
        }
        
        System.out.print(WHITE + "Min Memory (MB) (current: " + config.getMinMemory() + "): " + RESET);
        try {
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                int mem = Integer.parseInt(input);
                config.setMinMemory(mem);
                System.out.println(GREEN + "Min memory set to " + mem + " MB" + RESET);
            }
        } catch (NumberFormatException e) {
            System.out.println(RED + "Please enter a valid number." + RESET);
        }
    }
    
    private void configureLaunchArgs(ConfigurationRef config) {
        System.out.println();
        System.out.println(BRIGHT_WHITE + "JVM Arguments" + RESET);
        System.out.println(DIM + "Current: " + (config.getJvmArgs() != null ? config.getJvmArgs() : "(none)") + RESET);
        System.out.print(WHITE + "New arguments (empty to clear): " + RESET);
        
        String args = scanner.nextLine().trim();
        config.setJvmArgs(args.isEmpty() ? null : args);
        System.out.println(GREEN + "JVM arguments updated." + RESET);
    }
    
    private void resetSettings(ConfigurationRef config) {
        System.out.print(YELLOW + "Reset all settings to defaults? (Y/N): " + RESET);
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            config.setMaxMemory(4096);
            config.setMinMemory(1024);
            config.setJvmArgs(null);
            config.setOfflineEnabled(false);
            saveSettings(config);
            System.out.println(GREEN + "Settings reset to defaults." + RESET);
        } else {
            System.out.println(DIM + "Reset cancelled." + RESET);
        }
    }    private void saveSettings(ConfigurationRef config) {
        try {
            launcher.getPersistence().commitAndForget(config);
            System.out.println(GREEN + "Settings saved." + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Failed to save: " + e.getMessage() + RESET);
        }
    }

    private void initializeActiveInstanceSelection() {
        keepActiveInstanceValid(launcher.getInstances());
    }

    private InstanceRef getActiveInstance() {
        InstanceListRef instances = launcher.getInstances();

        if (instances.size() == 0) {
            System.out.println(YELLOW + "\nNo instances found. Refreshing..." + RESET);
            refreshInstances();
            instances = launcher.getInstances();
        }

        if (instances.size() == 0) {
            System.out.println(RED + "Still no instances. Check your connection or config." + RESET);
            return null;
        }

        keepActiveInstanceValid(instances);
        return findInstanceByTitle(instances, activeInstanceTitle);
    }

    private void switchActiveInstance() {
        InstanceListRef instances = launcher.getInstances();
        if (instances.size() == 0) {
            System.out.println(YELLOW + "\nNo instances found. Refreshing..." + RESET);
            refreshInstances();
            instances = launcher.getInstances();
        }

        if (instances.size() == 0) {
            System.out.println(RED + "No instances are available." + RESET);
            return;
        }

        keepActiveInstanceValid(instances);

        System.out.println();
        System.out.println(LIGHT_CYAN + "=== SWITCH ACTIVE INSTANCE ===" + RESET);
        for (int i = 0; i < instances.size(); i++) {
            InstanceRef inst = instances.get(i);
            String marker = inst.getTitle().equalsIgnoreCase(activeInstanceTitle) ? LIGHT_GREEN + "[ACTIVE] " : "         ";
            String status = inst.isInstalled() ? GREEN + "[Ready]" : YELLOW + "[Installation Required]";
            System.out.printf("%s%s%d. %s%-25s %s%s%n", WHITE, marker, i + 1, BRIGHT_WHITE, inst.getTitle(), status, RESET);
        }
        System.out.println(DIM + "  0. Cancel" + RESET);
        System.out.print(BRIGHT_WHITE + "\n> " + RESET);

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice == 0) {
                return;
            }
            if (choice < 1 || choice > instances.size()) {
                System.out.println(RED + "Invalid selection." + RESET);
                return;
            }
            InstanceRef selected = instances.get(choice - 1);
            activeInstanceTitle = selected.getTitle();
            System.out.println(GREEN + "Active instance set to: " + activeInstanceTitle + RESET);
        } catch (NumberFormatException e) {
            System.out.println(RED + "Invalid input." + RESET);
        }
    }

    private void keepActiveInstanceValid(InstanceListRef instances) {
        if (instances.size() == 0) {
            activeInstanceTitle = DEFAULT_INSTANCE_NAME;
            return;
        }

        InstanceRef active = findInstanceByTitle(instances, activeInstanceTitle);
        if (active != null) {
            return;
        }

        InstanceRef defaultInstance = findInstanceByTitle(instances, DEFAULT_INSTANCE_NAME);
        if (defaultInstance != null) {
            activeInstanceTitle = defaultInstance.getTitle();
            return;
        }

        activeInstanceTitle = instances.get(0).getTitle();
    }

    private InstanceRef findInstanceByTitle(InstanceListRef instances, String title) {
        for (int i = 0; i < instances.size(); i++) {
            InstanceRef instance = instances.get(i);
            if (instance.getTitle().equalsIgnoreCase(title)) {
                return instance;
            }
        }
        return null;
    }
}