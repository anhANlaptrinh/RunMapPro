package com.example.runmapproapp.data.model;

import com.google.gson.annotations.SerializedName;

public class CreateGroupRequest {
    @SerializedName("name")
    private final String name;

    @SerializedName("description")
    private final String description;

    @SerializedName("coverImageUrl")
    private final String coverImageUrl;

    @SerializedName("privacy")
    private final String privacy;

    public CreateGroupRequest(String name, String description, String coverImageUrl, String privacy) {
        this.name = name;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.privacy = privacy;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public String getPrivacy() { return privacy; }
}
