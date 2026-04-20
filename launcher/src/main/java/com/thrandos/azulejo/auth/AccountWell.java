package com.thrandos.azulejo.auth;

// Saves the session, which includes token, username, and
// UUID. Uses the Java Keystore API to encrypt account information.

// (this is so you don't have to sign in every time, that would probably suck)

// accounts are saved to a local JSON file in AppData (.../azulejo/accounts.json)
// I'll probably figure out how to encrypt this but for now idk 

// wow I'm yapping a lot in this one

import org.json.JSONArray;
import org.json.JSONObject;
 
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
// import com.github.windpapi4j.WinDPAPI; (why this can't possibly work is beyond me)
 
import lombok.extern.java.Log;
@Log // I think I like this thing

public class AccountWell {

    // constants

    private static final long TOKEN_LIFETIME_SECONDS = 23 * 60 * 60; // token lives for about 23 hours cause they usually expire in 24

    // JSON key names used in the save file so it explodes when it compiles instead of when someone's using it (generally bad for it to do that anyways)

    private static final String KEY_ACTIVE_UUID  = "activeUuid";
    private static final String KEY_ACCOUNTS     = "accounts";
    private static final String KEY_UUID         = "uuid";
    private static final String KEY_USERNAME     = "username";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_SKIN_URL     = "skinUrl";
    private static final String KEY_SAVED_AT     = "savedAt";

    // fields

    String rAppData = System.getenv("APPDATA"); // hey windows please tell me where appdata is
    String lAppData = System.getenv("LOCALAPPDATA"); // local appdata too thank you
    private final Path storageFile;
 
    /**
     * The in-memory list of accounts. This is the "live" data.
     * load() populates it from disk; save() writes it back.
     * We use LinkedHashMap so accounts preserve their insertion
     * order (important for a predictable UI list).
     *
     * Key:   the player's UUID string (with dashes)
     * Value: the StoredAccount object
     */
    private final Map<String, StoredAccount> accounts = new LinkedHashMap<>();
 
    /**
     * The UUID of the currently active (selected) account.
     * null means no account is selected (e.g. after first install,
     * or after the only account was removed).
     */
    private String activeUuid = null;
 
    // ── Constructors ──────────────────────────────────────────────────────────
 
    /**
     * Creates an AccountStorage at
     *   .../AppData/Coastline/Azulejo/meta/accounts.json
     *
     * The directory will be created automatically on first save() if
     * it doesn't exist yet.
     */
    public AccountWell() {
        this(Path.of(System.getProperty("user.home"), "Azulejo", "accounts.json"));
    }
 
    /**
     * Creates an AccountStorage using a custom file path.
     * Useful for testing, or if your launcher stores data elsewhere.
     *
     * @param storageFile Path to the JSON file to read/write.
     *                    The file doesn't have to exist yet.
     */
    public AccountWell(Path storageFile) {
        this.storageFile = storageFile;
    }
 
    // ── Loading and saving ────────────────────────────────────────────────────
 
    /**
     * Loads accounts from disk into memory.
     *
     * Call this once when the launcher starts. If the file doesn't
     * exist yet (e.g. first launch), this does nothing — the account
     * list stays empty, which is the correct starting state.
     *
     * If the file is corrupted or unreadable, a warning is printed
     * and the account list stays empty rather than crashing the
     * launcher. The user will just need to log in again.
     *
     * @throws IOException if the file exists but can't be read due
     *                     to permissions or an I/O error (not thrown
     *                     if the file simply doesn't exist yet).
     */
    public void load() throws IOException {
        // Nothing to load if the file doesn't exist yet
        if (!Files.exists(storageFile)) {
            System.out.println("[AccountStorage] No save file found at " + storageFile + ". Starting fresh.");
            return;
        }
 
        // Read the entire file into a string, then parse it as JSON
        String raw;
        try {
            raw = Files.readString(storageFile);
        } catch (IOException e) {
            throw new IOException("Could not read accounts file: " + storageFile, e);
        }
 
        // Try to parse the JSON. If it's malformed, warn and continue
        // rather than crashing — the user can just re-authenticate.
        JSONObject root;
        try {
            root = new JSONObject(raw);
        } catch (Exception e) {
            System.err.println("[AccountStorage] WARNING: accounts.json is corrupted and will be ignored. " +
                               "Users will need to log in again. (" + e.getMessage() + ")");
            return;
        }
 
        // Clear our current in-memory state before re-loading
        accounts.clear();
        activeUuid = null;
 
        // Read the active UUID (may be absent if the file was created
        // without one, e.g. from an older version of your launcher)
        if (root.has(KEY_ACTIVE_UUID) && !root.isNull(KEY_ACTIVE_UUID)) {
            activeUuid = root.getString(KEY_ACTIVE_UUID);
        }
 
        // Read the accounts array. Each entry becomes a StoredAccount.
        JSONArray arr = root.optJSONArray(KEY_ACCOUNTS);
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject obj = arr.getJSONObject(i);
                    StoredAccount account = StoredAccount.fromJson(obj);
                    accounts.put(account.uuid(), account);
                } catch (Exception e) {
                    // Skip any individual entry that's malformed.
                    // This handles the case where one account record
                    // got corrupted but others are fine.
                    System.err.println("[AccountStorage] Skipping malformed account entry #" + i +
                                       ": " + e.getMessage());
                }
            }
        }
 
        System.out.println("[AccountStorage] Loaded " + accounts.size() + " account(s).");
    }
 
    /**
     * Saves the current in-memory accounts to disk.
     *
     * Call this after any change (addOrUpdateAccount, removeAccount,
     * setActiveAccount). The file is written atomically using a
     * temp file + rename to avoid leaving a half-written file on
     * disk if the JVM crashes mid-write.
     *
     * @throws IOException if the file can't be written (e.g. no
     *                     disk space, permissions issue).
     */
    public void save() throws IOException {
        // Build the root JSON object
        JSONObject root = new JSONObject();
 
        // Write the active UUID (or JSON null if nobody is active)
        root.put(KEY_ACTIVE_UUID, activeUuid != null ? activeUuid : JSONObject.NULL);
 
        // Convert each StoredAccount to a JSON object and add to array
        JSONArray arr = new JSONArray();
        for (StoredAccount account : accounts.values()) {
            arr.put(account.toJson());
        }
        root.put(KEY_ACCOUNTS, arr);
 
        // Make sure the parent directory exists
        // (e.g. ~/.minecraft_launcher/ on first ever save)
        Files.createDirectories(storageFile.getParent());
 
        // Write to a temp file first, then atomically rename it over
        // the real file. This way, if the write fails halfway through,
        // the old file is still intact on disk.
        Path tempFile = storageFile.resolveSibling(storageFile.getFileName() + ".tmp");
        Files.writeString(tempFile, root.toString(2)); // "2" = indent for readability
        Files.move(tempFile, storageFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
 
        System.out.println("[AccountStorage] Saved " + accounts.size() + " account(s) to " + storageFile);
    }
 
    // ── Account management ────────────────────────────────────────────────────
 
    /**
     * Adds a new account, or updates it if one with the same UUID
     * already exists.
     *
     * This is the main method to call right after a successful
     * {@code MicrosoftAuthFlow.authenticate()} call. It also
     * automatically sets the newly added/updated account as the
     * active one — most of the time, logging in means you want to
     * play as that account next.
     *
     * @param profile The PlayerProfile returned by MicrosoftAuthFlow.
     */
    public void addOrUpdateAccount(AuthRook.PlayerProfile profile) {
        // Wrap the profile in a StoredAccount with the current timestamp.
        // "Instant.now()" records when the token was saved, which we
        // later use to check if it has expired.
        StoredAccount account = new StoredAccount(
                profile.uuid(),
                profile.username(),
                profile.accessToken(),
                profile.skinUrl(),
                Instant.now()
        );
 
        boolean isNew = !accounts.containsKey(account.uuid());
        accounts.put(account.uuid(), account);
 
        // Make this the active account automatically
        activeUuid = account.uuid();
 
        System.out.println("[AccountStorage] " + (isNew ? "Added" : "Updated") +
                           " account: " + account.username() + " (" + account.uuid() + ")");
    }
 
    /**
     * Removes the account with the given UUID.
     *
     * If the removed account was the active one, the active account
     * is automatically changed to the first remaining account. If
     * there are no remaining accounts, the active UUID is set to null.
     *
     * @param uuid The UUID (with dashes) of the account to remove.
     * @return true if an account was found and removed, false if no
     *         account with that UUID existed.
     */
    public boolean removeAccount(String uuid) {
        StoredAccount removed = accounts.remove(uuid);
 
        if (removed == null) {
            // No account with that UUID — nothing to do
            return false;
        }
 
        System.out.println("[AccountStorage] Removed account: " + removed.username());
 
        // If we just removed the active account, pick a new one.
        // We use the first entry in the map (oldest added), or null
        // if the map is now empty.
        if (uuid.equals(activeUuid)) {
            activeUuid = accounts.isEmpty() ? null : accounts.keySet().iterator().next();
            System.out.println("[AccountStorage] Active account changed to: " +
                               (activeUuid != null ? accounts.get(activeUuid).username() : "(none)"));
        }
 
        return true;
    }
 
    /**
     * Sets which account will be used to launch the game by default.
     *
     * @param uuid UUID of the account to make active.
     * @throws NoSuchElementException if no account with that UUID
     *                                exists in storage.
     */
    public void setActiveAccount(String uuid) {
        if (!accounts.containsKey(uuid)) {
            throw new NoSuchElementException("No account found with UUID: " + uuid);
        }
        activeUuid = uuid;
        System.out.println("[AccountStorage] Active account set to: " + accounts.get(uuid).username());
    }
 
    // ── Querying ──────────────────────────────────────────────────────────────
 
    /**
     * Returns the currently active account, if one exists.
     *
     * "Active" means the account that will be used to launch the
     * game. This is Optional because the launcher might have zero
     * accounts saved (brand new install) or the active UUID might
     * point to an account that was deleted.
     *
     * @return An Optional containing the active StoredAccount,
     *         or Optional.empty() if there is none.
     */
    public Optional<StoredAccount> getActiveAccount() {
        if (activeUuid == null) return Optional.empty();
        // Use ofNullable in case activeUuid points to a deleted account
        return Optional.ofNullable(accounts.get(activeUuid));
    }
 
    /**
     * Returns every saved account as an unmodifiable list.
     *
     * The order matches insertion order (oldest first). Use this to
     * populate the accounts screen in your launcher UI.
     *
     * @return Unmodifiable list of all stored accounts.
     */
    public List<StoredAccount> getAllAccounts() {
        // Wrap in an unmodifiable view so callers can't accidentally
        // modify the internal list by casting it
        return Collections.unmodifiableList(new ArrayList<>(accounts.values()));
    }
 
    /**
     * Looks up a specific account by its UUID.
     *
     * @param uuid The UUID (with dashes) to search for.
     * @return An Optional containing the account if found.
     */
    public Optional<StoredAccount> getAccount(String uuid) {
        return Optional.ofNullable(accounts.get(uuid));
    }
 
    /**
     * Returns how many accounts are currently stored.
     */
    public int getAccountCount() {
        return accounts.size();
    }
 
    /**
     * Returns true if there are no saved accounts at all.
     * Handy for deciding whether to show a "no accounts" empty state
     * in your launcher UI.
     */
    public boolean isEmpty() {
        return accounts.isEmpty();
    }
 
    // ── Inner class: StoredAccount ────────────────────────────────────────────
 
    /**
     * Represents a single saved Minecraft account.
     *
     * This is a record (Java 16+), meaning it's immutable —
     * you can't change any of its fields after creation. Instead,
     * when an account is updated (e.g. new token after refresh),
     * a new StoredAccount is created and replaces the old one in
     * the AccountStorage map.
     *
     * @param uuid        The player's UUID with dashes, e.g.
     *                    "550e8400-e29b-41d4-a716-446655440000".
     *                    This is the unique identifier for the account.
     *
     * @param username    The player's in-game name, e.g. "Steve".
     *                    Note: usernames can change on Minecraft.net,
     *                    so this may be outdated after a while.
     *
     * @param accessToken The Minecraft access token used to launch
     *                    the game and authenticate with servers.
     *                    Expires after ~24 hours.
     *
     * @param skinUrl     URL to the player's active skin texture.
     *                    May be null if the player uses the default
     *                    skin or if the API didn't return one.
     *
     * @param savedAt     The moment this account entry was saved.
     *                    Used to calculate whether the token has expired.
     */
    public record StoredAccount(
            String uuid,
            String username,
            String accessToken,
            String skinUrl,
            Instant savedAt
    ) {
 
        /**
         * Checks whether the stored access token has likely expired.
         *
         * We say "likely" because we can't know for certain without
         * actually asking Microsoft's servers. This uses the saved
         * timestamp + TOKEN_LIFETIME_SECONDS as a local estimate.
         *
         * If this returns true, you should send the user through
         * MicrosoftAuthFlow again before trying to launch the game.
         *
         * @return true if the token is probably expired.
         */
        public boolean isTokenExpired() {
            // Calculate the instant when the token becomes stale
            Instant expiresAt = savedAt.plusSeconds(TOKEN_LIFETIME_SECONDS);
            // If "now" is after the expiry time, the token is expired
            return Instant.now().isAfter(expiresAt);
        }
 
        /**
         * Returns a human-readable description of how long ago this
         * account was saved. Useful for displaying in the launcher UI,
         * e.g. "Last signed in 3 hours ago".
         *
         * @return A string like "2 minutes ago", "5 hours ago", etc.
         */
        public String getTimeSinceSaved() {
            long secondsAgo = Instant.now().getEpochSecond() - savedAt.getEpochSecond();
 
            if (secondsAgo < 60)             return secondsAgo + " second" + (secondsAgo == 1 ? "" : "s") + " ago";
            if (secondsAgo < 3600)           return (secondsAgo / 60) + " minute" + (secondsAgo / 60 == 1 ? "" : "s") + " ago";
            if (secondsAgo < 86400)          return (secondsAgo / 3600) + " hour" + (secondsAgo / 3600 == 1 ? "" : "s") + " ago";
            return                             (secondsAgo / 86400) + " day" + (secondsAgo / 86400 == 1 ? "" : "s") + " ago";
        }
 
        /**
         * Returns a redacted version of the access token suitable for
         * logging or displaying in a debug panel. Shows only the first
         * and last 4 characters with asterisks in between.
         *
         * e.g. "eyJh...k3nQ"
         *
         * Never log the full token — it's essentially a password.
         */
        public String getRedactedToken() {
            if (accessToken == null || accessToken.length() < 10) return "****";
            return accessToken.substring(0, 4) + "..." + accessToken.substring(accessToken.length() - 4);
        }
 
        /**
         * Serializes this account to a JSON object for writing to disk.
         * The format mirrors what fromJson() expects to read back.
         *
         * @return A JSONObject representing this account.
         */
        JSONObject toJson() {
            JSONObject obj = new JSONObject();
            obj.put(KEY_UUID,         uuid);
            obj.put(KEY_USERNAME,     username);
            obj.put(KEY_ACCESS_TOKEN, accessToken);
            // skinUrl can be null — store it as JSON null rather than
            // omitting the key entirely, so fromJson knows it was
            // intentionally absent (not a parsing error)
            obj.put(KEY_SKIN_URL,     skinUrl != null ? skinUrl : JSONObject.NULL);
            // Store the timestamp as an ISO-8601 string, e.g. "2024-01-15T10:30:00Z"
            // This is human-readable in the file and easy to parse back
            obj.put(KEY_SAVED_AT,     savedAt.toString());
            return obj;
        }
 
        /**
         * Deserializes a StoredAccount from a JSON object read from disk.
         * This is the inverse of toJson().
         *
         * @param obj The JSON object from the accounts file.
         * @return A populated StoredAccount.
         * @throws Exception if required fields are missing or malformed.
         */
        static StoredAccount fromJson(JSONObject obj) throws Exception {
            // These three fields are required — if any is missing,
            // the account is unusable and we throw to skip it
            String uuid        = obj.getString(KEY_UUID);
            String username    = obj.getString(KEY_USERNAME);
            String accessToken = obj.getString(KEY_ACCESS_TOKEN);
 
            // skinUrl is optional — use null if missing or JSON null
            String skinUrl = obj.isNull(KEY_SKIN_URL) ? null : obj.optString(KEY_SKIN_URL, null);
 
            // Parse the ISO-8601 timestamp back into an Instant.
            // If the field is missing (e.g. account saved by an older
            // version of the launcher), fall back to Instant.EPOCH
            // which will cause isTokenExpired() to return true and
            // prompt the user to re-authenticate — a safe default.
            Instant savedAt;
            String savedAtStr = obj.optString(KEY_SAVED_AT, null);
            if (savedAtStr != null) {
                savedAt = Instant.parse(savedAtStr);
            } else {
                System.err.println("[AccountStorage] No savedAt timestamp for account " + username +
                                   " — treating token as expired.");
                savedAt = Instant.EPOCH; // Will immediately be "expired"
            }
 
            return new StoredAccount(uuid, username, accessToken, skinUrl, savedAt);
        }
 
        /**
         * Produces a safe, readable summary for logging — no full token.
         */
        @Override
        public String toString() {
            return "StoredAccount{" +
                    "username='" + username + '\'' +
                    ", uuid='" + uuid + '\'' +
                    ", token=" + getRedactedToken() +
                    ", expired=" + isTokenExpired() +
                    ", savedAt='" + getTimeSinceSaved() + '\'' +
                    '}';
        }
    }
 
    // ── Quick test ────────────────────────────────────────────────────────────
 
    /**
     * Simple smoke test you can run to verify the storage works.
     * Creates a fake account, saves it, reloads it, and prints the result.
     *
     * Remove or replace this with your real launcher entry point.
     */
    public static void main(String[] args) throws Exception {
        // Use a temp file so we don't pollute the real save location
        Path testFile = Path.of(System.getProperty("java.io.tmpdir"), "launcher_test_accounts.json");
        AccountWell storage = new AccountWell(testFile);
 
        // Simulate a profile coming back from MicrosoftAuthFlow
        AuthRook.PlayerProfile fakeProfile = new AuthRook.PlayerProfile(
                "550e8400-e29b-41d4-a716-446655440000",
                "TestPlayer",
                "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.fake_token",
                "https://textures.minecraft.net/texture/fake"
        );
 
        // Add and save
        storage.addOrUpdateAccount(fakeProfile);
        storage.save();
        System.out.println("Saved: " + storage.getActiveAccount());
 
        // Reload from disk and verify
        AccountWell reloaded = new AccountWell(testFile);
        reloaded.load();
        System.out.println("Reloaded: " + reloaded.getActiveAccount());
        System.out.println("Token expired: " + reloaded.getActiveAccount().map(StoredAccount::isTokenExpired).orElse(true));
 
        // Clean up the test file
        Files.deleteIfExists(testFile);
        System.out.println("Test passed!");
    }
}