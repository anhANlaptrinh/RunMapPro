package com.example.runningapp.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Reset Your Password - OTP Code");
            message.setText(buildOtpEmailBody(otpCode));

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    private String buildOtpEmailBody(String otpCode) {
        return String.format(
            "Hi,\n\n" +
            "You requested to reset your password for RunMap Pro.\n\n" +
            "Your OTP verification code is:\n\n" +
            "    %s\n\n" +
            "This code will expire in 5 minutes.\n\n" +
            "If you did not request this password reset, please ignore this email and your password will remain unchanged.\n\n" +
            "Best regards,\n" +
            "RunMap Pro Team",
            otpCode
        );
    }
}
