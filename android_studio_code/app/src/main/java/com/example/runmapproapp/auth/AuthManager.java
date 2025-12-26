package com.example.runmapproapp.auth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.example.runmapproapp.data.model.LoginResponse;
import com.example.runmapproapp.data.model.User;

/**
 * Simple wrapper around SharedPreferences for storing auth state.
 */
public class AuthManager {

    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_FULL_NAME = "userFullName";
    private static final String KEY_USER_USERNAME = "userUsername";
    private static final String KEY_USER_AVATAR = "userAvatar";
    private static final String KEY_USER_BIO = "userBio";
    private static final String KEY_USER_BANNED = "userBanned";

    private final SharedPreferences prefs;

    public AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveLogin(LoginResponse response) {
        if (response == null) {
            return;
        }
        User user = response.getUser();
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, response.getAccessToken())
                .putString(KEY_REFRESH_TOKEN, response.getRefreshToken())
            .putString(KEY_USER_ID, user != null ? user.getId() : null)
            .putString(KEY_USER_EMAIL, user != null ? user.getEmail() : null)
            .putString(KEY_USER_FULL_NAME, user != null ? user.getFullName() : null)
            .putString(KEY_USER_USERNAME, user != null ? user.getUsername() : null)
            .putString(KEY_USER_AVATAR, user != null ? user.getAvatarUrl() : null)
            .putString(KEY_USER_BIO, user != null ? user.getBio() : null)
            .putBoolean(KEY_USER_BANNED, user != null && user.isBanned())
                .apply();
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    @Nullable
    public String getToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    @Nullable
    public String getUserName() {
        return prefs.getString(KEY_USER_FULL_NAME, null);
    }

    @Nullable
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    @Nullable
    public String getUsername() {
        return prefs.getString(KEY_USER_USERNAME, null);
    }

    @Nullable
    public String getAvatarUrl() {
        return prefs.getString(KEY_USER_AVATAR, null);
    }

    @Nullable
    public String getBio() {
        return prefs.getString(KEY_USER_BIO, null);
    }

    @Nullable
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public boolean isLoggedIn() {
        // Consider user banned: treat as not logged in
        String token = getToken();
        boolean banned = prefs.getBoolean(KEY_USER_BANNED, false);
        return token != null && !banned;
    }

    public boolean isUserBanned() {
        return prefs.getBoolean(KEY_USER_BANNED, false);
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public void updateCachedProfile(String fullName, String email, String username, String avatarUrl, String bio) {
        prefs.edit()
                .putString(KEY_USER_FULL_NAME, fullName)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_USERNAME, username)
                .putString(KEY_USER_AVATAR, avatarUrl)
                .putString(KEY_USER_BIO, bio)
                .apply();
    }
}
