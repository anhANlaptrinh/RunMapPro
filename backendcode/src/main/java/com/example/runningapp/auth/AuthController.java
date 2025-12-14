package com.example.runningapp.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.runningapp.auth.dto.AuthResponse;
import com.example.runningapp.auth.dto.ForgotPasswordRequest;
import com.example.runningapp.auth.dto.LoginRequest;
import com.example.runningapp.auth.dto.OtpResponse;
import com.example.runningapp.auth.dto.RegisterRequest;
import com.example.runningapp.auth.dto.ResetPasswordRequest;
import com.example.runningapp.auth.dto.SendOtpRequest;
import com.example.runningapp.auth.dto.UserProfileResponse;
import com.example.runningapp.auth.dto.VerifyOtpRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("resetToken", token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("status", "updated"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> currentUser() {
        return ResponseEntity.ok(authService.currentUserProfile());
    }

    // OTP-based password reset endpoints
    @PostMapping("/send-otp")
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        return ResponseEntity.ok(authService.sendOtp(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<OtpResponse> resendOtp(@Valid @RequestBody SendOtpRequest request) {
        return ResponseEntity.ok(authService.resendOtp(request));
    }

    @PostMapping("/verify-otp-reset")
    public ResponseEntity<OtpResponse> verifyOtpAndReset(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtpAndResetPassword(request));
    }
}
