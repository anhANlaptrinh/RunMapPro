package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordResponse {

    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }
}
