package com.example.runningapp.social;

import java.time.Instant;

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
@Document(collection = "comments")
public class Comment {

    @Id
    private String id;
    private String postId;
    private String authorId;
    private String contentText;
    private String parentCommentId;
    private long likeCount;
    private boolean deleted;
    private boolean blocked;
    private Instant createdAt;
    private Instant updatedAt;
    
    @Transient
    private String authorName;
    
    @Transient
    private String authorAvatar;
    
    @Transient
    private boolean likedByCurrentUser;
}
