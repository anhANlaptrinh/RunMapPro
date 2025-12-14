package com.example.runningapp.chat.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateGroupChatRequest(
        @NotEmpty List<String> memberIds,
        @NotBlank String groupName,
        String groupAvatarUrl
) {
}
