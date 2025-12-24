package com.example.runmapproapp.ui.social;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.R;
import com.example.runmapproapp.api.RetrofitClient;
import com.example.runmapproapp.api.RunApiService;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.MediaApi;
import com.example.runmapproapp.data.api.MediaUploadResponse;
import com.example.runmapproapp.data.api.PostApi;
import com.example.runmapproapp.data.model.CreatePostRequest;
import com.example.runmapproapp.data.model.Post;
import com.example.runmapproapp.dto.RunResponse;
import com.example.runmapproapp.ui.dashboard.RunsAdapter;
import com.example.runmapproapp.utils.FormatUtils;
import com.google.android.material.card.MaterialCardView;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.generated.LineLayer;
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap;
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin;
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource;
import com.mapbox.geojson.Feature;

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

    private static final String TAG = "CreatePostActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etPostContent;
    private ImageView ivSelectedImage;
    private Button btnSelectImage;
    private Button btnSelectRun;
    private Button btnPublish;
    private ProgressBar progressBar;
    private MaterialCardView cardOriginalPost;
    private TextView tvOriginalAuthor;
    private TextView tvOriginalContent;
    private View selectedRunCard;
    private MapView mapViewSelectedRun;
    private TextView tvRunDate, tvRunDistance, tvRunDuration, tvRunPace;

    private Uri selectedImageUri;
    private String sharePostId;
    private Post originalPost;
    private RunResponse selectedRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.create_post_title);
        }

        initViews();
        setupListeners();

        // Check if sharing a post
        sharePostId = getIntent().getStringExtra("SHARE_POST_ID");
        if (sharePostId != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.share_post_title);
            }
            etPostContent.setHint(R.string.share_post_hint);
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
                    Toast.makeText(CreatePostActivity.this, R.string.failed_to_load_original_post, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                Toast.makeText(CreatePostActivity.this, getString(R.string.error_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
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
        btnSelectRun = findViewById(R.id.btnSelectRun);
        btnPublish = findViewById(R.id.btnPublish);
        progressBar = findViewById(R.id.progressBar);
        cardOriginalPost = findViewById(R.id.cardOriginalPost);
        tvOriginalAuthor = findViewById(R.id.tvOriginalAuthor);
        tvOriginalContent = findViewById(R.id.tvOriginalContent);
        
        selectedRunCard = findViewById(R.id.selectedRunCard);
        mapViewSelectedRun = selectedRunCard.findViewById(R.id.mapViewRun);
        tvRunDate = selectedRunCard.findViewById(R.id.tvRunDate);
        tvRunDistance = selectedRunCard.findViewById(R.id.tvRunDistance);
        tvRunDuration = selectedRunCard.findViewById(R.id.tvRunDuration);
        tvRunPace = selectedRunCard.findViewById(R.id.tvRunPace);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnSelectRun.setOnClickListener(v -> showRunSelectionDialog());
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

        // Allow empty content only when sharing a post or attaching run
        if (content.isEmpty() && selectedImageUri == null && sharePostId == null && selectedRun == null) {
            Toast.makeText(this, "Please add content, image, or run", Toast.LENGTH_SHORT).show();
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
        String runId = selectedRun != null ? selectedRun.getId() : null;
        Log.d(TAG, "Creating post with runId: " + runId + ", content: " + content + ", mediaIds: " + mediaIds);
        
        CreatePostRequest request = new CreatePostRequest(content, mediaIds, null, runId);
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
                    Log.d(TAG, "Post published successfully!");
                    Toast.makeText(CreatePostActivity.this, R.string.post_published, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Failed: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Failed to publish post: " + errorMsg);
                    Toast.makeText(CreatePostActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnPublish.setEnabled(true);
                Log.e(TAG, "Network error publishing post", t);
                Toast.makeText(CreatePostActivity.this, getString(R.string.error_prefix, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRunSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_run, null);
        builder.setView(dialogView);

        RecyclerView rvRuns = dialogView.findViewById(R.id.rvRuns);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        TextView tvNoRuns = dialogView.findViewById(R.id.tvNoRuns);

        rvRuns.setLayoutManager(new LinearLayoutManager(this));

        AlertDialog dialog = builder.create();
        dialog.show();

        // Load runs
        progressBar.setVisibility(View.VISIBLE);
        RunApiService runApiService = RetrofitClient.getRunApiService();
        runApiService.getRuns().enqueue(new Callback<List<RunResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<RunResponse>> call, @NonNull Response<List<RunResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<RunResponse> runs = response.body();
                    
                    // Create adapter with click listener
                    RunsAdapter adapter = new RunsAdapter((run, position) -> {
                        selectedRun = run;
                        displaySelectedRun(run);
                        dialog.dismiss();
                    });
                    adapter.setRuns(runs);
                    rvRuns.setAdapter(adapter);
                } else {
                    tvNoRuns.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RunResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvNoRuns.setVisibility(View.VISIBLE);
                Toast.makeText(CreatePostActivity.this, "Failed to load runs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displaySelectedRun(RunResponse run) {
        selectedRunCard.setVisibility(View.VISIBLE);
        
        // Format and display run data
        tvRunDate.setText(FormatUtils.formatDate(run.getStartTime()));
        tvRunDistance.setText(FormatUtils.formatDistance(run.getDistanceMeters()));
        tvRunDuration.setText(FormatUtils.formatDuration(run.getDurationMs()));
        tvRunPace.setText(FormatUtils.formatPace(run.getAvgPaceSecPerKm()));

        // Initialize and display map
        mapViewSelectedRun.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
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
                    // Create and display route
                    LineString lineString = LineString.fromLngLats(points);
                    Feature feature = Feature.fromGeometry(lineString);
                    
                    GeoJsonSource source = new GeoJsonSource.Builder("run-source")
                            .feature(feature)
                            .build();
                    source.bindTo(style);

                    LineLayer lineLayer = new LineLayer("run-layer", "run-source");
                    lineLayer.lineColor("#FF5722");
                    lineLayer.lineWidth(4.0);
                    lineLayer.lineCap(LineCap.ROUND);
                    lineLayer.lineJoin(LineJoin.ROUND);
                    lineLayer.bindTo(style);

                    // Center camera
                    Point centerPoint = points.get(points.size() / 2);
                    mapViewSelectedRun.getMapboxMap().setCamera(
                            new CameraOptions.Builder()
                                    .center(centerPoint)
                                    .zoom(13.0)
                                    .build()
                    );
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapViewSelectedRun != null) {
            mapViewSelectedRun.onDestroy();
        }
    }
}
