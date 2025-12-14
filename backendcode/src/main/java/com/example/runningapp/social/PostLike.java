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
@Document(collection = "post_likes")
@CompoundIndex(name = "post_user_unique", def = "{ 'postId': 1, 'userId': 1 }", unique = true)
public class PostLike {

    @Id
    private String id;
    private String postId;
    private String userId;
    private Instant createdAt;
}
