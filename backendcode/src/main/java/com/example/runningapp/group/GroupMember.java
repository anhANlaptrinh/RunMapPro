package com.example.runningapp.group;

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
@Document(collection = "group_members")
@CompoundIndex(name = "group_user_unique", def = "{ 'groupId': 1, 'userId': 1 }", unique = true)
public class GroupMember {

    @Id
    private String id;
    private String groupId;
    private String userId;
    private String role; // owner, admin, member
    private Instant joinedAt;
}
