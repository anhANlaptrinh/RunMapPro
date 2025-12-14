package com.example.runmapproapp.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.example.runmapproapp.auth.AuthManager;

import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;

@GlideModule
public class AuthenticatedGlideModule extends AppGlideModule {
    
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        AuthManager authManager = new AuthManager(context);
        
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request original = chain.request();
                String url = original.url().toString();
                
                // Only add auth header for our API endpoints
                if (url.contains("10.0.2.2:8080/api/media/") || url.contains("localhost:8080/api/media/")) {
                    String token = authManager.getToken();
                    if (token != null) {
                        Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + token);
                        return chain.proceed(requestBuilder.build());
                    }
                }
                return chain.proceed(original);
            })
            .build();
        
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }
    
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
