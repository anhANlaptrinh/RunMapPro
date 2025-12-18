package com.example.runmapproapp.ui.social;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.runmapproapp.R;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.NotificationApi;
import com.example.runmapproapp.data.api.PostApi;
import com.example.runmapproapp.data.model.CreatePostRequest;
import com.example.runmapproapp.data.model.Post;
import com.example.runmapproapp.data.model.UnreadCountResponse;
import com.example.runmapproapp.ui.notifications.NotificationsActivity;
import com.example.runmapproapp.ui.social.adapter.PostAdapter;
import com.example.runmapproapp.ui.profile.UserProfileActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.runmapproapp.utils.BottomNavigationHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedActivity extends AppCompatActivity implements PostAdapter.OnPostInteractionListener {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private FloatingActionButton fabCreatePost;

    private PostApi postApi;
    private NotificationApi notificationApi;
    private int currentPage = 0;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private TextView tvBadgeCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        setupToolbar();
        initViews();
        setupRecyclerView();
        setupListeners();

        // Setup bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav, R.id.nav_feed);

        postApi = ApiClient.getPostApi();
        notificationApi = ApiClient.getClient().create(NotificationApi.class);
        
        loadFeed(false);
        loadUnreadCount();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feed, menu);
        
        MenuItem notificationItem = menu.findItem(R.id.action_notifications);
        View actionView = notificationItem.getActionView();
        if (actionView != null) {
            tvBadgeCount = actionView.findViewById(R.id.tvBadgeCount);
            actionView.setOnClickListener(v -> openNotifications());
        }
        
        return true;
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewFeed);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        fabCreatePost = findViewById(R.id.fabCreatePost);
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);

        // Pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadFeed(true);
                    }
                }
            }
        });
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            isLastPage = false;
            loadFeed(false);
        });

        fabCreatePost.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });
    }

    private void loadFeed(boolean append) {
        if (isLoading) return;

        isLoading = true;
        if (!append) {
            progressBar.setVisibility(View.VISIBLE);
        }

        postApi.getFeed(currentPage, pageSize).enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = response.body();
                    
                    if (posts.isEmpty()) {
                        isLastPage = true;
                    } else {
                        if (append) {
                            postAdapter.addPosts(posts);
                        } else {
                            postAdapter.setPosts(posts);
                        }
                        currentPage++;
                    }
                } else {
                    Toast.makeText(FeedActivity.this, "Failed to load feed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(FeedActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLikeClick(Post post, int position) {
        PostApi api = ApiClient.getPostApi();
        Call<Post> call = post.isLikedByCurrentUser() ? 
            api.unlikePost(post.getId()) : 
            api.likePost(post.getId());

        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NonNull Call<Post> call, @NonNull Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postAdapter.updatePost(position, response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                Toast.makeText(FeedActivity.this, "Failed to update like", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCommentClick(Post post) {
        Intent intent = new Intent(FeedActivity.this, CommentsActivity.class);
        intent.putExtra("POST_ID", post.getId());
        startActivity(intent);
    }

    @Override
    public void onPostClick(Post post) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("POST_ID", post.getId());
        startActivity(intent);
    }

    @Override
    public void onShareClick(Post post) {
        Intent intent = new Intent(FeedActivity.this, CreatePostActivity.class);
        intent.putExtra("SHARE_POST_ID", post.getId());
        startActivity(intent);
    }

    @Override
    public void onAuthorClick(String authorId) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("USER_ID", authorId);
        // Note: Could pass more info here if available in Post model
        startActivity(intent);
    }

    @Override
    public void onEditPost(Post post, int position) {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_post, null);
        android.widget.EditText etEditContent = dialogView.findViewById(R.id.etEditContent);
        etEditContent.setText(post.getContentText());

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.edit_post)
            .setView(dialogView)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                String newContent = etEditContent.getText().toString().trim();
                if (newContent.isEmpty()) {
                    Toast.makeText(this, R.string.empty_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatePost(post, newContent, position);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void updatePost(Post post, String newContent, int position) {
        PostApi postApi = ApiClient.getPostApi();
        CreatePostRequest request = new CreatePostRequest(
            newContent,
            post.getMediaIds(),
            post.getGroupId(),
            post.getRunId()
        );
        
        postApi.updatePost(post.getId(), request).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NonNull Call<Post> call, @NonNull Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postAdapter.updatePost(position, response.body());
                    Toast.makeText(FeedActivity.this, R.string.post_updated, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FeedActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                Toast.makeText(FeedActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeletePost(Post post, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Post")
            .setMessage(R.string.delete_post_confirm)
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                PostApi postApi = ApiClient.getPostApi();
                postApi.deletePost(post.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(FeedActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                            loadFeed(true);
                        } else {
                            Toast.makeText(FeedActivity.this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(FeedActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh feed when returning from create post
        currentPage = 0;
        isLastPage = false;
        loadFeed(false);
        loadUnreadCount();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav, R.id.nav_feed);
    }

    private void loadUnreadCount() {
        notificationApi.getUnreadCount().enqueue(new Callback<UnreadCountResponse>() {
            @Override
            public void onResponse(@NonNull Call<UnreadCountResponse> call, 
                                   @NonNull Response<UnreadCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long count = response.body().getCount();
                    updateBadge(count);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UnreadCountResponse> call, @NonNull Throwable t) {
                // Ignore failure
            }
        });
    }

    private void updateBadge(long count) {
        if (tvBadgeCount != null) {
            if (count > 0) {
                tvBadgeCount.setVisibility(View.VISIBLE);
                tvBadgeCount.setText(count > 99 ? "99+" : String.valueOf(count));
            } else {
                tvBadgeCount.setVisibility(View.GONE);
            }
        }
    }

    private void openNotifications() {
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
    }
}
