package com.example.runmapproapp.data.api;

import com.example.runmapproapp.data.model.CreateGroupRequest;
import com.example.runmapproapp.data.model.Group;
import com.example.runmapproapp.data.model.GroupJoinRequest;
import com.example.runmapproapp.data.model.GroupMember;
import com.example.runmapproapp.data.model.GroupPost;
import com.example.runmapproapp.data.model.Post;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GroupApi {
    
    // Create a new group
    @POST("/api/groups")
    Call<Group> createGroup(@Body CreateGroupRequest request);
    
    // Get group by ID
    @GET("/api/groups/{groupId}")
    Call<Group> getGroup(@Path("groupId") String groupId);
    
    // Update group
    @PUT("/api/groups/{groupId}")
    Call<Group> updateGroup(
        @Path("groupId") String groupId,
        @Body CreateGroupRequest request
    );
    
    // Delete group (owner only)
    @DELETE("/api/groups/{groupId}")
    Call<Map<String, String>> deleteGroup(@Path("groupId") String groupId);
    
    // Join a group
    @POST("/api/groups/{groupId}/join")
    Call<Void> joinGroup(@Path("groupId") String groupId);
    
    // Leave a group
    @POST("/api/groups/{groupId}/leave")
    Call<Void> leaveGroup(@Path("groupId") String groupId);
    
    // Get user's groups
    @GET("/api/groups/my-groups")
    Call<List<Group>> getMyGroups(
        @Query("page") int page,
        @Query("size") int size
    );
    
    // Get all public groups
    @GET("/api/groups/public")
    Call<List<Group>> getPublicGroups(
        @Query("page") int page,
        @Query("size") int size
    );
    
    // Get group posts (feed)
    @GET("/api/groups/{groupId}/posts")
    Call<List<Post>> getGroupPosts(
        @Path("groupId") String groupId,
        @Query("page") int page,
        @Query("size") int size
    );
    
    // Search groups
    @GET("/api/groups/search")
    Call<List<Group>> searchGroups(
        @Query("query") String query,
        @Query("page") int page,
        @Query("size") int size
    );
    
    // Request to join private group with invite code
    @POST("/api/groups/{groupId}/request-join")
    Call<Map<String, Object>> requestJoinGroup(
        @Path("groupId") String groupId,
        @Body Map<String, String> body
    );
    
    // Join by invite code only
    @POST("/api/groups/join-by-code")
    Call<Map<String, Object>> joinByInviteCode(
        @Body Map<String, String> body
    );
    
    // Admin: Update group settings
    @PUT("/api/groups/{groupId}/settings")
    Call<Group> updateGroupSettings(
        @Path("groupId") String groupId,
        @Body Map<String, Object> settings
    );
    
    // Admin: Update member role
    @PUT("/api/groups/{groupId}/members/{userId}/role")
    Call<Map<String, String>> updateMemberRole(
        @Path("groupId") String groupId,
        @Path("userId") String userId,
        @Body Map<String, String> body
    );
    
    // Admin: Remove member
    @DELETE("/api/groups/{groupId}/members/{userId}")
    Call<Map<String, String>> removeMember(
        @Path("groupId") String groupId,
        @Path("userId") String userId
    );
    
    // Admin: Get pending join requests
    @GET("/api/groups/{groupId}/join-requests")
    Call<List<GroupJoinRequest>> getPendingJoinRequests(
        @Path("groupId") String groupId,
        @Query("page") int page,
        @Query("size") int size
    );
    
    // Admin: Approve join request
    @POST("/api/groups/join-requests/{requestId}/approve")
    Call<Map<String, String>> approveJoinRequest(@Path("requestId") String requestId);
    
    // Admin: Reject join request
    @POST("/api/groups/join-requests/{requestId}/reject")
    Call<Map<String, String>> rejectJoinRequest(@Path("requestId") String requestId);
    
    // Create group post
    @POST("/api/groups/{groupId}/group-posts")
    Call<GroupPost> createGroupPost(
        @Path("groupId") String groupId,
        @Body Map<String, Object> body
    );
    
    // Get group posts
    @GET("/api/groups/{groupId}/group-posts")
    Call<List<GroupPost>> getGroupPostList(
        @Path("groupId") String groupId,
        @Query("page") int page,
        @Query("size") int size
    );
    
    // Admin: Get pending posts
    @GET("/api/groups/{groupId}/group-posts/pending")
    Call<List<GroupPost>> getPendingPosts(
        @Path("groupId") String groupId,
        @Query("page") int page,
        @Query("size") int size
    );
    
    // Admin: Approve post
    @POST("/api/groups/group-posts/{postId}/approve")
    Call<Map<String, String>> approvePost(@Path("postId") String postId);
    
    // Admin: Reject post
    @POST("/api/groups/group-posts/{postId}/reject")
    Call<Map<String, String>> rejectPost(@Path("postId") String postId);
    
    // Get group members
    @GET("/api/groups/{groupId}/members")
    Call<List<GroupMember>> getGroupMembers(
        @Path("groupId") String groupId,
        @Query("page") int page,
        @Query("size") int size
    );
    
    // Get single group post detail
    @GET("/api/groups/group-posts/{postId}")
    Call<GroupPost> getGroupPost(@Path("postId") String postId);
    
    // Get comments for group post
    @GET("/api/groups/group-posts/{postId}/comments")
    Call<List<com.example.runmapproapp.data.model.Comment>> getGroupPostComments(
        @Path("postId") String postId,
        @Query("page") int page,
        @Query("size") int size
    );
    
    // Add comment to group post
    @POST("/api/groups/group-posts/{postId}/comments")
    Call<com.example.runmapproapp.data.model.Comment> addGroupPostComment(
        @Path("postId") String postId,
        @Body Map<String, String> request
    );
    
    // Like group post
    @POST("/api/groups/group-posts/{postId}/like")
    Call<GroupPost> likeGroupPost(@Path("postId") String postId);
    
    // Unlike group post
    @POST("/api/groups/group-posts/{postId}/unlike")
    Call<GroupPost> unlikeGroupPost(@Path("postId") String postId);
    
    // Delete group post
    @DELETE("/api/groups/group-posts/{postId}")
    Call<Map<String, String>> deleteGroupPost(@Path("postId") String postId);
}

