package com.example.runningapp.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDirectChatRequest(@NotBlank String otherUserId) {
}
