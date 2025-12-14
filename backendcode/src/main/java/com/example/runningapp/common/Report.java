package com.example.runningapp.common;

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
@Document(collection = "reports")
public class Report {

    @Id
    private String id;
    private String reporterId;
    private String targetType;
    private String targetId;
    private String reason;
    private String detail;
    private String status;
    private String handledBy;
    private Instant handledAt;
    private Instant createdAt;
    private Instant updatedAt;
}
