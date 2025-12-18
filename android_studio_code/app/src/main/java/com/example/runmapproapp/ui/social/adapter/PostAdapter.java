package com.example.runmapproapp.ui.social.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.api.RetrofitClient;
import com.example.runmapproapp.api.RunApiService;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.model.Post;
import com.example.runmapproapp.dto.RunResponse;
import com.example.runmapproapp.ui.run.RunDetailActivity;
import com.example.runmapproapp.utils.FormatUtils;
import com.google.android.material.card.MaterialCardView;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.generated.LineLayer;
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap;
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin;
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private final OnPostInteractionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US);

    public interface OnPostInteractionListener {
        void onPostClick(Post post);
        void onLikeClick(Post post, int position);
        void onCommentClick(Post post);
        void onShareClick(Post post);
        void onAuthorClick(String authorId);
        void onEditPost(Post post, int position);
        void onDeletePost(Post post, int position);
    }

    public PostAdapter(List<Post> posts, OnPostInteractionListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    public void addPosts(List<Post> newPosts) {
        int startPosition = posts.size();
        posts.addAll(newPosts);
        notifyItemRangeInserted(startPosition, newPosts.size());
    }

    public void updatePost(int position, Post updatedPost) {
        if (position >= 0 && position < posts.size()) {
            posts.set(position, updatedPost);
            notifyItemChanged(position);
        }
    }

    public void removePost(int position) {
        if (position >= 0 && position < posts.size()) {
            posts.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, posts.size());
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post, position);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull java.util.List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            Post post = posts.get(position);
            for (Object payload : payloads) {
                if ("LIKE_UPDATE".equals(payload)) {
                    // Only update like button without rebinding entire view
                    holder.updateLikeButton(post);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAuthorAvatar;
        private final TextView tvAuthorName;
        private final TextView tvTimestamp;
        private final TextView tvContent;
        private final MaterialCardView cardOriginalPost;
        private final TextView tvOriginalAuthor;
        private final TextView tvOriginalContent;
        private final ImageView ivOriginalPostImage;
        private final ImageView ivPostImage;
        private final ImageButton btnLike;
        private final ImageButton btnComment;
        private final ImageButton btnShare;
        private final ImageButton btnMenu;
        private final TextView tvLikeCount;
        private final TextView tvCommentCount;
        private final TextView tvShareCount;
        
        // Run card views
        private final View runCardLayout;
        private MapView mapViewRun;
        private TextView tvRunDate, tvRunDistance, tvRunDuration, tvRunPace;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAuthorAvatar = itemView.findViewById(R.id.ivAuthorAvatar);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvContent = itemView.findViewById(R.id.tvContent);
            cardOriginalPost = itemView.findViewById(R.id.cardOriginalPost);
            tvOriginalAuthor = itemView.findViewById(R.id.tvOriginalAuthor);
            tvOriginalContent = itemView.findViewById(R.id.tvOriginalContent);
            ivOriginalPostImage = itemView.findViewById(R.id.ivOriginalPostImage);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            tvShareCount = itemView.findViewById(R.id.tvShareCount);
            
            // Run card
            runCardLayout = itemView.findViewById(R.id.runCardLayout);
            if (runCardLayout != null) {
                mapViewRun = runCardLayout.findViewById(R.id.mapViewRun);
                tvRunDate = runCardLayout.findViewById(R.id.tvRunDate);
                tvRunDistance = runCardLayout.findViewById(R.id.tvRunDistance);
                tvRunDuration = runCardLayout.findViewById(R.id.tvRunDuration);
                tvRunPace = runCardLayout.findViewById(R.id.tvRunPace);
            }
        }

        public void bind(Post post, int position) {
            // Author name - use authorName if available, fallback to userId
            String authorDisplayName = post.getAuthorName();
            if (authorDisplayName == null || authorDisplayName.isEmpty()) {
                if (post.getAuthorId() != null && post.getAuthorId().length() >= 8) {
                    authorDisplayName = "User " + post.getAuthorId().substring(0, 8);
                } else {
                    authorDisplayName = "User";
                }
            }
            tvAuthorName.setText(authorDisplayName);
            
            // Load author avatar if available
            if (post.getAuthorAvatar() != null && !post.getAuthorAvatar().isEmpty()) {
                String avatarUrl = post.getAuthorAvatar();
                // Convert relative path to full URL
                if (avatarUrl.startsWith("/api/")) {
                    avatarUrl = "http://10.0.2.2:8080" + avatarUrl;
                }
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivAuthorAvatar);
            } else {
                ivAuthorAvatar.setImageResource(R.drawable.ic_person);
            }
            
            // Timestamp
            if (post.getCreatedAt() != null) {
                tvTimestamp.setText(formatDate(post.getCreatedAt()));
            }
            
            // Content
            tvContent.setText(post.getContentText() != null ? post.getContentText() : "");
            tvContent.setVisibility(post.getContentText() != null && !post.getContentText().isEmpty() 
                ? View.VISIBLE : View.GONE);
            
            // Original post preview (for shared posts)
            if (post.getOriginalPost() != null) {
                cardOriginalPost.setVisibility(View.VISIBLE);
                Post originalPost = post.getOriginalPost();
                
                // Original author name
                String originalAuthorName = originalPost.getAuthorName();
                if (originalAuthorName == null || originalAuthorName.isEmpty()) {
                    if (originalPost.getAuthorId() != null && originalPost.getAuthorId().length() >= 8) {
                        originalAuthorName = "User " + originalPost.getAuthorId().substring(0, 8);
                    } else {
                        originalAuthorName = "User";
                    }
                }
                tvOriginalAuthor.setText(originalAuthorName);
                
                // Original content
                String originalContent = originalPost.getContentText();
                if (originalContent != null && !originalContent.isEmpty()) {
                    tvOriginalContent.setText(originalContent);
                    tvOriginalContent.setVisibility(View.VISIBLE);
                } else {
                    tvOriginalContent.setText("[No text content]");
                    tvOriginalContent.setVisibility(View.VISIBLE);
                }
                
                // Original post image
                if (originalPost.getMediaIds() != null && !originalPost.getMediaIds().isEmpty()) {
                    ivOriginalPostImage.setVisibility(View.VISIBLE);
                    String originalMediaUrl = "http://10.0.2.2:8080/api/media/" + originalPost.getMediaIds().get(0);
                    Glide.with(itemView.getContext())
                            .load(originalMediaUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_person)
                            .into(ivOriginalPostImage);
                } else {
                    ivOriginalPostImage.setVisibility(View.GONE);
                }
                
                // Click listener for original post - open original post detail
                cardOriginalPost.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onPostClick(originalPost);
                    }
                });
            } else {
                cardOriginalPost.setVisibility(View.GONE);
            }
            
            // Post image (show first media if available)
            if (post.getMediaIds() != null && !post.getMediaIds().isEmpty()) {
                ivPostImage.setVisibility(View.VISIBLE);
                String mediaUrl = "http://10.0.2.2:8080/api/media/" + post.getMediaIds().get(0);
                android.util.Log.d("PostAdapter", "Loading post image: " + mediaUrl);
                Glide.with(itemView.getContext())
                        .load(mediaUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_person)
                        .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                android.util.Log.e("PostAdapter", "Failed to load post image: " + model, e);
                                if (e != null) {
                                    e.logRootCauses("PostAdapter");
                                }
                                return false;
                            }
                            
                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                android.util.Log.d("PostAdapter", "Successfully loaded post image: " + model);
                                return false;
                            }
                        })
                        .into(ivPostImage);
            } else {
                ivPostImage.setVisibility(View.GONE);
            }
            
            // Run card (if post has attached run)
            if (post.getRunId() != null && !post.getRunId().isEmpty() && runCardLayout != null) {
                runCardLayout.setVisibility(View.VISIBLE);
                
                // Clear previous map data to avoid showing old route
                if (mapViewRun != null) {
                    mapViewRun.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
                        // Style loaded, will draw route in loadAndDisplayRun
                    });
                }
                
                loadAndDisplayRun(post.getRunId());
                
                // Add click listener to open run detail
                final String runId = post.getRunId();
                runCardLayout.setOnClickListener(v -> {
                    Intent intent = new Intent(itemView.getContext(), RunDetailActivity.class);
                    intent.putExtra(RunDetailActivity.EXTRA_RUN_ID, runId);
                    itemView.getContext().startActivity(intent);
                });
            } else if (runCardLayout != null) {
                runCardLayout.setVisibility(View.GONE);
                runCardLayout.setOnClickListener(null);
            }
            
            // Counters
            tvLikeCount.setText(String.valueOf(post.getLikeCount()));
            tvCommentCount.setText(String.valueOf(post.getCommentCount()));
            tvShareCount.setText(String.valueOf(post.getShareCount()));
            
            // Like button state with color
            if (post.isLikedByCurrentUser()) {
                btnLike.setImageResource(R.drawable.ic_favorite_filled);
                btnLike.setColorFilter(itemView.getContext().getColor(R.color.pink));
            } else {
                btnLike.setImageResource(R.drawable.ic_favorite_border);
                btnLike.setColorFilter(itemView.getContext().getColor(android.R.color.darker_gray));
            }
            
            // Show menu button only if current user is the author
            AuthManager authManager = new AuthManager(itemView.getContext());
            String currentUserId = authManager.getUserId();
            if (currentUserId != null && currentUserId.equals(post.getAuthorId())) {
                btnMenu.setVisibility(View.VISIBLE);
                btnMenu.setOnClickListener(v -> showPostMenu(v, post, position));
            } else {
                btnMenu.setVisibility(View.GONE);
            }
            
            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post);
                }
            });
            
            ivAuthorAvatar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAuthorClick(post.getAuthorId());
                }
            });
            tvAuthorName.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAuthorClick(post.getAuthorId());
                }
            });
            
            btnLike.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLikeClick(post, position);
                }
            });
            btnComment.setOnClickListener(v -> listener.onCommentClick(post));
            btnShare.setOnClickListener(v -> listener.onShareClick(post));
        }
        
        private void loadAndDisplayRun(String runId) {
            RunApiService runApiService = RetrofitClient.getRunApiService();
            runApiService.getRun(runId).enqueue(new Callback<RunResponse>() {
                @Override
                public void onResponse(@NonNull Call<RunResponse> call, @NonNull Response<RunResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        displayRunData(response.body());
                    } else {
                        // API failed - log error and hide run card
                        android.util.Log.e("PostAdapter", "Failed to load run " + runId + ": " + response.code());
                        if (runCardLayout != null) {
                            runCardLayout.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RunResponse> call, @NonNull Throwable t) {
                    // Network error - log and hide run card
                    android.util.Log.e("PostAdapter", "Error loading run " + runId, t);
                    if (runCardLayout != null) {
                        runCardLayout.setVisibility(View.GONE);
                    }
                }
            });
        }

        private void displayRunData(RunResponse run) {
            // Display run stats
            tvRunDate.setText(FormatUtils.formatDate(run.getStartTime()));
            tvRunDistance.setText(FormatUtils.formatDistance(run.getDistanceMeters()));
            tvRunDuration.setText(FormatUtils.formatDuration(run.getDurationMs()));
            tvRunPace.setText(FormatUtils.formatPace(run.getAvgPaceSecPerKm()));

            // Display map
            if (mapViewRun != null) {
                mapViewRun.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
                    if (run.getPath() != null && run.getPath().getCoordinates() != null 
                            && run.getPath().getCoordinates().size() >= 2) {
                        
                        // Convert coordinates to Points
                        List<Point> points = new ArrayList<>();
                        for (List<Double> coord : run.getPath().getCoordinates()) {
                            if (coord.size() >= 2) {
                                points.add(Point.fromLngLat(coord.get(0), coord.get(1)));
                            }
                        }

                        if (points.size() >= 2) {
                            // Create unique IDs for this run
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
                            LineString lineString = LineString.fromLngLats(points);
                            Feature feature = Feature.fromGeometry(lineString);
                            
                            GeoJsonSource source = new GeoJsonSource.Builder(sourceId)
                                    .feature(feature)
                                    .build();
                            source.bindTo(style);

                            LineLayer lineLayer = new LineLayer(layerId, sourceId);
                            lineLayer.lineColor("#FF5722");
                            lineLayer.lineWidth(4.0);
                            lineLayer.lineCap(LineCap.ROUND);
                            lineLayer.lineJoin(LineJoin.ROUND);
                            lineLayer.bindTo(style);

                            // Center camera
                            Point centerPoint = points.get(points.size() / 2);
                            mapViewRun.getMapboxMap().setCamera(
                                    new CameraOptions.Builder()
                                            .center(centerPoint)
                                            .zoom(13.0)
                                            .build()
                            );
                        }
                    }
                });
            }
        }
        
        private void showPostMenu(View view, Post post, int position) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenuInflater().inflate(R.menu.post_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.action_edit_post) {
                        listener.onEditPost(post, position);
                        return true;
                    } else if (id == R.id.action_delete_post) {
                        listener.onDeletePost(post, position);
                        return true;
                    }
                    return false;
                }
            });
            popup.show();
        }

        private String formatDate(Date date) {
            return dateFormat.format(date);
        }
        
        // Update only like button without rebinding entire view
        public void updateLikeButton(Post post) {
            tvLikeCount.setText(String.valueOf(post.getLikeCount()));
            
            if (post.isLikedByCurrentUser()) {
                btnLike.setImageResource(R.drawable.ic_favorite_filled);
                btnLike.setColorFilter(itemView.getContext().getColor(R.color.pink));
            } else {
                btnLike.setImageResource(R.drawable.ic_favorite_border);
                btnLike.setColorFilter(itemView.getContext().getColor(android.R.color.darker_gray));
            }
        }
    }
}
