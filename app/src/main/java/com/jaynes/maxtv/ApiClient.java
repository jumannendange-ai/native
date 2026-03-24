package com.jaynes.maxtv;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {

    private static final String TAG      = "ApiClient";
    public  static final String BASE_URL = "https://api-jaynestvmax2.onrender.com";
    private static final int    TIMEOUT  = 20_000;

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public interface Callback {
        void onSuccess(JSONObject response);
        void onError(String message, int httpCode);
    }

    public static void getAllChannels(String token, Callback cb) {
        get("/api/channels/all", token, cb);
    }

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
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("X-App-Version", "1.0.0");
                if (token != null && !token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                handleResponse(conn, cb);

            } catch (Exception e) {
                Log.e(TAG, "GET failed: " + e.getMessage());
                cb.onError("Hitilafu ya mtandao: " + e.getMessage(), -1);
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    public static void logout(String token, Callback cb) {}
    public static void login(String email, String password, Callback cb) {}
    public static void register(String email, String password, Callback cb) {}

    private static void handleResponse(HttpURLConnection conn, Callback cb) throws Exception {
        int code = conn.getResponseCode();

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
        Log.d(TAG, "Response [" + code + "]: " + raw.substring(0, Math.min(raw.length(), 300)));

        // Jaribu kama ni JSONObject
        if (raw.startsWith("{")) {
            JSONObject json = new JSONObject(raw);
            boolean success = json.optBoolean("success", true);
            if (success) {
                cb.onSuccess(json);
            } else {
                cb.onError(json.optString("message", "Hitilafu"), code);
            }
            return;
        }

        // Jaribu kama ni JSONArray
        if (raw.startsWith("[")) {
            JSONObject wrapper = new JSONObject();
            wrapper.put("success", true);
            wrapper.put("channels", new JSONArray(raw));
            cb.onSuccess(wrapper);
            return;
        }

        cb.onError("Jibu baya kutoka seva: " + raw.substring(0, Math.min(raw.length(), 100)), code);
    }
}
