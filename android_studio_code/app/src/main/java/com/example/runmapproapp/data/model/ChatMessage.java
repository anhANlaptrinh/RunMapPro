package com.example.runmapproapp.data.model;

import java.util.ArrayList;
import java.util.List;

public class ChatMessage {
    private String id;
    private String conversationId;
    private String senderId;
    private String text;
    private List<String> mediaIds = new ArrayList<>();
    private List<String> readBy = new ArrayList<>();
    private String createdAt;
    
    // Additional fields for UI display
    private String senderName;
    private String senderAvatarUrl;
    private boolean isMine;

    public ChatMessage() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<String> getMediaIds() { return mediaIds; }
    public void setMediaIds(List<String> mediaIds) { this.mediaIds = mediaIds; }

    public List<String> getReadBy() { return readBy; }
    public void setReadBy(List<String> readBy) { this.readBy = readBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatarUrl() { return senderAvatarUrl; }
    public void setSenderAvatarUrl(String senderAvatarUrl) { this.senderAvatarUrl = senderAvatarUrl; }

    public boolean isMine() { return isMine; }
    public void setMine(boolean mine) { isMine = mine; }
}
