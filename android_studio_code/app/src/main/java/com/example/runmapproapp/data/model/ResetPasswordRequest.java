package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {

    @SerializedName("resetToken")
    private final String resetToken;

    @SerializedName("password")
    private final String password;

    public ResetPasswordRequest(String resetToken, String password) {
        this.resetToken = resetToken;
        this.password = password;
    }

    public String getResetToken() {
        return resetToken;
    }

    public String getPassword() {
        return password;
    }
}
