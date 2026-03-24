package com.jaynes.maxtv;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ApiClient — HTTP calls kwa JAYNES MAX TV API server
 * Base URL: https://jaynes-max-tv-api.onrender.com
 *
 * Endpoints zinazotumika:
 *   GET  /api/app/init
 *   GET  /api/channels/all
 *   GET  /api/update/status
 *   POST /api/auth   { action, email, password, token }
 */
public class ApiClient {

    private static final String TAG     = "ApiClient";
    public  static final String BASE_URL = "https://jaynes-max-tv-api.onrender.com";
    private static final int    TIMEOUT  = 20_000; // 20s

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    // ── Callback interface ───────────────────────────────
    public interface Callback {
        void onSuccess(JSONObject response);
        void onError(String message, int httpCode);
    }

    // ══════════════════════════════════════════════════════
    //  PUBLIC API METHODS
    // ══════════════════════════════════════════════════════

    /** GET /api/app/init?version=X */
    public static void appInit(String appVersion, Callback cb) {
        get("/api/app/init?version=" + appVersion, null, cb);
    }

    /** GET /api/channels/all */
    public static void getAllChannels(String token, Callback cb) {
        get("/api/channels/all", token, cb);
    }

    /** GET /api/update/status?version=X */
    public static void checkUpdate(String appVersion, Callback cb) {
        get("/api/update/status?version=" + appVersion, null, cb);
    }

    /** POST /api/auth { action:"login", email, password } */
    public static void login(String email, String password, Callback cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("action",   "login");
            body.put("email",    email);
            body.put("password", password);
        } catch (Exception e) { /* won't happen */ }
        post("/api/auth", body, null, cb);
    }

    /** POST /api/auth { action:"register", email, password } */
    public static void register(String email, String password, Callback cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("action",   "register");
            body.put("email",    email);
            body.put("password", password);
        } catch (Exception e) { /* won't happen */ }
        post("/api/auth", body, null, cb);
    }

    /** POST /api/auth { action:"me", token } */
    public static void getMe(String token, Callback cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("action", "me");
            body.put("token",  token);
        } catch (Exception e) { /* won't happen */ }
        post("/api/auth", body, token, cb);
    }

    /** POST /api/auth { action:"logout", token } */
    public static void logout(String token, Callback cb) {
        JSONObject body = new JSONObject();
        try {
            body.put("action", "logout");
            body.put("token",  token);
        } catch (Exception e) { /* won't happen */ }
        post("/api/auth", body, token, cb);
    }

    // ══════════════════════════════════════════════════════
    //  PRIVATE HTTP HELPERS
    // ══════════════════════════════════════════════════════

    private static void get(String path, String token, Callback cb) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(BASE_URL + path);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT);
                conn.setReadTimeout(TIMEOUT);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept",       "application/json");
                conn.setRequestProperty("X-App-Version", "1.0.0");
                if (token != null && !token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                handleResponse(conn, cb);

            } catch (Exception e) {
                Log.e(TAG, "GET " + path + " failed: " + e.getMessage());
                cb.onError("Hitilafu ya mtandao: " + e.getMessage(), -1);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private static void post(String path, JSONObject body, String token, Callback cb) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(BASE_URL + path);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(TIMEOUT);
                conn.setReadTimeout(TIMEOUT);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setRequestProperty("Accept",       "application/json");
                conn.setRequestProperty("X-App-Version", "1.0.0");
                if (token != null && !token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                byte[] bodyBytes = body.toString().getBytes(StandardCharsets.UTF_8);
                conn.setFixedLengthStreamingMode(bodyBytes.length);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bodyBytes);
                }

                handleResponse(conn, cb);

            } catch (Exception e) {
                Log.e(TAG, "POST " + path + " failed: " + e.getMessage());
                cb.onError("Hitilafu ya mtandao: " + e.getMessage(), -1);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private static void handleResponse(HttpURLConnection conn, Callback cb) throws Exception {
        int code = conn.getResponseCode();

        // Read body — success or error stream
        java.io.InputStream stream = code >= 400
                ? conn.getErrorStream()
                : conn.getInputStream();

        StringBuilder sb = new StringBuilder();
        if (stream != null) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
        }

        String raw = sb.toString().trim();
        Log.d(TAG, "Response [" + code + "]: " + raw.substring(0, Math.min(raw.length(), 200)));

        if (raw.isEmpty()) {
            cb.onError("Jibu tupu kutoka seva (" + code + ")", code);
            return;
        }

        JSONObject json = new JSONObject(raw);
        boolean success = json.optBoolean("success", false);

        if (success) {
            cb.onSuccess(json);
        } else {
            String msg = json.optString("message", "Hitilafu isiyojulikana");
            cb.onError(msg, code);
        }
    }
}
