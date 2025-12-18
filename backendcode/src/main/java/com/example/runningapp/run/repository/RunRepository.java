package com.example.runningapp.run.repository;

import com.example.runningapp.run.model.Run;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB repository for Run entities.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface RunRepository extends MongoRepository<Run, String> {
    
    /**
     * Find all runs for a specific user, ordered by start time descending
     * @param userId The user's MongoDB ObjectId
     * @return List of runs sorted by most recent first
     */
    List<Run> findByUserIdOrderByStartTimeDesc(String userId);
    
    /**
     * Find all runs ordered by start time descending
     * @return List of all runs sorted by most recent first
     */
    List<Run> findAllByOrderByStartTimeDesc();
}
