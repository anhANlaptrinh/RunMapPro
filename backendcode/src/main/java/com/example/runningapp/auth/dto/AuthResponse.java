package com.example.runningapp.auth.dto;

public record AuthResponse(
        String token,
        String userId,
        String email,
        String username,
        String fullName,
        String avatarMediaId,
        String avatarUrl,
        String role
) {
}
