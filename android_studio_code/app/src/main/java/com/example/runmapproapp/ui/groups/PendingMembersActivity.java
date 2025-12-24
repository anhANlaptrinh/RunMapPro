package com.example.runmapproapp.ui.groups;

import android.content.Intent;
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

import com.example.runmapproapp.R;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.GroupApi;
import com.example.runmapproapp.data.model.GroupJoinRequest;
import com.example.runmapproapp.ui.groups.adapter.PendingMemberAdapter;
import com.example.runmapproapp.ui.profile.UserProfileActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingMembersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private PendingMemberAdapter adapter;
    private GroupApi groupApi;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_members);

        groupId = getIntent().getStringExtra("GROUP_ID");
        if (groupId == null) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.pending_members_title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerPendingMembers);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendingMemberAdapter(new ArrayList<>(), this::showMemberOptions);
        recyclerView.setAdapter(adapter);

        groupApi = ApiClient.getGroupApi();

        loadPendingRequests();
    }

    private void loadPendingRequests() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        groupApi.getPendingJoinRequests(groupId, 0, 50).enqueue(new Callback<List<GroupJoinRequest>>() {
            @Override
            public void onResponse(@NonNull Call<List<GroupJoinRequest>> call, @NonNull Response<List<GroupJoinRequest>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<GroupJoinRequest> requests = response.body();
                    if (requests.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        adapter.updateData(requests);
                    }
                } else {
                    Toast.makeText(PendingMembersActivity.this, "Không thể tải danh sách", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GroupJoinRequest>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PendingMembersActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMemberOptions(GroupJoinRequest request) {
        String[] options = {
            getString(R.string.accept), 
            getString(R.string.reject), 
            getString(R.string.view_profile)
        };
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.manage_request)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        approveMember(request.getId());
                    } else if (which == 1) {
                        rejectMember(request.getId());
                    } else if (which == 2) {
                        // Open user profile
                        Intent intent = new Intent(PendingMembersActivity.this, UserProfileActivity.class);
                        intent.putExtra("USER_ID", request.getUserId());
                        startActivity(intent);
                    }
                })
                .show();
    }

    private void approveMember(String requestId) {
        progressBar.setVisibility(View.VISIBLE);
        
        groupApi.approveJoinRequest(requestId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(PendingMembersActivity.this, R.string.member_approved, Toast.LENGTH_SHORT).show();
                    loadPendingRequests();
                } else {
                    Toast.makeText(PendingMembersActivity.this, R.string.failed_to_approve, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PendingMembersActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectMember(String requestId) {
        progressBar.setVisibility(View.VISIBLE);
        
        groupApi.rejectJoinRequest(requestId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(PendingMembersActivity.this, R.string.member_rejected, Toast.LENGTH_SHORT).show();
                    loadPendingRequests();
                } else {
                    Toast.makeText(PendingMembersActivity.this, R.string.failed_to_reject, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PendingMembersActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
