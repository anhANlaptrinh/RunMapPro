package com.example.runmapproapp.ui.social;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.MediaApi;
import com.example.runmapproapp.data.api.MediaUploadResponse;
import com.example.runmapproapp.data.api.PostApi;
import com.example.runmapproapp.data.model.CreatePostRequest;
import com.example.runmapproapp.data.model.Post;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etPostContent;
    private ImageView ivSelectedImage;
    private Button btnSelectImage;
    private Button btnPublish;
    private ProgressBar progressBar;
    private MaterialCardView cardOriginalPost;
    private TextView tvOriginalAuthor;
    private TextView tvOriginalContent;

    private Uri selectedImageUri;
    private String sharePostId;
    private Post originalPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Post");
        }

        initViews();
        setupListeners();

        // Check if sharing a post
        sharePostId = getIntent().getStringExtra("SHARE_POST_ID");
        if (sharePostId != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Share Post");
            }
            etPostContent.setHint("Add your thoughts (optional)...");
            loadOriginalPost();
        }
    }

    private void loadOriginalPost() {
        PostApi postApi = ApiClient.getPostApi();
        postApi.getPost(sharePostId).enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NonNull Call<Post> call, @NonNull Response<Post> response) {
                if (response.isSuccessful() && response.body() != null) {
                    originalPost = response.body();
                    displayOriginalPost(originalPost);
                } else {
                    Toast.makeText(CreatePostActivity.this, "Failed to load original post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                Toast.makeText(CreatePostActivity.this, "Error loading post: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOriginalPost(Post post) {
        cardOriginalPost.setVisibility(View.VISIBLE);
        
        String authorName = post.getAuthorName();
        if (authorName == null || authorName.isEmpty()) {
            authorName = "User " + post.getAuthorId().substring(0, Math.min(8, post.getAuthorId().length()));
        }
        tvOriginalAuthor.setText(authorName);
        
        String content = post.getContentText();
        if (content != null && !content.isEmpty()) {
            tvOriginalContent.setText(content);
        } else {
            tvOriginalContent.setText("[No text content]");
        }
    }

    private void initViews() {
        etPostContent = findViewById(R.id.etPostContent);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnPublish = findViewById(R.id.btnPublish);
        progressBar = findViewById(R.id.progressBar);
        cardOriginalPost = findViewById(R.id.cardOriginalPost);
        tvOriginalAuthor = findViewById(R.id.tvOriginalAuthor);
        tvOriginalContent = findViewById(R.id.tvOriginalContent);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnPublish.setOnClickListener(v -> publishPost());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivSelectedImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(selectedImageUri).into(ivSelectedImage);
        }
    }

    private void publishPost() {
        String content = etPostContent.getText().toString().trim();

        // Allow empty content only when sharing a post
        if (content.isEmpty() && selectedImageUri == null && sharePostId == null) {
            Toast.makeText(this, "Please add content or image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnPublish.setEnabled(false);

        // If image is selected, upload it first
        if (selectedImageUri != null) {
            uploadImageAndCreatePost(content);
        } else {
            createPost(content, new ArrayList<>());
        }
    }

    private void uploadImageAndCreatePost(String content) {
        try {
            // Create a temporary file from the URI
            File file = createFileFromUri(selectedImageUri);
            if (file == null) {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnPublish.setEnabled(true);
                return;
            }

            // Create request body
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

            // Upload to server
            MediaApi mediaApi = ApiClient.getClient().create(MediaApi.class);
            mediaApi.uploadMedia(filePart).enqueue(new Callback<MediaUploadResponse>() {
                @Override
                public void onResponse(@NonNull Call<MediaUploadResponse> call, 
                                       @NonNull Response<MediaUploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String mediaId = response.body().getMediaId();
                        List<String> mediaIds = new ArrayList<>();
                        mediaIds.add(mediaId);
                        createPost(content, mediaIds);
                    } else {
                        Toast.makeText(CreatePostActivity.this, 
                                "Failed to upload image", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        btnPublish.setEnabled(true);
                    }
                    
                    // Clean up temp file
                    file.delete();
                }

                @Override
                public void onFailure(@NonNull Call<MediaUploadResponse> call, @NonNull Throwable t) {
                    Toast.makeText(CreatePostActivity.this, 
                            "Error uploading image: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btnPublish.setEnabled(true);
                    file.delete();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnPublish.setEnabled(true);
        }
    }

    private File createFileFromUri(Uri uri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            String fileName = getFileName(uri);
            if (fileName == null) {
                fileName = "image_" + System.currentTimeMillis() + ".jpg";
            }

            File file = new File(getCacheDir(), fileName);
            InputStream inputStream = contentResolver.openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void createPost(String content, List<String> mediaIds) {
        CreatePostRequest request = new CreatePostRequest(content, mediaIds, null);
        PostApi postApi = ApiClient.getPostApi();

        Call<Post> call;
        if (sharePostId != null) {
            // Share existing post
            call = postApi.sharePost(sharePostId, request);
        } else {
            // Create new post
            call = postApi.createPost(request);
        }

        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NonNull Call<Post> call, @NonNull Response<Post> response) {
                progressBar.setVisibility(View.GONE);
                btnPublish.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(CreatePostActivity.this, "Post published!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreatePostActivity.this, "Failed to publish post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnPublish.setEnabled(true);
                Toast.makeText(CreatePostActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
