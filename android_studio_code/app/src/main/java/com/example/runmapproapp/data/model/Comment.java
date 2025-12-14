package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Comment {
    @SerializedName("id")
    private String id;

    @SerializedName("postId")
    private String postId;

    @SerializedName("authorId")
    private String authorId;

    @SerializedName("contentText")
    private String contentText;

    @SerializedName("parentCommentId")
    private String parentCommentId;

    @SerializedName("likeCount")
    private long likeCount;

    @SerializedName("deleted")
    private boolean deleted;

    @SerializedName("blocked")
    private boolean blocked;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("authorName")
    private String authorName;

    @SerializedName("authorAvatar")
    private String authorAvatar;

    @SerializedName("likedByCurrentUser")
    private boolean likedByCurrentUser;

    private static final SimpleDateFormat dateFormat = 
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    public String getId() { return id; }
    public String getPostId() { return postId; }
    public String getAuthorId() { return authorId; }
    public String getContentText() { return contentText; }
    public String getParentCommentId() { return parentCommentId; }
    public long getLikeCount() { return likeCount; }
    public boolean isDeleted() { return deleted; }
    public boolean isBlocked() { return blocked; }
    public String getAuthorName() { return authorName; }
    public String getAuthorAvatar() { return authorAvatar; }
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
    public void setLikedByCurrentUser(boolean liked) { this.likedByCurrentUser = liked; }
    
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
}
