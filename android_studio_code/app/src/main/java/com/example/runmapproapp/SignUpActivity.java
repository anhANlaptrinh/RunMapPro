package com.example.runmapproapp;

import android.content.Context;
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
import com.example.runmapproapp.data.model.LoginResponse;
import com.example.runmapproapp.data.model.RegisterRequest;
import com.example.runmapproapp.utils.LocaleHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout inputLayoutFullName;
    private TextInputLayout inputLayoutEmail;
    private TextInputLayout inputLayoutPassword;
    private TextInputLayout inputLayoutConfirmPassword;
    private TextInputEditText inputFullName;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private TextInputEditText inputConfirmPassword;
    private MaterialButton buttonSignup;
    private TextView textLogin;
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
    private TextView textError;
    private ProgressBar progressBar;

    private AuthApi authApi;
    private AuthManager authManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        authApi = ApiClient.getAuthApi();
        authManager = new AuthManager(this);

        bindViews();
        setupToolbar();
        setupListeners();
    }

    private void bindViews() {
        inputLayoutFullName = findViewById(R.id.inputLayoutFullName);
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
        inputLayoutConfirmPassword = findViewById(R.id.inputLayoutConfirmPassword);
        inputFullName = findViewById(R.id.inputFullName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        buttonSignup = findViewById(R.id.buttonSignup);
        textLogin = findViewById(R.id.textLogin);
        textError = findViewById(R.id.textError);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.signup_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        buttonSignup.setOnClickListener(v -> attemptSignup());
        textLogin.setOnClickListener(v -> finish());
    }

    private void attemptSignup() {
        clearErrors();
        String fullName = getText(inputFullName);
        String email = getText(inputEmail);
        String password = getText(inputPassword);
        String confirmPassword = getText(inputConfirmPassword);

        boolean valid = true;
        if (TextUtils.isEmpty(fullName)) {
            inputLayoutFullName.setError(getString(R.string.error_empty_full_name));
            valid = false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputLayoutEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            inputLayoutPassword.setError(getString(R.string.error_invalid_password));
            valid = false;
        }
        if (!password.equals(confirmPassword)) {
            inputLayoutConfirmPassword.setError(getString(R.string.error_password_mismatch));
            valid = false;
        }
        if (!valid) {
            return;
        }

        toggleLoading(true);
        // Use email as username for now
        String username = email.split("@")[0];
        RegisterRequest request = new RegisterRequest(username, fullName, email, password);
        authApi.register(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                toggleLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    authManager.saveLogin(response.body());
                    Toast.makeText(SignUpActivity.this, R.string.msg_signup_success, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, MapActivity.class));
                    finishAffinity();
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
        inputLayoutFullName.setError(null);
        inputLayoutEmail.setError(null);
        inputLayoutPassword.setError(null);
        inputLayoutConfirmPassword.setError(null);
        textError.setVisibility(View.GONE);
        textError.setText(null);
    }

    private void toggleLoading(boolean loading) {
        inputFullName.setEnabled(!loading);
        inputEmail.setEnabled(!loading);
        inputPassword.setEnabled(!loading);
        inputConfirmPassword.setEnabled(!loading);
        buttonSignup.setEnabled(!loading);
        textLogin.setEnabled(!loading);
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
}