package com.example.runningapp.social;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
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
@Document(collection = "comment_likes")
@CompoundIndex(name = "comment_user_unique", def = "{ 'commentId': 1, 'userId': 1 }", unique = true)
public class CommentLike {

    @Id
    private String id;
    private String commentId;
    private String userId;
    private Instant createdAt;
}
