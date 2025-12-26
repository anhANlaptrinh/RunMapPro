package com.example.runningapp.user;

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
@Document(collection = "users")
public class User {

    @Id
    private String id;
    private String email;
    private String passwordHash;
    private String username;
    private String fullName;
    private String avatarMediaId;
    private String bio;
    private String role;
    private Boolean banned; // Admin can ban users
    private Instant createdAt;
    private Instant updatedAt;
}
