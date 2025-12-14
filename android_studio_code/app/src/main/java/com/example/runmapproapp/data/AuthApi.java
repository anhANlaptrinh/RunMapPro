package com.example.runmapproapp.data;

import com.example.runmapproapp.data.model.ForgotPasswordRequest;
import com.example.runmapproapp.data.model.ForgotPasswordResponse;
import com.example.runmapproapp.data.model.LoginRequest;
import com.example.runmapproapp.data.model.LoginResponse;
import com.example.runmapproapp.data.model.RegisterRequest;
import com.example.runmapproapp.data.model.ResetPasswordRequest;
import com.example.runmapproapp.data.model.ResetPasswordResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @POST("/api/auth/register")
    Call<LoginResponse> register(@Body RegisterRequest body);

    @POST("/api/auth/forgot-password")
    Call<ForgotPasswordResponse> forgotPassword(@Body ForgotPasswordRequest body);

    @POST("/api/auth/reset-password")
    Call<ResetPasswordResponse> resetPassword(@Body ResetPasswordRequest body);

    // OTP-based endpoints
    @POST("/api/auth/send-otp")
    Call<ForgotPasswordResponse> sendOtp(@Body ForgotPasswordRequest body);

    @POST("/api/auth/verify-otp-reset")
    Call<VerifyOtpResponse> verifyOtpAndReset(@Body VerifyOtpRequest body);

    @POST("/api/auth/resend-otp")
    Call<ForgotPasswordResponse> resendOtp(@Body ForgotPasswordRequest body);
}
