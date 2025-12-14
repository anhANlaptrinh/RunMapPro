package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class Group {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("coverImageUrl")
    private String coverImageUrl;

    @SerializedName("ownerId")
    private String ownerId;

    @SerializedName("privacy")
    private String privacy;

    @SerializedName("inviteCode")
    private String inviteCode;

    @SerializedName("inviteCodeExpiresAt")
    private String inviteCodeExpiresAt;

    @SerializedName("requireMemberApproval")
    private boolean requireMemberApproval;

    @SerializedName("requirePostApproval")
    private boolean requirePostApproval;

    @SerializedName("memberCount")
    private long memberCount;

    @SerializedName("postCount")
    private long postCount;

    @SerializedName("blocked")
    private boolean blocked;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("userRole")
    private String userRole; // owner, admin, member

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public String getOwnerId() { return ownerId; }
    public String getPrivacy() { return privacy; }
    public String getInviteCode() { return inviteCode; }
    public String getInviteCodeExpiresAt() { return inviteCodeExpiresAt; }
    public boolean isRequireMemberApproval() { return requireMemberApproval; }
    public boolean isRequirePostApproval() { return requirePostApproval; }
    public long getMemberCount() { return memberCount; }
    public long getPostCount() { return postCount; }
    public boolean isBlocked() { return blocked; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getUserRole() { return userRole; }
    
    public void setUserRole(String userRole) { this.userRole = userRole; }
}
