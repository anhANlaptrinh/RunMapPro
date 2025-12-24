package com.example.runmapproapp.ui.group;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.GroupApi;
import com.example.runmapproapp.data.api.MediaApi;
import com.example.runmapproapp.data.api.MediaUploadResponse;
import com.example.runmapproapp.data.model.Group;
import com.example.runmapproapp.data.model.GroupJoinRequest;
import com.example.runmapproapp.data.model.GroupMember;
import com.example.runmapproapp.data.model.GroupPost;
import com.example.runmapproapp.utils.FileUtils;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupSettingsActivity extends AppCompatActivity {
    
    private TextView tvInviteCode, tvGroupName, tvInviteCodeLabel;
    private LinearLayout layoutInviteCode;
    private CheckBox cbRequireMemberApproval, cbRequirePostApproval;
    private Button btnSaveSettings, btnRegenerateCode, btnCopyCode, btnViewMembers, btnDeleteGroup, btnChangeCoverImage;
    private ImageView ivCoverImage;
    private ProgressBar progressBar;
    
    private String groupId;
    private Group currentGroup;
    private String selectedCoverImageMediaId;
    
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    uploadCoverImage(imageUri);
                }
            }
        }
    );
    
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        isGranted -> {
            if (isGranted) {
                selectCoverImage();
            } else {
                Toast.makeText(this, "Cần quyền truy cập ảnh để chọn ảnh bìa", Toast.LENGTH_SHORT).show();
            }
        }
    );
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);
        
        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            Toast.makeText(this, "Invalid group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        initViews();
        setupListeners();
        loadGroupDetails();
    }

    private void setupToolbar() {
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.group_settings_title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void initViews() {
        tvGroupName = findViewById(R.id.tvGroupName);
        ivCoverImage = findViewById(R.id.ivCoverImage);
        btnChangeCoverImage = findViewById(R.id.btnChangeCoverImage);
        tvInviteCode = findViewById(R.id.tvInviteCode);
        tvInviteCodeLabel = findViewById(R.id.tvInviteCodeLabel);
        layoutInviteCode = findViewById(R.id.layoutInviteCode);
        cbRequireMemberApproval = findViewById(R.id.cbRequireMemberApproval);
        cbRequirePostApproval = findViewById(R.id.cbRequirePostApproval);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnRegenerateCode = findViewById(R.id.btnRegenerateCode);
        btnCopyCode = findViewById(R.id.btnCopyCode);
        btnViewMembers = findViewById(R.id.btnViewMembers);
        btnDeleteGroup = findViewById(R.id.btnDeleteGroup);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupListeners() {
        btnChangeCoverImage.setOnClickListener(v -> selectCoverImage());
        btnSaveSettings.setOnClickListener(v -> saveSettings());
        btnRegenerateCode.setOnClickListener(v -> regenerateInviteCode());
        btnCopyCode.setOnClickListener(v -> copyInviteCode());
        btnViewMembers.setOnClickListener(v -> showMembers());
        btnDeleteGroup.setOnClickListener(v -> confirmDeleteGroup());
    }
    
    private void loadGroupDetails() {
        progressBar.setVisibility(View.VISIBLE);
        GroupApi groupApi = ApiClient.getGroupApi();
        groupApi.getGroup(groupId).enqueue(new Callback<Group>() {
            @Override
            public void onResponse(Call<Group> call, Response<Group> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentGroup = response.body();
                    
                    // Debug logging
                    android.util.Log.d("GroupSettings", "Privacy: " + currentGroup.getPrivacy());
                    android.util.Log.d("GroupSettings", "InviteCode: " + currentGroup.getInviteCode());
                    
                    displayGroupInfo();
                } else {
                    Toast.makeText(GroupSettingsActivity.this, "Failed to load group", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Group> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupSettingsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayGroupInfo() {
        tvGroupName.setText(currentGroup.getName());
        
        // Load cover image
        if (currentGroup.getCoverImageUrl() != null && !currentGroup.getCoverImageUrl().isEmpty()) {
            String coverImageUrl = "http://10.0.2.2:8080/api/media/" + currentGroup.getCoverImageUrl();
            Glide.with(this)
                .load(coverImageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(ivCoverImage);
        }
        
        // Check privacy (case-insensitive)
        if (currentGroup.getPrivacy() != null && 
            currentGroup.getPrivacy().equalsIgnoreCase("private")) {
            // Show invite code section for private groups
            if (currentGroup.getInviteCode() != null && !currentGroup.getInviteCode().isEmpty()) {
                // Display code with expiration info
                String codeDisplay = currentGroup.getInviteCode();
                if (currentGroup.getInviteCodeExpiresAt() != null) {
                    String expiryDate = formatExpirationDate(currentGroup.getInviteCodeExpiresAt());
                    codeDisplay = currentGroup.getInviteCode() + "\n" + getString(R.string.expires_on, expiryDate);
                }
                tvInviteCode.setText(codeDisplay);
                tvInviteCodeLabel.setVisibility(View.VISIBLE);
                layoutInviteCode.setVisibility(View.VISIBLE);
                btnRegenerateCode.setVisibility(View.VISIBLE);
            } else {
                // No code yet - show button to generate
                tvInviteCode.setText(R.string.no_invite_code);
                tvInviteCodeLabel.setVisibility(View.VISIBLE);
                layoutInviteCode.setVisibility(View.VISIBLE);
                btnRegenerateCode.setText(R.string.generate_invite_code);
                btnRegenerateCode.setVisibility(View.VISIBLE);
            }
        } else {
            // Hide for public groups
            tvInviteCodeLabel.setVisibility(View.GONE);
            layoutInviteCode.setVisibility(View.GONE);
            btnRegenerateCode.setVisibility(View.GONE);
        }
        
        cbRequireMemberApproval.setChecked(currentGroup.isRequireMemberApproval());
        cbRequirePostApproval.setChecked(currentGroup.isRequirePostApproval());
    }
    
    private String formatExpirationDate(String isoDate) {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
            inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date date = inputFormat.parse(isoDate);
            
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US);
            return outputFormat.format(date);
        } catch (Exception e) {
            return "N/A";
        }
    }
    
    private void generateInviteCodeSilently() {
        progressBar.setVisibility(View.VISIBLE);
        
        Map<String, Object> settings = new HashMap<>();
        settings.put("regenerateInviteCode", true);
        
        GroupApi groupApi = ApiClient.getGroupApi();
        groupApi.updateGroupSettings(groupId, settings).enqueue(new Callback<Group>() {
            @Override
            public void onResponse(Call<Group> call, Response<Group> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentGroup = response.body();
                    displayGroupInfo();
                    Toast.makeText(GroupSettingsActivity.this, R.string.invite_code_generated, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GroupSettingsActivity.this, R.string.failed_generate_code, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Group> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupSettingsActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void copyInviteCode() {
        if (currentGroup != null && currentGroup.getInviteCode() != null) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.invite_code), currentGroup.getInviteCode());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.invite_code_copied, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveSettings() {
        progressBar.setVisibility(View.VISIBLE);
        
        Map<String, Object> settings = new HashMap<>();
        settings.put("requireMemberApproval", cbRequireMemberApproval.isChecked());
        settings.put("requirePostApproval", cbRequirePostApproval.isChecked());
        
        GroupApi groupApi = ApiClient.getGroupApi();
        groupApi.updateGroupSettings(groupId, settings).enqueue(new Callback<Group>() {
            @Override
            public void onResponse(Call<Group> call, Response<Group> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    currentGroup = response.body();
                    Toast.makeText(GroupSettingsActivity.this, R.string.settings_updated, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GroupSettingsActivity.this, R.string.failed_update_settings, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Group> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupSettingsActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void regenerateInviteCode() {
        // If no code exists, generate without confirmation
        if (currentGroup.getInviteCode() == null || currentGroup.getInviteCode().isEmpty()) {
            generateInviteCodeSilently();
            return;
        }
        
        // If code exists, confirm before regenerating
        new AlertDialog.Builder(this)
            .setTitle(R.string.regenerate_code_title)
            .setMessage(R.string.regenerate_code_message)
            .setPositiveButton(R.string.yes, (dialog, which) -> generateInviteCodeSilently())
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    private void showMembers() {
        Intent intent = new Intent(this, com.example.runmapproapp.ui.groups.GroupMembersActivity.class);
        intent.putExtra("GROUP_ID", groupId);
        startActivity(intent);
    }
    
    private void showMembersDialog(List<GroupMember> members) {
        String[] items = new String[members.size()];
        for (int i = 0; i < members.size(); i++) {
            GroupMember member = members.get(i);
            items[i] = "User: " + member.getUserId() + " (" + member.getRole() + ")";
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Group Members (" + members.size() + ")")
            .setItems(items, (dialog, which) -> {
                GroupMember selectedMember = members.get(which);
                showMemberActions(selectedMember);
            })
            .setNegativeButton("Close", null)
            .show();
    }
    
    private void showMemberActions(GroupMember member) {
        if ("owner".equals(member.getRole())) {
            Toast.makeText(this, "Cannot modify owner", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] actions = {"Make Admin", "Make Member", "Remove"};
        
        new AlertDialog.Builder(this)
            .setTitle("Member Actions")
            .setItems(actions, (dialog, which) -> {
                switch (which) {
                    case 0:
                        updateMemberRole(member.getUserId(), "admin");
                        break;
                    case 1:
                        updateMemberRole(member.getUserId(), "member");
                        break;
                    case 2:
                        removeMember(member.getUserId());
                        break;
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void updateMemberRole(String userId, String role) {
        Map<String, String> body = new HashMap<>();
        body.put("role", role);
        
        GroupApi groupApi = ApiClient.getGroupApi();
        groupApi.updateMemberRole(groupId, userId, body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GroupSettingsActivity.this, "Role updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GroupSettingsActivity.this, "Failed to update role", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(GroupSettingsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void removeMember(String userId) {
        new AlertDialog.Builder(this)
            .setTitle("Remove Member")
            .setMessage("Are you sure?")
            .setPositiveButton("Yes", (dialog, which) -> {
                GroupApi groupApi = ApiClient.getGroupApi();
                groupApi.removeMember(groupId, userId).enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(GroupSettingsActivity.this, "Member removed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GroupSettingsActivity.this, "Failed to remove member", Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        Toast.makeText(GroupSettingsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    private void showJoinRequests() {
        GroupApi groupApi = ApiClient.getGroupApi();
        groupApi.getPendingJoinRequests(groupId, 0, 50).enqueue(new Callback<List<GroupJoinRequest>>() {
            @Override
            public void onResponse(Call<List<GroupJoinRequest>> call, Response<List<GroupJoinRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showJoinRequestsDialog(response.body());
                } else {
                    Toast.makeText(GroupSettingsActivity.this, "Failed to load requests", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<GroupJoinRequest>> call, Throwable t) {
                Toast.makeText(GroupSettingsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showJoinRequestsDialog(List<GroupJoinRequest> requests) {
        if (requests.isEmpty()) {
            Toast.makeText(this, "No pending requests", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] items = new String[requests.size()];
        for (int i = 0; i < requests.size(); i++) {
            items[i] = "User: " + requests.get(i).getUserId();
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Join Requests (" + requests.size() + ")")
            .setItems(items, (dialog, which) -> {
                GroupJoinRequest request = requests.get(which);
                showJoinRequestActions(request);
            })
            .setNegativeButton("Close", null)
            .show();
    }
    
    private void showJoinRequestActions(GroupJoinRequest request) {
        new AlertDialog.Builder(this)
            .setTitle("Approve this request?")
            .setPositiveButton("Approve", (dialog, which) -> approveJoinRequest(request.getId()))
            .setNegativeButton("Reject", (dialog, which) -> rejectJoinRequest(request.getId()))
            .setNeutralButton("Cancel", null)
            .show();
    }
    
    private void approveJoinRequest(String requestId) {
        GroupApi groupApi = ApiClient.getGroupApi();
        groupApi.approveJoinRequest(requestId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GroupSettingsActivity.this, "Request approved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GroupSettingsActivity.this, "Failed to approve", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(GroupSettingsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void rejectJoinRequest(String requestId) {
        GroupApi groupApi = ApiClient.getGroupApi();
        groupApi.rejectJoinRequest(requestId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GroupSettingsActivity.this, "Request rejected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GroupSettingsActivity.this, "Failed to reject", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(GroupSettingsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showPendingPosts() {
        GroupApi groupApi = ApiClient.getGroupApi();
        groupApi.getPendingPosts(groupId, 0, 50).enqueue(new Callback<List<GroupPost>>() {
            @Override
            public void onResponse(Call<List<GroupPost>> call, Response<List<GroupPost>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showPendingPostsDialog(response.body());
                } else {
                    Toast.makeText(GroupSettingsActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<GroupPost>> call, Throwable t) {
                Toast.makeText(GroupSettingsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showPendingPostsDialog(List<GroupPost> posts) {
        if (posts.isEmpty()) {
            Toast.makeText(this, "No pending posts", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] items = new String[posts.size()];
        for (int i = 0; i < posts.size(); i++) {
            String content = posts.get(i).getContent();
            items[i] = content.length() > 50 ? content.substring(0, 47) + "..." : content;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Pending Posts (" + posts.size() + ")")
            .setItems(items, (dialog, which) -> {
                GroupPost post = posts.get(which);
                showPostActions(post);
            })
            .setNegativeButton("Close", null)
            .show();
    }
    
    private void showPostActions(GroupPost post) {
        new AlertDialog.Builder(this)
            .setTitle("Approve this post?")
            .setMessage(post.getContent())
            .setPositiveButton("Approve", (dialog, which) -> approvePost(post.getId()))
            .setNegativeButton("Reject", (dialog, which) -> rejectPost(post.getId()))
            .setNeutralButton("Cancel", null)
            .show();
    }
    
    private void approvePost(String postId) {
        GroupApi groupApi = ApiClient.getGroupApi();
        groupApi.approvePost(postId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GroupSettingsActivity.this, "Post approved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GroupSettingsActivity.this, "Failed to approve", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(GroupSettingsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void rejectPost(String postId) {
        GroupApi groupApi = ApiClient.getGroupApi();
        groupApi.rejectPost(postId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(GroupSettingsActivity.this, "Post rejected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GroupSettingsActivity.this, "Failed to reject", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(GroupSettingsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void selectCoverImage() {
        // Check permission for Android 13+ (READ_MEDIA_IMAGES) or older (READ_EXTERNAL_STORAGE)
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU 
            ? Manifest.permission.READ_MEDIA_IMAGES 
            : Manifest.permission.READ_EXTERNAL_STORAGE;
        
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }
    
    private void uploadCoverImage(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        btnChangeCoverImage.setEnabled(false);
        
        try {
            java.io.File file = FileUtils.getFileFromUri(this, imageUri);
            if (file == null) {
                Toast.makeText(this, "Không thể đọc file ảnh", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnChangeCoverImage.setEnabled(true);
                return;
            }
            
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
            
            MediaApi mediaApi = ApiClient.getMediaApi();
            mediaApi.uploadMedia(body).enqueue(new Callback<MediaUploadResponse>() {
                @Override
                public void onResponse(Call<MediaUploadResponse> call, Response<MediaUploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String mediaId = response.body().getMediaId();
                        selectedCoverImageMediaId = mediaId;
                        
                        // Update cover image immediately
                        updateCoverImage(mediaId);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnChangeCoverImage.setEnabled(true);
                        Toast.makeText(GroupSettingsActivity.this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<MediaUploadResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    btnChangeCoverImage.setEnabled(true);
                    Toast.makeText(GroupSettingsActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            btnChangeCoverImage.setEnabled(true);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateCoverImage(String mediaId) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("coverImageUrl", mediaId);
        
        GroupApi groupApi = ApiClient.getGroupApi();
        groupApi.updateGroupSettings(groupId, settings).enqueue(new Callback<Group>() {
            @Override
            public void onResponse(Call<Group> call, Response<Group> response) {
                progressBar.setVisibility(View.GONE);
                btnChangeCoverImage.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    currentGroup = response.body();
                    
                    // Update UI with new cover image
                    String coverImageUrl = "http://10.0.2.2:8080/api/media/" + mediaId;
                    Glide.with(GroupSettingsActivity.this)
                        .load(coverImageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(ivCoverImage);
                    
                    Toast.makeText(GroupSettingsActivity.this, "Đã cập nhật ảnh bìa", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GroupSettingsActivity.this, "Không thể cập nhật ảnh bìa", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Group> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnChangeCoverImage.setEnabled(true);
                Toast.makeText(GroupSettingsActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void confirmDeleteGroup() {
        new AlertDialog.Builder(this)
            .setTitle("Xóa nhóm")
            .setMessage("Bạn có chắc chắn muốn xóa nhóm này? Hành động này không thể hoàn tác!")
            .setPositiveButton("Xóa", (dialog, which) -> deleteGroup())
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void deleteGroup() {
        progressBar.setVisibility(View.VISIBLE);
        btnDeleteGroup.setEnabled(false);
        
        GroupApi groupApi = ApiClient.getGroupApi();
        groupApi.deleteGroup(groupId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                progressBar.setVisibility(View.GONE);
                btnDeleteGroup.setEnabled(true);
                
                if (response.isSuccessful()) {
                    Toast.makeText(GroupSettingsActivity.this, "Đã xóa nhóm", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(GroupSettingsActivity.this, "Không thể xóa nhóm", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnDeleteGroup.setEnabled(true);
                Toast.makeText(GroupSettingsActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
