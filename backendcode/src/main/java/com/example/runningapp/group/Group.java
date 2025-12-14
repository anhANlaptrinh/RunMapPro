package com.example.runningapp.group;

import java.time.Instant;

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
@Document(collection = "groups")
public class Group {

    @Id
    private String id;
    private String name;
    private String description;
    private String coverImageUrl;
    private String ownerId;
    private String privacy; // public or private
    private String inviteCode; // for private groups
    private Instant inviteCodeExpiresAt; // expiration date for invite code (3 months or until regenerated)
    private boolean requireMemberApproval; // require admin approval for join requests
    private boolean requirePostApproval; // require admin approval for posts
    private long memberCount;
    private long postCount;
    private boolean blocked;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Transient field - not stored in database
    @org.springframework.data.annotation.Transient
    private String userRole; // owner, admin, member
}
