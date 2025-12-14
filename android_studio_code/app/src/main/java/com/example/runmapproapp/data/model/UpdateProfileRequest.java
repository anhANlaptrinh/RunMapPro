package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {

    @SerializedName("fullName")
    private final String fullName;

    @SerializedName("username")
    private final String username;

    @SerializedName("bio")
    private final String bio;

    public UpdateProfileRequest(String fullName, String username, String bio) {
        this.fullName = fullName;
        this.username = username;
        this.bio = bio;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getBio() {
        return bio;
    }
}
