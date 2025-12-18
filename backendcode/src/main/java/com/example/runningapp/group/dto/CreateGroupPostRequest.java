package com.example.runningapp.group.dto;

import java.util.List;

public record CreateGroupPostRequest(
    String content,
    List<String> mediaUrls,
    String runId
) {}
