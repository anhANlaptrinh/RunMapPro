package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("token")
    private String accessToken;

    @SerializedName("refreshToken")
    private String refreshToken;

    @SerializedName("userId")
    private String userId;

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("avatarMediaId")
    private String avatarMediaId;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("role")
    private String role;

    @SerializedName("banned")
    private Boolean banned;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public User getUser() {
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setUsername(username);
        user.setFullName(fullName);
        user.setAvatarUrl(avatarUrl);
        user.setRole(role);
        // Map banned flag if present
        user.setBanned(banned != null && banned);
        return user;
    }
}
