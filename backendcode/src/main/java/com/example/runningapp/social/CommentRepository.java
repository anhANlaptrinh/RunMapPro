package com.example.runningapp.social;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, String> {

    List<Comment> findByPostIdAndDeletedFalseOrderByCreatedAtAsc(String postId);
    
    Page<Comment> findByPostIdAndDeletedFalse(String postId, Pageable pageable);
}
