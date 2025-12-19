package com.example.runmapproapp.ui.groups;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.runmapproapp.R;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.GroupApi;
import com.example.runmapproapp.data.model.GroupPost;
import com.example.runmapproapp.ui.groups.adapter.PendingPostAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingPostsActivity extends AppCompatActivity implements PendingPostAdapter.OnPendingPostActionListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    
    private PendingPostAdapter adapter;
    private GroupApi groupApi;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_posts);

        groupId = getIntent().getStringExtra("GROUP_ID");
        if (groupId == null) {
            Toast.makeText(this, "Invalid group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        setupRecyclerView();
        
        groupApi = ApiClient.getGroupApi();
        loadPendingPosts();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewPendingPosts);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        swipeRefresh.setOnRefreshListener(this::loadPendingPosts);
    }

    private void setupRecyclerView() {
        adapter = new PendingPostAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadPendingPosts() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        groupApi.getPendingPosts(groupId, 0, 50).enqueue(new Callback<List<GroupPost>>() {
            @Override
            public void onResponse(@NonNull Call<List<GroupPost>> call, @NonNull Response<List<GroupPost>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<GroupPost> posts = response.body();
                    adapter.setPosts(posts);
                    
                    if (posts.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(PendingPostsActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GroupPost>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(PendingPostsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onApprove(GroupPost post, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Chấp nhận bài viết")
                .setMessage("Bạn có chắc muốn chấp nhận bài viết này?")
                .setPositiveButton("Chấp nhận", (dialog, which) -> approvePost(post.getId(), position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onReject(GroupPost post, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Từ chối bài viết")
                .setMessage("Bạn có chắc muốn từ chối bài viết này? Bài viết sẽ bị xóa vĩnh viễn.")
                .setPositiveButton("Từ chối", (dialog, which) -> rejectPost(post.getId(), position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void approvePost(String postId, int position) {
        progressBar.setVisibility(View.VISIBLE);
        
        groupApi.approvePost(postId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful()) {
                    Toast.makeText(PendingPostsActivity.this, "Đã chấp nhận bài viết", Toast.LENGTH_SHORT).show();
                    adapter.removePost(position);
                    
                    // Check if list is empty
                    if (adapter.getItemCount() == 0) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(PendingPostsActivity.this, "Failed to approve post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PendingPostsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectPost(String postId, int position) {
        progressBar.setVisibility(View.VISIBLE);
        
        groupApi.rejectPost(postId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful()) {
                    Toast.makeText(PendingPostsActivity.this, "Đã từ chối bài viết", Toast.LENGTH_SHORT).show();
                    adapter.removePost(position);
                    
                    // Check if list is empty
                    if (adapter.getItemCount() == 0) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(PendingPostsActivity.this, "Failed to reject post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PendingPostsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
