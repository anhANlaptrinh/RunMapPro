package com.example.runningapp.group;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupRepository extends MongoRepository<Group, String> {

    Optional<Group> findByIdAndBlockedFalse(String id);

    List<Group> findByIdInAndBlockedFalse(List<String> ids, Pageable pageable);
    
    Optional<Group> findByInviteCode(String inviteCode);
    
    List<Group> findByPrivacyAndBlockedFalse(String privacy, Pageable pageable);
}
