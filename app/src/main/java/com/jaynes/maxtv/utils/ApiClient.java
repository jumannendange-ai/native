package com.jaynes.maxtv.utils;

import android.util.Log;
import com.jaynes.maxtv.JaynesApp;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static OkHttpClient client;

    public interface Callback {
        void onSuccess(JSONObject response);
        void onError(String message);
    }

    private static OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request req = chain.request().newBuilder()
                        .addHeader("X-App-Version", "1.0.0")
                        .addHeader("Content-Type", "application/json")
                        .build();
                    return chain.proceed(req);
                })
                .build();
        }
        return client;
    }

    // ── GET request ──────────────────────────────────────
    public static void get(String endpoint, Callback callback) {
        String url = JaynesApp.API_BASE + endpoint;
        Request request = new Request.Builder().url(url).get().build();
        execute(request, callback);
    }

    // ── POST request ─────────────────────────────────────
    public static void post(String endpoint, JSONObject body, Callback callback) {
        String url = JaynesApp.API_BASE + endpoint;
        RequestBody rb = RequestBody.create(
            body.toString(), MediaType.parse("application/json")
        );
        Request request = new Request.Builder().url(url).post(rb).build();
        execute(request, callback);
    }

    // ── Execute ──────────────────────────────────────────
    private static void execute(Request request, Callback callback) {
        getClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Request failed: " + e.getMessage());
                callback.onError("Tatizo la mtandao: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "{}";
                try {
                    JSONObject json = new JSONObject(body);
                    if (response.isSuccessful()) {
                        callback.onSuccess(json);
                    } else {
                        String msg = json.optString("message", "Kosa la server");
                        callback.onError(msg);
                    }
                } catch (Exception e) {
                    callback.onError("Jibu batili kutoka server");
                }
            }
        });
    }

    // ── Auth via API (login / register / reset_password) ─
    public static void authPost(String action, JSONObject body, Callback callback) {
        post("/api/auth?action=" + action, body, callback);
    }

    // ── App Init (remote config + update check) ──────────
    public static void appInit(String version, Callback callback) {
        get("/api/app/init?version=" + version, callback);
    }

    // ── Channels ─────────────────────────────────────────
    public static void getChannels(Callback callback) {
        get("/api/channels/all", callback);
    }

    public static void getLocalChannels(Callback callback) {
        get("/api/channels/local", callback);
    }

    // ── Health check ─────────────────────────────────────
    public static void healthCheck(Callback callback) {
        get("/api/health", callback);
    }
}
