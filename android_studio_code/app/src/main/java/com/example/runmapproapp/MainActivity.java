package com.example.runmapproapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runmapproapp.api.RetrofitClient;
import com.example.runmapproapp.api.RunApiService;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.dto.GeoJsonLineStringDto;
import com.example.runmapproapp.dto.RunResponse;
import com.example.runmapproapp.ui.dashboard.RunsAdapter;
import com.example.runmapproapp.utils.FormatUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String ROUTE_SOURCE_ID = "route-source";
    private static final String ROUTE_LAYER_ID = "route-layer";

    // Auth
    private AuthManager authManager;
    private RunApiService runApiService;

    // UI - Loading & Error
    private ProgressBar progressBar;
    private TextView tvError;
    private TextView tvEmptyState;

    // UI - Summary
    private TextView tvTotalDistance;
    private TextView tvTotalDuration;
    private TextView tvTotalCalories;
    private TextView tvTotalSteps;
    private TextView tvTotalRuns;

    // UI - Personal Records
    private TextView tvLongestRun;
    private TextView tvFastestAvgPace;
    private TextView tvBestPace;
    private TextView tvMostSteps;

    // UI - Map Preview
    private MapView mapView;
    private MaterialCardView cardMapPreview;
    private View layoutSelectedRunStats;
    private TextView tvMapEmptyState;
    private TextView tvSelectedRunDate;
    private TextView tvSelectedRunDistance;
    private TextView tvSelectedRunDuration;
    private TextView tvSelectedRunAvgPace;
    private TextView tvSelectedRunBestPace;
    private TextView tvSelectedRunCalories;
    private TextView tvSelectedRunSteps;

    // UI - Recent Runs
    private RecyclerView recyclerViewRuns;
    private RunsAdapter runsAdapter;
    private MaterialButton btnRefresh;

    // Data
    private List<RunResponse> allRuns = new ArrayList<>();
    private RunResponse selectedRun = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dashboard);

        authManager = new AuthManager(this);
        runApiService = RetrofitClient.getRunApiService();

        if (!authManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        setupToolbar();
        initViews();
        setupRecyclerView();
        setupListeners();
        
        // Load data
        loadRuns();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (authManager != null && !authManager.isLoggedIn()) {
            redirectToLogin();
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        // Loading & Error
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Summary
        tvTotalDistance = findViewById(R.id.tvTotalDistance);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvTotalSteps = findViewById(R.id.tvTotalSteps);
        tvTotalRuns = findViewById(R.id.tvTotalRuns);

        // Personal Records
        tvLongestRun = findViewById(R.id.tvLongestRun);
        tvFastestAvgPace = findViewById(R.id.tvFastestAvgPace);
        tvBestPace = findViewById(R.id.tvBestPace);
        tvMostSteps = findViewById(R.id.tvMostSteps);

        // Map Preview
        mapView = findViewById(R.id.mapView);
        cardMapPreview = findViewById(R.id.cardMapPreview);
        layoutSelectedRunStats = findViewById(R.id.layoutSelectedRunStats);
        tvMapEmptyState = findViewById(R.id.tvMapEmptyState);
        tvSelectedRunDate = findViewById(R.id.tvSelectedRunDate);
        tvSelectedRunDistance = findViewById(R.id.tvSelectedRunDistance);
        tvSelectedRunDuration = findViewById(R.id.tvSelectedRunDuration);
        tvSelectedRunAvgPace = findViewById(R.id.tvSelectedRunAvgPace);
        tvSelectedRunBestPace = findViewById(R.id.tvSelectedRunBestPace);
        tvSelectedRunCalories = findViewById(R.id.tvSelectedRunCalories);
        tvSelectedRunSteps = findViewById(R.id.tvSelectedRunSteps);

        // Recent Runs
        recyclerViewRuns = findViewById(R.id.recyclerViewRuns);
        btnRefresh = findViewById(R.id.btnRefresh);

        // Initialize Mapbox
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
    }

    private void setupRecyclerView() {
        runsAdapter = new RunsAdapter((run, position) -> {
            // On run clicked
            selectRun(run, position);
        });
        
        recyclerViewRuns.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRuns.setAdapter(runsAdapter);
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> loadRuns());
    }

    private void loadRuns() {
        showLoading(true);
        tvError.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        runApiService.getRuns().enqueue(new Callback<List<RunResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<RunResponse>> call, @NonNull Response<List<RunResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    allRuns = response.body();
                    Log.d(TAG, "Loaded " + allRuns.size() + " runs");

                    if (allRuns.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        // Hide map preview when no runs
                        if (cardMapPreview != null) {
                            cardMapPreview.setVisibility(View.GONE);
                        }
                    } else {
                        // Show map preview when have runs
                        if (cardMapPreview != null) {
                            cardMapPreview.setVisibility(View.VISIBLE);
                        }
                        updateSummary();
                        updatePersonalRecords();
                        updateRecentRunsList();
                        
                        // Auto-select first run
                        if (!allRuns.isEmpty()) {
                            selectRun(allRuns.get(0), 0);
                        }
                    }
                } else {
                    showError("Failed to load runs: " + response.code());
                    Log.e(TAG, "Error loading runs: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RunResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Network error loading runs", t);
            }
        });
    }

    private void updateSummary() {
        double totalDistance = 0;
        long totalDuration = 0;
        double totalCalories = 0;
        int totalSteps = 0;

        for (RunResponse run : allRuns) {
            totalDistance += run.getDistanceMeters();
            totalDuration += run.getDurationMs();
            totalCalories += run.getCalories();
            totalSteps += run.getSteps();
        }

        tvTotalDistance.setText(FormatUtils.formatDistance(totalDistance));
        tvTotalDuration.setText(FormatUtils.formatDuration(totalDuration));
        tvTotalCalories.setText(String.valueOf((int) totalCalories));
        tvTotalSteps.setText(FormatUtils.formatNumber(totalSteps));
        tvTotalRuns.setText(String.valueOf(allRuns.size()));
    }

    private void updatePersonalRecords() {
        if (allRuns.isEmpty()) return;

        // Longest run
        RunResponse longestRun = allRuns.get(0);
        for (RunResponse run : allRuns) {
            if (run.getDistanceMeters() > longestRun.getDistanceMeters()) {
                longestRun = run;
            }
        }
        tvLongestRun.setText(FormatUtils.formatDistance(longestRun.getDistanceMeters()));

        // Fastest avg pace (lowest value is fastest)
        RunResponse fastestAvg = allRuns.get(0);
        for (RunResponse run : allRuns) {
            if (run.getAvgPaceSecPerKm() > 0 && run.getAvgPaceSecPerKm() < fastestAvg.getAvgPaceSecPerKm()) {
                fastestAvg = run;
            }
        }
        tvFastestAvgPace.setText(FormatUtils.formatPace(fastestAvg.getAvgPaceSecPerKm()));

        // Best pace
        RunResponse bestPaceRun = allRuns.get(0);
        for (RunResponse run : allRuns) {
            if (run.getBestPaceSecPerKm() > 0 && run.getBestPaceSecPerKm() < bestPaceRun.getBestPaceSecPerKm()) {
                bestPaceRun = run;
            }
        }
        tvBestPace.setText(FormatUtils.formatPace(bestPaceRun.getBestPaceSecPerKm()));

        // Most steps
        RunResponse mostStepsRun = allRuns.get(0);
        for (RunResponse run : allRuns) {
            if (run.getSteps() > mostStepsRun.getSteps()) {
                mostStepsRun = run;
            }
        }
        tvMostSteps.setText(FormatUtils.formatNumber(mostStepsRun.getSteps()));
    }

    private void updateRecentRunsList() {
        // Show recent 10 runs (already sorted by backend)
        List<RunResponse> recentRuns = allRuns.size() > 10 
                ? allRuns.subList(0, 10) 
                : allRuns;
        
        runsAdapter.setRuns(recentRuns);
    }

    private void selectRun(RunResponse run, int position) {
        if (run == null || run.getId() == null) {
            Log.w(TAG, "Cannot select run: run or id is null");
            Toast.makeText(this, "Invalid run selected", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedRun = run;
        runsAdapter.setSelectedPosition(position);

        // Update map
        updateMapPreview(run);

        // Update stats
        updateSelectedRunStats(run);
    }

    private void updateMapPreview(RunResponse run) {
        GeoJsonLineStringDto path = run.getPath();
        
        if (path == null || path.getCoordinates() == null || path.getCoordinates().size() < 2) {
            Log.w(TAG, "Run " + run.getId() + " has no valid path");
            tvMapEmptyState.setVisibility(View.VISIBLE);
            tvMapEmptyState.setText("No route data available");
            layoutSelectedRunStats.setVisibility(View.VISIBLE);
            return;
        }

        tvMapEmptyState.setVisibility(View.GONE);
        layoutSelectedRunStats.setVisibility(View.VISIBLE);

        // Convert coordinates to Mapbox Points
        List<Point> points = new ArrayList<>();
        for (List<Double> coord : path.getCoordinates()) {
            if (coord.size() >= 2) {
                points.add(Point.fromLngLat(coord.get(0), coord.get(1)));
            }
        }

        if (points.isEmpty()) {
            Log.w(TAG, "No valid points to draw");
            return;
        }

        // Draw route on map
        mapView.getMapboxMap().getStyle(style -> {
            // Remove existing source/layer
            try {
                style.removeStyleLayer(ROUTE_LAYER_ID);
            } catch (Exception e) {
                // Layer doesn't exist yet
            }
            try {
                style.removeStyleSource(ROUTE_SOURCE_ID);
            } catch (Exception e) {
                // Source doesn't exist yet
            }

            // Add new route
            LineString lineString = LineString.fromLngLats(points);
            Feature feature = Feature.fromGeometry(lineString);
            
            GeoJsonSource source = new GeoJsonSource.Builder(ROUTE_SOURCE_ID)
                    .feature(feature)
                    .build();
            source.bindTo(style);

            LineLayer lineLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);
            lineLayer.lineColor("#FF5722");
            lineLayer.lineWidth(4.0);
            lineLayer.lineCap(LineCap.ROUND);
            lineLayer.lineJoin(LineJoin.ROUND);
            lineLayer.bindTo(style);

            // Center camera on route
            Point centerPoint = points.get(points.size() / 2);
            mapView.getMapboxMap().setCamera(
                    new CameraOptions.Builder()
                            .center(centerPoint)
                            .zoom(13.0)
                            .build()
            );
        });
    }

    private void updateSelectedRunStats(RunResponse run) {
        tvSelectedRunDate.setText(FormatUtils.formatDateTime(run.getStartTime()));
        tvSelectedRunDistance.setText("Distance: " + FormatUtils.formatDistance(run.getDistanceMeters()));
        tvSelectedRunDuration.setText("Duration: " + FormatUtils.formatDuration(run.getDurationMs()));
        tvSelectedRunAvgPace.setText("Avg Pace: " + FormatUtils.formatPace(run.getAvgPaceSecPerKm()));
        tvSelectedRunBestPace.setText("Best Pace: " + FormatUtils.formatPace(run.getBestPaceSecPerKm()));
        tvSelectedRunCalories.setText("Calories: " + FormatUtils.formatCalories(run.getCalories()));
        tvSelectedRunSteps.setText("Steps: " + FormatUtils.formatNumber(run.getSteps()));
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerViewRuns.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}