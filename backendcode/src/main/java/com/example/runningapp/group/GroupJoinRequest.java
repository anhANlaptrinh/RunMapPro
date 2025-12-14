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
@Document(collection = "group_join_requests")
@CompoundIndex(name = "group_user_request_unique", def = "{ 'groupId': 1, 'userId': 1 }", unique = true)
public class GroupJoinRequest {

    @Id
    private String id;
    private String groupId;
    private String userId;
    private String inviteCode; // code provided by user when requesting to join private group
    private String status; // pending, approved, rejected
    private Instant requestedAt;
    private Instant reviewedAt;
    private String reviewedBy; // admin who approved/rejected
}
