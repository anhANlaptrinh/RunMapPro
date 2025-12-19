package com.example.runmapproapp.ui.social;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.PostApi;
import com.example.runmapproapp.data.model.Comment;
import com.example.runmapproapp.data.model.CreateCommentRequest;
import com.example.runmapproapp.data.model.Post;
import com.example.runmapproapp.ui.profile.UserProfileActivity;
import com.example.runmapproapp.ui.social.adapter.CommentAdapter;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailActivity extends AppCompatActivity implements CommentAdapter.OnCommentInteractionListener {

    private Post post;
    private PostApi postApi;
    private com.example.runmapproapp.data.api.GroupApi groupApi;
    private CommentAdapter commentAdapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerViewComments;
    private EditText etComment;
    private Button btnSendComment;
    private String parentCommentId = null; // For reply functionality
    private boolean isGroupPost = false; // Flag to indicate if this is a group post

    // Post views
    private ImageView ivAuthorAvatar;
    private TextView tvAuthorName;
    private TextView tvTimestamp;
    private TextView tvContent;
    private ImageView ivPostImage;
    private MaterialCardView cardOriginalPost;
    private TextView tvOriginalAuthor;
    private TextView tvOriginalContent;
    private ImageView ivOriginalPostImage;
    private ImageButton btnLike;
    private ImageButton btnComment;
    private ImageButton btnShare;
    private ImageButton btnMenu;
    private TextView tvLikeCount;
    private TextView tvCommentCount;
    private TextView tvShareCount;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String postId = getIntent().getStringExtra("POST_ID");
        isGroupPost = getIntent().getBooleanExtra("IS_GROUP_POST", false);
        
        if (postId == null) {
            Toast.makeText(this, "Invalid post", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();

        postApi = ApiClient.getPostApi();
        groupApi = ApiClient.getGroupApi();

        loadPost(postId);
        loadComments(postId);
        setupRecyclerView();
        
        postApi = ApiClient.getPostApi();
        loadPost(postId);
        loadComments(postId);
    }

    private void initViews() {
        // Post views from included layout
        View postContent = findViewById(R.id.postContent);
        ivAuthorAvatar = postContent.findViewById(R.id.ivAuthorAvatar);
        tvAuthorName = postContent.findViewById(R.id.tvAuthorName);
        tvTimestamp = postContent.findViewById(R.id.tvTimestamp);
        tvContent = postContent.findViewById(R.id.tvContent);
        ivPostImage = postContent.findViewById(R.id.ivPostImage);
        cardOriginalPost = postContent.findViewById(R.id.cardOriginalPost);
        tvOriginalAuthor = postContent.findViewById(R.id.tvOriginalAuthor);
        tvOriginalContent = postContent.findViewById(R.id.tvOriginalContent);
        ivOriginalPostImage = postContent.findViewById(R.id.ivOriginalPostImage);
        btnLike = postContent.findViewById(R.id.btnLike);
        btnComment = postContent.findViewById(R.id.btnComment);
        btnShare = postContent.findViewById(R.id.btnShare);
        btnMenu = postContent.findViewById(R.id.btnMenu);
        tvLikeCount = postContent.findViewById(R.id.tvLikeCount);
        tvCommentCount = postContent.findViewById(R.id.tvCommentCount);
        tvShareCount = postContent.findViewById(R.id.tvShareCount);

        // Comment views
        progressBar = findViewById(R.id.progressBar);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);

        btnSendComment.setOnClickListener(v -> postComment());
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter(new ArrayList<>(), this);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);
    }

    private void loadPost(String postId) {
        if (isGroupPost) {
            // Load group post
            groupApi.getGroupPost(postId).enqueue(new Callback<com.example.runmapproapp.data.model.GroupPost>() {
                @Override
                public void onResponse(@NonNull Call<com.example.runmapproapp.data.model.GroupPost> call, @NonNull Response<com.example.runmapproapp.data.model.GroupPost> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        com.example.runmapproapp.data.model.GroupPost groupPost = response.body();
                        // Convert GroupPost to Post for display
                        post = convertGroupPostToPost(groupPost);
                        displayPost();
                    } else {
                        Toast.makeText(PostDetailActivity.this, "Failed to load post", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<com.example.runmapproapp.data.model.GroupPost> call, @NonNull Throwable t) {
                    Toast.makeText(PostDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Load regular post
            postApi.getPost(postId).enqueue(new Callback<Post>() {
                @Override
                public void onResponse(@NonNull Call<Post> call, @NonNull Response<Post> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        post = response.body();
                        displayPost();
                    } else {
                        Toast.makeText(PostDetailActivity.this, "Failed to load post", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                    Toast.makeText(PostDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private Post convertGroupPostToPost(com.example.runmapproapp.data.model.GroupPost groupPost) {
        Post post = new Post();
        post.setId(groupPost.getId());
        post.setAuthorId(groupPost.getUserId());
        post.setAuthorName(groupPost.getAuthorName());
        
        // Convert avatar mediaId to full URL
        if (groupPost.getAuthorAvatar() != null && !groupPost.getAuthorAvatar().isEmpty()) {
            String avatarUrl = "http://10.0.2.2:8080/api/media/" + groupPost.getAuthorAvatar();
            post.setAuthorAvatar(avatarUrl);
        }
        
        post.setContentText(groupPost.getContent());
        post.setMediaIds(groupPost.getMediaUrls());
        post.setRunId(groupPost.getRunId());
        
        // Set createdAt directly (both are String type)
        post.setCreatedAt(groupPost.getCreatedAt());
        
        post.setLikeCount((int) groupPost.getLikeCount());
        post.setCommentCount((int) groupPost.getCommentCount());
        post.setShareCount(0); // GroupPosts don't have share count
        post.setLikedByCurrentUser(groupPost.isLikedByCurrentUser()); // Use actual liked status from backend
        
        return post;
    }

    private void displayPost() {
        tvAuthorName.setText(post.getAuthorName());
        tvContent.setText(post.getContentText());
        
        if (post.getCreatedAt() != null) {
            tvTimestamp.setText(dateFormat.format(post.getCreatedAt()));
        }

        if (post.getAuthorAvatar() != null && !post.getAuthorAvatar().isEmpty()) {
            String avatarUrl = post.getAuthorAvatar();
            // Convert relative path to full URL
            if (avatarUrl.startsWith("/api/")) {
                avatarUrl = "http://10.0.2.2:8080" + avatarUrl;
            }
            Glide.with(this).load(avatarUrl).into(ivAuthorAvatar);
        }

        // Post image (only for non-shared posts)
        if (post.getMediaIds() != null && !post.getMediaIds().isEmpty()) {
            ivPostImage.setVisibility(View.VISIBLE);
            String mediaUrl = "http://10.0.2.2:8080/api/media/" + post.getMediaIds().get(0);
            android.util.Log.d("PostDetailActivity", "Loading post image: " + mediaUrl);
            Glide.with(this)
                    .load(mediaUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_person)
                    .into(ivPostImage);
        } else {
            ivPostImage.setVisibility(View.GONE);
        }

        // Run card (only for non-shared posts)
        View runCardLayout = findViewById(R.id.runCardLayout);
        if (post.getRunId() != null && !post.getRunId().isEmpty() && runCardLayout != null) {
            runCardLayout.setVisibility(View.VISIBLE);
            loadAndDisplayRun(post.getRunId(), runCardLayout);
            
            // Add click listener to open run detail
            final String runId = post.getRunId();
            runCardLayout.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.example.runmapproapp.ui.run.RunDetailActivity.class);
                intent.putExtra(com.example.runmapproapp.ui.run.RunDetailActivity.EXTRA_RUN_ID, runId);
                startActivity(intent);
            });
        } else if (runCardLayout != null) {
            runCardLayout.setVisibility(View.GONE);
        }

        // Original post for shared posts
        if (post.getOriginalPost() != null) {
            cardOriginalPost.setVisibility(View.VISIBLE);
            Post originalPost = post.getOriginalPost();
            tvOriginalAuthor.setText(originalPost.getAuthorName());
            tvOriginalContent.setText(originalPost.getContentText());
            
            // Load original post image if available
            View cardOriginalImage = findViewById(R.id.cardOriginalImage);
            if (originalPost.getMediaIds() != null && !originalPost.getMediaIds().isEmpty()) {
                if (cardOriginalImage != null) {
                    cardOriginalImage.setVisibility(View.VISIBLE);
                }
                String originalMediaUrl = "http://10.0.2.2:8080/api/media/" + originalPost.getMediaIds().get(0);
                Glide.with(this)
                        .load(originalMediaUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_person)
                        .into(ivOriginalPostImage);
            } else {
                if (cardOriginalImage != null) {
                    cardOriginalImage.setVisibility(View.GONE);
                }
            }

            // Show minimap if original post has runId
            View originalRunCardLayout = findViewById(R.id.originalRunCardLayout);
            if (originalPost.getRunId() != null && !originalPost.getRunId().isEmpty() && originalRunCardLayout != null) {
                originalRunCardLayout.setVisibility(View.VISIBLE);
                loadOriginalRunData(originalPost.getRunId(), originalRunCardLayout);
                
                // Add click listener to open run detail
                final String originalRunId = originalPost.getRunId();
                originalRunCardLayout.setOnClickListener(v -> {
                    Intent intent = new Intent(this, com.example.runmapproapp.ui.run.RunDetailActivity.class);
                    intent.putExtra(com.example.runmapproapp.ui.run.RunDetailActivity.EXTRA_RUN_ID, originalRunId);
                    startActivity(intent);
                });
            } else if (originalRunCardLayout != null) {
                originalRunCardLayout.setVisibility(View.GONE);
            }
            
            // Click listener for original post - open original post detail
            cardOriginalPost.setOnClickListener(v -> {
                Intent intent = new Intent(this, PostDetailActivity.class);
                intent.putExtra("POST_ID", originalPost.getId());
                startActivity(intent);
            });
        } else {
            cardOriginalPost.setVisibility(View.GONE);
        }

        // Stats
        tvLikeCount.setText(String.valueOf(post.getLikeCount()));
        tvCommentCount.setText(String.valueOf(post.getCommentCount()));
        tvShareCount.setText(String.valueOf(post.getShareCount()));

        // Like button state (use post.isLikedByCurrentUser for both regular and group posts)
        if (post.isLikedByCurrentUser()) {
            btnLike.setImageResource(R.drawable.ic_favorite_filled);
            btnLike.setColorFilter(getResources().getColor(R.color.pink, null));
        } else {
            btnLike.setImageResource(R.drawable.ic_favorite_border);
            btnLike.setColorFilter(getResources().getColor(android.R.color.darker_gray, null));
        }

        // Click listeners
        ivAuthorAvatar.setOnClickListener(v -> openUserProfile(post.getAuthorId()));
        tvAuthorName.setOnClickListener(v -> openUserProfile(post.getAuthorId()));
        btnLike.setOnClickListener(v -> toggleLike());
        btnShare.setOnClickListener(v -> sharePost());
        
        // Show menu button and add delete functionality
        if (btnMenu != null) {
            btnMenu.setVisibility(View.VISIBLE);
            btnMenu.setOnClickListener(v -> showPostMenu());
        }
    }

    private void loadAndDisplayRun(String runId, View runCardLayout) {
        com.example.runmapproapp.api.RunApiService runApiService = 
                com.example.runmapproapp.api.RetrofitClient.getRunApiService();
        
        runApiService.getRun(runId).enqueue(new Callback<com.example.runmapproapp.dto.RunResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.example.runmapproapp.dto.RunResponse> call, 
                                 @NonNull Response<com.example.runmapproapp.dto.RunResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayRunData(response.body(), runCardLayout);
                } else {
                    android.util.Log.e("PostDetailActivity", "Failed to load run " + runId);
                    if (runCardLayout != null) {
                        runCardLayout.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.example.runmapproapp.dto.RunResponse> call, @NonNull Throwable t) {
                android.util.Log.e("PostDetailActivity", "Error loading run " + runId, t);
                if (runCardLayout != null) {
                    runCardLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void displayRunData(com.example.runmapproapp.dto.RunResponse run, View runCardLayout) {
        if (runCardLayout == null) return;

        // Find views in runCardLayout
        TextView tvRunDate = runCardLayout.findViewById(R.id.tvRunDate);
        TextView tvRunDistance = runCardLayout.findViewById(R.id.tvRunDistance);
        TextView tvRunDuration = runCardLayout.findViewById(R.id.tvRunDuration);
        TextView tvRunPace = runCardLayout.findViewById(R.id.tvRunPace);
        com.mapbox.maps.MapView mapViewRun = runCardLayout.findViewById(R.id.mapViewRun);

        // Display run stats
        tvRunDate.setText(com.example.runmapproapp.utils.FormatUtils.formatDate(run.getStartTime()));
        tvRunDistance.setText(com.example.runmapproapp.utils.FormatUtils.formatDistance(run.getDistanceMeters()));
        tvRunDuration.setText(com.example.runmapproapp.utils.FormatUtils.formatDuration(run.getDurationMs()));
        tvRunPace.setText(com.example.runmapproapp.utils.FormatUtils.formatPace(run.getAvgPaceSecPerKm()));

        // Display map
        if (mapViewRun != null) {
            mapViewRun.getMapboxMap().loadStyleUri(com.mapbox.maps.Style.MAPBOX_STREETS, style -> {
                if (run.getPath() != null && run.getPath().getCoordinates() != null 
                        && run.getPath().getCoordinates().size() >= 2) {
                    
                    // Convert coordinates to Points
                    java.util.List<com.mapbox.geojson.Point> points = new java.util.ArrayList<>();
                    for (java.util.List<Double> coord : run.getPath().getCoordinates()) {
                        if (coord.size() >= 2) {
                            points.add(com.mapbox.geojson.Point.fromLngLat(coord.get(0), coord.get(1)));
                        }
                    }

                    if (points.size() >= 2) {
                        String sourceId = "run-source-" + run.getId();
                        String layerId = "run-layer-" + run.getId();
                        
                        // Remove existing if any
                        try {
                            style.removeStyleLayer(layerId);
                        } catch (Exception e) {}
                        try {
                            style.removeStyleSource(sourceId);
                        } catch (Exception e) {}
                        
                        // Create and display route
                        com.mapbox.geojson.LineString lineString = com.mapbox.geojson.LineString.fromLngLats(points);
                        com.mapbox.geojson.Feature feature = com.mapbox.geojson.Feature.fromGeometry(lineString);
                        
                        com.mapbox.maps.extension.style.sources.generated.GeoJsonSource source = 
                                new com.mapbox.maps.extension.style.sources.generated.GeoJsonSource.Builder(sourceId)
                                .feature(feature)
                                .build();
                        source.bindTo(style);
                        
                        com.mapbox.maps.extension.style.layers.generated.LineLayer lineLayer = 
                                new com.mapbox.maps.extension.style.layers.generated.LineLayer(layerId, sourceId);
                        lineLayer.lineColor("#1976D2");
                        lineLayer.lineWidth(4.0);
                        lineLayer.lineCap(com.mapbox.maps.extension.style.layers.properties.generated.LineCap.ROUND);
                        lineLayer.lineJoin(com.mapbox.maps.extension.style.layers.properties.generated.LineJoin.ROUND);
                        lineLayer.bindTo(style);
                        
                        // Center camera
                        com.mapbox.geojson.Point centerPoint = points.get(points.size() / 2);
                        mapViewRun.getMapboxMap().setCamera(
                                new com.mapbox.maps.CameraOptions.Builder()
                                        .center(centerPoint)
                                        .zoom(13.0)
                                        .build()
                        );
                    }
                }
            });
        }
    }
    private void loadOriginalRunData(String runId, View originalRunCardLayout) {
        com.example.runmapproapp.api.RunApiService runApiService = 
                com.example.runmapproapp.api.RetrofitClient.getRunApiService();
        
        runApiService.getRun(runId).enqueue(new Callback<com.example.runmapproapp.dto.RunResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.example.runmapproapp.dto.RunResponse> call, 
                                 @NonNull Response<com.example.runmapproapp.dto.RunResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayOriginalRunData(response.body(), originalRunCardLayout);
                } else {
                    android.util.Log.e("PostDetailActivity", "Failed to load original run " + runId);
                    if (originalRunCardLayout != null) {
                        originalRunCardLayout.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.example.runmapproapp.dto.RunResponse> call, @NonNull Throwable t) {
                android.util.Log.e("PostDetailActivity", "Error loading original run " + runId, t);
                if (originalRunCardLayout != null) {
                    originalRunCardLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void displayOriginalRunData(com.example.runmapproapp.dto.RunResponse run, View originalRunCardLayout) {
        if (originalRunCardLayout == null) return;

        // Find views in originalRunCardLayout
        TextView tvRunDate = originalRunCardLayout.findViewById(R.id.tvRunDate);
        TextView tvRunDistance = originalRunCardLayout.findViewById(R.id.tvRunDistance);
        TextView tvRunDuration = originalRunCardLayout.findViewById(R.id.tvRunDuration);
        TextView tvRunPace = originalRunCardLayout.findViewById(R.id.tvRunPace);
        com.mapbox.maps.MapView mapViewRun = originalRunCardLayout.findViewById(R.id.mapViewRun);

        // Display run stats
        tvRunDate.setText(com.example.runmapproapp.utils.FormatUtils.formatDate(run.getStartTime()));
        tvRunDistance.setText(com.example.runmapproapp.utils.FormatUtils.formatDistance(run.getDistanceMeters()));
        tvRunDuration.setText(com.example.runmapproapp.utils.FormatUtils.formatDuration(run.getDurationMs()));
        tvRunPace.setText(com.example.runmapproapp.utils.FormatUtils.formatPace(run.getAvgPaceSecPerKm()));

        // Display map
        if (mapViewRun != null) {
            mapViewRun.getMapboxMap().loadStyleUri(com.mapbox.maps.Style.MAPBOX_STREETS, style -> {
                if (run.getPath() != null && run.getPath().getCoordinates() != null 
                        && run.getPath().getCoordinates().size() >= 2) {
                    
                    // Convert coordinates to Points
                    java.util.List<com.mapbox.geojson.Point> points = new java.util.ArrayList<>();
                    for (java.util.List<Double> coord : run.getPath().getCoordinates()) {
                        if (coord.size() >= 2) {
                            points.add(com.mapbox.geojson.Point.fromLngLat(coord.get(0), coord.get(1)));
                        }
                    }

                    if (points.size() >= 2) {
                        String sourceId = "original-run-source-" + run.getId();
                        String layerId = "original-run-layer-" + run.getId();
                        
                        // Remove existing if any
                        try {
                            style.removeStyleLayer(layerId);
                        } catch (Exception e) {}
                        try {
                            style.removeStyleSource(sourceId);
                        } catch (Exception e) {}
                        
                        // Create and display route
                        com.mapbox.geojson.LineString lineString = com.mapbox.geojson.LineString.fromLngLats(points);
                        com.mapbox.geojson.Feature feature = com.mapbox.geojson.Feature.fromGeometry(lineString);
                        
                        com.mapbox.maps.extension.style.sources.generated.GeoJsonSource source = 
                                new com.mapbox.maps.extension.style.sources.generated.GeoJsonSource.Builder(sourceId)
                                .feature(feature)
                                .build();
                        source.bindTo(style);
                        
                        com.mapbox.maps.extension.style.layers.generated.LineLayer lineLayer = 
                                new com.mapbox.maps.extension.style.layers.generated.LineLayer(layerId, sourceId);
                        lineLayer.lineColor("#1976D2");
                        lineLayer.lineWidth(4.0);
                        lineLayer.lineCap(com.mapbox.maps.extension.style.layers.properties.generated.LineCap.ROUND);
                        lineLayer.lineJoin(com.mapbox.maps.extension.style.layers.properties.generated.LineJoin.ROUND);
                        lineLayer.bindTo(style);
                        
                        // Center camera
                        com.mapbox.geojson.Point centerPoint = points.get(points.size() / 2);
                        mapViewRun.getMapboxMap().setCamera(
                                new com.mapbox.maps.CameraOptions.Builder()
                                        .center(centerPoint)
                                        .zoom(13.0)
                                        .build()
                        );
                    }
                }
            });
        }
    }
    private void loadComments(String postId) {
        progressBar.setVisibility(View.VISIBLE);
        
        if (isGroupPost) {
            // Load comments from group post API
            groupApi.getGroupPostComments(postId, 0, 100).enqueue(new Callback<List<Comment>>() {
                @Override
                public void onResponse(@NonNull Call<List<Comment>> call, @NonNull Response<List<Comment>> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Comment> sortedComments = sortCommentsWithReplies(response.body());
                        commentAdapter.setComments(sortedComments);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Comment>> call, @NonNull Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PostDetailActivity.this, "Error loading comments", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Load comments from regular post API
            postApi.getComments(postId, 0, 100).enqueue(new Callback<List<Comment>>() {
                @Override
                public void onResponse(@NonNull Call<List<Comment>> call, @NonNull Response<List<Comment>> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Comment> sortedComments = sortCommentsWithReplies(response.body());
                        commentAdapter.setComments(sortedComments);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Comment>> call, @NonNull Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PostDetailActivity.this, "Error loading comments", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private List<Comment> sortCommentsWithReplies(List<Comment> comments) {
        List<Comment> result = new ArrayList<>();
        
        // First, add all parent comments (no parentCommentId)
        for (Comment comment : comments) {
            if (comment.getParentCommentId() == null) {
                result.add(comment);
                addRepliesRecursively(comments, comment.getId(), result);
            }
        }
        
        return result;
    }
    
    private void addRepliesRecursively(List<Comment> allComments, String parentId, List<Comment> result) {
        // Add all direct replies to this parent
        for (Comment comment : allComments) {
            if (comment.getParentCommentId() != null && 
                comment.getParentCommentId().equals(parentId)) {
                result.add(comment);
                // Recursively add replies to this comment
                addRepliesRecursively(allComments, comment.getId(), result);
            }
        }
    }

    private void postComment() {
        String commentText = etComment.getText().toString().trim();
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isGroupPost) {
            // Post comment to group post
            java.util.Map<String, String> request = new java.util.HashMap<>();
            request.put("contentText", commentText);
            if (parentCommentId != null) {
                request.put("parentCommentId", parentCommentId);
            }
            
            groupApi.addGroupPostComment(post.getId(), request).enqueue(new Callback<Comment>() {
                @Override
                public void onResponse(@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
                    if (response.isSuccessful()) {
                        etComment.setText("");
                        etComment.setHint(R.string.write_comment);
                        parentCommentId = null;
                        Toast.makeText(PostDetailActivity.this, "Comment posted", Toast.LENGTH_SHORT).show();
                        loadComments(post.getId());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                    Toast.makeText(PostDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Post comment to regular post
            CreateCommentRequest request = new CreateCommentRequest(post.getId(), commentText, parentCommentId);
            postApi.addComment(post.getId(), request).enqueue(new Callback<Comment>() {
                @Override
                public void onResponse(@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
                    if (response.isSuccessful()) {
                        etComment.setText("");
                        etComment.setHint(R.string.write_comment);
                        parentCommentId = null;
                        Toast.makeText(PostDetailActivity.this, "Comment posted", Toast.LENGTH_SHORT).show();
                        loadComments(post.getId());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                    Toast.makeText(PostDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void toggleLike() {
        if (isGroupPost) {
            // Like/unlike group post based on current state
            com.example.runmapproapp.data.api.GroupApi api = ApiClient.getGroupApi();
            Call<com.example.runmapproapp.data.model.GroupPost> call = post.isLikedByCurrentUser() ? 
                api.unlikeGroupPost(post.getId()) : 
                api.likeGroupPost(post.getId());

            call.enqueue(new Callback<com.example.runmapproapp.data.model.GroupPost>() {
                @Override
                public void onResponse(@NonNull Call<com.example.runmapproapp.data.model.GroupPost> call, @NonNull Response<com.example.runmapproapp.data.model.GroupPost> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        com.example.runmapproapp.data.model.GroupPost groupPost = response.body();
                        post = convertGroupPostToPost(groupPost);
                        displayPost();
                    } else {
                        Toast.makeText(PostDetailActivity.this, "Failed to update like", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<com.example.runmapproapp.data.model.GroupPost> call, @NonNull Throwable t) {
                    Toast.makeText(PostDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Like/unlike regular post
            PostApi api = ApiClient.getPostApi();
            Call<Post> call = post.isLikedByCurrentUser() ? 
                api.unlikePost(post.getId()) : 
                api.likePost(post.getId());

            call.enqueue(new Callback<Post>() {
                @Override
                public void onResponse(@NonNull Call<Post> call, @NonNull Response<Post> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        post = response.body();
                        displayPost();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                    Toast.makeText(PostDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sharePost() {
        if (isGroupPost) {
            Toast.makeText(this, "Không thể chia sẻ bài viết trong nhóm", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, CreatePostActivity.class);
        intent.putExtra("SHARE_POST_ID", post.getId());
        startActivity(intent);
    }

    private void openUserProfile(String userId) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }

    @Override
    public void onReplyClick(Comment comment) {
        // Set hint to show replying to username
        parentCommentId = comment.getId();
        etComment.setHint("Reply to " + comment.getAuthorName());
        etComment.requestFocus();
    }

    @Override
    public void onAuthorClick(String authorId) {
        openUserProfile(authorId);
    }

    @Override
    public void onLikeClick(Comment comment) {
        PostApi api = ApiClient.getPostApi();
        Call<Comment> call = comment.isLikedByCurrentUser() ?
            api.unlikeComment(comment.getId()) :
            api.likeComment(comment.getId());

        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
                if (response.isSuccessful()) {
                    loadComments(post.getId());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
            }
        });
    }

    @Override
    public void onEditComment(Comment comment) {
        String currentUserId = new AuthManager(this).getUserId();
        if (!comment.getAuthorId().equals(currentUserId)) {
            return;
        }

        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_comment, null);
        android.widget.EditText etEditComment = dialogView.findViewById(R.id.etEditComment);
        etEditComment.setText(comment.getContentText());

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.edit_comment)
            .setView(dialogView)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                String newText = etEditComment.getText().toString().trim();
                if (newText.isEmpty()) {
                    Toast.makeText(this, R.string.empty_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updateComment(comment, newText);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void updateComment(Comment comment, String newText) {
        CreateCommentRequest request = new CreateCommentRequest(
            comment.getPostId(),
            newText,
            comment.getParentCommentId()
        );

        postApi.updateComment(comment.getId(), request).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int position = commentAdapter.findCommentPosition(comment.getId());
                    if (position != -1) {
                        commentAdapter.updateComment(position, response.body());
                    }
                    Toast.makeText(PostDetailActivity.this, R.string.comment_updated, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PostDetailActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
                Toast.makeText(PostDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteComment(Comment comment) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Comment")
            .setMessage(R.string.delete_comment_confirm)
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                postApi.deleteComment(comment.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(PostDetailActivity.this, "Comment deleted", Toast.LENGTH_SHORT).show();
                            loadComments(post.getId());
                        } else {
                            Toast.makeText(PostDetailActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(PostDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void showPostMenu() {
        String[] options = {"Delete Post"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Post Options")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    deletePost();
                }
            })
            .show();
    }
    
    private void deletePost() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (isGroupPost) {
                    // Delete group post
                    groupApi.deleteGroupPost(post.getId()).enqueue(new Callback<java.util.Map<String, String>>() {
                        @Override
                        public void onResponse(@NonNull Call<java.util.Map<String, String>> call, @NonNull Response<java.util.Map<String, String>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(PostDetailActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                                finish(); // Close activity after delete
                            } else {
                                Toast.makeText(PostDetailActivity.this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<java.util.Map<String, String>> call, @NonNull Throwable t) {
                            Toast.makeText(PostDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Delete regular post
                    postApi.deletePost(post.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(PostDetailActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                                finish(); // Close activity after delete
                            } else {
                                Toast.makeText(PostDetailActivity.this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            Toast.makeText(PostDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
