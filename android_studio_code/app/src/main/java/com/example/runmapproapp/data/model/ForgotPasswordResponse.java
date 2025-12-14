package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("resetToken")
    private String resetToken;

    public String getMessage() {
        return message;
    }

    public String getResetToken() {
        return resetToken;
    }
}
