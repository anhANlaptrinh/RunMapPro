package com.example.runmapproapp.ui.group;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runmapproapp.R;
import com.example.runmapproapp.api.RetrofitClient;
import com.example.runmapproapp.api.RunApiService;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.api.GroupApi;
import com.example.runmapproapp.data.api.MediaApi;
import com.example.runmapproapp.data.api.MediaUploadResponse;
import com.example.runmapproapp.data.model.GroupPost;
import com.example.runmapproapp.dto.RunResponse;
import com.example.runmapproapp.ui.dashboard.RunsAdapter;
import com.example.runmapproapp.utils.FormatUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.generated.LineLayer;
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap;
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin;
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource;

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
    private Button btnSelectImage, btnSelectRun, btnPost;
    private ProgressBar progressBar;
    private View selectedRunCard;
    private MapView mapViewSelectedRun;
    private TextView tvRunDate, tvRunDistance, tvRunDuration, tvRunPace;
    
    private String groupId;
    private Uri selectedImageUri;
    private List<String> uploadedMediaUrls = new ArrayList<>();
    private RunResponse selectedRun;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_post);
        
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            Toast.makeText(this, "Invalid group", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupListeners();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    private void initViews() {
        etContent = findViewById(R.id.etContent);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSelectRun = findViewById(R.id.btnSelectRun);
        btnPost = findViewById(R.id.btnPost);
        progressBar = findViewById(R.id.progressBar);
        
        // Run card views - from included layout
        selectedRunCard = findViewById(R.id.selectedRunCard);
        if (selectedRunCard != null) {
            mapViewSelectedRun = selectedRunCard.findViewById(R.id.mapViewRun);
            tvRunDate = selectedRunCard.findViewById(R.id.tvRunDate);
            tvRunDistance = selectedRunCard.findViewById(R.id.tvRunDistance);
            tvRunDuration = selectedRunCard.findViewById(R.id.tvRunDuration);
            tvRunPace = selectedRunCard.findViewById(R.id.tvRunPace);
        }
    }
    
    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnSelectRun.setOnClickListener(v -> showRunSelectionDialog());
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
        if (selectedRun != null) {
            body.put("runId", selectedRun.getId());
        }
        
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
                    setResult(RESULT_OK);
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
            public void onResponse(Call<List<RunResponse>> call, Response<List<RunResponse>> response) {
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
            public void onFailure(Call<List<RunResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvNoRuns.setVisibility(View.VISIBLE);
                Toast.makeText(GroupPostActivity.this, "Error loading runs", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displaySelectedRun(RunResponse run) {
        selectedRunCard.setVisibility(View.VISIBLE);
        tvRunDate.setText(FormatUtils.formatDate(run.getStartTime()));
        tvRunDistance.setText(FormatUtils.formatDistance(run.getDistanceMeters()));
        tvRunDuration.setText(FormatUtils.formatDuration(run.getDurationMs()));
        tvRunPace.setText(FormatUtils.formatPace(run.getAvgPaceSecPerKm()));
        
        mapViewSelectedRun.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            if (run.getPath() != null && run.getPath().getCoordinates() != null 
                    && run.getPath().getCoordinates().size() >= 2) {
                
                List<Point> points = new ArrayList<>();
                for (List<Double> coord : run.getPath().getCoordinates()) {
                    if (coord.size() >= 2) {
                        points.add(Point.fromLngLat(coord.get(0), coord.get(1)));
                    }
                }
                
                if (points.size() >= 2) {
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
}
