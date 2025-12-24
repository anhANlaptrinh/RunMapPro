package com.example.runmapproapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.AuthApi;
import com.example.runmapproapp.data.VerifyOtpRequest;
import com.example.runmapproapp.data.VerifyOtpResponse;
import com.example.runmapproapp.data.model.ErrorResponse;
import com.example.runmapproapp.data.model.ForgotPasswordRequest;
import com.example.runmapproapp.data.model.ForgotPasswordResponse;
import com.example.runmapproapp.utils.LocaleHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private static final String TAG = "ForgotPasswordActivity";
    private static final long OTP_EXPIRY_MILLISECONDS = 5 * 60 * 1000; // 5 minutes

    private TextInputEditText inputEmail;
    private TextInputEditText inputOtp;
    private TextInputEditText inputNewPassword;
    private TextInputEditText inputConfirmPassword;
    private MaterialButton buttonSendOtp;
    private MaterialButton buttonVerifyAndReset;
    private TextView textTimer;
    private TextView textError;
    private ProgressBar progressBar;

    private AuthApi authApi;
    private String currentEmail;
    private CountDownTimer otpTimer;
    private boolean isTimerRunning = false;
    private String defaultSendButtonText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        authApi = ApiClient.getAuthApi();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        inputEmail = findViewById(R.id.inputEmail);
        inputOtp = findViewById(R.id.inputOtp);
        inputNewPassword = findViewById(R.id.inputNewPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        buttonSendOtp = findViewById(R.id.buttonSendOtp);
        buttonVerifyAndReset = findViewById(R.id.buttonVerifyAndReset);
        textTimer = findViewById(R.id.textTimer);
        textError = findViewById(R.id.textError);
        progressBar = findViewById(R.id.progressBar);

        CharSequence existingLabel = buttonSendOtp.getText();
        defaultSendButtonText = existingLabel != null ? existingLabel.toString() : "Send code";

        TextView textLogin = findViewById(R.id.textLogin);
        textLogin.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        buttonSendOtp.setOnClickListener(v -> sendOtp());
        buttonVerifyAndReset.setOnClickListener(v -> verifyOtpAndResetPassword());
    }

    private void sendOtp() {
        String email = inputEmail.getText() != null ? inputEmail.getText().toString().trim() : "";

        if (email.isEmpty()) {
            showError(getString(R.string.please_enter_email));
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.please_enter_valid_email));
            return;
        }

        currentEmail = email;
        hideError();
        setLoading(true);
        setSendButtonEnabled(false);

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        authApi.sendOtp(request).enqueue(new Callback<ForgotPasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ForgotPasswordActivity.this, getString(R.string.otp_sent, email), Toast.LENGTH_SHORT).show();
                    startOtpTimer();
                } else {
                    handleError(response);
                    if (!isTimerRunning) {
                        setSendButtonEnabled(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Send OTP failed: " + t.getMessage());
                showError("Network error: " + t.getMessage());
                if (!isTimerRunning) {
                    setSendButtonEnabled(true);
                }
            }
        });
    }

    private void verifyOtpAndResetPassword() {
        if (currentEmail == null) {
            showError(getString(R.string.please_request_otp_first));
            return;
        }

        String otp = inputOtp.getText() != null ? inputOtp.getText().toString().trim() : "";
        String newPassword = inputNewPassword.getText() != null ? inputNewPassword.getText().toString().trim() : "";
        String confirmPassword = inputConfirmPassword.getText() != null ? inputConfirmPassword.getText().toString().trim() : "";

        if (otp.length() != 6) {
            showError(getString(R.string.please_enter_6digit_otp));
            return;
        }

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError(getString(R.string.please_enter_confirm_password));
            return;
        }

        if (newPassword.length() < 6) {
            showError(getString(R.string.password_min_6_chars));
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError(getString(R.string.passwords_not_match));
            return;
        }

        hideError();
        setLoading(true);

        VerifyOtpRequest request = new VerifyOtpRequest(currentEmail, otp, newPassword);
        authApi.verifyOtpAndReset(request).enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, Response<VerifyOtpResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    VerifyOtpResponse result = response.body();
                    if (result.isSuccess()) {
                        Toast.makeText(ForgotPasswordActivity.this, R.string.password_reset_success, Toast.LENGTH_LONG).show();
                        stopOtpTimer();
                        navigateToLogin();
                    } else {
                        showError(result.getMessage());
                    }
                } else {
                    handleError(response);
                }
            }

            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Verify OTP failed: " + t.getMessage());
                showError(getString(R.string.network_error_message, t.getMessage()));
            }
        });
    }

    private void startOtpTimer() {
        stopOtpTimer();
        isTimerRunning = true;
        textTimer.setVisibility(View.VISIBLE);
        updateSendButtonCountdown(OTP_EXPIRY_MILLISECONDS);
        otpTimer = new CountDownTimer(OTP_EXPIRY_MILLISECONDS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                textTimer.setText(String.format(Locale.getDefault(), "OTP valid for %02d:%02d", minutes, seconds));
                updateSendButtonCountdown(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                textTimer.setText(R.string.otp_expired);
                textTimer.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
                setSendButtonEnabled(true);
                buttonSendOtp.setText(defaultSendButtonText);
            }
        }.start();
    }

    private void updateSendButtonCountdown(long millisUntilFinished) {
        long minutes = (millisUntilFinished / 1000) / 60;
        long seconds = (millisUntilFinished / 1000) % 60;
        String label = String.format(Locale.getDefault(), "%s (%02d:%02d)", defaultSendButtonText, minutes, seconds);
        buttonSendOtp.setText(label);
    }

    private void stopOtpTimer() {
        if (otpTimer != null) {
            otpTimer.cancel();
            otpTimer = null;
        }
        isTimerRunning = false;
    }

    private void setSendButtonEnabled(boolean enabled) {
        buttonSendOtp.setEnabled(enabled);
        buttonSendOtp.setAlpha(enabled ? 1f : 0.6f);
        if (enabled) {
            textTimer.setTextColor(getResources().getColor(android.R.color.holo_blue_dark, null));
            buttonSendOtp.setText(defaultSendButtonText);
        }
    }

    private void handleError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                ErrorResponse errorResponse = new Gson().fromJson(errorJson, ErrorResponse.class);
                showError(errorResponse.getMessage());
            } else {
                showError(getString(R.string.unknown_error));
            }
        } catch (IOException e) {
            showError(getString(R.string.error_parsing_response));
        }
    }

    private void showError(String message) {
        textError.setText(message);
        textError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        textError.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonVerifyAndReset.setEnabled(!loading);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopOtpTimer();
    }
}
