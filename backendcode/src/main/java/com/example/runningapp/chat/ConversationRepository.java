package com.example.runningapp.chat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    @Query("{ 'type': ?0, 'members': { $all: ?1 } }")
    Optional<Conversation> findByTypeAndMembers(String type, List<String> members);

    List<Conversation> findByMembersContaining(String memberId, Sort sort);
}
