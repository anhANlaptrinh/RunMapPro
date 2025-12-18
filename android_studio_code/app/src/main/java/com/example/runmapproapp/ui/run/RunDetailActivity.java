package com.example.runmapproapp.ui.run;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.runmapproapp.R;
import com.example.runmapproapp.api.RetrofitClient;
import com.example.runmapproapp.api.RunApiService;
import com.example.runmapproapp.dto.RunResponse;
import com.example.runmapproapp.utils.FormatUtils;
import com.google.android.material.appbar.MaterialToolbar;
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

public class RunDetailActivity extends AppCompatActivity {

    private static final String TAG = "RunDetailActivity";
    public static final String EXTRA_RUN_ID = "run_id";

    private MapView mapView;
    private TextView tvRunDate, tvDistance, tvDuration, tvAvgPace;
    private TextView tvBestPace, tvCalories, tvSteps;
    private MaterialToolbar toolbar;

    private String runId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_detail);

        initViews();
        setupToolbar();

        // Get run ID from intent
        runId = getIntent().getStringExtra(EXTRA_RUN_ID);
        if (runId == null || runId.isEmpty()) {
            Toast.makeText(this, "Invalid run ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadRunDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        mapView = findViewById(R.id.mapView);
        tvRunDate = findViewById(R.id.tvRunDate);
        tvDistance = findViewById(R.id.tvDistance);
        tvDuration = findViewById(R.id.tvDuration);
        tvAvgPace = findViewById(R.id.tvAvgPace);
        tvBestPace = findViewById(R.id.tvBestPace);
        tvCalories = findViewById(R.id.tvCalories);
        tvSteps = findViewById(R.id.tvSteps);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadRunDetails() {
        RunApiService runApiService = RetrofitClient.getRunApiService();
        runApiService.getRun(runId).enqueue(new Callback<RunResponse>() {
            @Override
            public void onResponse(@NonNull Call<RunResponse> call, @NonNull Response<RunResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayRunDetails(response.body());
                } else {
                    Log.e(TAG, "Failed to load run: " + response.code());
                    Toast.makeText(RunDetailActivity.this, 
                        "Failed to load run details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RunResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading run", t);
                Toast.makeText(RunDetailActivity.this, 
                    "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayRunDetails(RunResponse run) {
        // Display stats
        tvRunDate.setText(FormatUtils.formatDateTime(run.getStartTime()));
        tvDistance.setText(FormatUtils.formatDistance(run.getDistanceMeters()));
        tvDuration.setText(FormatUtils.formatDuration(run.getDurationMs()));
        tvAvgPace.setText(FormatUtils.formatPace(run.getAvgPaceSecPerKm()));
        tvBestPace.setText(FormatUtils.formatPace(run.getBestPaceSecPerKm()));
        tvCalories.setText(FormatUtils.formatCalories(run.getCalories()));
        tvSteps.setText(FormatUtils.formatNumber(run.getSteps()));

        // Display route on map
        displayRouteOnMap(run);
    }

    private void displayRouteOnMap(RunResponse run) {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
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
                    // Create route
                    LineString lineString = LineString.fromLngLats(points);
                    Feature feature = Feature.fromGeometry(lineString);
                    
                    GeoJsonSource source = new GeoJsonSource.Builder("route-source")
                            .feature(feature)
                            .build();
                    source.bindTo(style);

                    LineLayer lineLayer = new LineLayer("route-layer", "route-source");
                    lineLayer.lineColor("#FF5722");
                    lineLayer.lineWidth(5.0);
                    lineLayer.lineCap(LineCap.ROUND);
                    lineLayer.lineJoin(LineJoin.ROUND);
                    lineLayer.bindTo(style);

                    // Center camera on route
                    Point centerPoint = points.get(points.size() / 2);
                    mapView.getMapboxMap().setCamera(
                            new CameraOptions.Builder()
                                    .center(centerPoint)
                                    .zoom(14.0)
                                    .build()
                    );
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }
}
