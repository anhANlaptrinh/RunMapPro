package com.example.runmapproapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.AuthApi;
import com.example.runmapproapp.data.model.ErrorResponse;
import com.example.runmapproapp.data.model.LoginRequest;
import com.example.runmapproapp.data.model.LoginResponse;
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

    private AuthApi authApi;
    private AuthManager authManager;

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
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        });
        textForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
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
                    android.util.Log.d("LoginActivity", "Login response: token=" + loginResponse.getAccessToken() + 
                        ", user=" + (loginResponse.getUser() != null ? loginResponse.getUser().getEmail() : "null"));
                    authManager.saveLogin(loginResponse);
                    android.util.Log.d("LoginActivity", "Token saved: " + authManager.getToken());
                    android.util.Log.d("LoginActivity", "isLoggedIn: " + authManager.isLoggedIn());
                    Toast.makeText(LoginActivity.this, R.string.msg_login_success, Toast.LENGTH_SHORT).show();
                    openMainAndFinish();
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
                return error.getMessage();
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
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}