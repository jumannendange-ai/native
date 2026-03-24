package com.jaynes.maxtv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.jaynes.maxtv.R;
import com.jaynes.maxtv.utils.ApiClient;
import com.jaynes.maxtv.utils.SessionManager;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_splash);

        session = SessionManager.getInstance(this);

        // App init — chukua remote config na update status
        ApiClient.appInit("1.0.0", new ApiClient.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> checkUpdateAndNavigate(response));
            }
            @Override
            public void onError(String message) {
                // API haifiki — nenda moja kwa moja
                runOnUiThread(() -> navigateAfterDelay(1500));
            }
        });
    }

    private void checkUpdateAndNavigate(JSONObject response) {
        try {
            JSONObject updateStatus = response.optJSONObject("update_status");
            if (updateStatus != null) {
                boolean isForce = updateStatus.optBoolean("is_force", false);
                boolean hasUpdate = updateStatus.optBoolean("has_update", false);
                String downloadUrl = updateStatus.optString("download_url", "");
                String title = updateStatus.optString("title", "Update Ipo!");
                String message = updateStatus.optString("message", "Toleo jipya lipo.");

                if (isForce) {
                    showForceUpdate(title, message, downloadUrl);
                    return;
                }
            }

            // Angalia maintenance
            JSONObject config = response.optJSONObject("config");
            if (config != null) {
                JSONObject maintenance = config.optJSONObject("maintenance");
                if (maintenance != null && maintenance.optBoolean("enabled", false)) {
                    showMaintenance(
                        maintenance.optString("title", "Matengenezo"),
                        maintenance.optString("message", "Tafadhali rudi baadaye.")
                    );
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        navigateAfterDelay(1200);
    }

    private void showForceUpdate(String title, String message, String url) {
        new android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("PAKUA SASA", (d, w) -> {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(url));
                startActivity(intent);
                finish();
            })
            .show();
    }

    private void showMaintenance(String title, String message) {
        new android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Sawa", (d, w) -> finish())
            .show();
    }

    private void navigateAfterDelay(long delay) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (session.isLoggedIn()) {
                intent = new Intent(this, MainActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }, delay);
    }
}
