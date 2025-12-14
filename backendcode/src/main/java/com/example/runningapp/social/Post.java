package com.example.runningapp.social;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
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
@Document(collection = "posts")
public class Post {

    @Id
    private String id;
    private String authorId;
    private String contentText;
    @Builder.Default
    private List<String> mediaIds = new ArrayList<>();
    private String groupId;
    private String originalPostId;
    private String visibility; // public, friends, group_only
    private long likeCount;
    private long commentCount;
    private long shareCount;
    private boolean deleted;
    private boolean blocked;
    private String blockedReason;
    private Instant createdAt;
    private Instant updatedAt;
    
    @Transient
    private boolean likedByCurrentUser;
    
    @Transient
    private String authorName;
    
    @Transient
    private String authorAvatar;
    
    @Transient
    private Post originalPost;
}
