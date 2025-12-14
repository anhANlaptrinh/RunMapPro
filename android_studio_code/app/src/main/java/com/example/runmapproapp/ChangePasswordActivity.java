package com.example.runmapproapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.UserApi;
import com.example.runmapproapp.data.model.ChangePasswordRequest;
import com.example.runmapproapp.data.model.ErrorResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText inputCurrentPassword;
    private TextInputEditText inputNewPassword;
    private TextInputEditText inputConfirmPassword;
    private TextView textFeedback;
    private ProgressBar progressBar;
    private MaterialButton buttonSubmit;

    private AuthManager authManager;
    private UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        authManager = new AuthManager(this);
        if (!authManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        userApi = ApiClient.getUserApi();
        initializeViews();
        setupToolbar();
        setupListeners();
    }

    private void initializeViews() {
        inputCurrentPassword = findViewById(R.id.inputCurrentPassword);
        inputNewPassword = findViewById(R.id.inputNewPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        textFeedback = findViewById(R.id.textFeedback);
        progressBar = findViewById(R.id.progressBar);
        buttonSubmit = findViewById(R.id.buttonSubmit);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        buttonSubmit.setOnClickListener(v -> submitChange());
    }

    private void submitChange() {
        hideFeedback();
        String current = getTextValue(inputCurrentPassword);
        String next = getTextValue(inputNewPassword);
        String confirm = getTextValue(inputConfirmPassword);

        if (current.isEmpty() || next.isEmpty() || confirm.isEmpty()) {
            showFeedback(getString(R.string.error_empty_password), true);
            return;
        }

        if (next.length() < 6) {
            showFeedback(getString(R.string.error_invalid_password), true);
            return;
        }

        if (!TextUtils.equals(next, confirm)) {
            showFeedback(getString(R.string.error_password_mismatch), true);
            return;
        }

        String header = buildAuthHeader();
        if (header == null) {
            redirectToLogin();
            return;
        }

        setLoading(true);
        ChangePasswordRequest request = new ChangePasswordRequest(current, next);
        userApi.changePassword(header, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    clearFields();
                    showFeedback(getString(R.string.password_update_success), false);
                } else {
                    handleError(response);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setLoading(false);
                showFeedback(getString(R.string.error_network), true);
            }
        });
    }

    private void handleError(Response<?> response) {
        if (response.code() == 401) {
            redirectToLogin();
            return;
        }
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                ErrorResponse error = new Gson().fromJson(raw, ErrorResponse.class);
                if (error != null && error.getMessage() != null) {
                    showFeedback(error.getMessage(), true);
                    return;
                }
            }
        } catch (IOException ignored) {
        }
        showFeedback(getString(R.string.error_unexpected), true);
    }

    private void clearFields() {
        inputCurrentPassword.setText("");
        inputNewPassword.setText("");
        inputConfirmPassword.setText("");
    }

    private void hideFeedback() {
        textFeedback.setVisibility(View.GONE);
    }

    private void showFeedback(String message, boolean isError) {
        int color = ContextCompat.getColor(this,
                isError ? android.R.color.holo_red_dark : android.R.color.holo_green_dark);
        textFeedback.setTextColor(color);
        textFeedback.setText(message);
        textFeedback.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonSubmit.setEnabled(!loading);
    }

    private String getTextValue(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private String buildAuthHeader() {
        String token = authManager.getToken();
        return token == null ? null : "Bearer " + token;
    }

    private void redirectToLogin() {
        authManager.logout();
        Toast.makeText(this, R.string.error_unexpected, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
