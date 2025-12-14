package com.example.runmapproapp.data.model;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private String id;
    private String type; // "direct" or "group"
    private List<String> members = new ArrayList<>();
    private String groupName;
    private String groupAvatarUrl;
    private String createdAt;
    private String updatedAt;
    private String lastMessageAt;
    private String lastMessageText;
    
    // Additional fields for UI display
    private String otherUserName; // For direct chats
    private String otherUserAvatarUrl; // For direct chats
    private int unreadCount;

    public Conversation() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupAvatarUrl() { return groupAvatarUrl; }
    public void setGroupAvatarUrl(String groupAvatarUrl) { this.groupAvatarUrl = groupAvatarUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(String lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public String getLastMessageText() { return lastMessageText; }
    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }

    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }

    public String getOtherUserAvatarUrl() { return otherUserAvatarUrl; }
    public void setOtherUserAvatarUrl(String otherUserAvatarUrl) { this.otherUserAvatarUrl = otherUserAvatarUrl; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public boolean isGroupChat() {
        return "group".equals(type);
    }

    public String getDisplayName() {
        return isGroupChat() ? groupName : otherUserName;
    }

    public String getDisplayAvatar() {
        return isGroupChat() ? groupAvatarUrl : otherUserAvatarUrl;
    }
}
