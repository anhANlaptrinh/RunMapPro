package com.example.runningapp.group;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupMemberRepository extends MongoRepository<GroupMember, String> {

    Optional<GroupMember> findByGroupIdAndUserId(String groupId, String userId);

    boolean existsByGroupIdAndUserId(String groupId, String userId);

    long countByGroupId(String groupId);

    List<GroupMember> findByGroupId(String groupId, Pageable pageable);

    List<GroupMember> findByUserId(String userId);
    
    void deleteByGroupId(String groupId);
}
