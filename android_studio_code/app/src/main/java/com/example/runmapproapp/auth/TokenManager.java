package com.example.runmapproapp.auth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages authentication tokens in SharedPreferences
 */
public class TokenManager {
    private static final String PREFS_NAME = "RunMapProPrefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static Context appContext;
    
    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }
    
    public static void saveToken(String token) {
        if (appContext != null) {
            SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_TOKEN, token).apply();
        }
    }
    
    public static String getToken() {
        if (appContext != null) {
            SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getString(KEY_TOKEN, null);
        }
        return null;
    }
    
    public static void clearToken() {
        if (appContext != null) {
            SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().remove(KEY_TOKEN).apply();
        }
    }
}
