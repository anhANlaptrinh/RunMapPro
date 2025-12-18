package com.example.runmapproapp.api;

import android.content.Context;
import android.util.Log;

import com.example.runmapproapp.auth.AuthInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Retrofit client for making API calls
 * MUST call init(Context) before using getClient() or getRunApiService()
 */
public class RetrofitClient {
    
    private static final String TAG = "RetrofitClient";
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    
    private static Retrofit retrofit = null;
    private static boolean isInitialized = false;

    /**
     * Initialize RetrofitClient with application context
     * MUST be called once from Application.onCreate() or MainActivity.onCreate()
     * 
     * @param context Application context
     */
    public static void init(Context context) {
        if (isInitialized) {
            Log.d(TAG, "RetrofitClient already initialized");
            return;
        }
        
        Log.d(TAG, "Initializing RetrofitClient with Context");
        
        // Create logging interceptor
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> 
            Log.d("OkHttp", message)
        );
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // Create AuthInterceptor WITH CONTEXT
        AuthInterceptor authInterceptor = new AuthInterceptor(context);
        
        // Build OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)  // Auth interceptor FIRST
                .addInterceptor(logging)           // Logging interceptor SECOND
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        // Build Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        isInitialized = true;
        Log.d(TAG, "✅ RetrofitClient initialized successfully");
        Log.d(TAG, "Base URL: " + BASE_URL);
    }

    /**
     * Get Retrofit instance
     * Make sure init(Context) was called first!
     * 
     * @return Retrofit instance
     * @throws IllegalStateException if not initialized
     */
    public static Retrofit getClient() {
        if (!isInitialized || retrofit == null) {
            throw new IllegalStateException(
                "RetrofitClient not initialized! Call RetrofitClient.init(context) first."
            );
        }
        return retrofit;
    }

    /**
     * Get RunApiService instance
     * 
     * @return RunApiService for making run-related API calls
     */
    public static RunApiService getRunApiService() {
        return getClient().create(RunApiService.class);
    }
    
    /**
     * Check if RetrofitClient is initialized
     * 
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return isInitialized;
    }
}
