package com.example.runmapproapp.ui.admin.content;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.api.RetrofitClient;
import com.example.runmapproapp.api.RunApiService;
import com.example.runmapproapp.data.model.Post;
import com.example.runmapproapp.dto.RunResponse;
import com.example.runmapproapp.utils.FormatUtils;
import com.google.android.material.button.MaterialButton;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostsAdminAdapter extends RecyclerView.Adapter<PostsAdminAdapter.PostViewHolder> {

    private List<Post> posts;
    private final Context context;
    private OnPostActionListener listener;

    public interface OnPostActionListener {
        void onDeletePost(Post post);
    }

    public PostsAdminAdapter(Context context, OnPostActionListener listener) {
        this.context = context;
        this.posts = new ArrayList<>();
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_admin, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAuthorAvatar;
        private TextView tvUserName;
        private TextView tvTimestamp;
        private TextView tvContent;
        private MaterialCardView cardOriginalPost;
        private TextView tvOriginalAuthor;
        private TextView tvOriginalContent;
        private MaterialCardView cardOriginalImage;
        private ImageView ivOriginalPostImage;
        private ImageView ivPostImage;
        private MaterialButton btnDelete;
        
        // Run card views
        private View runCardLayout;
        private MapView mapViewRun;
        private TextView tvRunDate, tvRunDistance, tvRunDuration, tvRunPace;
        
        // Original run card views (for shared posts)
        private View originalRunCardLayout;
        private MapView originalMapViewRun;
        private TextView originalTvRunDate, originalTvRunDistance, originalTvRunDuration, originalTvRunPace;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAuthorAvatar = itemView.findViewById(R.id.ivAuthorAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvContent = itemView.findViewById(R.id.tvContent);
            cardOriginalPost = itemView.findViewById(R.id.cardOriginalPost);
            tvOriginalAuthor = itemView.findViewById(R.id.tvOriginalAuthor);
            tvOriginalContent = itemView.findViewById(R.id.tvOriginalContent);
            cardOriginalImage = itemView.findViewById(R.id.cardOriginalImage);
            ivOriginalPostImage = itemView.findViewById(R.id.ivOriginalPostImage);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            
            // Run card
            runCardLayout = itemView.findViewById(R.id.runCardLayout);
            if (runCardLayout != null) {
                mapViewRun = runCardLayout.findViewById(R.id.mapViewRun);
                tvRunDate = runCardLayout.findViewById(R.id.tvRunDate);
                tvRunDistance = runCardLayout.findViewById(R.id.tvRunDistance);
                tvRunDuration = runCardLayout.findViewById(R.id.tvRunDuration);
                tvRunPace = runCardLayout.findViewById(R.id.tvRunPace);
            }
            
            // Original run card (for shared posts)
            originalRunCardLayout = itemView.findViewById(R.id.originalRunCardLayout);
            if (originalRunCardLayout != null) {
                originalMapViewRun = originalRunCardLayout.findViewById(R.id.mapViewRun);
                originalTvRunDate = originalRunCardLayout.findViewById(R.id.tvRunDate);
                originalTvRunDistance = originalRunCardLayout.findViewById(R.id.tvRunDistance);
                originalTvRunDuration = originalRunCardLayout.findViewById(R.id.tvRunDuration);
                originalTvRunPace = originalRunCardLayout.findViewById(R.id.tvRunPace);
            }
        }

        public void bind(Post post) {
            // Author name (same logic as PostAdapter)
            String authorDisplayName = post.getAuthorName();
            if (authorDisplayName == null || authorDisplayName.isEmpty()) {
                if (post.getAuthorId() != null && post.getAuthorId().length() >= 8) {
                    authorDisplayName = "User " + post.getAuthorId().substring(0, 8);
                } else {
                    authorDisplayName = "User";
                }
            }
            tvUserName.setText(authorDisplayName);
            
            // Load author avatar (same logic as PostAdapter)
            if (post.getAuthorAvatar() != null && !post.getAuthorAvatar().isEmpty()) {
                String avatarUrl = post.getAuthorAvatar();
                // Convert relative path to full URL
                if (avatarUrl.startsWith("/api/")) {
                    avatarUrl = "http://10.0.2.2:8080" + avatarUrl;
                }
                Glide.with(context)
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
            if (post.getContentText() != null && !post.getContentText().isEmpty()) {
                tvContent.setText(post.getContentText());
                tvContent.setVisibility(View.VISIBLE);
            } else {
                tvContent.setVisibility(View.GONE);
            }
            
            // Original post preview (for shared posts) - same logic as PostAdapter
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
                    cardOriginalImage.setVisibility(View.VISIBLE);
                    String originalMediaUrl = "http://10.0.2.2:8080/api/media/" + originalPost.getMediaIds().get(0);
                    Glide.with(context)
                            .load(originalMediaUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_person)
                            .into(ivOriginalPostImage);
                } else {
                    cardOriginalImage.setVisibility(View.GONE);
                }
                
                // Original run card (minimap for shared posts)
                if (originalPost.getRunId() != null && !originalPost.getRunId().isEmpty() && originalRunCardLayout != null) {
                    originalRunCardLayout.setVisibility(View.VISIBLE);
                    
                    // Clear previous map data
                    if (originalMapViewRun != null) {
                        originalMapViewRun.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
                            // Style loaded, will draw route in loadOriginalRunData
                        });
                    }
                    
                    loadOriginalRunData(originalPost.getRunId());
                } else if (originalRunCardLayout != null) {
                    originalRunCardLayout.setVisibility(View.GONE);
                }
            } else {
                cardOriginalPost.setVisibility(View.GONE);
            }
            
            // Load post image (same logic as PostAdapter - build URL from mediaIds)
            if (post.getMediaIds() != null && !post.getMediaIds().isEmpty()) {
                ivPostImage.setVisibility(View.VISIBLE);
                String imageUrl = "http://10.0.2.2:8080/api/media/" + post.getMediaIds().get(0);
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_article)
                        .error(R.drawable.ic_article)
                        .into(ivPostImage);
            } else {
                ivPostImage.setVisibility(View.GONE);
            }
            
            // Run card (if post has attached run) - same logic as PostAdapter
            if (post.getRunId() != null && !post.getRunId().isEmpty() && runCardLayout != null) {
                runCardLayout.setVisibility(View.VISIBLE);
                
                // Clear previous map data to avoid showing old route
                if (mapViewRun != null) {
                    mapViewRun.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
                        // Style loaded, will draw route in loadAndDisplayRun
                    });
                }
                
                loadAndDisplayRun(post.getRunId());
            } else if (runCardLayout != null) {
                runCardLayout.setVisibility(View.GONE);
            }
            
            // Delete button
            btnDelete.setOnClickListener(v -> listener.onDeletePost(post));
        }
        
        private String formatDate(Date date) {
            if (date == null) {
                return "";
            }
            
            try {
                long diff = System.currentTimeMillis() - date.getTime();
                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;
                
                if (days > 0) {
                    return days + " ngày trước";
                } else if (hours > 0) {
                    return hours + " giờ trước";
                } else if (minutes > 0) {
                    return minutes + " phút trước";
                } else {
                    return "Vừa xong";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }
        
        private void loadAndDisplayRun(String runId) {
            RunApiService runApiService = RetrofitClient.getRunApiService();
            runApiService.getRun(runId).enqueue(new Callback<RunResponse>() {
                @Override
                public void onResponse(@NonNull Call<RunResponse> call, @NonNull Response<RunResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        displayRunData(response.body());
                    } else {
                        android.util.Log.e("PostsAdminAdapter", "Failed to load run " + runId + ": " + response.code());
                        if (runCardLayout != null) {
                            runCardLayout.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RunResponse> call, @NonNull Throwable t) {
                    android.util.Log.e("PostsAdminAdapter", "Error loading run " + runId, t);
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
        
        private void loadOriginalRunData(String runId) {
            RunApiService runApiService = RetrofitClient.getRunApiService();
            runApiService.getRun(runId).enqueue(new Callback<RunResponse>() {
                @Override
                public void onResponse(@NonNull Call<RunResponse> call, @NonNull Response<RunResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        displayOriginalRunData(response.body());
                    } else {
                        android.util.Log.e("PostsAdminAdapter", "Failed to load original run " + runId + ": " + response.code());
                        if (originalRunCardLayout != null) {
                            originalRunCardLayout.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RunResponse> call, @NonNull Throwable t) {
                    android.util.Log.e("PostsAdminAdapter", "Error loading original run " + runId, t);
                    if (originalRunCardLayout != null) {
                        originalRunCardLayout.setVisibility(View.GONE);
                    }
                }
            });
        }
        
        private void displayOriginalRunData(RunResponse run) {
            // Display run stats
            originalTvRunDate.setText(FormatUtils.formatDate(run.getStartTime()));
            originalTvRunDistance.setText(FormatUtils.formatDistance(run.getDistanceMeters()));
            originalTvRunDuration.setText(FormatUtils.formatDuration(run.getDurationMs()));
            originalTvRunPace.setText(FormatUtils.formatPace(run.getAvgPaceSecPerKm()));

            // Display map
            if (originalMapViewRun != null) {
                originalMapViewRun.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
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
                            originalMapViewRun.getMapboxMap().setCamera(
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
    }
}
