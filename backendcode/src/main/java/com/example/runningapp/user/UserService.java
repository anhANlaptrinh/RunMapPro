package com.example.runningapp.user;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.runningapp.auth.dto.UserProfileResponse;
import com.example.runningapp.common.SecurityUtils;
import com.example.runningapp.common.exception.BadRequestException;
import com.example.runningapp.common.exception.NotFoundException;
import com.example.runningapp.common.exception.UnauthorizedException;
import com.example.runningapp.media.MediaCategory;
import com.example.runningapp.media.MediaService;
import com.example.runningapp.media.dto.MediaUploadResponse;
import com.example.runningapp.user.dto.ChangePasswordRequest;
import com.example.runningapp.user.dto.UpdateProfileRequest;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MediaService mediaService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, MediaService mediaService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mediaService = mediaService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfileResponse getCurrentProfile() {
        return toProfile(requireCurrentUser());
    }

    public UserProfileResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toProfile(user);
    }

    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = requireCurrentUser();

        if (request.username() != null && !request.username().isBlank()) {
            String normalizedUsername = request.username().trim();
            if (!normalizedUsername.equalsIgnoreCase(user.getUsername()) && userRepository.existsByUsername(normalizedUsername)) {
                throw new BadRequestException("Username already in use");
            }
            user.setUsername(normalizedUsername);
        }

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }

        if (request.bio() != null) {
            user.setBio(request.bio().trim());
        }

        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return toProfile(user);
    }

    public UserProfileResponse uploadAvatar(MultipartFile file) {
        User user = requireCurrentUser();
        String oldAvatarId = user.getAvatarMediaId();
        MediaUploadResponse uploadResponse = mediaService.upload(file, MediaCategory.AVATAR);
        user.setAvatarMediaId(uploadResponse.mediaId());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        if (oldAvatarId != null && !oldAvatarId.equals(uploadResponse.mediaId())) {
            mediaService.delete(oldAvatarId);
        }
        return toProfile(user);
    }

    public void changePassword(ChangePasswordRequest request) {
        User user = requireCurrentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    public java.util.List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return userRepository.findAll();
        }
        String searchQuery = query.trim().toLowerCase();
        return userRepository.findAll().stream()
                .filter(user -> 
                    user.getFullName().toLowerCase().contains(searchQuery) ||
                    user.getEmail().toLowerCase().contains(searchQuery) ||
                    (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchQuery))
                )
                .limit(50)
                .collect(java.util.stream.Collectors.toList());
    }

    private String requireUserId() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return userId;
    }

    private User requireCurrentUser() {
        String userId = requireUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getAvatarMediaId(),
                mediaService.buildPublicUrl(user.getAvatarMediaId()),
                user.getBio(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
