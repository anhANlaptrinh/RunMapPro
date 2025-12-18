package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CreatePostRequest {
    @SerializedName("contentText")
    private final String contentText;

    @SerializedName("mediaIds")
    private final List<String> mediaIds;

    @SerializedName("groupId")
    private final String groupId;

    @SerializedName("runId")
    private final String runId;

    public CreatePostRequest(String contentText, List<String> mediaIds, String groupId, String runId) {
        this.contentText = contentText;
        this.mediaIds = mediaIds;
        this.groupId = groupId;
        this.runId = runId;
    }

    public String getContentText() { return contentText; }
    public List<String> getMediaIds() { return mediaIds; }
    public String getGroupId() { return groupId; }
    public String getRunId() { return runId; }
}
