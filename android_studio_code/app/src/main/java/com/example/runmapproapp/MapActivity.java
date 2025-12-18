package com.example.runmapproapp;

import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.runmapproapp.api.RetrofitClient;
import com.example.runmapproapp.api.RunApiService;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.dto.CreateRunRequest;
import com.example.runmapproapp.dto.GeoJsonLineStringDto;
import com.example.runmapproapp.dto.RunResponse;
import com.example.runmapproapp.utils.BottomNavigationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.generated.LineLayer;
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap;
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin;
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity {

    // UI
    private MapView mapView;
    private MaterialButton btnStartStop;
    private FloatingActionButton fabRecenter;
    private FloatingActionButton fabLoadRuns;
    private FloatingActionButton fabClearRoute;
    private ImageButton fabDashboard;
    private TextView tvDistance, tvMaxPace, tvAvgSpeed; // Max Pace & Avg Pace
    private TextView tvTemperature, tvWeatherDesc, tvWeatherIcon;
    private TextView tvDuration, tvCadence, tvCalories;

    // Mapbox
    private final NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    private MapboxNavigation mapboxNavigation;
    private GeoJsonSource runningRouteSource;

    // Running tracking
    private boolean isTracking = false;
    private final List<Point> runningPath = new ArrayList<>();
    private static final String RUNNING_ROUTE_SOURCE_ID = "running-route-source";
    private static final String RUNNING_ROUTE_LAYER_ID = "running-route-layer";

    // Running stats
    private double totalDistance = 0.0; // in meters
    private long startTime = 0;
    private Location lastLocation = null;
    private double bestPaceSecondsPerKm = Double.MAX_VALUE; // Track max (fastest) pace
    
    // Step counter using accelerometer
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int stepCount = 0;
    private long lastStepTime = 0;
    private long cadenceStartTime = 0; // For calculating SPM
    private static final float STEP_THRESHOLD = 1.1f * SensorManager.GRAVITY_EARTH; // 1.1× gravity
    private static final long MIN_STEP_INTERVAL_MS = 200; // Minimum 200ms between steps
    private static final long MAX_STEP_INTERVAL_MS = 2000; // Maximum 2s between steps
    
    // Low-pass filter for accelerometer
    private float[] gravity = new float[3];
    private static final float ALPHA = 0.8f; // Filter coefficient

    // Logging tag
    private static final String TAG = "MapActivity";

    // Giả định cân nặng người dùng (để tính calories)
    private static final double USER_WEIGHT_KG = 60.0;

    // State
    private boolean focusLocation = true;
    private String permissionGranted;

    // Weather
    // Open-Meteo API (No API key needed!)
    private static final String WEATHER_API_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m,weather_code&temperature_unit=celsius";
    private boolean weatherFetched = false;

    // Observers
    private final LocationObserver locationObserver = new LocationObserver() {
        @Override
        public void onNewRawLocation(@NonNull Location location) {
        }

        @Override
        public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
            Location location = locationMatcherResult.getEnhancedLocation();
            navigationLocationProvider.changePosition(
                    location,
                    locationMatcherResult.getKeyPoints(),
                    null,
                    null
            );

            // Track running path + stats (chỉ khi đang tracking)
            if (isTracking) {
                Point newPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                runningPath.add(newPoint);

                // Calculate distance
                if (lastLocation != null) {
                    float[] results = new float[1];
                    Location.distanceBetween(
                            lastLocation.getLatitude(), lastLocation.getLongitude(),
                            location.getLatitude(), location.getLongitude(),
                            results
                    );
                    totalDistance += results[0];
                }
                lastLocation = location;

                updateRunningRoute();
                updateStats(location);
            }

            // Weather: chỉ cần có location, không phụ thuộc isTracking
            if (!weatherFetched) {
                weatherFetched = true;
                fetchWeather(location.getLatitude(), location.getLongitude());
            }

            if (focusLocation) {
                updateCamera(
                        Point.fromLngLat(location.getLongitude(), location.getLatitude()),
                        (double) location.getBearing()
                );
            }
        }
    };

    private final ActivityResultLauncher<String> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    new ActivityResultCallback<Boolean>() {
                        @Override
                        public void onActivityResult(Boolean result) {
                            if (result) {
                                if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permissionGranted)) {
                                    startLocationUpdates();
                                }
                                recreate();
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Views
        mapView = findViewById(R.id.mapView);
        btnStartStop = findViewById(R.id.setRoute);
        fabRecenter = findViewById(R.id.fab);
        fabRecenter.hide();
        fabLoadRuns = findViewById(R.id.fabLoadRuns);
        fabClearRoute = findViewById(R.id.fabClearRoute);
        fabClearRoute.hide(); // Ẩn ban đầu, chỉ hiện khi có đường chạy
        fabDashboard = findViewById(R.id.fabDashboard);

        // Stats TextViews
        tvDistance = findViewById(R.id.tvDistance);
        tvMaxPace = findViewById(R.id.tvCurrentSpeed); // hiển thị Max Pace
        tvAvgSpeed = findViewById(R.id.tvAvgSpeed);    // hiển thị Avg Pace
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWeatherDesc = findViewById(R.id.tvWeatherDesc);
        tvWeatherIcon = findViewById(R.id.tvWeatherIcon);
        tvDuration = findViewById(R.id.tvDuration);
        tvCadence = findViewById(R.id.tvMaxPace); // Reuse MaxPace TextView for Cadence
        tvCalories = findViewById(R.id.tvCalories);

        // Setup bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav, R.id.nav_map);

        // Set default UI cho weather
        tvTemperature.setText("Loading...");
        tvWeatherDesc.setText("");
        tvWeatherIcon.setText("");
        
        // Initialize step counter sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Mapbox Navigation setup
        NavigationOptions navigationOptions = new NavigationOptions
                .Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build();
        mapboxNavigation = new MapboxNavigation(navigationOptions);
        mapboxNavigation.registerLocationObserver(locationObserver);

        // Permissions
        checkPermissions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            activityResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            mapboxNavigation.startTripSession();
        }

        // Map init
        final LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
        getGestures(mapView).addOnMoveListener(onMoveListener);

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                mapView.getMapboxMap().setCamera(
                        new CameraOptions.Builder().zoom(16.5).build()
                );

                locationComponentPlugin.setEnabled(true);
                locationComponentPlugin.setLocationProvider(navigationLocationProvider);
                getGestures(mapView).addOnMoveListener(onMoveListener);

                locationComponentPlugin.updateSettings(settings -> {
                    settings.setEnabled(true);
                    settings.setPulsingEnabled(true);
                    return null;
                });

                // Initialize running route source
                runningRouteSource = new GeoJsonSource
                        .Builder(RUNNING_ROUTE_SOURCE_ID)
                        .featureCollection(
                                FeatureCollection.fromFeatures(new Feature[]{})
                        )
                        .build();

                // Gắn source vào style
                runningRouteSource.bindTo(style);

                // Tạo layer cho đường chạy
                LineLayer lineLayer = new LineLayer(RUNNING_ROUTE_LAYER_ID, RUNNING_ROUTE_SOURCE_ID);
                lineLayer.lineColor("#0000FF");  // Blue
                lineLayer.lineWidth(6.0);
                lineLayer.lineCap(LineCap.ROUND);
                lineLayer.lineJoin(LineJoin.ROUND);

                // Gắn layer vào style
                lineLayer.bindTo(style);

                // Re-center FAB
                fabRecenter.setOnClickListener(v -> {
                    focusLocation = true;
                    getGestures(mapView).addOnMoveListener(onMoveListener);
                    fabRecenter.hide();
                });
            }
        });

        // Start/Stop Running button
        btnStartStop.setText("Start Running");
        btnStartStop.setOnClickListener(v -> {
            if (isTracking) {
                stopTracking();
            } else {
                startTracking();
            }
        });

        // Load Runs button
        fabLoadRuns.setOnClickListener(v -> {
            showLoadRunsDialog();
        });

        // Dashboard button
        fabDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        // Clear Route button
        fabClearRoute.setOnClickListener(v -> {
            clearRouteAndReset();
        });
    }

    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector detector) {
            focusLocation = false;
            getGestures(mapView).removeOnMoveListener(this);
            fabRecenter.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector detector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector detector) {
        }
    };

    private void startTracking() {
        isTracking = true;
        runningPath.clear();
        totalDistance = 0.0;
        lastLocation = null;
        startTime = System.currentTimeMillis();
        stepCount = 0;
        lastStepTime = 0;
        cadenceStartTime = System.currentTimeMillis();
        gravity = new float[3];
        bestPaceSecondsPerKm = Double.MAX_VALUE;

        btnStartStop.setText("Stop Running");
        btnStartStop.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.RED)
        );

        // Reset stats display
        tvDistance.setText("0.00 km");
        tvMaxPace.setText("--");           // Max Pace
        tvAvgSpeed.setText("--");          // Avg Pace
        tvDuration.setText("00:00:00");
        tvCadence.setText("0 SPM");
        tvCalories.setText("0 kcal");
        
        // Register accelerometer sensor
        if (accelerometer != null) {
            sensorManager.registerListener(stepDetector, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    private void stopTracking() {
        isTracking = false;
        btnStartStop.setText("Start Running");
        btnStartStop.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        getResources().getColor(R.color.orange, null)
                )
        );
        
        // Unregister sensor
        if (sensorManager != null) {
            sensorManager.unregisterListener(stepDetector);
        }
        
        // Max pace remains visible (other stats remain)
        // tvMaxPace keeps showing the best pace achieved
        
        // Show save dialog after 3 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showSaveRunDialog();
        }, 3000);
    }
    
    /**
     * Clear route and reset without stopping tracking
     */
    private void clearRouteAndReset() {
        if (runningPath.isEmpty()) {
            Toast.makeText(this, "No route to clear", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Clear Route")
                .setMessage("Are you sure you want to clear the current route? This cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    // Clear the route on map
                    runningPath.clear();
                    if (runningRouteSource != null) {
                        runningRouteSource.featureCollection(
                                FeatureCollection.fromFeatures(new Feature[]{})
                        );
                    }
                    
                    // Reset all stats
                    totalDistance = 0.0;
                    lastLocation = null;
                    stepCount = 0;
                    lastStepTime = 0;
                    cadenceStartTime = 0;
                    bestPaceSecondsPerKm = Double.MAX_VALUE;
                    
                    // Update UI
                    tvDistance.setText("0.00 km");
                    tvMaxPace.setText("--");
                    tvAvgSpeed.setText("--");
                    tvDuration.setText("00:00:00");
                    tvCadence.setText("0 SPM");
                    tvCalories.setText("0 kcal");
                    
                    // Hide clear button
                    fabClearRoute.hide();
                    
                    // If tracking, reset start time
                    if (isTracking) {
                        startTime = System.currentTimeMillis();
                        cadenceStartTime = System.currentTimeMillis();
                    }
                    
                    Toast.makeText(this, "Route cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void clearAllData() {
        // Stop tracking if running
        if (isTracking) {
            isTracking = false;
            btnStartStop.setText("Start Running");
            btnStartStop.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            getResources().getColor(R.color.orange, null)
                    )
            );
        }
        
        // Reset all stats to default values
        tvDistance.setText("0.00 km");
        tvMaxPace.setText("--");
        tvAvgSpeed.setText("--");
        tvDuration.setText("00:00:00");
        tvCadence.setText("0 SPM");
        tvCalories.setText("0 kcal");
        
        // Reset tracking data
        totalDistance = 0.0;
        runningPath.clear();
        lastLocation = null;
        stepCount = 0;
        lastStepTime = 0;
        cadenceStartTime = 0;
        bestPaceSecondsPerKm = Double.MAX_VALUE;
        
        // Clear the route on map
        if (runningRouteSource != null) {
            runningRouteSource.featureCollection(
                    FeatureCollection.fromFeatures(new Feature[]{})
            );
        }
    }
    
    private void showSaveRunDialog() {
        // Only show if there's data to save (distance > 0)
        double distanceKm = totalDistance / 1000.0;
        if (distanceKm < 0.01) {
            // No meaningful data, just clear
            clearAllData();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Lưu lần chạy này?")
            .setMessage(String.format(Locale.US, 
                "Bạn đã chạy được %.2f km. Bạn có muốn lưu lại lần chạy này không?", distanceKm))
            .setPositiveButton("Có", (dialog, which) -> {
                // Save run data to backend
                saveRunToBackend();
            })
            .setNegativeButton("Không", (dialog, which) -> {
                // User chose not to save, just clear data
                clearAllData();
                Toast.makeText(this, "Đã hủy lần chạy", Toast.LENGTH_SHORT).show();
            })
            .setCancelable(false)
            .show();
    }

    // Cập nhật đường chạy: 1 LineString duy nhất từ toàn bộ runningPath
    private void updateRunningRoute() {
        if (runningRouteSource == null) return;
        if (runningPath.size() < 2) return;

        LineString lineString = LineString.fromLngLats(runningPath);
        Feature feature = Feature.fromGeometry(lineString);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);

        // chỉ update lại data cho source
        runningRouteSource.featureCollection(featureCollection);
        
        // Show clear button when there's a route
        if (runningPath.size() >= 2) {
            fabClearRoute.show();
        }
    }

    private void updateCamera(Point point, Double bearing) {
        MapAnimationOptions anim = new MapAnimationOptions.Builder()
                .duration(1500L)
                .build();
        CameraOptions cam = new CameraOptions.Builder()
                .center(point)
                .zoom(16.5)
                .padding(new EdgeInsets(0.0, 0.0, 0.0, 0.0))
                .bearing(bearing)
                .build();
        getCamera(mapView).easeTo(cam, anim);
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionGranted = Manifest.permission.ACCESS_FINE_LOCATION;
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mapboxNavigation.startTripSession();
            fabRecenter.hide();
            LocationComponentPlugin plugin = getLocationComponent(mapView);
            plugin.setEnabled(true);
            plugin.setLocationProvider(navigationLocationProvider);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(stepDetector);
        }
        if (mapboxNavigation != null) {
            mapboxNavigation.unregisterLocationObserver(locationObserver);
            mapboxNavigation.onDestroy();
        }
    }
    
    // ------- Step Counter with Accelerometer -------
    
    private final SensorEventListener stepDetector = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!isTracking) return;
            
            // Apply low-pass filter to remove noise
            gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * event.values[0];
            gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * event.values[1];
            gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * event.values[2];
            
            // Remove gravity to get linear acceleration
            float linearAccelX = event.values[0] - gravity[0];
            float linearAccelY = event.values[1] - gravity[1];
            float linearAccelZ = event.values[2] - gravity[2];
            
            // Calculate magnitude of acceleration vector
            float magnitude = (float) Math.sqrt(
                linearAccelX * linearAccelX +
                linearAccelY * linearAccelY +
                linearAccelZ * linearAccelZ
            );
            
            // Detect peak (step) if magnitude exceeds threshold
            long currentTime = System.currentTimeMillis();
            long timeSinceLastStep = currentTime - lastStepTime;
            
            // Check conditions for valid step:
            // 1. Magnitude exceeds threshold
            // 2. Enough time passed since last step (200-2000ms)
            // 3. Not too long (to reset if user stopped)
            if (magnitude > STEP_THRESHOLD && 
                timeSinceLastStep >= MIN_STEP_INTERVAL_MS) {
                
                // Reset counter if too much time passed (user stopped)
                if (timeSinceLastStep > MAX_STEP_INTERVAL_MS && stepCount > 0) {
                    // Don't reset, just continue counting
                }
                
                stepCount++;
                lastStepTime = currentTime;
                
                // Calculate cadence (SPM - Steps Per Minute)
                long elapsedMinutes = (currentTime - cadenceStartTime) / 60000;
                if (elapsedMinutes > 0) {
                    int cadence = (int) (stepCount / elapsedMinutes);
                    // Update UI on main thread
                    runOnUiThread(() -> {
                        tvCadence.setText(cadence + " SPM");
                    });
                } else {
                    // First minute, calculate instantaneous cadence
                    long elapsedSeconds = (currentTime - cadenceStartTime) / 1000;
                    if (elapsedSeconds > 0) {
                        int instantCadence = (int) ((stepCount * 60.0) / elapsedSeconds);
                        runOnUiThread(() -> {
                            tvCadence.setText(instantCadence + " SPM");
                        });
                    }
                }
            }
        }
        
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Not needed
        }
    };

    // ------- Stats -------

    private void updateStats(Location location) {
        // Only update if tracking is active
        if (!isTracking) {
            return;
        }
        
        // Distance
        double distanceKm = totalDistance / 1000.0;
        tvDistance.setText(String.format(Locale.US, "%.2f km", distanceKm));

        // Duration
        long elapsedTime = System.currentTimeMillis() - startTime;
        tvDuration.setText(formatDuration(elapsedTime));

        // Max Pace - track the fastest pace achieved
        if (distanceKm > 0.01) { // Đã chạy ít nhất 10 mét
            double speedMps = location.getSpeed();
            if (speedMps > 0.5) { // tránh chia cho 0 & lúc đứng yên
                double currentPaceSecPerKm = 1000.0 / speedMps;
                // Update best pace if current is faster (lower seconds per km)
                if (currentPaceSecPerKm < bestPaceSecondsPerKm) {
                    bestPaceSecondsPerKm = currentPaceSecPerKm;
                    String paceStr = formatPaceFromSeconds(bestPaceSecondsPerKm);
                    tvMaxPace.setText(paceStr);
                }
            }
        }

        // Avg Pace - only calculate if moved enough distance
        double elapsedSeconds = elapsedTime / 1000.0;
        if (distanceKm > 0.01 && elapsedSeconds > 0) { // Đã chạy ít nhất 10 mét
            double avgPaceSecPerKm = elapsedSeconds / distanceKm;
            tvAvgSpeed.setText(formatPaceFromSeconds(avgPaceSecPerKm));
        } else {
            tvAvgSpeed.setText("--");
        }

        // Calories ~ weight(kg) * distance(km)
        double calories = distanceKm * USER_WEIGHT_KG * 1.036;
        tvCalories.setText(String.format(Locale.US, "%.0f kcal", calories));
    }

    private String formatDuration(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }

    private String formatPaceFromSeconds(double secPerKm) {
        if (secPerKm <= 0 || secPerKm == Double.MAX_VALUE) {
            return "--";
        }
        int totalSeconds = (int) Math.round(secPerKm);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.US, "%d'%02d\"/km", minutes, seconds);
    }

    // ------- Weather (Open-Meteo) -------

    private void fetchWeather(double lat, double lon) {
        new Thread(() -> {
            try {
                String urlString = String.format(Locale.US, WEATHER_API_URL, lat, lon);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse Open-Meteo JSON response
                JSONObject json = new JSONObject(response.toString());
                JSONObject current = json.getJSONObject("current");
                double temp = current.getDouble("temperature_2m");
                int weatherCode = current.getInt("weather_code");

                // Convert WMO weather code to description and emoji
                String[] weatherInfo = getWeatherFromCode(weatherCode);
                String description = weatherInfo[0];
                String emoji = weatherInfo[1];

                // Update UI on main thread
                runOnUiThread(() -> {
                    tvTemperature.setText(String.format(Locale.US, "%.1f°C", temp));
                    tvWeatherDesc.setText(description);
                    tvWeatherIcon.setText(emoji);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    tvTemperature.setText("--°C");
                    tvWeatherDesc.setText("Unable to load");
                    tvWeatherIcon.setText("");
                });
            }
        }).start();
    }

    // Convert WMO Weather Code to description and emoji
    // https://open-meteo.com/en/docs
    private String[] getWeatherFromCode(int code) {
        String description;
        String emoji;

        if (code == 0) {
            description = "Clear sky";
            emoji = "☀️";
        } else if (code <= 3) {
            description = "Partly cloudy";
            emoji = "⛅";
        } else if (code <= 48) {
            description = "Foggy";
            emoji = "🌫️";
        } else if (code <= 57) {
            description = "Drizzle";
            emoji = "🌦️";
        } else if (code <= 67) {
            description = "Rain";
            emoji = "🌧️";
        } else if (code <= 77) {
            description = "Snow";
            emoji = "❄️";
        } else if (code <= 82) {
            description = "Rain showers";
            emoji = "🌧️";
        } else if (code <= 86) {
            description = "Snow showers";
            emoji = "🌨️";
        } else {
            description = "Thunderstorm";
            emoji = "⛈️";
        }

        return new String[]{description, emoji};
    }

    // ------- Backend Integration (Retrofit) -------

    /**
     * Save the current run data to the backend server.
     * Builds the request from current tracking data and sends it asynchronously.
     */
    private void saveRunToBackend() {
        // Validate data
        if (runningPath.isEmpty() || totalDistance < 10) {
            Toast.makeText(this, "Không đủ dữ liệu để lưu", Toast.LENGTH_SHORT).show();
            clearAllData();
            return;
        }

        // Calculate derived values
        long endTime = System.currentTimeMillis();
        long durationMs = endTime - startTime;
        double distanceKm = totalDistance / 1000.0;
        double elapsedSeconds = durationMs / 1000.0;
        
        // Calculate average pace (seconds per km)
        double avgPaceSecPerKm = (distanceKm > 0) ? elapsedSeconds / distanceKm : 0;
        
        // Calculate calories
        double calories = distanceKm * USER_WEIGHT_KG * 1.036;
        
        // Get authenticated user ID
        AuthManager authManager = new AuthManager(this);
        String userId = authManager.getUserId();
        Log.d(TAG, "========== SAVING RUN ==========");
        Log.d(TAG, "UserId from AuthManager: " + userId);
        
        if (userId == null) {
            Log.e(TAG, "User not authenticated!");
            runOnUiThread(() -> 
                Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show()
            );
            return;
        }
        
        // Convert runningPath to GeoJSON format
        GeoJsonLineStringDto pathDto = convertPathToGeoJson(runningPath);

        // Build request using builder pattern
        CreateRunRequest request = new CreateRunRequest.Builder()
                .userId(userId)
                .startTime(formatToIso8601(startTime))
                .endTime(formatToIso8601(endTime))
                .distanceMeters(totalDistance)
                .durationMs(durationMs)
                .steps(stepCount)
                .calories(calories)
                .bestPaceSecPerKm(bestPaceSecondsPerKm == Double.MAX_VALUE ? 0 : bestPaceSecondsPerKm)
                .avgPaceSecPerKm(avgPaceSecPerKm)
                .path(pathDto)
                .build();

        // Get API service
        RunApiService apiService = RetrofitClient.getRunApiService();

        // Make asynchronous API call
        apiService.createRun(request).enqueue(new Callback<RunResponse>() {
            @Override
            public void onResponse(@NonNull Call<RunResponse> call, @NonNull Response<RunResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RunResponse savedRun = response.body();
                    Log.d(TAG, "Run saved successfully with ID: " + savedRun.getId());
                    
                    runOnUiThread(() -> {
                        Toast.makeText(MapActivity.this,
                                "Đã lưu lần chạy! ID: " + savedRun.getId(), 
                                Toast.LENGTH_LONG).show();
                        clearAllData();
                    });
                } else {
                    String err = null;
                    try {
                        if (response.errorBody() != null) err = response.errorBody().string();
                    } catch (Exception ignored) {}

                    Log.e(TAG, "Failed. code=" + response.code() + " message=" + response.message());
                    Log.e(TAG, "errorBody=" + err);
                    Log.e(TAG, "Failed to save run. Response code: " + response.code());
                    runOnUiThread(() -> {
                        Toast.makeText(MapActivity.this,
                                "Lưu thất bại. Mã lỗi: " + response.code(), 
                                Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<RunResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error while saving run", t);
                runOnUiThread(() -> {
                    Toast.makeText(MapActivity.this,
                            "Lỗi kết nối: " + t.getMessage(), 
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Load a specific run from the backend and draw it on the map.
     * This is an example method showing how to fetch and display saved runs.
     * 
     * @param runId The ID of the run to load
     */
    public void loadAndDrawRun(String runId) {
        // Get API service
        RunApiService apiService = RetrofitClient.getRunApiService();

        // Make asynchronous API call
        apiService.getRun(runId).enqueue(new Callback<RunResponse>() {
            @Override
            public void onResponse(@NonNull Call<RunResponse> call, @NonNull Response<RunResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RunResponse run = response.body();
                    Log.d(TAG, "Run loaded successfully: " + runId);

                    runOnUiThread(() -> {
                        // Extract path from response
                        GeoJsonLineStringDto pathDto = run.getPath();
                        if (pathDto != null && pathDto.getCoordinates() != null) {
                            // Convert coordinates back to List<Point>
                            List<Point> loadedPath = new ArrayList<>();
                            for (List<Double> coord : pathDto.getCoordinates()) {
                                if (coord.size() >= 2) {
                                    double lng = coord.get(0);
                                    double lat = coord.get(1);
                                    loadedPath.add(Point.fromLngLat(lng, lat));
                                }
                            }

                            // Replace current path with loaded path
                            runningPath.clear();
                            runningPath.addAll(loadedPath);

                            // Update the map
                            updateRunningRoute();

                            // Move camera to first point
                            if (!loadedPath.isEmpty()) {
                                Point firstPoint = loadedPath.get(0);
                                updateCamera(firstPoint, 0.0);
                            }

                            Toast.makeText(MapActivity.this,
                                    "Đã tải lần chạy: " + String.format(Locale.US, "%.2f km", 
                                            run.getDistanceMeters() / 1000.0), 
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to load run. Response code: " + response.code());
                    runOnUiThread(() -> {
                        Toast.makeText(MapActivity.this,
                                "Không thể tải lần chạy. Mã lỗi: " + response.code(), 
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<RunResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error while loading run", t);
                runOnUiThread(() -> {
                    Toast.makeText(MapActivity.this,
                            "Lỗi kết nối: " + t.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Convert a list of Mapbox Points to GeoJSON LineString format.
     * 
     * @param points List of Mapbox Points (lng, lat)
     * @return GeoJsonLineStringDto with coordinates in [[lng, lat], ...] format
     */
    private GeoJsonLineStringDto convertPathToGeoJson(List<Point> points) {
        List<List<Double>> coordinates = new ArrayList<>();
        
        for (Point point : points) {
            // Each coordinate is [longitude, latitude]
            coordinates.add(Arrays.asList(point.longitude(), point.latitude()));
        }
        
        GeoJsonLineStringDto lineString = new GeoJsonLineStringDto();
        lineString.setType("LineString");
        lineString.setCoordinates(coordinates);
        
        return lineString;
    }

    /**
     * Format a timestamp to ISO-8601 format (e.g., "2025-01-01T10:00:00Z").
     * 
     * @param timestampMs Timestamp in milliseconds
     * @return ISO-8601 formatted string
     */
    private String formatToIso8601(long timestampMs) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(timestampMs));
    }

    /**
     * Show dialog with list of saved runs from backend.
     * User can select a run to load and display on the map.
     */
    private void showLoadRunsDialog() {
        // Get API service
        RunApiService apiService = RetrofitClient.getRunApiService();

        // Show loading toast
        Toast.makeText(this, "Đang tải danh sách...", Toast.LENGTH_SHORT).show();

        // Fetch all runs from backend
        apiService.getRuns().enqueue(new Callback<List<RunResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<RunResponse>> call, @NonNull Response<List<RunResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RunResponse> runs = response.body();
                    
                    if (runs.isEmpty()) {
                        runOnUiThread(() -> {
                            Toast.makeText(MapActivity.this,
                                    "Chưa có lần chạy nào được lưu", 
                                    Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    // Create display strings for each run
                    String[] runDisplayStrings = new String[runs.size()];
                    for (int i = 0; i < runs.size(); i++) {
                        RunResponse run = runs.get(i);
                        double distanceKm = run.getDistanceMeters() / 1000.0;
                        long durationMinutes = run.getDurationMs() / 1000;
                        runDisplayStrings[i] = String.format(Locale.US, 
                                "Run #%d: %.2f km - %d giây (%d bước)",
                                i + 1, distanceKm, durationMinutes, run.getSteps());
                    }

                    // Show dialog with list of runs
                    runOnUiThread(() -> {
                        new AlertDialog.Builder(MapActivity.this)
                                .setTitle("Chọn lần chạy để xem")
                                .setItems(runDisplayStrings, (dialog, which) -> {
                                    // Load selected run
                                    RunResponse selectedRun = runs.get(which);
                                    loadAndDrawRun(selectedRun.getId());
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    });

                } else {
                    Log.e(TAG, "Failed to load runs. Response code: " + response.code());
                    runOnUiThread(() -> {
                        Toast.makeText(MapActivity.this,
                                "Không thể tải danh sách. Mã lỗi: " + response.code(), 
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RunResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error while loading runs", t);
                runOnUiThread(() -> {
                    Toast.makeText(MapActivity.this,
                            "Lỗi kết nối: " + t.getMessage(), 
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav, R.id.nav_map);
    }

}
