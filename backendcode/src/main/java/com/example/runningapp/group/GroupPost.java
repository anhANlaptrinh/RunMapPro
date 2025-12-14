package com.example.runningapp.group;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "group_posts")
public class GroupPost {

    @Id
    private String id;
    private String groupId;
    private String userId;
    private String content;
    private List<String> mediaUrls; // images or videos
    private String status; // pending, approved, rejected (for groups with post approval)
    private Instant createdAt;
    private Instant approvedAt;
    private String approvedBy; // admin who approved
    private long likeCount;
    private long commentCount;
    private List<String> likedByUsers; // List of user IDs who liked this post
    
    // Transient fields - not stored in database
    @org.springframework.data.annotation.Transient
    private String authorName;
    
    @org.springframework.data.annotation.Transient
    private String authorAvatar;
    
    @org.springframework.data.annotation.Transient
    private boolean likedByCurrentUser;
}
