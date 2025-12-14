package com.example.runningapp.user;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "password_resets")
public class PasswordResetToken {

    @Id
    private String id;
    private String userId;
    private String token;
    private Instant expiresAt;
    private boolean used;
    private Instant createdAt;
}
