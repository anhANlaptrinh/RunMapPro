package com.example.runmapproapp.data.model;

import android.content.Context;

import com.example.runmapproapp.R;

public class Notification {
    private String id;
    private String receiverId;
    private String senderId;
    private String type; // LIKE_POST, LIKE_COMMENT, COMMENT_POST, REPLY_COMMENT, SHARE_POST
    private String postId;
    private String commentId;
    private String contentText;
    private boolean read;
    private String senderName;
    private String senderAvatar;
    private String createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getNotificationText(Context context) {
        switch (type) {
            case "LIKE_POST":
                return context.getString(R.string.notification_liked_post, senderName);
            case "LIKE_COMMENT":
                return context.getString(R.string.notification_liked_comment, senderName);
            case "COMMENT_POST":
                return context.getString(R.string.notification_commented, senderName, contentText);
            case "REPLY_COMMENT":
                return context.getString(R.string.notification_replied, senderName, contentText);
            case "SHARE_POST":
                return context.getString(R.string.notification_shared_post, senderName);
            default:
                return context.getString(R.string.notification_interacted, senderName);
        }
    }
}
