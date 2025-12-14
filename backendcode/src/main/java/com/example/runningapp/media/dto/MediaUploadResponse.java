package com.example.runningapp.media.dto;

public record MediaUploadResponse(
        String mediaId,
        String contentType,
        long length,
        String downloadUrl
) {
}
