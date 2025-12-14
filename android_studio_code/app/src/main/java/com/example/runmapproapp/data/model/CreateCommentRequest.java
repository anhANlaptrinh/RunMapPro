package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class CreateCommentRequest {
    @SerializedName("postId")
    private final String postId;

    @SerializedName("contentText")
    private final String contentText;

    @SerializedName("parentCommentId")
    private final String parentCommentId;

    public CreateCommentRequest(String postId, String contentText, String parentCommentId) {
        this.postId = postId;
        this.contentText = contentText;
        this.parentCommentId = parentCommentId;
    }

    public String getPostId() { return postId; }
    public String getContentText() { return contentText; }
    public String getParentCommentId() { return parentCommentId; }
}
