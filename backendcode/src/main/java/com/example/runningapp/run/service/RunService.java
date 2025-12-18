package com.example.runningapp.run.service;

import com.example.runningapp.run.dto.CreateRunRequest;
import com.example.runningapp.run.dto.RunResponse;
import com.example.runningapp.run.model.Run;
import com.example.runningapp.run.repository.RunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Run operations.
 * Handles business logic and data mapping between entities and DTOs.
 */
@Service
@RequiredArgsConstructor
public class RunService {
    
    private final RunRepository runRepository;
    
    /**
     * Create a new run session
     * @param request The run data from client
     * @param userId The authenticated user's ID (from JWT)
     * @return The created run with generated ID
     */
    public RunResponse createRun(CreateRunRequest request, String userId) {
        Run run = new Run();
        run.setUserId(userId);
        run.setStartTime(request.getStartTime());
        run.setEndTime(request.getEndTime());
        run.setDistanceMeters(request.getDistanceMeters());
        run.setDurationMs(request.getDurationMs());
        run.setSteps(request.getSteps());
        run.setCalories(request.getCalories());
        run.setBestPaceSecPerKm(request.getBestPaceSecPerKm());
        run.setAvgPaceSecPerKm(request.getAvgPaceSecPerKm());
        run.setPath(request.getPath());
        
        Run savedRun = runRepository.save(run);
        return mapToResponse(savedRun);
    }
    
    /**
     * Get all runs for the authenticated user
     * @param userId The authenticated user's ID
     * @return List of runs sorted by most recent first
     */
    public List<RunResponse> getAllRunsForUser(String userId) {
        return runRepository.findByUserIdOrderByStartTimeDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all runs (admin/debugging - should be secured)
     * @return List of all runs sorted by most recent first
     */
    public List<RunResponse> getAllRuns() {
        return runRepository.findAllByOrderByStartTimeDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific run by ID
     * PUBLIC READ: Any authenticated user can view run data
     * This allows users to see run details in social posts shared by others
     * 
     * @param id The run's MongoDB ObjectId
     * @param userId The authenticated user's ID (not used for authorization, only for logging)
     * @return The run if found
     * @throws RuntimeException if not found
     */
    public RunResponse getRunById(String id, String userId) {
        Run run = runRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Run not found with id: " + id));
        
        // No ownership check - run data is public for viewing
        // Any authenticated user can see run details shared in social posts
        
        return mapToResponse(run);
    }
    
    /**
     * Delete a run by ID
     * @param id The run's MongoDB ObjectId
     * @param userId The authenticated user's ID (for authorization)
     * @throws RuntimeException if not found or unauthorized
     */
    public void deleteRun(String id, String userId) {
        Run run = runRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Run not found with id: " + id));
        
        // Authorization check: ensure the run belongs to the authenticated user
        if (!run.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to run with id: " + id);
        }
        
        runRepository.deleteById(id);
    }
    
    /**
     * Map Run entity to RunResponse DTO
     */
    private RunResponse mapToResponse(Run run) {
        return RunResponse.builder()
                .id(run.getId())
                .userId(run.getUserId())
                .startTime(run.getStartTime())
                .endTime(run.getEndTime())
                .distanceMeters(run.getDistanceMeters())
                .durationMs(run.getDurationMs())
                .steps(run.getSteps())
                .calories(run.getCalories())
                .bestPaceSecPerKm(run.getBestPaceSecPerKm())
                .avgPaceSecPerKm(run.getAvgPaceSecPerKm())
                .path(run.getPath())
                .build();
    }
}
