package com.example.runningapp.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.runningapp.group.Group;
import com.example.runningapp.user.User;

/**
 * Admin controller for managing the entire application
 * All endpoints require ADMIN role
 * 
 * Available endpoints:
 * - User management: ban/unban users
 * - Content moderation: delete posts, comments, groups, runs
 * - Statistics: get app statistics
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ============ User Management ============
    
    /**
     * Ban a user account
     * PUT /api/admin/users/{userId}/ban
     */
    @PutMapping("/users/{userId}/ban")
    public ResponseEntity<Map<String, String>> banUser(@PathVariable String userId) {
        adminService.banUser(userId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "User has been banned"
        ));
    }

    /**
     * Unban a user account
     * PUT /api/admin/users/{userId}/unban
     */
    @PutMapping("/users/{userId}/unban")
    public ResponseEntity<Map<String, String>> unbanUser(@PathVariable String userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "User has been unbanned"
        ));
    }

    /**
     * Get user details
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserDetails(@PathVariable String userId) {
        return ResponseEntity.ok(adminService.getUserDetails(userId));
    }

    /**
     * Get all users
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // ============ Content Moderation ============

    /**
     * Delete any user's run
     * DELETE /api/admin/runs/{runId}
     */
    @DeleteMapping("/runs/{runId}")
    public ResponseEntity<Map<String, String>> deleteRun(@PathVariable String runId) {
        adminService.deleteRun(runId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Run has been deleted"
        ));
    }

    /**
     * Delete any post
     * DELETE /api/admin/posts/{postId}
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable String postId) {
        adminService.deletePost(postId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Post has been deleted"
        ));
    }

    /**
     * Delete any comment
     * DELETE /api/admin/comments/{commentId}
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable String commentId) {
        adminService.deleteComment(commentId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Comment has been deleted"
        ));
    }

    /**
     * Get all groups
     * GET /api/admin/groups
     */
    @GetMapping("/groups")
    public ResponseEntity<List<Group>> getAllGroups() {
        return ResponseEntity.ok(adminService.getAllGroups());
    }

    /**
     * Delete any group
     * DELETE /api/admin/groups/{groupId}
     */
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<Map<String, String>> deleteGroup(@PathVariable String groupId) {
        adminService.deleteGroup(groupId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Group has been deleted"
        ));
    }

    // ============ Statistics ============

    /**
     * Get application statistics
     * GET /api/admin/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(adminService.getStatistics());
    }
}
