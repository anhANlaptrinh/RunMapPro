package com.example.runningapp.admin;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.runningapp.common.exception.NotFoundException;
import com.example.runningapp.group.Group;
import com.example.runningapp.group.GroupMemberRepository;
import com.example.runningapp.group.GroupPostRepository;
import com.example.runningapp.group.GroupRepository;
import com.example.runningapp.run.repository.RunRepository;
import com.example.runningapp.social.Comment;
import com.example.runningapp.social.CommentLikeRepository;
import com.example.runningapp.social.CommentRepository;
import com.example.runningapp.social.Post;
import com.example.runningapp.social.PostLikeRepository;
import com.example.runningapp.social.PostRepository;
import com.example.runningapp.user.User;
import com.example.runningapp.user.UserRepository;

/**
 * Admin service for managing users, posts, comments, groups, and runs
 * All methods in this service require ADMIN role authorization at controller level
 */
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final RunRepository runRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupPostRepository groupPostRepository;

    public AdminService(
            UserRepository userRepository,
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            CommentRepository commentRepository,
            CommentLikeRepository commentLikeRepository,
            RunRepository runRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            GroupPostRepository groupPostRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.runRepository = runRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupPostRepository = groupPostRepository;
    }

    /**
     * Ban a user account - prevents login
     * @param userId The user ID to ban
     */
    public void banUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        user.setBanned(true);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    /**
     * Unban a user account - allows login again
     * @param userId The user ID to unban
     */
    public void unbanUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        user.setBanned(false);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    /**
     * Admin delete any user's run
     * @param runId The run ID to delete
     */
    public void deleteRun(String runId) {
        if (!runRepository.existsById(runId)) {
            throw new NotFoundException("Run not found");
        }
        runRepository.deleteById(runId);
    }

    /**
     * Admin delete any post (including all associated data)
     * @param postId The post ID to delete
     */
    @Transactional
    public void deletePost(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        // Delete all likes for this post
        postLikeRepository.findAll().stream()
                .filter(like -> like.getPostId().equals(postId))
                .forEach(postLikeRepository::delete);
        
        // Delete all comments and their likes for this post
        commentRepository.findAll().stream()
                .filter(comment -> postId.equals(comment.getPostId()))
                .forEach(comment -> {
                    // Delete likes for each comment
                    commentLikeRepository.findAll().stream()
                            .filter(like -> like.getCommentId().equals(comment.getId()))
                            .forEach(commentLikeRepository::delete);
                    // Delete the comment
                    commentRepository.delete(comment);
                });
        
        // Delete the post
        postRepository.delete(post);
    }

    /**
     * Admin delete any comment (including all associated data)
     * @param commentId The comment ID to delete
     */
    @Transactional
    public void deleteComment(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        
        // Delete all likes for this comment
        commentLikeRepository.findAll().stream()
                .filter(like -> like.getCommentId().equals(commentId))
                .forEach(commentLikeRepository::delete);
        
        // Delete all child comments recursively (find by parentCommentId)
        commentRepository.findAll().stream()
                .filter(c -> commentId.equals(c.getParentCommentId()))
                .forEach(childComment -> deleteComment(childComment.getId()));
        
        // Decrease comment count in post
        postRepository.findById(comment.getPostId()).ifPresent(post -> {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postRepository.save(post);
        });
        
        // Delete the comment
        commentRepository.delete(comment);
    }

    /**
     * Admin delete any group (including all associated data)
     * @param groupId The group ID to delete
     */
    @Transactional
    public void deleteGroup(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        // Delete all group members
        groupMemberRepository.deleteByGroupId(groupId);
        
        // Delete all group posts and their comments
        groupPostRepository.findAll().stream()
                .filter(groupPost -> groupId.equals(groupPost.getGroupId()))
                .forEach(groupPost -> {
                    // Delete comments for each group post
                    commentRepository.findAll().stream()
                            .filter(comment -> groupPost.getId().equals(comment.getPostId()))
                            .forEach(commentRepository::delete);
                });
        
        // Delete all group posts
        groupPostRepository.deleteByGroupId(groupId);
        
        // Delete the group
        groupRepository.delete(group);
    }

    /**
     * Get user details by ID (for admin dashboard)
     * @param userId The user ID
     * @return User entity
     */
    public User getUserDetails(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    /**
     * Get all users (paginated would be better in production)
     * @return List of all users
     */
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get all groups (both public and private)
     * @return List of all groups
     */
    public java.util.List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    /**
     * Get statistics for admin dashboard
     * @return Map of statistics
     */
    public java.util.Map<String, Object> getStatistics() {
        long totalUsers = userRepository.count();
        long bannedUsers = userRepository.findAll().stream()
                .filter(user -> user.getBanned() != null && user.getBanned())
                .count();
        long totalPosts = postRepository.count();
        long totalGroups = groupRepository.count();
        long totalRuns = runRepository.count();
        
        return java.util.Map.of(
                "totalUsers", totalUsers,
                "bannedUsers", bannedUsers,
                "activeUsers", totalUsers - bannedUsers,
                "totalPosts", totalPosts,
                "totalGroups", totalGroups,
                "totalRuns", totalRuns
        );
    }
}
