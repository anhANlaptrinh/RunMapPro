package com.example.runmapproapp.data.api;

import com.example.runmapproapp.data.model.Comment;
import com.example.runmapproapp.data.model.CreateCommentRequest;
import com.example.runmapproapp.data.model.CreatePostRequest;
import com.example.runmapproapp.data.model.Post;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PostApi {
    
    // Create a new post
    @POST("/api/posts")
    Call<Post> createPost(@Body CreatePostRequest request);
    
    // Get feed (paginated)
    @GET("/api/posts/feed")
    Call<List<Post>> getFeed(
        @Query("page") int page,
        @Query("size") int size
    );
    
    // Get user's posts
    @GET("/api/posts/user/{userId}")
    Call<List<Post>> getUserPosts(
        @Path("userId") String userId,
        @Query("page") int page,
        @Query("size") int size
    );
    
    // Get single post
    @GET("/api/posts/{postId}")
    Call<Post> getPost(@Path("postId") String postId);
    
    // Update post
    @PUT("/api/posts/{postId}")
    Call<Post> updatePost(
        @Path("postId") String postId,
        @Body CreatePostRequest request
    );
    
    // Delete post
    @DELETE("/api/posts/{postId}")
    Call<Void> deletePost(@Path("postId") String postId);
    
    // Like a post
    @POST("/api/posts/{postId}/like")
    Call<Post> likePost(@Path("postId") String postId);
    
    // Unlike a post
    @POST("/api/posts/{postId}/unlike")
    Call<Post> unlikePost(@Path("postId") String postId);
    
    // Add comment to post
    @POST("/api/posts/{postId}/comments")
    Call<Comment> addComment(
        @Path("postId") String postId,
        @Body CreateCommentRequest request
    );
    
    // Get comments for a post
    @GET("/api/posts/{postId}/comments")
    Call<List<Comment>> getComments(
        @Path("postId") String postId,
        @Query("page") int page,
        @Query("size") int size
    );
    
    // Share a post
    @POST("/api/posts/{postId}/share")
    Call<Post> sharePost(
        @Path("postId") String postId,
        @Body CreatePostRequest request
    );
    
    // Like a comment
    @POST("/api/posts/comments/{commentId}/like")
    Call<Comment> likeComment(@Path("commentId") String commentId);
    
    // Unlike a comment
    @POST("/api/posts/comments/{commentId}/unlike")
    Call<Comment> unlikeComment(@Path("commentId") String commentId);
    
    // Update comment
    @PUT("/api/posts/comments/{commentId}")
    Call<Comment> updateComment(
        @Path("commentId") String commentId,
        @Body CreateCommentRequest request
    );
    
    // Delete comment
    @DELETE("/api/posts/comments/{commentId}")
    Call<Void> deleteComment(@Path("commentId") String commentId);
}
