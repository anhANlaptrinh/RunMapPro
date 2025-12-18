package com.example.runningapp.run.dto;

import com.example.runningapp.run.model.GeoJsonLineString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Response DTO for returning run data to clients.
 * Includes all fields from the Run entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunResponse {
    
    private String id;
    private String userId;
    private Date startTime;
    private Date endTime;
    private double distanceMeters;
    private long durationMs;
    private int steps;
    private double calories;
    private double bestPaceSecPerKm;
    private double avgPaceSecPerKm;
    private GeoJsonLineString path;
}
