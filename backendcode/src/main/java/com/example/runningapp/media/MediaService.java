package com.example.runningapp.media;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.runningapp.common.SecurityUtils;
import com.example.runningapp.common.exception.BadRequestException;
import com.example.runningapp.common.exception.NotFoundException;
import com.example.runningapp.common.exception.UnauthorizedException;
import com.example.runningapp.media.dto.MediaUploadResponse;

@Service
public class MediaService {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;

    public MediaService(GridFsTemplate gridFsTemplate, GridFsOperations gridFsOperations) {
        this.gridFsTemplate = gridFsTemplate;
        this.gridFsOperations = gridFsOperations;
    }

    public MediaUploadResponse upload(MultipartFile file, MediaCategory category) {
        String userId = requireUser();
        validateFile(file);
        try {
            String filename = buildFilename(file);
            Document metadata = new Document()
                    .append("ownerId", userId)
                    .append("category", category.name())
                    .append("uploadedAt", Instant.now());
            ObjectId storedId = gridFsTemplate.store(file.getInputStream(), filename, file.getContentType(), metadata);
            String mediaId = storedId.toHexString();
            return new MediaUploadResponse(
                    mediaId,
                    file.getContentType(),
                    file.getSize(),
                    buildPublicUrl(mediaId));
        } catch (IOException ex) {
            throw new BadRequestException("Unable to store media: " + ex.getMessage());
        }
    }

    public MediaResource load(String mediaId) {
        try {
            ObjectId objectId = toObjectId(mediaId);
            var file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(objectId)));
            if (file == null) {
                throw new NotFoundException("Media not found: " + mediaId);
            }
            GridFsResource resource = gridFsOperations.getResource(file);
            String contentType = resource.getContentType();
            if (!StringUtils.hasText(contentType) && file.getMetadata() != null) {
                contentType = file.getMetadata().getString("contentType");
            }
            
            // Fix invalid MIME types like "image/*"
            if (contentType != null && contentType.contains("/*")) {
                contentType = "image/jpeg"; // Default to JPEG for wildcard image types
            }
            
            if (!StringUtils.hasText(contentType)) {
                contentType = "application/octet-stream";
            }
            
            long length = file.getLength();
            System.out.println("Loading media " + mediaId + " - contentType: " + contentType + ", length: " + length);
            
            return new MediaResource(resource, contentType, length);
        } catch (Exception e) {
            System.err.println("ERROR loading media " + mediaId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void delete(String mediaId) {
        ObjectId objectId = toObjectId(mediaId);
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is(objectId)));
    }

    public String buildPublicUrl(String mediaId) {
        if (!StringUtils.hasText(mediaId)) {
            return null;
        }
        return "/api/media/" + mediaId;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("File exceeds 10MB limit");
        }
    }

    private String buildFilename(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original)) {
            return UUID.randomUUID().toString();
        }
        return UUID.randomUUID() + "-" + original.replace(" ", "-");
    }

    private ObjectId toObjectId(String mediaId) {
        try {
            return new ObjectId(mediaId);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException("Media not found");
        }
    }

    private String requireUser() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return userId;
    }

    public record MediaResource(GridFsResource resource, String contentType, long length) {
    }
}
