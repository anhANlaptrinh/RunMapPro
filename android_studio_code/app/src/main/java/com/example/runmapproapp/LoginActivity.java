package com.example.runmapproapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.AuthApi;
import com.example.runmapproapp.data.model.ErrorResponse;
import com.example.runmapproapp.data.model.LoginRequest;
import com.example.runmapproapp.data.model.LoginResponse;
import com.example.runmapproapp.utils.LocaleHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout inputLayoutEmail;
    private TextInputLayout inputLayoutPassword;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private MaterialButton buttonLogin;
    private TextView textSignup;
    private TextView textForgotPassword;
    private TextView textError;
    private ProgressBar progressBar;
    private ImageButton buttonLanguage;

    private AuthApi authApi;
    private AuthManager authManager;
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authApi = ApiClient.getAuthApi();
        authManager = new AuthManager(this);
        if (authManager.isLoggedIn()) {
            openMainAndFinish();
            return;
        }

        bindViews();
        setupToolbar();
        setupListeners();
    }

    private void bindViews() {
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textSignup = findViewById(R.id.textSignup);
        textForgotPassword = findViewById(R.id.textForgotPassword);
        textError = findViewById(R.id.textError);
        progressBar = findViewById(R.id.progressBar);
        buttonLanguage = findViewById(R.id.buttonLanguage);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.login_title);
        }
    }

    private void setupListeners() {
        buttonLogin.setOnClickListener(v -> attemptLogin());
        textSignup.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });
        textForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
        buttonLanguage.setOnClickListener(v -> showLanguageDialog());
    }
    
    private void showLanguageDialog() {
        String currentLang = LocaleHelper.getLanguage(this);
        String[] languages = {
            getString(R.string.language_vietnamese),
            getString(R.string.language_english),
            getString(R.string.language_chinese)
        };
        String[] langCodes = {"vi", "en", "zh"};
        
        int selectedIndex = 0;
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLang)) {
                selectedIndex = i;
                break;
            }
        }
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_language)
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String selectedLang = langCodes[which];
                    if (!selectedLang.equals(currentLang)) {
                        LocaleHelper.setLocale(this, selectedLang);
                        // Restart LoginActivity with new language
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void attemptLogin() {
        clearErrors();
        String email = getText(inputEmail);
        String password = getText(inputPassword);

        boolean valid = true;
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputLayoutEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            inputLayoutPassword.setError(getString(R.string.error_empty_password));
            valid = false;
        }
        if (!valid) {
            return;
        }

        toggleLoading(true);
        authApi.login(new LoginRequest(email, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                toggleLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    android.util.Log.d("LoginActivity", "========== LOGIN SUCCESS ==========");
                    android.util.Log.d("LoginActivity", "Token received: " + loginResponse.getAccessToken());
                    android.util.Log.d("LoginActivity", "User email: " + (loginResponse.getUser() != null ? loginResponse.getUser().getEmail() : "null"));
                    android.util.Log.d("LoginActivity", "User ID: " + (loginResponse.getUser() != null ? loginResponse.getUser().getId() : "null"));
                    
                    // Save token temporarily so we can fetch fresh profile
                    authManager.saveToken(loginResponse.getAccessToken());

                    // Fetch authoritative profile from server to check banned status
                    ApiClient.getUserApi().getProfile("Bearer " + loginResponse.getAccessToken()).enqueue(new Callback<com.example.runmapproapp.data.model.UserProfileResponse>() {
                        @Override
                        public void onResponse(Call<com.example.runmapproapp.data.model.UserProfileResponse> call, Response<com.example.runmapproapp.data.model.UserProfileResponse> resp) {
                            if (resp.isSuccessful() && resp.body() != null) {
                                // Log full profile JSON for debugging presence of `banned` field
                                try {
                                    String profileJson = new Gson().toJson(resp.body());
                                    android.util.Log.d("LoginActivity", "Profile JSON: " + profileJson);
                                } catch (Exception ignored) {}
                                Boolean bannedFlag = resp.body().getBanned();
                                if (bannedFlag != null && bannedFlag) {
                                    // Clear token and block login
                                    authManager.logout();
                                    android.util.Log.d("LoginActivity", "Login attempt blocked after profile check: user is banned");
                                    showError(getString(R.string.error_account_banned));
                                    return;
                                }

                                // Not banned — finalize login
                                authManager.saveLogin(loginResponse);
                                android.util.Log.d("LoginActivity", "Token saved successfully: " + (authManager.getToken() != null && !authManager.getToken().isEmpty()));
                                android.util.Log.d("LoginActivity", "UserId saved: " + authManager.getUserId());
                                android.util.Log.d("LoginActivity", "isLoggedIn: " + authManager.isLoggedIn());
                                if (loginResponse.getUser() != null) {
                                    android.util.Log.d("LoginActivity", "User role: " + loginResponse.getUser().getRole());
                                    android.util.Log.d("LoginActivity", "isAdmin: " + loginResponse.getUser().isAdmin());
                                }
                                // Continue navigation
                                Toast.makeText(LoginActivity.this, R.string.msg_login_success, Toast.LENGTH_SHORT).show();
                                if (loginResponse.getUser() != null && loginResponse.getUser().isAdmin()) {
                                    openAdminDashboardAndFinish();
                                } else {
                                    openMainAndFinish();
                                }
                            } else {
                                // Couldn't fetch profile — be conservative: clear saved token and show error
                                authManager.logout();
                                showError(getString(R.string.error_unexpected));
                            }
                        }

                        @Override
                        public void onFailure(Call<com.example.runmapproapp.data.model.UserProfileResponse> call, Throwable t) {
                            // Network failure — clear token and show network error
                            authManager.logout();
                            showError(getString(R.string.error_network));
                        }
                    });
                    android.util.Log.d("LoginActivity", "====================================");
                } else {
                    showError(parseErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                toggleLoading(false);
                showError(getString(R.string.error_network));
            }
        });
    }

    private void clearErrors() {
        inputLayoutEmail.setError(null);
        inputLayoutPassword.setError(null);
        textError.setVisibility(View.GONE);
        textError.setText(null);
    }

    private void toggleLoading(boolean loading) {
        inputEmail.setEnabled(!loading);
        inputPassword.setEnabled(!loading);
        buttonLogin.setEnabled(!loading);
        textSignup.setEnabled(!loading);
        textForgotPassword.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        textError.setText(message);
        textError.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String parseErrorMessage(Response<?> response) {
        if (response == null || response.errorBody() == null) {
            return getString(R.string.error_unexpected);
        }
        try {
            ErrorResponse error = new Gson().fromJson(response.errorBody().charStream(), ErrorResponse.class);
            if (error != null && !TextUtils.isEmpty(error.getMessage())) {
                String message = error.getMessage();
                // Map common server error messages to localized strings
                if (message.equalsIgnoreCase("Invalid credentials") || 
                    message.equalsIgnoreCase("Invalid email or password")) {
                    return getString(R.string.invalid_credentials);
                }
                return message;
            }
        } catch (Exception ignored) {
        }
        return getString(R.string.error_unexpected);
    }

    private String getText(TextInputEditText editText) {
        CharSequence text = editText.getText();
        return text != null ? text.toString().trim() : "";
    }

    private void openMainAndFinish() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void openAdminDashboardAndFinish() {
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        startActivity(intent);
        finish();
    }
}