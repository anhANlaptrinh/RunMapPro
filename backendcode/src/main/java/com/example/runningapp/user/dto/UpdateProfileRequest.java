package com.example.runningapp.user.dto;

import jakarta.validation.constraints.Size;

/**
 * Payload for updating mutable user profile fields.
 */
public record UpdateProfileRequest(
        @Size(min = 2, max = 60)
        String fullName,

        @Size(min = 3, max = 32)
        String username,

        @Size(max = 280)
        String bio
) {
}
