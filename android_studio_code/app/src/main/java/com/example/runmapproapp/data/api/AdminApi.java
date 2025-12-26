package com.example.runmapproapp.data.api;

import com.example.runmapproapp.data.model.User;
import com.example.runmapproapp.data.response.MessageResponse;
import com.example.runmapproapp.data.response.StatisticsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Retrofit API interface for Admin operations
 * All endpoints require Authorization header with admin role
 */
public interface AdminApi {
    
    // ==================== USER MANAGEMENT ====================
    
    /**
     * Get all users in the system
     * @return List of all users
     */
    @GET("/api/admin/users")
    Call<List<User>> getAllUsers();
    
    /**
     * Get specific user details
     * @param userId User ID
     * @return User details
     */
    @GET("/api/admin/users/{userId}")
    Call<User> getUserDetails(@Path("userId") String userId);
    
    /**
     * Ban a user account
     * @param userId User ID to ban
     * @return Success message
     */
    @PUT("/api/admin/users/{userId}/ban")
    Call<MessageResponse> banUser(@Path("userId") String userId);
    
    /**
     * Unban a user account
     * @param userId User ID to unban
     * @return Success message
     */
    @PUT("/api/admin/users/{userId}/unban")
    Call<MessageResponse> unbanUser(@Path("userId") String userId);
    
    // ==================== CONTENT MODERATION ====================
    
    /**
     * Delete any user's run
     * @param runId Run ID to delete
     * @return Success message
     */
    @DELETE("/api/admin/runs/{runId}")
    Call<MessageResponse> deleteRun(@Path("runId") String runId);
    
    /**
     * Get all runs in the system
     * @return List of all runs
     */
    @GET("/api/admin/runs")
    Call<List<com.example.runmapproapp.dto.RunResponse>> getAllRuns();
    
    /**
     * Delete any post (automatically deletes likes, comments)
     * @param postId Post ID to delete
     * @return Success message
     */
    @DELETE("/api/admin/posts/{postId}")
    Call<MessageResponse> deletePost(@Path("postId") String postId);
    
    /**
     * Delete any comment (automatically deletes child comments, likes)
     * @param commentId Comment ID to delete
     * @return Success message
     */
    @DELETE("/api/admin/comments/{commentId}")
    Call<MessageResponse> deleteComment(@Path("commentId") String commentId);
    
    /**
     * Delete any group (automatically deletes members, posts)
     * @param groupId Group ID to delete
     * @return Success message
     */
    @DELETE("/api/admin/groups/{groupId}")
    Call<MessageResponse> deleteGroup(@Path("groupId") String groupId);
    
    /**
     * Get all groups in the system (both public and private)
     * @return List of all groups
     */
    @GET("/api/admin/groups")
    Call<List<com.example.runmapproapp.data.model.Group>> getAllGroups();
    
    // ==================== STATISTICS ====================
    
    /**
     * Get app-wide statistics
     * @return Statistics including total users, posts, groups, runs
     */
    @GET("/api/admin/statistics")
    Call<StatisticsResponse> getStatistics();
}
