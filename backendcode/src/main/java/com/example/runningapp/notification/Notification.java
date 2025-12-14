package com.example.runningapp.notification;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
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
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;
    
    @Indexed
    private String receiverId;
    
    private String senderId;
    
    private NotificationType type; // LIKE_POST, LIKE_COMMENT, COMMENT_POST, REPLY_COMMENT, SHARE_POST
    
    private String postId;
    
    private String commentId;
    
    private String contentText; // Optional: comment text preview
    
    @Builder.Default
    private boolean read = false;
    
    private Instant createdAt;
    
    @Transient
    private String senderName;
    
    @Transient
    private String senderAvatar;
}
