package com.example.runmapproapp.data.websocket;

import com.google.gson.annotations.SerializedName;

public class WebSocketEvent {
    
    @SerializedName("type")
    private String type; // "message", "typing", "read"
    
    @SerializedName("conversationId")
    private String conversationId;
    
    @SerializedName("message")
    private MessageData message;
    
    @SerializedName("userId")
    private String userId;
    
    @SerializedName("isTyping")
    private Boolean isTyping;
    
    public WebSocketEvent() {}
    
    public WebSocketEvent(String type, String conversationId) {
        this.type = type;
        this.conversationId = conversationId;
    }
    
    // Getters and setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    
    public MessageData getMessage() {
        return message;
    }
    
    public void setMessage(MessageData message) {
        this.message = message;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Boolean getIsTyping() {
        return isTyping;
    }
    
    public void setIsTyping(Boolean isTyping) {
        this.isTyping = isTyping;
    }
    
    public static class MessageData {
        @SerializedName("id")
        private String id;
        
        @SerializedName("conversationId")
        private String conversationId;
        
        @SerializedName("senderId")
        private String senderId;
        
        @SerializedName("text")
        private String text;
        
        @SerializedName("createdAt")
        private String createdAt;
        
        @SerializedName("senderName")
        private String senderName;
        
        @SerializedName("senderAvatarUrl")
        private String senderAvatarUrl;
        
        // Getters and setters
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getConversationId() {
            return conversationId;
        }
        
        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }
        
        public String getSenderId() {
            return senderId;
        }
        
        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public String getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
        
        public String getSenderName() {
            return senderName;
        }
        
        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }
        
        public String getSenderAvatarUrl() {
            return senderAvatarUrl;
        }
        
        public void setSenderAvatarUrl(String senderAvatarUrl) {
            this.senderAvatarUrl = senderAvatarUrl;
        }
    }
}
