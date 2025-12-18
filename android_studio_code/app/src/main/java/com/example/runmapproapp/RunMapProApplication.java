package com.example.runmapproapp;

import android.app.Application;
import android.util.Log;

import com.example.runmapproapp.api.RetrofitClient;
import com.example.runmapproapp.auth.TokenManager;
import com.example.runmapproapp.data.ApiClient;

/**
 * Application class for global initialization
 */
public class RunMapProApplication extends Application {
    
    private static final String TAG = "RunMapProApp";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.d(TAG, "==========================================");
        Log.d(TAG, "Application onCreate() - Starting initialization");
        Log.d(TAG, "==========================================");
        
        // Initialize TokenManager with application context
        TokenManager.init(this);
        Log.d(TAG, "✅ TokenManager initialized");
        
        // Initialize RetrofitClient with application context
        // THIS IS CRITICAL for AuthInterceptor to work!
        RetrofitClient.init(this);
        Log.d(TAG, "✅ RetrofitClient initialized");
        
        // Initialize ApiClient with application context
        ApiClient.initialize(this);
        Log.d(TAG, "✅ ApiClient initialized");
        
        Log.d(TAG, "==========================================");
        Log.d(TAG, "Application initialization completed");
        Log.d(TAG, "==========================================");
    }
}
