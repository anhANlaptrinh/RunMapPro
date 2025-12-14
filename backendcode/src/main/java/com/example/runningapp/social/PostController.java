package com.example.runningapp.social;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.runningapp.social.dto.CreateCommentRequest;
import com.example.runningapp.social.dto.CreatePostRequest;
import com.example.runningapp.social.dto.SharePostRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts")
@Validated
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }

    @GetMapping("/feed")
    public ResponseEntity<List<Post>> feed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(postService.publicFeed(page, size));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable String postId) {
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> userPosts(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(postService.userPosts(userId, page, size));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Post> like(@PathVariable String postId) {
        return ResponseEntity.ok(postService.likePost(postId));
    }

    @PostMapping("/{postId}/unlike")
    public ResponseEntity<Post> unlike(@PathVariable String postId) {
        return ResponseEntity.ok(postService.unlikePost(postId));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> comment(@PathVariable String postId,
            @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(postService.addComment(postId, request));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<Comment>> comments(
            @PathVariable String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(postService.getComments(postId, page, size));
    }

    @PostMapping("/{postId}/share")
    public ResponseEntity<Post> share(@PathVariable String postId,
            @RequestBody SharePostRequest request) {
        return ResponseEntity.ok(postService.sharePost(postId, request));
    }
    
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Comment> likeComment(@PathVariable String commentId) {
        return ResponseEntity.ok(postService.likeComment(commentId));
    }
    
    @PostMapping("/comments/{commentId}/unlike")
    public ResponseEntity<Comment> unlikeComment(@PathVariable String commentId) {
        return ResponseEntity.ok(postService.unlikeComment(commentId));
    }
    
    @PutMapping("/{postId}")
    public ResponseEntity<Post> updatePost(@PathVariable String postId,
            @Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.updatePost(postId, request));
    }
    
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable String postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Comment> updateComment(@PathVariable String commentId,
            @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(postService.updateComment(commentId, request));
    }
    
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
        postService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
