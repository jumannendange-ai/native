package com.jaynes.maxtv;

import android.content.Intent;
import android.os.Bundle;
import com.jaynes.maxtv.BuildConfig;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

/**
 * SplashActivity — App init + update check
 * 1. Inatuma GET /api/app/init
 * 2. Inaangalia update status
 * 3. Inaelekeza LoginActivity au MainActivity
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG        = "SplashActivity";
    private static final int    MIN_SPLASH = 2000; // ms

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean        initDone   = false;
    private boolean        timerDone  = false;
    private String         nextScreen = "login"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvVersion = findViewById(R.id.tv_version);
        if (tvVersion != null) {
            tvVersion.setText("v" + BuildConfig.VERSION_NAME);
        }

        // Min splash delay
        mainHandler.postDelayed(() -> {
            timerDone = true;
            tryProceed();
        }, MIN_SPLASH);

        // API init
        callAppInit();
    }

    private void callAppInit() {
        ApiClient.appInit(BuildConfig.VERSION_NAME, new ApiClient.Callback() {
            @Override public void onSuccess(JSONObject response) {
                Log.d(TAG, "App init OK");

                // Angalia update
                JSONObject updateStatus = response.optJSONObject("update_status");
                if (updateStatus != null) {
                    boolean isForce   = updateStatus.optBoolean("is_force", false);
                    boolean hasUpdate = updateStatus.optBoolean("has_update", false);

                    if (isForce) {
                        Log.d(TAG, "Force update inahitajika!");
                        // TODO: onyesha force update dialog
                    } else if (hasUpdate) {
                        Log.d(TAG, "Update ndogo ipo");
                        // TODO: onyesha minor update dialog
                    }
                }

                // Angalia maintenance
                JSONObject config = response.optJSONObject("config");
                if (config != null) {
                    JSONObject maintenance = config.optJSONObject("maintenance");
                    if (maintenance != null && maintenance.optBoolean("enabled", false)) {
                        // TODO: onyesha maintenance screen
                        Log.d(TAG, "Maintenance mode imewashwa");
                    }
                }

                determineNextScreen();
                mainHandler.post(() -> {
                    initDone = true;
                    tryProceed();
                });
            }

            @Override public void onError(String message, int httpCode) {
                Log.e(TAG, "App init failed: " + message);
                // Endelea hata kama init imeshindwa
                determineNextScreen();
                mainHandler.post(() -> {
                    initDone = true;
                    tryProceed();
                });
            }
        });
    }

    private void determineNextScreen() {
        SessionManager session = new SessionManager(this);
        nextScreen = session.isLoggedIn() ? "main" : "login";
    }

    private void tryProceed() {
        if (!initDone || !timerDone) return;

        Class<?> target = "main".equals(nextScreen)
                ? MainActivity.class
                : LoginActivity.class;

        startActivity(new Intent(this, target));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
