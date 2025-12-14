package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class GroupJoinRequest {
    @SerializedName("id")
    private String id;

    @SerializedName("groupId")
    private String groupId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("inviteCode")
    private String inviteCode;

    @SerializedName("status")
    private String status;

    @SerializedName("requestedAt")
    private String requestedAt;

    @SerializedName("reviewedAt")
    private String reviewedAt;

    @SerializedName("reviewedBy")
    private String reviewedBy;

    public String getId() { return id; }
    public String getGroupId() { return groupId; }
    public String getUserId() { return userId; }
    public String getInviteCode() { return inviteCode; }
    public String getStatus() { return status; }
    public String getRequestedAt() { return requestedAt; }
    public String getReviewedAt() { return reviewedAt; }
    public String getReviewedBy() { return reviewedBy; }
}
