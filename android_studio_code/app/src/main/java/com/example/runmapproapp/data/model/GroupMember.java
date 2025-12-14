package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class GroupMember {
    @SerializedName("id")
    private String id;

    @SerializedName("groupId")
    private String groupId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("role")
    private String role;

    @SerializedName("joinedAt")
    private String joinedAt;

    public String getId() { return id; }
    public String getGroupId() { return groupId; }
    public String getUserId() { return userId; }
    public String getRole() { return role; }
    public String getJoinedAt() { return joinedAt; }
}
