package com.jaynes.maxtv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.jaynes.maxtv.R;
import com.jaynes.maxtv.utils.ApiClient;
import com.jaynes.maxtv.utils.SessionManager;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etName;
    private Button btnAuth;
    private TextView tvToggle, tvMode, tvForgot;
    private LinearLayout nameLayout;
    private ProgressBar progressBar;
    private TextView tvError;
    private ImageButton btnEye;

    private boolean isRegister = false;
    private boolean passwordVisible = false;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = SessionManager.getInstance(this);

        // Views
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        etName      = findViewById(R.id.etName);
        btnAuth     = findViewById(R.id.btnAuth);
        tvToggle    = findViewById(R.id.tvToggle);
        tvMode      = findViewById(R.id.tvMode);
        tvForgot    = findViewById(R.id.tvForgot);
        nameLayout  = findViewById(R.id.nameLayout);
        progressBar = findViewById(R.id.progressBar);
        tvError     = findViewById(R.id.tvError);
        btnEye      = findViewById(R.id.btnEye);

        btnAuth.setOnClickListener(v -> doAuth());
        tvToggle.setOnClickListener(v -> toggleMode());
        tvForgot.setOnClickListener(v -> showForgotDialog());
        btnEye.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void toggleMode() {
        isRegister = !isRegister;
        nameLayout.setVisibility(isRegister ? View.VISIBLE : View.GONE);
        tvForgot.setVisibility(isRegister ? View.GONE : View.VISIBLE);
        btnAuth.setText(isRegister ? "JIANDIKISHE" : "INGIA");
        tvMode.setText(isRegister ? "JIANDIKISHE" : "INGIA");
        tvToggle.setText(isRegister ? "Tayari una akaunti? Ingia" : "Huna akaunti? Jiandikishe");
        hideError();
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnEye.setImageResource(android.R.drawable.ic_menu_view);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        etPassword.setSelection(etPassword.length());
    }

    private void doAuth() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String name     = etName.getText().toString().trim();

        // Validation
        if (email.isEmpty())         { showError("Weka email yako!"); return; }
        if (!email.contains("@"))    { showError("Email si sahihi!"); return; }
        if (password.isEmpty())      { showError("Weka nywila yako!"); return; }
        if (password.length() < 6)   { showError("Nywila lazima iwe herufi 6+!"); return; }
        if (isRegister && name.isEmpty()) { showError("Weka jina lako kamili!"); return; }

        setLoading(true);
        hideError();

        String action = isRegister ? "register" : "login";

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);
            if (isRegister) body.put("name", name);

            // Tumia auth.php via web server
            authViaWeb(action, body);

        } catch (Exception e) {
            showError("Kosa la ndani. Jaribu tena.");
            setLoading(false);
        }
    }

    private void authViaWeb(String action, JSONObject body) {
        // Call auth.php via web
        String url = "https://dde.ct.ws/auth.php?action=" + action;
        okhttp3.RequestBody rb = okhttp3.RequestBody.create(
            body.toString(), okhttp3.MediaType.parse("application/json")
        );
        okhttp3.Request request = new okhttp3.Request.Builder()
            .url(url)
            .post(rb)
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "JaynesMaxTV-Android/1.0")
            .build();

        new okhttp3.OkHttpClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    showError("Tatizo la mtandao. Angalia internet yako.");
                    setLoading(false);
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String bodyStr = response.body() != null ? response.body().string() : "{}";
                runOnUiThread(() -> {
                    setLoading(false);
                    try {
                        JSONObject data = new JSONObject(bodyStr);
                        if (data.optBoolean("success", false)) {
                            // Save session
                            JSONObject user = data.optJSONObject("user");
                            String token   = data.optString("token", "");
                            String refresh = data.optString("refresh_token", "");

                            if (user != null) {
                                session.saveSession(
                                    token, refresh,
                                    user.optString("email"),
                                    user.optString("name"),
                                    user.optString("id"),
                                    user.optString("plan", "trial"),
                                    user.optString("trial_end", ""),
                                    user.optString("sub_end", ""),
                                    user.optString("created_at", "")
                                );
                            }

                            // Nenda MainActivity
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finishAffinity();

                        } else {
                            showError(data.optString("error", "Imeshindwa. Jaribu tena."));
                        }
                    } catch (Exception e) {
                        showError("Jibu batili. Jaribu tena.");
                    }
                });
            }
        });
    }

    private void showForgotDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Badilisha Nywila");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("Barua pepe yako");
        int p = (int)(16 * getResources().getDisplayMetrics().density);
        builder.setView(input);
        builder.setPositiveButton("TUMA", (d, w) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty()) return;
            resetPassword(email);
        });
        builder.setNegativeButton("Ghairi", null);
        builder.show();
    }

    private void resetPassword(String email) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            okhttp3.RequestBody rb = okhttp3.RequestBody.create(
                body.toString(), okhttp3.MediaType.parse("application/json")
            );
            okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://dde.ct.ws/auth.php?action=reset_password")
                .post(rb)
                .build();
            new okhttp3.OkHttpClient().newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call c, java.io.IOException e) {}
                @Override
                public void onResponse(okhttp3.Call c, okhttp3.Response r) {
                    runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this,
                            "Barua ya reset imetumwa kwa " + email,
                            Toast.LENGTH_LONG).show()
                    );
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnAuth.setEnabled(!loading);
        btnAuth.setText(loading ? "SUBIRI..." : (isRegister ? "JIANDIKISHE" : "INGIA"));
    }
}
