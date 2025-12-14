package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {

    @SerializedName("id")
    private String id;

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

    @SerializedName("bio")
    private String bio;

    @SerializedName("role")
    private String role;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAvatarMediaId() {
        return avatarMediaId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public String getRole() {
        return role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
