package com.example.runmapproapp.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.UserApi;
import com.example.runmapproapp.data.api.PostApi;
import com.example.runmapproapp.data.model.CreatePostRequest;
import com.example.runmapproapp.data.model.Post;
import com.example.runmapproapp.data.model.UserProfileResponse;
import com.example.runmapproapp.ui.social.CommentsActivity;
import com.example.runmapproapp.ui.social.CreatePostActivity;
import com.example.runmapproapp.ui.social.PostDetailActivity;
import com.example.runmapproapp.ui.social.adapter.PostAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity implements PostAdapter.OnPostInteractionListener {

    private ImageView ivProfileAvatar;
    private TextView tvProfileName;
    private TextView tvProfileUsername;
    private TextView tvProfileBio;
    private TextView tvPostCount;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private com.google.android.material.button.MaterialButton btnMessage;

    private PostAdapter postAdapter;
    private String userId;
    private boolean isCurrentUser;
    private AuthManager authManager;
    private String displayName;
    private String displayEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        setupToolbar();

        // Get userId and optional display info from intent
        userId = getIntent().getStringExtra("USER_ID");
        displayName = getIntent().getStringExtra("USER_NAME");
        displayEmail = getIntent().getStringExtra("USER_EMAIL");
        
        authManager = new AuthManager(this);
        
        if (userId == null || userId.isEmpty()) {
            userId = authManager.getUserId();
            isCurrentUser = true;
        } else {
            isCurrentUser = userId.equals(authManager.getUserId());
        }

        initViews();
        setupRecyclerView();
        setupTabs();

        loadUserProfile();
        loadUserPosts();
    }

    private void initViews() {
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileBio = findViewById(R.id.tvProfileBio);
        tvPostCount = findViewById(R.id.tvPostCount);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.recyclerViewProfile);
        progressBar = findViewById(R.id.progressBar);
        btnMessage = findViewById(R.id.btnMessage);

        // Show message button only for other users
        if (!isCurrentUser) {
            btnMessage.setVisibility(View.VISIBLE);
            btnMessage.setOnClickListener(v -> openChatWithUser());
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void openChatWithUser() {
        com.example.runmapproapp.data.ChatApi chatApi = ApiClient.getChatApi();
        com.example.runmapproapp.data.ChatApi.CreateDirectChatRequest request = 
                new com.example.runmapproapp.data.ChatApi.CreateDirectChatRequest(userId);

        chatApi.createDirectChat(request).enqueue(new Callback<com.example.runmapproapp.data.model.Conversation>() {
            @Override
            public void onResponse(Call<com.example.runmapproapp.data.model.Conversation> call, 
                                   Response<com.example.runmapproapp.data.model.Conversation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.runmapproapp.data.model.Conversation conversation = response.body();
                    Intent intent = new Intent(UserProfileActivity.this, 
                            com.example.runmapproapp.ui.chat.ChatActivity.class);
                    intent.putExtra("conversationId", conversation.getId());
                    intent.putExtra("conversationName", displayName != null ? displayName : tvProfileName.getText().toString());
                    intent.putExtra("isGroup", false);
                    startActivity(intent);
                } else {
                    Toast.makeText(UserProfileActivity.this, "Failed to open chat", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.runmapproapp.data.model.Conversation> call, Throwable t) {
                Toast.makeText(UserProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Posts"));
        tabLayout.addTab(tabLayout.newTab().setText("Liked"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadUserPosts();
                } else {
                    // TODO: Load liked posts
                    Toast.makeText(UserProfileActivity.this, "Liked posts coming soon", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadUserProfile() {
        if (isCurrentUser) {
            // Load from cache for current user
            loadCurrentUserProfileFromCache();
        } else {
            // For other users, show info from Intent extras or minimal placeholder
            loadOtherUserProfile();
        }
    }

    private void loadOtherUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        UserApi userApi = ApiClient.getUserApi();
        userApi.getUserById(userId).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse user = response.body();
                    displayUserProfile(user);
                } else {
                    // Fallback to basic display
                    displayBasicProfile();
                    Toast.makeText(UserProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                displayBasicProfile();
                Toast.makeText(UserProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBasicProfile() {
        // Display basic info as fallback
        String name = displayName != null ? displayName : "User";
        String username = "user";
        
        if (displayEmail != null && displayEmail.contains("@")) {
            username = displayEmail.substring(0, displayEmail.indexOf("@"));
        } else if (userId != null && userId.length() >= 8) {
            username = userId.substring(0, 8);
        }
        
        tvProfileName.setText(name);
        tvProfileUsername.setText("@" + username);
        tvProfileBio.setVisibility(View.GONE);
        ivProfileAvatar.setImageResource(R.drawable.ic_person);
    }

    private void loadCurrentUserProfileFromCache() {
        // Create UserProfileResponse from cached data
        String fullName = authManager.getUserName();
        String email = authManager.getUserEmail();
        String username = authManager.getUsername();
        String avatarUrl = authManager.getAvatarUrl();
        String bio = authManager.getBio();

        // Generate username from email if not set
        if (username == null || username.isEmpty()) {
            if (email != null && email.contains("@")) {
                username = email.substring(0, email.indexOf("@"));
            } else {
                username = "user";
            }
        }

        // Display cached profile
        tvProfileName.setText(fullName != null && !fullName.isEmpty() ? fullName : "Anonymous");
        tvProfileUsername.setText("@" + username);
        
        if (bio != null && !bio.isEmpty()) {
            tvProfileBio.setVisibility(View.VISIBLE);
            tvProfileBio.setText(bio);
        } else {
            tvProfileBio.setVisibility(View.GONE);
        }

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            // Convert relative path to full URL
            if (avatarUrl.startsWith("/api/")) {
                avatarUrl = "http://10.0.2.2:8080" + avatarUrl;
            }
            Glide.with(this)
                    .load(avatarUrl)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(ivProfileAvatar);
        } else {
            ivProfileAvatar.setImageResource(R.drawable.ic_person);
        }
    }

    private void displayUserProfile(UserProfileResponse user) {
        tvProfileName.setText(user.getFullName() != null && !user.getFullName().isEmpty() ? user.getFullName() : "Anonymous");
        
        // Display username with @ prefix - use email prefix if username not set
        String displayUsername = user.getUsername();
        if (displayUsername == null || displayUsername.isEmpty()) {
            if (user.getEmail() != null && user.getEmail().contains("@")) {
                displayUsername = user.getEmail().substring(0, user.getEmail().indexOf("@"));
            } else {
                displayUsername = "user";
            }
        }
        tvProfileUsername.setText("@" + displayUsername);
        
        // Display bio
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            tvProfileBio.setVisibility(View.VISIBLE);
            tvProfileBio.setText(user.getBio());
        } else {
            tvProfileBio.setVisibility(View.GONE);
        }

        // Load avatar
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            String avatarUrl = user.getAvatarUrl();
            // Convert relative path to full URL
            if (avatarUrl.startsWith("/api/")) {
                avatarUrl = "http://10.0.2.2:8080" + avatarUrl;
            }
            Glide.with(this)
                    .load(avatarUrl)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(ivProfileAvatar);
        } else {
            ivProfileAvatar.setImageResource(R.drawable.ic_person);
        }
    }

    private void loadUserPosts() {
        progressBar.setVisibility(View.VISIBLE);

        PostApi postApi = ApiClient.getPostApi();
        postApi.getUserPosts(userId, 0, 20).enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = response.body();
                    postAdapter.setPosts(posts);
                    tvPostCount.setText(String.valueOf(posts.size()) + " posts");
                } else {
                    Toast.makeText(UserProfileActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPostClick(Post post) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("POST_ID", post.getId());
        startActivity(intent);
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
                Toast.makeText(UserProfileActivity.this, "Failed to update like", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCommentClick(Post post) {
        Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtra("POST_ID", post.getId());
        startActivity(intent);
    }

    @Override
    public void onShareClick(Post post) {
        Intent intent = new Intent(this, CreatePostActivity.class);
        intent.putExtra("SHARE_POST_ID", post.getId());
        startActivity(intent);
    }

    @Override
    public void onAuthorClick(String authorId) {
        if (!authorId.equals(userId)) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("USER_ID", authorId);
            startActivity(intent);
        }
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
                    Toast.makeText(UserProfileActivity.this, R.string.post_updated, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserProfileActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                Toast.makeText(UserProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(UserProfileActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                            loadUserPosts();
                        } else {
                            Toast.makeText(UserProfileActivity.this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(UserProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
