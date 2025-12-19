package com.example.runmapproapp.ui.groups;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.GroupApi;
import com.example.runmapproapp.data.api.PostApi;
import com.example.runmapproapp.data.model.Group;
import com.example.runmapproapp.data.model.GroupPost;
import com.example.runmapproapp.data.model.Post;
import com.example.runmapproapp.ui.group.GroupPostActivity;
import com.example.runmapproapp.ui.group.GroupSettingsActivity;
import com.example.runmapproapp.ui.social.CreatePostActivity;
import com.example.runmapproapp.ui.social.PostDetailActivity;
import com.example.runmapproapp.ui.social.adapter.PostAdapter;
import com.example.runmapproapp.ui.profile.UserProfileActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupDetailActivity extends AppCompatActivity 
        implements PostAdapter.OnPostInteractionListener {

    private ImageView ivGroupCover;
    private TextView tvGroupName;
    private TextView tvGroupDescription;
    private TextView tvGroupStats;
    private Button btnJoinLeave, btnSettings;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private FloatingActionButton fabCreatePost;

    private GroupApi groupApi;
    private PostApi postApi;
    private String groupId;
    private String groupName;
    private Group currentGroup;
    private boolean isMember = false;

    private int currentPage = 0;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");

        setupToolbar();
        initViews();
        setupRecyclerView();
        setupListeners();

        groupApi = ApiClient.getGroupApi();
        postApi = ApiClient.getPostApi();

        loadGroupDetails();
        loadGroupPosts(false);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(groupName != null ? groupName : "Group");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Disable CollapsingToolbar title to avoid duplicate
        com.google.android.material.appbar.CollapsingToolbarLayout collapsingToolbar = 
            findViewById(R.id.collapsingToolbar);
        if (collapsingToolbar != null) {
            collapsingToolbar.setTitleEnabled(false);
        }
    }

    private void initViews() {
        ivGroupCover = findViewById(R.id.ivGroupCover);
        tvGroupName = findViewById(R.id.tvGroupName);
        tvGroupDescription = findViewById(R.id.tvGroupDescription);
        tvGroupStats = findViewById(R.id.tvGroupStats);
        btnJoinLeave = findViewById(R.id.btnJoinLeave);
        btnSettings = findViewById(R.id.btnSettings);
        recyclerView = findViewById(R.id.recyclerViewGroupPosts);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        fabCreatePost = findViewById(R.id.fabCreatePost);
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);

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
                        loadGroupPosts(true);
                    }
                }
            }
        });
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            isLastPage = false;
            loadGroupPosts(false);
            loadGroupDetails();
        });

        fabCreatePost.setOnClickListener(v -> {
            if (!isMember) {
                Toast.makeText(this, "Bạn phải tham gia nhóm trước", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, GroupPostActivity.class);
            intent.putExtra("groupId", groupId);
            startActivityForResult(intent, 1);
        });

        btnJoinLeave.setOnClickListener(v -> handleJoinLeave());
        
        btnSettings.setOnClickListener(v -> showAdminMenu());
    }
    
    private void showAdminMenu() {
        String userRole = currentGroup != null ? currentGroup.getUserRole() : null;
        
        String[] options;
        if ("owner".equals(userRole)) {
            // Owner has access to all features
            options = new String[]{"Duyệt thành viên", "Duyệt bài viết", "Cài đặt nhóm"};
        } else {
            // Admin only has approval features, no settings
            options = new String[]{"Duyệt thành viên", "Duyệt bài viết"};
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Quản lý nhóm")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Duyệt thành viên
                        Intent intent = new Intent(this, PendingMembersActivity.class);
                        intent.putExtra("GROUP_ID", groupId);
                        startActivity(intent);
                    } else if (which == 1) {
                        // Duyệt bài viết
                        Intent intent = new Intent(this, PendingPostsActivity.class);
                        intent.putExtra("GROUP_ID", groupId);
                        startActivity(intent);
                    } else if (which == 2 && "owner".equals(userRole)) {
                        // Cài đặt nhóm (owner only)
                        Intent intent = new Intent(this, GroupSettingsActivity.class);
                        intent.putExtra("groupId", groupId);
                        startActivity(intent);
                    }
                })
                .show();
    }

    private void loadGroupDetails() {
        groupApi.getGroup(groupId).enqueue(new Callback<Group>() {
            @Override
            public void onResponse(@NonNull Call<Group> call, @NonNull Response<Group> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentGroup = response.body();
                    displayGroupDetails(currentGroup);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Group> call, @NonNull Throwable t) {
                Toast.makeText(GroupDetailActivity.this, 
                        "Failed to load group: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayGroupDetails(Group group) {
        tvGroupName.setText(group.getName());
        tvGroupDescription.setText(group.getDescription());
        
        String stats = group.getMemberCount() + " thành viên · " + group.getPostCount() + " bài viết";
        tvGroupStats.setText(stats);

        if (group.getCoverImageUrl() != null && !group.getCoverImageUrl().isEmpty()) {
            String coverImageUrl = "http://10.0.2.2:8080/api/media/" + group.getCoverImageUrl();
            Glide.with(this)
                    .load(coverImageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(ivGroupCover);
        }
        
        // Check if user is member and update UI
        String userRole = group.getUserRole();
        isMember = userRole != null;
        updateJoinButton(userRole);
        
        // Set group role for post adapter (owner can delete any post)
        if (postAdapter != null) {
            postAdapter.setGroupUserRole(userRole);
        }
    }
    
    private void updateJoinButton(String userRole) {
        if (userRole != null) {
            // User is a member
            btnJoinLeave.setText("Rời nhóm");
            btnJoinLeave.setVisibility(View.VISIBLE);
            fabCreatePost.setVisibility(View.VISIBLE);
            
            // Show settings button for owner and admin
            if ("owner".equals(userRole) || "admin".equals(userRole)) {
                btnSettings.setVisibility(View.VISIBLE);
            } else {
                btnSettings.setVisibility(View.GONE);
            }
        } else {
            // User is not a member
            btnJoinLeave.setText("Tham gia nhóm");
            btnJoinLeave.setVisibility(View.VISIBLE);
            btnSettings.setVisibility(View.GONE);
            fabCreatePost.setVisibility(View.GONE);
        }
    }
    
    private void handleJoinLeave() {
        if (currentGroup == null) return;
        
        if (isMember) {
            // Leave group
            leaveGroup();
        } else {
            // Join group
            if ("private".equals(currentGroup.getPrivacy())) {
                showInviteCodeDialog();
            } else {
                joinGroup(null);
            }
        }
    }
    
    private void leaveGroup() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Rời nhóm")
                .setMessage("Bạn có chắc muốn rời khỏi nhóm này?")
                .setPositiveButton("Rời", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    btnJoinLeave.setEnabled(false);
                    
                    groupApi.leaveGroup(groupId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            progressBar.setVisibility(View.GONE);
                            btnJoinLeave.setEnabled(true);
                            
                            if (response.isSuccessful()) {
                                Toast.makeText(GroupDetailActivity.this, "Đã rời nhóm", Toast.LENGTH_SHORT).show();
                                loadGroupDetails();
                            } else {
                                Toast.makeText(GroupDetailActivity.this, "Không thể rời nhóm", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            btnJoinLeave.setEnabled(true);
                            Toast.makeText(GroupDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void showInviteCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Invite Code");
        
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Invite code");
        builder.setView(input);
        
        builder.setPositiveButton("Join", (dialog, which) -> {
            String inviteCode = input.getText().toString().trim();
            if (!inviteCode.isEmpty()) {
                joinGroup(inviteCode);
            } else {
                Toast.makeText(this, "Please enter invite code", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }
    
    private void joinGroup(String inviteCode) {
        progressBar.setVisibility(View.VISIBLE);
        btnJoinLeave.setEnabled(false);
        
        if (inviteCode != null) {
            // Private group - request join with invite code
            Map<String, String> body = new HashMap<>();
            body.put("inviteCode", inviteCode);
            
            groupApi.requestJoinGroup(groupId, body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                    progressBar.setVisibility(View.GONE);
                    btnJoinLeave.setEnabled(true);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        String status = (String) response.body().get("status");
                        if ("joined".equals(status)) {
                            Toast.makeText(GroupDetailActivity.this, "Đã tham gia nhóm", Toast.LENGTH_SHORT).show();
                            loadGroupDetails();
                            loadGroupPosts(false);
                        } else if ("pending".equals(status)) {
                            Toast.makeText(GroupDetailActivity.this, "Yêu cầu đã gửi, chờ quản trị viên phê duyệt", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(GroupDetailActivity.this, "Mã mời không hợp lệ hoặc đã là thành viên", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    btnJoinLeave.setEnabled(true);
                    Toast.makeText(GroupDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Public group - direct join
            groupApi.joinGroup(groupId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    progressBar.setVisibility(View.GONE);
                    btnJoinLeave.setEnabled(true);
                    
                    if (response.isSuccessful()) {
                        Toast.makeText(GroupDetailActivity.this, "Đã tham gia nhóm", Toast.LENGTH_SHORT).show();
                        loadGroupDetails();
                        loadGroupPosts(false);
                    } else {
                        Toast.makeText(GroupDetailActivity.this, "Không thể tham gia nhóm", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    btnJoinLeave.setEnabled(true);
                    Toast.makeText(GroupDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadGroupPosts(boolean loadMore) {
        if (isLoading) return;

        isLoading = true;
        if (!loadMore) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Use GroupPost API to load group posts
        groupApi.getGroupPostList(groupId, currentPage, pageSize).enqueue(new Callback<List<GroupPost>>() {
            @Override
            public void onResponse(@NonNull Call<List<GroupPost>> call, @NonNull Response<List<GroupPost>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<GroupPost> groupPosts = response.body();

                    if (groupPosts.isEmpty()) {
                        isLastPage = true;
                        if (currentPage == 0) {
                            Toast.makeText(GroupDetailActivity.this, "Chưa có bài viết nào", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    // Filter out pending posts - only show approved posts
                    List<GroupPost> approvedPosts = new ArrayList<>();
                    for (GroupPost post : groupPosts) {
                        if (!"pending".equals(post.getStatus())) {
                            approvedPosts.add(post);
                        }
                    }

                    // Convert GroupPost to Post for PostAdapter
                    List<Post> posts = convertGroupPostsToPosts(approvedPosts);
                    
                    if (loadMore) {
                        postAdapter.addPosts(posts);
                    } else {
                        postAdapter.setPosts(posts);
                    }

                    currentPage++;
                } else {
                    Toast.makeText(GroupDetailActivity.this, 
                            "Không thể tải bài viết", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GroupPost>> call, @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(GroupDetailActivity.this, 
                        "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private List<Post> convertGroupPostsToPosts(List<GroupPost> groupPosts) {
        List<Post> posts = new ArrayList<>();
        for (GroupPost groupPost : groupPosts) {
            Post post = new Post();
            post.setId(groupPost.getId());
            post.setAuthorId(groupPost.getUserId());
            post.setAuthorName(groupPost.getAuthorName());
            
            // Convert authorAvatar mediaId to full URL
            if (groupPost.getAuthorAvatar() != null && !groupPost.getAuthorAvatar().isEmpty()) {
                String avatarUrl = "http://10.0.2.2:8080/api/media/" + groupPost.getAuthorAvatar();
                post.setAuthorAvatar(avatarUrl);
            } else {
                post.setAuthorAvatar(null);
            }
            
            post.setContentText(groupPost.getContent());
            post.setMediaIds(groupPost.getMediaUrls());
            post.setRunId(groupPost.getRunId());
            
            // Debug log
            android.util.Log.d("GroupDetail", "Converting post: " + groupPost.getId() + 
                    ", runId: " + groupPost.getRunId() + 
                    ", content: " + groupPost.getContent());
            
            post.setCreatedAt(groupPost.getCreatedAt());
            post.setLikeCount(groupPost.getLikeCount());
            post.setCommentCount(groupPost.getCommentCount());
            post.setLikedByCurrentUser(groupPost.isLikedByCurrentUser()); // FIXED: Set liked status
            posts.add(post);
        }
        return posts;
    }

    // PostAdapter.OnPostInteractionListener implementations
    @Override
    public void onLikeClick(Post post, int position) {
        // Use GroupApi for group posts
        Call<com.example.runmapproapp.data.model.GroupPost> call = post.isLikedByCurrentUser() ? 
            groupApi.unlikeGroupPost(post.getId()) : 
            groupApi.likeGroupPost(post.getId());
            
        call.enqueue(new Callback<com.example.runmapproapp.data.model.GroupPost>() {
            @Override
            public void onResponse(@NonNull Call<com.example.runmapproapp.data.model.GroupPost> call, @NonNull Response<com.example.runmapproapp.data.model.GroupPost> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.runmapproapp.data.model.GroupPost groupPost = response.body();
                    android.util.Log.d("GroupDetailActivity", "Like response - likedByCurrentUser: " + groupPost.isLikedByCurrentUser() + ", likeCount: " + groupPost.getLikeCount());
                    
                    // Update ONLY like-related fields, don't replace entire Post object
                    post.setLikedByCurrentUser(groupPost.isLikedByCurrentUser());
                    post.setLikeCount(groupPost.getLikeCount());
                    
                    // Use payload to update only like button without rebinding whole view
                    postAdapter.notifyItemChanged(position, "LIKE_UPDATE");
                } else {
                    android.util.Log.e("GroupDetailActivity", "Failed to update like - code: " + response.code());
                    Toast.makeText(GroupDetailActivity.this, "Failed to update like", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.example.runmapproapp.data.model.GroupPost> call, @NonNull Throwable t) {
                android.util.Log.e("GroupDetailActivity", "Like/Unlike error: " + t.getMessage(), t);
                Toast.makeText(GroupDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCommentClick(Post post) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("POST_ID", post.getId());
        intent.putExtra("IS_GROUP_POST", true);
        startActivityForResult(intent, 3); // requestCode 3 for detail view
    }

    @Override
    public void onShareClick(Post post) {
        Toast.makeText(this, "Không thể chia sẻ bài viết trong nhóm", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthorClick(String authorId) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("USER_ID", authorId);
        startActivity(intent);
    }

    @Override
    public void onPostClick(Post post) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("POST_ID", post.getId());
        intent.putExtra("IS_GROUP_POST", true);
        startActivityForResult(intent, 3); // requestCode 3 for detail view
    }

    @Override
    public void onEditPost(Post post, int position) {
        Intent intent = new Intent(this, CreatePostActivity.class);
        intent.putExtra("EDIT_POST_ID", post.getId());
        intent.putExtra("EDIT_POST_CONTENT", post.getContentText());
        startActivityForResult(intent, 2);
    }

    @Override
    public void onDeletePost(Post post, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    groupApi.deleteGroupPost(post.getId()).enqueue(new Callback<java.util.Map<String, String>>() {
                        @Override
                        public void onResponse(@NonNull Call<java.util.Map<String, String>> call, @NonNull Response<java.util.Map<String, String>> response) {
                            if (response.isSuccessful()) {
                                postAdapter.removePost(position);
                                Toast.makeText(GroupDetailActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(GroupDetailActivity.this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<java.util.Map<String, String>> call, @NonNull Throwable t) {
                            Toast.makeText(GroupDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Refresh posts after create (requestCode 1) or edit (requestCode 2)
            currentPage = 0;
            isLastPage = false;
            loadGroupPosts(false);
        } else if (requestCode == 3) {
            // Refresh posts after viewing detail (requestCode 3) - user may have liked/unliked
            currentPage = 0;
            isLastPage = false;
            loadGroupPosts(false);
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
        post.setCreatedAt(groupPost.getCreatedAt());
        post.setLikeCount((int) groupPost.getLikeCount());
        post.setCommentCount((int) groupPost.getCommentCount());
        post.setShareCount(0);
        post.setLikedByCurrentUser(groupPost.isLikedByCurrentUser()); // Use actual status from backend
        
        return post;
    }
}

