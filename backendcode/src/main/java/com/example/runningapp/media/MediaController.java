package com.example.runningapp.media;

import java.io.IOException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.runningapp.media.MediaService.MediaResource;
import com.example.runningapp.media.dto.MediaUploadResponse;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "POST_ATTACHMENT") MediaCategory category) {
        return ResponseEntity.ok(mediaService.upload(file, category));
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String mediaId) {
        try {
            System.out.println("Downloading media: " + mediaId);
            MediaResource resource = mediaService.load(mediaId);
            
            // Wrap GridFsResource InputStream to avoid serialization issues
            InputStreamResource streamResource = new InputStreamResource(resource.resource().getInputStream());
            
            System.out.println("Media loaded successfully: " + mediaId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(resource.contentType()))
                    .contentLength(resource.length())
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(streamResource);
        } catch (IOException e) {
            System.err.println("ERROR downloading media " + mediaId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load media: " + mediaId, e);
        }
    }
}
