package com.example.runmapproapp.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.runmapproapp.auth.AuthManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    
    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Skip adding token for auth endpoints
        String path = originalRequest.url().encodedPath();
        if (path.startsWith("/api/auth/")) {
            return chain.proceed(originalRequest);
        }

        // Get token from AuthManager
        AuthManager authManager = new AuthManager(context);
        String token = authManager.getToken();

        // If no token, proceed without Authorization header
        if (token == null || token.isEmpty()) {
            return chain.proceed(originalRequest);
        }

        // Add Authorization header with Bearer token
        Request authorizedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(authorizedRequest);
    }
}
