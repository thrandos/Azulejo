package com.thrandos.azulejo.launcher.cli;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Scanner;
import java.util.logging.Logger;

import com.thrandos.azulejo.launcher.Configuration;
import com.thrandos.azulejo.launcher.Instance;
import com.thrandos.azulejo.launcher.InstanceList;
import com.thrandos.azulejo.launcher.Launcher;
import com.thrandos.azulejo.launcher.auth.AccountList;
import com.thrandos.azulejo.launcher.auth.LoginService;
import com.thrandos.azulejo.launcher.auth.SavedSession;
import com.thrandos.azulejo.launcher.auth.Session;
import com.thrandos.azulejo.launcher.launch.LaunchListener;
import com.thrandos.azulejo.launcher.launch.LaunchOptions;
import com.thrandos.azulejo.launcher.persistence.Persistence;

/*
Another amazing idea from ME!

This is the CLI UI for Azulejo. 
I plan to keep it once we switch to JavaFX as a cool little extra bit.
FYI I have never worked with CLI or ANSI codes before so good luck to me!
*/

public class LauncherCLI {
    private static final Logger LOGGER = Logger.getLogger(LauncherCLI.class.getName());
    private final Launcher launcher;
    private final Scanner scanner = new Scanner(System.in);
    
    // ANSI color codes
    
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
    
    // light variants
    private static final String LIGHT_CYAN = "\u001B[96m";
    private static final String LIGHT_GREEN = "\u001B[92m";
    private static final String LIGHT_YELLOW = "\u001B[93m";
    private static final String LIGHT_RED = "\u001B[91m";
    
    // and bright white (idk why this one is 'bright' ask windows CMD)
    private static final String BRIGHT_WHITE = "\u001B[97m";

    // spinner characters yay
    private static final char[] SPINNER = {'/', '-', '\\', '|'};

    public LauncherCLI(Launcher launcher) {
        this.launcher = launcher;
    }

    public void run() {
        // Enable ANSI on Windows
        enableAnsiSupport();
        
        // Show splash screen
        printSplashScreen();
        
        while (true) {
            String input = scanner.nextLine().trim().toLowerCase();
            
            switch (input) {
                case "", "enter" -> launchCoastline(); //launches game
                case "m" -> openModsFolder(); //opens mods folder for selected instance
                case "s" -> settings(); // opens settings
                case "h" -> showHelp(); // shows help options
                case "c" -> printSplashScreen(); // clears screen and shows splash again
                case "r" -> // refreshes instances list
                { 
                    refreshInstances(); 
                    printSplashScreen(); 
                }
                case "q" -> // quits the launcher
                {
                    System.out.println(WHITE + "\nGoodbye!" + RESET);
                    LOGGER.info("Launcher closed by user");
                    scanner.close();
                    System.exit(0);
                }
                default -> // what was that? 
                {
                    System.out.println(LIGHT_RED + "Unknown command. Press H for help." + RESET);
                    LOGGER.fine("Unknown command entered: " + input);
                    printPrompt();
                }
            }
        }
    }
    
    private void enableAnsiSupport() {
        // Windows 10+ supports ANSI natively, but we need to enable it
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "").inheritIO().start().waitFor();
            }
        } catch (Exception ignored) {
            // If it fails, colors just won't work
        }
    }
    
    private void printSplashScreen() {
        clearScreen();
        
        System.out.println();
        System.out.println(DIM + " Welcome to" + RESET + BRIGHT_WHITE + " Coastline" + RESET);
        System.out.println();
        
        // Logo
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
    
    // menu options printMainOptions
    private void printMainOptions() {
        System.out.println(LIGHT_GREEN + " ENTER" + RESET + DIM + "      ->  Launch Coastline" + RESET);
        System.out.println(WHITE + " m + enter" + RESET + DIM + "  ->  Open mods folder" + RESET);
        System.out.println(WHITE + " s + enter" + RESET + DIM + "  ->  Settings" + RESET);
        System.out.println(WHITE + " q + enter" + RESET + DIM + "  ->  Quit" + RESET);
        System.out.println();
    }
    
    
    private void printPrompt() {
        System.out.print(BRIGHT_WHITE + " > " + RESET);
    }
    
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
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
    
    private void launchCoastline() {
        // check for instances
        InstanceList instances = launcher.getInstances();
        
        if (instances.size() == 0) {
            System.out.println(YELLOW + "\nNo instances found. Refreshing..." + RESET);
            refreshInstances();
            instances = launcher.getInstances();
            
            if (instances.size() == 0) {
                System.out.println(RED + "Still no instances. Check your connection or config." + RESET);
                printPrompt();
                return;
            }
        }
        
        // check for account
        AccountList accounts = launcher.getAccounts();
        if (accounts.getSize() == 0) {
            System.out.println(RED + "\nNo accounts configured!" + RESET);
            System.out.println(YELLOW + "Please add a Microsoft account first." + RESET);
            manageAccounts();
            return;
        }
        
        // let user choose if multiple instances exist
        Instance instanceToLaunch;
        if (instances.size() == 1) {
            instanceToLaunch = instances.get(0);
        } else {
            instanceToLaunch = selectInstance(instances);
            if (instanceToLaunch == null) {
                printPrompt();
                return;
            }
        }
        
        // launch with progress meter
        launchWithProgress(instanceToLaunch);
    }
    
    private Instance selectInstance(InstanceList instances) {
        System.out.println();
        System.out.println(LIGHT_CYAN + "=== SELECT INSTANCE ===" + RESET);
        
        for (int i = 0; i < instances.size(); i++) {
            Instance inst = instances.get(i);
            String status = inst.isInstalled() ? GREEN + "[Ready]" : YELLOW + "[Needs Install]";
            System.out.printf("%s  %%d. %s%%-25s %s%s%%n", WHITE, BRIGHT_WHITE, status, RESET);
            System.out.printf("%s  %d. %s%-25s %s%s%n", WHITE, i + 1, BRIGHT_WHITE, inst.getTitle(), status, RESET);
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
    private void launchWithProgress(Instance instance) {
        SavedSession savedSession = launcher.getAccounts().getElementAt(0);
        
        System.out.println();
        System.out.println(BRIGHT_WHITE + "||       Coastline is booting up.       ||" + RESET);
        System.out.println();
        
        // Step 1: Restore the session (authenticate)
        System.out.println(DIM + "Authenticating..." + RESET);
        Session session;
        try {
            LoginService loginService = launcher.getLoginService(savedSession.getType());
            session = loginService.restore(savedSession);
            System.out.println(GREEN + "Logged in as " + session.getName() + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Authentication failed: " + e.getMessage() + RESET);
            System.out.println(YELLOW + "Try removing and re-adding your account." + RESET);
            printPrompt();
            return;
        }
        
        // Step 2: Build launch options
        LaunchOptions options = new LaunchOptions.Builder()
                .setInstance(instance)
                .setSession(session)
                .setListener(new CLILaunchListener())
                .setUpdatePolicy(LaunchOptions.UpdatePolicy.UPDATE_IF_SESSION_ONLINE)
                .build();
        
        // Step 3: Launch the game
        System.out.println(DIM + "Preparing to launch..." + RESET);
        System.out.println();
        
        try {
            launcher.getLaunchSupervisor().launch(options);
            System.out.println(GREEN + "Game launched! The launcher will close when the game exits." + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Launch failed: " + e.getMessage() + RESET);
            LOGGER.severe("Launch error: " + e.getMessage());
            printPrompt();
        }
    }
    
    /**
     * CLI-friendly launch listener that prints status updates to console.
     */
    private class CLILaunchListener implements LaunchListener {
        @Override
        public void instancesUpdated() {
            System.out.println(DIM + "Instances updated." + RESET);
        }
        
        @Override
        public void gameStarted() {
            System.out.println(GREEN + "\n=== GAME STARTED ==="  + RESET);
            System.out.println(DIM + "Azulejo is running in the background." + RESET);
            System.out.println(DIM + "The launcher will close when you exit the game." + RESET);
        }
        
        @Override
        public void gameClosed() {
            System.out.println(YELLOW + "\nGame closed. Goodbye!" + RESET);
            scanner.close();
            System.exit(0);
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
        InstanceList instances = launcher.getInstances();
        
        if (instances.size() == 0) {
            System.out.println(RED + "\nNo instances found." + RESET);
            printPrompt();
            return;
        }
        
        Instance instance = instances.size() == 1 ? instances.get(0) : selectInstance(instances);
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
            InstanceList.Enumerator enumerator = launcher.getInstances().createEnumerator();
            enumerator.call();
            System.out.println(GREEN + "Instances refreshed successfully!" + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Failed to refresh: " + e.getMessage() + RESET);
        }
    }
    
    private void manageAccounts() {
        boolean inMenu = true;
        
        while (inMenu) {
            AccountList accounts = launcher.getAccounts();
            
            System.out.println();
            System.out.println(LIGHT_CYAN + "=== ACCOUNT MANAGEMENT ===" + RESET);
            System.out.println();
            
            if (accounts.getSize() == 0) {
                System.out.println(DIM + "  No accounts configured" + RESET);
            } else {
                for (int i = 0; i < accounts.getSize(); i++) {
                    SavedSession session = accounts.getElementAt(i);
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
        
        printSplashScreen();
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
        AccountList accounts = launcher.getAccounts();
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
            
            SavedSession session = accounts.getElementAt(choice - 1);
            accounts.remove(session);
            Persistence.commitAndForget(accounts);
            System.out.println(GREEN + "Removed: " + session.getUsername() + RESET);
        } catch (NumberFormatException e) {
            System.out.println(RED + "Invalid input." + RESET);
        }
    }
    
    private void settings() {
        boolean inMenu = true;
        
        while (inMenu) {
            Configuration config = launcher.getConfig();
            
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
            System.out.println(DIM + "  0" + RESET + "  ->  Back (saves automatically)");
            System.out.print(BRIGHT_WHITE + "\nChoice: " + RESET);
            
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> configureMemory(config);
                case "2" -> configureLaunchArgs(config);
                case "3" -> resetSettings(config);
                case "0", "" -> {
                    saveSettings(config);
                    inMenu = false;
                }
                default -> System.out.println(RED + "Invalid option." + RESET);
            }
        }
        
        printSplashScreen();
    }

    // The memory configuration screen.
    private void configureMemory(Configuration config) {
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
    
    private void configureLaunchArgs(Configuration config) {
        System.out.println();
        System.out.println(BRIGHT_WHITE + "JVM Arguments" + RESET);
        System.out.println(DIM + "Current: " + (config.getJvmArgs() != null ? config.getJvmArgs() : "(none)") + RESET);
        System.out.print(WHITE + "New arguments (empty to clear): " + RESET);
        
        String args = scanner.nextLine().trim();
        config.setJvmArgs(args.isEmpty() ? null : args);
        System.out.println(GREEN + "JVM arguments updated." + RESET);
    }
    
    private void resetSettings(Configuration config) {
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
    }
    
    private void saveSettings(Configuration config) {
        try {
            Persistence.commitAndForget(config);
            System.out.println(GREEN + "Settings saved." + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Failed to save: " + e.getMessage() + RESET);
        }
    }
}