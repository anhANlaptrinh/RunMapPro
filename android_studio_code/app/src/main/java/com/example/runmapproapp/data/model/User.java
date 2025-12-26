package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("avatarMediaId")
    private String avatarMediaId;

    @SerializedName("bio")
    private String bio;

    @SerializedName("role")
    private String role;

    @SerializedName("banned")
    private boolean banned;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        // If avatarUrl exists, use it
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            return avatarUrl;
        }
        // Otherwise, construct URL from avatarMediaId
        if (avatarMediaId != null && !avatarMediaId.isEmpty()) {
            return "http://10.0.2.2:8080/api/media/" + avatarMediaId;
        }
        return null;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarMediaId() {
        return avatarMediaId;
    }

    public void setAvatarMediaId(String avatarMediaId) {
        this.avatarMediaId = avatarMediaId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
