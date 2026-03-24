package com.jaynes.maxtv;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText    etEmail, etPassword;
    private Button      btnLogin;
    private ProgressBar progress;
    private TextView    tvError;

    private SessionManager session;
    private final Handler  mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(this);

        if (session.isLoggedIn() && !session.getToken().isEmpty()) {
            goToMain();
            return;
        }

        etEmail    = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin   = findViewById(R.id.btn_login);
        progress   = findViewById(R.id.progress);
        tvError    = findViewById(R.id.tv_error);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private boolean validate() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Barua pepe si sahihi");
            etEmail.requestFocus();
            return false;
        }
        if (pass.length() < 6) {
            showError("Nywila lazima iwe herufi 6 au zaidi");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void attemptLogin() {
        if (!validate()) return;
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString();

        setLoading(true);

        ApiClient.login(email, pass, new ApiClient.Callback() {
            @Override public void onSuccess(JSONObject response) {
                mainHandler.post(() -> {
                    session.saveSession(response);
                    setLoading(false);
                    goToMain();
                });
            }
            @Override public void onError(String message, int httpCode) {
                mainHandler.post(() -> {
                    setLoading(false);
                    showError(message);
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        if (loading) tvError.setVisibility(View.GONE);
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
