package com.example.runningapp.auth.dto;

import java.time.Instant;

public record UserProfileResponse(
        String userId,
        String email,
        String username,
        String fullName,
        String avatarMediaId,
        String avatarUrl,
        String bio,
        String role,
        Boolean banned,
        Instant createdAt,
        Instant updatedAt
) {
}
