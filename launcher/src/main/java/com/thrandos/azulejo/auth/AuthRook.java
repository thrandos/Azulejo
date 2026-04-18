package com.thrandos.azulejo.auth;

// The liaison between Azulejo and Microsoft OAuth.

// when CLI prompts to fetch account information, grab token

import com.sun.net.httpserver.HttpServer; // http server
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Desktop;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AuthRook {

    // various urls
    private static final String AUTH_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize";
    private static final String TOKEN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    private static final String XBL_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MC_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MC_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";
    private static final String SCOPE = "XboxLive.signin offline_access"; // tf is this?

    // local
    private static final String REDIRECT_URI = "http://localhost:9999/callback";

    private final String clientId;
    private final HttpClient http; // httpserver library is crazy

    public AuthRook(String clientId) {
        this.clientId = clientId;
        this.http = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    // auth chain opener, returns PlayerProfile
    public PlayerProfile authenticate() throws Exception {
        String authCode = getAuthorizationCode();
        String msToken = exchangeCodeForMSToken(authCode);
        XboxTokenResult xbl = getXboxLiveToken(msToken);
        XboxTokenResult xsts = getXSTSToken(xbl.token());
        String mcToken = getMinecraftToken(xsts.token(), xsts.userHash());
        return getPlayerProfile(mcToken);
    }

    // this is the Web Authorizer; it yoinkities auth code in browser
    private String getAuthorizationCode() throws Exception {
        String state = UUID.randomUUID().toString().replace("-", "");

        String authUrl = AUTH_URL
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8)
                + "&state=" + state;

        CompletableFuture<String> codeFuture = new CompletableFuture<>(); // hello completeable future

        // tiny http server for you to have
        HttpServer server = HttpServer.create(new InetSocketAddress(9999), 0);
        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String code = extractParam(query, "code");
            String receivedState = extractParam(query, "state");

            String response;
            if (code != null && state.equals(receivedState)) {
                response = "<html><body><h2>Authentication complete. You can close this tab.</h2></body></html>";
                codeFuture.complete(code);
            } else {
                response = "<html><body><h2>Failed to generate authentication token.</h2></body></html>";
                codeFuture.completeExceptionally(new RuntimeException("State mismatch or missing authorization code."));
            }

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            server.stop(1);
        });
        server.start();

        // Open the browser
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(authUrl));
        } else {
            System.out.println("Open this URL in your browser to sign in:\n" + authUrl);
        }

        return codeFuture.get(); // blocks until redirect arrives
    }

    // swaps auth code for microsoft access token

    private String exchangeCodeForMSToken(String code) throws Exception {
        String body = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code"
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8);

        JSONObject json = postForm(TOKEN_URL, body);
        return json.getString("access_token");
    }

    // now swaps that for an Xbox Live token which they abbreviate as XBL

    private XboxTokenResult getXboxLiveToken(String msAccessToken) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("Properties", new JSONObject(Map.of(
                "AuthMethod", "RPS",
                "SiteName", "user.auth.xboxlive.com",
                "RpsTicket", "d=" + msAccessToken
        )));
        payload.put("RelyingParty", "http://auth.xboxlive.com");
        payload.put("TokenType", "JWT");

        JSONObject resp = postJson(XBL_AUTH_URL, payload);
        String token = resp.getString("Token");
        String userHash = resp.getJSONObject("DisplayClaims")
                .getJSONArray("xui")
                .getJSONObject(0)
                .getString("uhs");
        return new XboxTokenResult(token, userHash);
    }

    // now swap THAT for an XSTS token (minecraft relying party) last one I promise!!

    private XboxTokenResult getXSTSToken(String xblToken) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("Properties", new JSONObject()
                .put("SandboxId", "RETAIL")
                .put("UserTokens", new JSONArray().put(xblToken)));
        payload.put("RelyingParty", "rp://api.minecraftservices.com/");
        payload.put("TokenType", "JWT");

        JSONObject resp = postJson(XSTS_AUTH_URL, payload); // sends the XSTS request then parses the response into a JSON object, cause XSTS returns a JSON body even when it fails which is super nice

        // "y'all got any XSTS error codes"
        // "we got three"
        // "damn"
        // XSTS has a lot of error codes and I am not implementing allat
        if (resp.has("XErr")) {
            long xErr = resp.getLong("XErr");
            throw new RuntimeException(xstsErrorMessage(xErr));
        }

        String token = resp.getString("Token");
        String userHash = resp.getJSONObject("DisplayClaims")
                .getJSONArray("xui")
                .getJSONObject(0)
                .getString("uhs");
        return new XboxTokenResult(token, userHash);
    }

    // ok I lied now we log into Minecraft services

    private String getMinecraftToken(String xstsToken, String userHash) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("identityToken", "XBL3.0 x=" + userHash + ";" + xstsToken);

        JSONObject resp = postJson(MC_LOGIN_URL, payload);
        return resp.getString("access_token");
    }

    // someone go get me the Minecraft player profile

    private PlayerProfile getPlayerProfile(String mcAccessToken) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(MC_PROFILE_URL))
                .header("Authorization", "Bearer " + mcAccessToken)
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Profile fetch failed (" + resp.statusCode() + "): " + resp.body());
        }

        JSONObject json = new JSONObject(resp.body());
        String id = json.getString("id");
        String name = json.getString("name");
        String skinUrl = extractSkinUrl(json);

        // Format UUID with dashes: 8-4-4-4-12
        String uuid = id.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5");

        return new PlayerProfile(uuid, name, mcAccessToken, skinUrl);
    }

    // yaaay helpers (which I did not write)

    /** POST application/x-www-form-urlencoded and return the JSON response. */
    private JSONObject postForm(String url, String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("POST failed (" + resp.statusCode() + "): " + resp.body());
        }
        return new JSONObject(resp.body());
    }

    /** POST application/json and return the JSON response. */
    private JSONObject postJson(String url, JSONObject payload) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        // XSTS returns 401 with an XErr body; parse it rather than throwing immediately
        if (resp.statusCode() != 200 && resp.statusCode() != 401) {
            throw new RuntimeException("POST failed (" + resp.statusCode() + "): " + resp.body());
        }
        return new JSONObject(resp.body());
    }

    /** Pull a single query-string parameter out of a raw query string. */
    private static String extractParam(String query, String name) {
        if (query == null) {
            return null;
        }
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    /** Pull the active skin URL out of the profile skins array. */
    private static String extractSkinUrl(JSONObject profile) {
        try {
            JSONArray skins = profile.getJSONArray("skins");
            for (int i = 0; i < skins.length(); i++) {
                JSONObject skin = skins.getJSONObject(i);
                if ("ACTIVE".equals(skin.optString("state"))) {
                    return skin.getString("url");
                }
            }
        } catch (Exception ignored) {
            // no-op: skin URL is optional in the final profile
        }
        return null;
    }

    /** Human-readable message for common XSTS error codes. */
    private static String xstsErrorMessage(long xErr) {
        return switch ((int) xErr) {
            case 0x8015DC0B ->  // error 2148916235
                    "Account is not associated with an Xbox account. " +
                    "The user needs to create one at xbox.com.";
            case 0x8015DC16 ->  // error 2148916246
                    "Xbox Live is not available in this region.";
            case 0x8015DC17 ->  // error 2148916247
                    "This account is a child account. Parental consent is required.";
            default ->
                    "Xbox XSTS error: " + xErr;
        };
    }

    // ── Records ────────────────────────────────────────────────────────────────

    /** Intermediate Xbox token + associated user hash. */
    public record XboxTokenResult(String token, String userHash) {}

    /**
     * The final result — everything your launcher needs to launch the game.
     *
     * @param uuid         Player UUID (formatted with dashes)
     * @param username     In-game name (e.g. "Steve")
     * @param accessToken  Minecraft access token — pass to the game as --accessToken
     * @param skinUrl      Active skin texture URL (may be null)
     */
    public record PlayerProfile(
            String uuid,
            String username,
            String accessToken,
            String skinUrl
    ) {
        /** Build the standard Minecraft launch arguments. */
        public String[] toLaunchArgs() {
            return new String[]{
                    "--username", username,
                    "--uuid", uuid,
                    "--accessToken", accessToken,
                    "--userType", "msa"
            };
        }

        @Override
        public String toString() {
            return "PlayerProfile{username='" + username + "', uuid='" + uuid + "'}";
        }
    }

    // ── Quick test ─────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        // Replace with your Azure Application (client) ID
        String clientId = "YOUR_AZURE_CLIENT_ID";

        AuthRook auth = new AuthRook(clientId);
        PlayerProfile profile = auth.authenticate();

        System.out.println("Logged in as: " + profile.username());
        System.out.println("UUID:         " + profile.uuid());
        System.out.println("Skin URL:     " + profile.skinUrl());
        System.out.println("Launch args:  " + String.join(" ", profile.toLaunchArgs()));
    }

}