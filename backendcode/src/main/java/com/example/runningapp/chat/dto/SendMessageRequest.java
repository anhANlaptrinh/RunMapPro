package com.example.runningapp.chat.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank String conversationId,
        @NotBlank String text,
        List<String> mediaIds
) {
}
