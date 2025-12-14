package com.example.runningapp.group;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupPostRepository extends MongoRepository<GroupPost, String> {
    
    List<GroupPost> findByGroupIdAndStatus(String groupId, String status, Pageable pageable);
    
    List<GroupPost> findByGroupId(String groupId, Pageable pageable);
    
    long countByGroupIdAndStatus(String groupId, String status);
    
    List<GroupPost> findByUserIdAndGroupId(String userId, String groupId, Pageable pageable);
    
    void deleteByGroupId(String groupId);
}
