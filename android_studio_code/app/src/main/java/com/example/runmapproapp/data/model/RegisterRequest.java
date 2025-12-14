package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("username")
    private final String username;

    @SerializedName("fullName")
    private final String fullName;

    @SerializedName("email")
    private final String email;

    @SerializedName("password")
    private final String password;

    public RegisterRequest(String username, String fullName, String email, String password) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
