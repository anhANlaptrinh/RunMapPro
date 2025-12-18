package com.example.runmapproapp.ui.groups;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runmapproapp.R;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.GroupApi;
import com.example.runmapproapp.data.model.Group;
import com.example.runmapproapp.data.model.GroupMember;
import com.example.runmapproapp.ui.groups.adapter.GroupMemberAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupMembersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private GroupMemberAdapter adapter;
    private GroupApi groupApi;
    private String groupId;
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        groupId = getIntent().getStringExtra("GROUP_ID");
        if (groupId == null) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thành viên nhóm");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerMembers);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupMemberAdapter(new ArrayList<>(), this::showMemberActions);
        recyclerView.setAdapter(adapter);

        groupApi = ApiClient.getGroupApi();

        loadGroupInfo();
        loadMembers();
    }
    
    private void loadGroupInfo() {
        groupApi.getGroup(groupId).enqueue(new Callback<Group>() {
            @Override
            public void onResponse(@NonNull Call<Group> call, @NonNull Response<Group> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUserRole = response.body().getUserRole();
                    adapter.setCurrentUserRole(currentUserRole);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Group> call, @NonNull Throwable t) {
                // Silent fail, just won't show member actions
            }
        });
    }
    
    private void showMemberActions(GroupMember member, String memberName) {
        if (!"owner".equals(currentUserRole)) {
            return; // Only owner can manage members
        }
        
        if ("owner".equals(member.getRole())) {
            Toast.makeText(this, "Không thể quản lý chủ nhóm", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] options;
        if ("admin".equals(member.getRole())) {
            options = new String[]{"Hạ xuống thành viên", "Xóa khỏi nhóm"};
        } else {
            options = new String[]{"Thăng lên Admin", "Xóa khỏi nhóm"};
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Quản lý " + memberName)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Change role
                        String newRole = "admin".equals(member.getRole()) ? "member" : "admin";
                        changeMemberRole(member.getUserId(), newRole);
                    } else if (which == 1) {
                        // Remove member
                        removeMember(member.getUserId(), memberName);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void changeMemberRole(String userId, String newRole) {
        progressBar.setVisibility(View.VISIBLE);
        
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("role", newRole);
        
        groupApi.updateMemberRole(groupId, userId, body).enqueue(new Callback<java.util.Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<java.util.Map<String, String>> call, @NonNull Response<java.util.Map<String, String>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    String roleName = "admin".equals(newRole) ? "Quản trị viên" : "Thành viên";
                    Toast.makeText(GroupMembersActivity.this, "Đã thay đổi chức vụ thành " + roleName, Toast.LENGTH_SHORT).show();
                    loadMembers(); // Reload list
                } else {
                    Toast.makeText(GroupMembersActivity.this, "Không thể thay đổi chức vụ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<java.util.Map<String, String>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupMembersActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void removeMember(String userId, String memberName) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xóa thành viên")
                .setMessage("Bạn có chắc muốn xóa " + memberName + " khỏi nhóm?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    
                    groupApi.removeMember(groupId, userId).enqueue(new Callback<java.util.Map<String, String>>() {
                        @Override
                        public void onResponse(@NonNull Call<java.util.Map<String, String>> call, @NonNull Response<java.util.Map<String, String>> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(GroupMembersActivity.this, "Đã xóa " + memberName + " khỏi nhóm", Toast.LENGTH_SHORT).show();
                                loadMembers(); // Reload list
                            } else {
                                Toast.makeText(GroupMembersActivity.this, "Không thể xóa thành viên", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<java.util.Map<String, String>> call, @NonNull Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(GroupMembersActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadMembers() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        groupApi.getGroupMembers(groupId, 0, 100).enqueue(new Callback<List<GroupMember>>() {
            @Override
            public void onResponse(@NonNull Call<List<GroupMember>> call, @NonNull Response<List<GroupMember>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<GroupMember> members = response.body();
                    if (members.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        adapter.updateData(members);
                    }
                } else {
                    Toast.makeText(GroupMembersActivity.this, "Không thể tải danh sách thành viên", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GroupMember>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupMembersActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
