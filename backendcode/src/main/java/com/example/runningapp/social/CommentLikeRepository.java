package com.example.runningapp.social;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends MongoRepository<CommentLike, String> {
    
    Optional<CommentLike> findByCommentIdAndUserId(String commentId, String userId);
    
    void deleteByCommentIdAndUserId(String commentId, String userId);
    
    long countByCommentId(String commentId);
    
    List<CommentLike> findByCommentIdInAndUserId(List<String> commentIds, String userId);
}
