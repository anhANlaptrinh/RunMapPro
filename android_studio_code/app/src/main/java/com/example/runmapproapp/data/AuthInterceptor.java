package com.example.runmapproapp.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.runmapproapp.auth.AuthManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    
    private static final String TAG = "AuthInterceptor";
    private final AuthManager authManager;

    public AuthInterceptor(Context context) {
        this.authManager = new AuthManager(context.getApplicationContext());
        Log.d(TAG, "AuthInterceptor initialized");
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Skip adding token for auth endpoints
        String path = originalRequest.url().encodedPath();
        if (path.startsWith("/api/auth/")) {
            Log.d(TAG, "Skipping auth header for: " + path);
            return chain.proceed(originalRequest);
        }

        // Get token from AuthManager
        String token = authManager.getToken();
        
        Log.d(TAG, "========== AUTH INTERCEPTOR ==========");
        Log.d(TAG, "Request: " + originalRequest.method() + " " + path);
        Log.d(TAG, "Token available: " + (token != null && !token.isEmpty()));
        if (token != null && !token.isEmpty()) {
            Log.d(TAG, "Token (first 20 chars): " + token.substring(0, Math.min(20, token.length())) + "...");
        }

        // If no token, proceed without Authorization header
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "⚠️ NO TOKEN - Request will be unauthorized");
            return chain.proceed(originalRequest);
        }

        // Add Authorization header with Bearer token
        Request authorizedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        
        Log.d(TAG, "✅ Added Authorization header");
        Log.d(TAG, "======================================");

        return chain.proceed(authorizedRequest);
    }
}
