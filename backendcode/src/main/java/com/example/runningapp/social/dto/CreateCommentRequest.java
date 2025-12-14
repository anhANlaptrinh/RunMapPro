package com.example.runningapp.social.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank String contentText,
        String parentCommentId
) {
}
