package com.example.runningapp.run.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * MongoDB entity representing a running session.
 * Stores all data captured during a run including path, stats, and metrics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "runs")
public class Run {
    
    @Id
    private String id;
    
    /**
     * The MongoDB ObjectId of the user who recorded this run
     */
    private String userId;
    
    /**
     * When the run started (ISO timestamp)
     */
    private Date startTime;
    
    /**
     * When the run ended (ISO timestamp)
     */
    private Date endTime;
    
    /**
     * Total distance covered in meters
     */
    private double distanceMeters;
    
    /**
     * Duration of the run in milliseconds
     */
    private long durationMs;
    
    /**
     * Number of steps taken during the run
     */
    private int steps;
    
    /**
     * Estimated calories burned
     */
    private double calories;
    
    /**
     * Best (fastest) pace in seconds per kilometer
     */
    private double bestPaceSecPerKm;
    
    /**
     * Average pace in seconds per kilometer
     */
    private double avgPaceSecPerKm;
    
    /**
     * GeoJSON LineString representing the path/route of the run
     * Contains coordinates as [longitude, latitude] pairs
     */
    private GeoJsonLineString path;
}
