package com.example.runmapproapp;

import android.app.Application;

import com.example.runmapproapp.data.ApiClient;

public class RunMapProApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize ApiClient with application context
        ApiClient.initialize(this);
    }
}
