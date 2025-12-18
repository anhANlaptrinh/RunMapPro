package com.example.runningapp.run.controller;

import com.example.runningapp.common.SecurityUtils;
import com.example.runningapp.run.dto.CreateRunRequest;
import com.example.runningapp.run.dto.RunResponse;
import com.example.runningapp.run.service.RunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing running sessions.
 * All endpoints require authentication (JWT token).
 * 
 * Security approach:
 * - All endpoints are authenticated (protected by Spring Security)
 * - userId is extracted from JWT authentication principal
 * - Each user can only access their own runs (authorization in service layer)
 */
@RestController
@RequestMapping("/api/runs")
@RequiredArgsConstructor
public class RunController {
    
    private final RunService runService;
    
    /**
     * Create a new run session
     * POST /api/runs
     * 
     * @param request The run data from client
     * @return 201 Created with the saved run including generated ID
     */
    @PostMapping
    public ResponseEntity<RunResponse> createRun(
            @Valid @RequestBody CreateRunRequest request) {
        
        // Extract userId (ObjectId) from JWT token
        String userId = SecurityUtils.getCurrentUserId();
        
        RunResponse response = runService.createRun(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get all runs for the authenticated user
     * GET /api/runs
     * 
     * @return 200 OK with list of runs sorted by most recent first
     */
    @GetMapping
    public ResponseEntity<List<RunResponse>> getAllRuns() {
        String userId = SecurityUtils.getCurrentUserId();
        List<RunResponse> runs = runService.getAllRunsForUser(userId);
        return ResponseEntity.ok(runs);
    }
    
    /**
     * Get a specific run by ID
     * GET /api/runs/{id}
     * 
     * @param id The MongoDB ObjectId of the run
     * @return 200 OK with the run data, or 404 if not found/unauthorized
     */
    @GetMapping("/{id}")
    public ResponseEntity<RunResponse> getRunById(
            @PathVariable String id) {
        
        String userId = SecurityUtils.getCurrentUserId();
        RunResponse response = runService.getRunById(id, userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a run by ID
     * DELETE /api/runs/{id}
     * 
     * @param id The MongoDB ObjectId of the run to delete
     * @return 204 No Content on success, or 404 if not found/unauthorized
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRun(
            @PathVariable String id) {
        
        String userId = SecurityUtils.getCurrentUserId();
        runService.deleteRun(id, userId);
        return ResponseEntity.noContent().build();
    }
}
