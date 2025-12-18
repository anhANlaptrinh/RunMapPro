package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GroupPost {
    @SerializedName("id")
    private String id;

    @SerializedName("groupId")
    private String groupId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("content")
    private String content;

    @SerializedName("mediaUrls")
    private List<String> mediaUrls;

    @SerializedName("runId")
    private String runId;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("approvedAt")
    private String approvedAt;

    @SerializedName("approvedBy")
    private String approvedBy;

    @SerializedName("likeCount")
    private long likeCount;

    @SerializedName("commentCount")
    private long commentCount;
    
    @SerializedName("authorName")
    private String authorName;
    
    @SerializedName("authorAvatar")
    private String authorAvatar;
    
    @SerializedName("likedByCurrentUser")
    private boolean likedByCurrentUser;

    public String getId() { return id; }
    public String getGroupId() { return groupId; }
    public String getUserId() { return userId; }
    public String getContent() { return content; }
    public List<String> getMediaUrls() { return mediaUrls; }
    public String getRunId() { return runId; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getApprovedAt() { return approvedAt; }
    public String getApprovedBy() { return approvedBy; }
    public long getLikeCount() { return likeCount; }
    public long getCommentCount() { return commentCount; }
    public String getAuthorName() { return authorName; }
    public String getAuthorAvatar() { return authorAvatar; }
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }

    public void setContent(String content) { this.content = content; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }
}
