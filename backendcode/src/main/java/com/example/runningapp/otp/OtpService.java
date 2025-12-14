package com.example.runningapp.otp;

import com.example.runningapp.email.EmailService;
import com.example.runningapp.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.otp.expiration-minutes:5}")
    private int expirationMinutes;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Transactional
    public void generateAndSendOtp(String email) {
        // Delete any existing OTP for this email
        otpRepository.deleteByEmail(email);

        // Generate new OTP
        String otpCode = generateOtpCode();
        
        // Save to database
        PasswordResetOtp otp = PasswordResetOtp.builder()
                .email(email.toLowerCase())
                .otpCode(otpCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .used(false)
                .build();
        
        otpRepository.save(otp);
        log.info("Generated OTP for email: {}", email);

        // Send email
        emailService.sendOtpEmail(email, otpCode);
    }

    public boolean verifyOtp(String email, String otpCode) {
        return otpRepository.findByEmailAndOtpCodeAndUsedFalse(email.toLowerCase(), otpCode)
                .map(otp -> {
                    if (otp.isExpired()) {
                        log.warn("OTP expired for email: {}", email);
                        return false;
                    }
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void markOtpAsUsed(String email, String otpCode) {
        otpRepository.findByEmailAndOtpCodeAndUsedFalse(email.toLowerCase(), otpCode)
                .ifPresent(otp -> {
                    otp.setUsed(true);
                    otpRepository.save(otp);
                    log.info("Marked OTP as used for email: {}", email);
                });
    }

    private String generateOtpCode() {
        int upperBound = (int) Math.pow(10, otpLength);
        int otpNumber = random.nextInt(upperBound);
        return String.format("%0" + otpLength + "d", otpNumber);
    }
}
