package com.example.runningapp.social.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        @NotBlank String contentText,
        List<String> mediaIds,
        String groupId
) {
}
