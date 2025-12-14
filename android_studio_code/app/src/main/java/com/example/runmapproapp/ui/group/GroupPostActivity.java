package com.example.runmapproapp.ui.group;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.runmapproapp.R;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.GroupApi;
import com.example.runmapproapp.data.api.MediaApi;
import com.example.runmapproapp.data.api.MediaUploadResponse;
import com.example.runmapproapp.data.model.GroupPost;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupPostActivity extends AppCompatActivity {
    
    private static final int PICK_IMAGE_REQUEST = 1;
    
    private EditText etContent;
    private ImageView ivSelectedImage;
    private Button btnSelectImage, btnPost;
    private ProgressBar progressBar;
    
    private String groupId;
    private Uri selectedImageUri;
    private List<String> uploadedMediaUrls = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_post);
        
        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            Toast.makeText(this, "Invalid group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupListeners();
    }
    
    private void initViews() {
        etContent = findViewById(R.id.etContent);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnPost = findViewById(R.id.btnPost);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnPost.setOnClickListener(v -> createPost());
    }
    
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivSelectedImage.setImageURI(selectedImageUri);
            ivSelectedImage.setVisibility(View.VISIBLE);
        }
    }
    
    private void createPost() {
        String content = etContent.getText().toString().trim();
        
        if (content.isEmpty() && selectedImageUri == null) {
            Toast.makeText(this, "Please add content or image", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        btnPost.setEnabled(false);
        
        if (selectedImageUri != null) {
            uploadImage();
        } else {
            submitPost(content, uploadedMediaUrls);
        }
    }
    
    private void uploadImage() {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            File file = new File(getCacheDir(), "upload_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
            
            MediaApi mediaApi = ApiClient.getClient().create(MediaApi.class);
            mediaApi.uploadMedia(body).enqueue(new Callback<MediaUploadResponse>() {
                @Override
                public void onResponse(Call<MediaUploadResponse> call, Response<MediaUploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        uploadedMediaUrls.add(response.body().getMediaId());
                        submitPost(etContent.getText().toString().trim(), uploadedMediaUrls);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnPost.setEnabled(true);
                        Toast.makeText(GroupPostActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<MediaUploadResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    btnPost.setEnabled(true);
                    Toast.makeText(GroupPostActivity.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            btnPost.setEnabled(true);
            Toast.makeText(this, "Error reading image", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void submitPost(String content, List<String> mediaUrls) {
        Map<String, Object> body = new HashMap<>();
        body.put("content", content);
        body.put("mediaUrls", mediaUrls);
        
        GroupApi groupApi = ApiClient.getGroupApi();
        groupApi.createGroupPost(groupId, body).enqueue(new Callback<GroupPost>() {
            @Override
            public void onResponse(Call<GroupPost> call, Response<GroupPost> response) {
                progressBar.setVisibility(View.GONE);
                btnPost.setEnabled(true);
                
                if (response.isSuccessful()) {
                    GroupPost post = response.body();
                    if (post != null && "pending".equals(post.getStatus())) {
                        Toast.makeText(GroupPostActivity.this, "Post submitted for approval", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GroupPostActivity.this, "Post created successfully", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                } else {
                    Toast.makeText(GroupPostActivity.this, "Failed to create post", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<GroupPost> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnPost.setEnabled(true);
                Toast.makeText(GroupPostActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
