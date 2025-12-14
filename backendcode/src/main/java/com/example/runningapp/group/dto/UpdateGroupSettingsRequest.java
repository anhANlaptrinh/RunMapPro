package com.example.runningapp.group.dto;

public record UpdateGroupSettingsRequest(
    String name,
    String description,
    String coverImageUrl,
    Boolean requireMemberApproval,
    Boolean requirePostApproval,
    Boolean regenerateInviteCode
) {}
