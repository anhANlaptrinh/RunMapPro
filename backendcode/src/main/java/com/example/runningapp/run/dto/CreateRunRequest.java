package com.example.runningapp.run.dto;

import com.example.runningapp.run.model.GeoJsonLineString;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Request DTO for creating a new run session.
 * Includes validation constraints to ensure data quality.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRunRequest {
    
    @NotNull(message = "Start time is required")
    private Date startTime;
    
    @NotNull(message = "End time is required")
    private Date endTime;
    
    @Min(value = 0, message = "Distance must be greater than or equal to 0")
    private double distanceMeters;
    
    @Min(value = 0, message = "Duration must be greater than or equal to 0")
    private long durationMs;
    
    @Min(value = 0, message = "Steps must be greater than or equal to 0")
    private int steps;
    
    @Min(value = 0, message = "Calories must be greater than or equal to 0")
    private double calories;
    
    @Min(value = 0, message = "Best pace must be greater than or equal to 0")
    private double bestPaceSecPerKm;
    
    @Min(value = 0, message = "Average pace must be greater than or equal to 0")
    private double avgPaceSecPerKm;
    
    @NotNull(message = "Path is required")
    private GeoJsonLineString path;
}
