package com.example.runningapp.auth;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.runningapp.auth.dto.AuthResponse;
import com.example.runningapp.auth.dto.ForgotPasswordRequest;
import com.example.runningapp.auth.dto.LoginRequest;
import com.example.runningapp.auth.dto.OtpResponse;
import com.example.runningapp.auth.dto.RegisterRequest;
import com.example.runningapp.auth.dto.ResetPasswordRequest;
import com.example.runningapp.auth.dto.SendOtpRequest;
import com.example.runningapp.auth.dto.UserProfileResponse;
import com.example.runningapp.auth.dto.VerifyOtpRequest;
import com.example.runningapp.common.SecurityUtils;
import com.example.runningapp.common.exception.BadRequestException;
import com.example.runningapp.common.exception.NotFoundException;
import com.example.runningapp.common.exception.UnauthorizedException;
import com.example.runningapp.media.MediaService;
import com.example.runningapp.otp.OtpService;
import com.example.runningapp.security.JwtService;
import com.example.runningapp.user.PasswordResetToken;
import com.example.runningapp.user.PasswordResetTokenRepository;
import com.example.runningapp.user.User;
import com.example.runningapp.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MediaService mediaService;
    private final OtpService otpService;

    public AuthService(UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            MediaService mediaService,
            OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mediaService = mediaService;
        this.otpService = otpService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already in use");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already in use");
        }
        Instant now = Instant.now();
        User user = User.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .username(request.username())
                .fullName(request.fullName())
                .avatarMediaId(request.avatarMediaId())
                .role("user")
                .createdAt(now)
                .updatedAt(now)
                .build();
        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return buildAuthResponse(user);
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        return userRepository.findByEmail(request.email().toLowerCase())
                .map(user -> {
                    String token = UUID.randomUUID().toString();
                    PasswordResetToken resetToken = PasswordResetToken.builder()
                            .userId(user.getId())
                            .token(token)
                            .expiresAt(Instant.now().plusSeconds(30 * 60))
                            .used(false)
                            .createdAt(Instant.now())
                            .build();
                    passwordResetTokenRepository.save(resetToken);
                    return token;
                })
                .orElse(null);
    }

    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new NotFoundException("Invalid reset token"));
        if (token.isUsed() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Token has expired or already used");
        }
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);
    }

    public UserProfileResponse currentUserProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getAvatarMediaId(),
                resolveAvatarUrl(user),
                user.getBio(),
                user.getRole(),
                user.getBanned(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getAvatarMediaId(),
                resolveAvatarUrl(user),
                user.getRole());
    }

    private String resolveAvatarUrl(User user) {
        return mediaService.buildPublicUrl(user.getAvatarMediaId());
    }

    // OTP-based password reset methods
    public OtpResponse sendOtp(SendOtpRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found with this email"));

        otpService.generateAndSendOtp(request.email());
        return new OtpResponse("OTP code has been sent to your email", true);
    }

    public OtpResponse resendOtp(SendOtpRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found with this email"));

        otpService.generateAndSendOtp(request.email());
        return new OtpResponse("New OTP code has been sent to your email", true);
    }

    public OtpResponse verifyOtpAndResetPassword(VerifyOtpRequest request) {
        // Verify user exists
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Verify OTP
        boolean isValidOtp = otpService.verifyOtp(request.email(), request.otp());
        if (!isValidOtp) {
            throw new BadRequestException("Invalid or expired OTP code");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        // Mark OTP as used
        otpService.markOtpAsUsed(request.email(), request.otp());

        return new OtpResponse("Password reset successfully", true);
    }
}
