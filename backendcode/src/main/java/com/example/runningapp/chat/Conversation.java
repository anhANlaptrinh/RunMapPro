package com.example.runningapp.chat;

import java.time.Instant;
import java.util.ArrayList;
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
@Document(collection = "conversations")
public class Conversation {

    @Id
    private String id;
    private String type; // "direct" or "group"
    @Builder.Default
    private List<String> members = new ArrayList<>();
    private String groupName;
    private String groupAvatarUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastMessageAt;
    private String lastMessageText;
}
