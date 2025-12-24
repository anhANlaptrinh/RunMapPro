package com.example.runmapproapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.utils.LocaleHelper;

public class SplashActivity extends AppCompatActivity {
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
    
    private static final int SPLASH_DELAY = 2000; // 2 seconds
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Delay and navigate
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AuthManager authManager = new AuthManager(this);
            Intent intent;
            
            if (authManager.isLoggedIn()) {
                // User is logged in, go to MapActivity
                intent = new Intent(SplashActivity.this, MapActivity.class);
            } else {
                // User not logged in, go to LoginActivity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
