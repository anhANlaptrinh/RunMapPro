package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class ErrorResponse {

    @SerializedName("status")
    private int status;

    @SerializedName("message")
    private String message;

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
