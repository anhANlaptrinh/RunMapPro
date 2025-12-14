package com.example.runningapp.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for changing the authenticated user's password.
 */
public record ChangePasswordRequest(
        @NotBlank
        String currentPassword,

        @NotBlank
        @Size(min = 6, max = 100)
        String newPassword
) {
}
