package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Post {
    @SerializedName("id")
    private String id;

    @SerializedName("authorId")
    private String authorId;

    @SerializedName("authorName")
    private String authorName;

    @SerializedName("authorAvatar")
    private String authorAvatar;

    @SerializedName("contentText")
    private String contentText;

    @SerializedName("mediaIds")
    private List<String> mediaIds;

    @SerializedName("groupId")
    private String groupId;

    @SerializedName("originalPostId")
    private String originalPostId;

    @SerializedName("visibility")
    private String visibility;

    @SerializedName("likeCount")
    private long likeCount;

    @SerializedName("commentCount")
    private long commentCount;

    @SerializedName("shareCount")
    private long shareCount;

    @SerializedName("deleted")
    private boolean deleted;

    @SerializedName("blocked")
    private boolean blocked;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("likedByCurrentUser")
    private boolean likedByCurrentUser;

    @SerializedName("originalPost")
    private Post originalPost;

    private static final SimpleDateFormat dateFormat = 
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    // Getters
    public String getId() { return id; }
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getAuthorAvatar() { return authorAvatar; }
    public String getContentText() { return contentText; }
    public List<String> getMediaIds() { return mediaIds; }
    public String getGroupId() { return groupId; }
    public String getOriginalPostId() { return originalPostId; }
    public String getVisibility() { return visibility; }
    public long getLikeCount() { return likeCount; }
    public long getCommentCount() { return commentCount; }
    public long getShareCount() { return shareCount; }
    public boolean isDeleted() { return deleted; }
    public boolean isBlocked() { return blocked; }
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public Post getOriginalPost() { return originalPost; }
    
    public Date getCreatedAt() {
        if (createdAt == null) return null;
        try {
            return dateFormat.parse(createdAt);
        } catch (ParseException e) {
            return null;
        }
    }
    
    public Date getUpdatedAt() {
        if (updatedAt == null) return null;
        try {
            return dateFormat.parse(updatedAt);
        } catch (ParseException e) {
            return null;
        }
    }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public void setMediaIds(List<String> mediaIds) { this.mediaIds = mediaIds; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }
    public void setShareCount(long shareCount) { this.shareCount = shareCount; }
    public void setLikedByCurrentUser(boolean liked) { this.likedByCurrentUser = liked; }
}
