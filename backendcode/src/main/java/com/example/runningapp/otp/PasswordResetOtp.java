package com.example.runningapp.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "password_reset_otps")
public class PasswordResetOtp {

    @Id
    private String id;

    @Indexed
    private String email;

    private String otpCode;

    @Indexed(expireAfterSeconds = 0)
    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    private boolean used;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}
