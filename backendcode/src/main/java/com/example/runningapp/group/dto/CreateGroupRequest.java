package com.example.runningapp.group.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGroupRequest(
        @NotBlank String name,
        String description,
        String coverImageUrl,
        @NotBlank String privacy
) {
}
