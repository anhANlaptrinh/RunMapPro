package com.example.runmapproapp.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor to add JWT authentication token to all API requests
 * Must be initialized with Context to access SharedPreferences
 */
public class AuthInterceptor implements Interceptor {

    private final AuthManager authManager;

    public AuthInterceptor(Context context) {
        this.authManager = new AuthManager(context);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String token = authManager.getToken(); // accessToken

        if (token != null && !token.isEmpty()) {
            Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(request);
        }

        return chain.proceed(original);
    }
}

