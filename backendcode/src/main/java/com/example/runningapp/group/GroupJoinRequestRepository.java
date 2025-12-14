package com.example.runningapp.group;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupJoinRequestRepository extends MongoRepository<GroupJoinRequest, String> {
    
    List<GroupJoinRequest> findByGroupIdAndStatus(String groupId, String status, Pageable pageable);
    
    boolean existsByGroupIdAndUserIdAndStatus(String groupId, String userId, String status);
    
    GroupJoinRequest findByGroupIdAndUserId(String groupId, String userId);
    
    void deleteByGroupIdAndUserId(String groupId, String userId);
    
    long countByGroupIdAndStatus(String groupId, String status);
    
    void deleteByGroupId(String groupId);
}
