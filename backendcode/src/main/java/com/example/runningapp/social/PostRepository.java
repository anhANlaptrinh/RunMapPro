package com.example.runningapp.social;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<Post, String> {

    Page<Post> findByVisibilityInAndDeletedFalseAndBlockedFalse(List<String> visibility, Pageable pageable);

    Page<Post> findByAuthorIdAndDeletedFalseAndBlockedFalse(String authorId, Pageable pageable);

    List<Post> findByGroupIdAndDeletedFalseAndBlockedFalseOrderByCreatedAtDesc(String groupId);
}
