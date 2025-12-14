package com.example.runningapp.social;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostLikeRepository extends MongoRepository<PostLike, String> {

    Optional<PostLike> findByPostIdAndUserId(String postId, String userId);

    List<PostLike> findByPostIdInAndUserId(List<String> postIds, String userId);

    long countByPostId(String postId);
}
