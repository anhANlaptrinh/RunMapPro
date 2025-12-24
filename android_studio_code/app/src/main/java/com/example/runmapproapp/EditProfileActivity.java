package com.example.runmapproapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.data.ApiClient;
import com.example.runmapproapp.data.UserApi;
import com.example.runmapproapp.data.model.ErrorResponse;
import com.example.runmapproapp.data.model.UpdateProfileRequest;
import com.example.runmapproapp.data.model.UserProfileResponse;
import com.example.runmapproapp.utils.LocaleHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private ShapeableImageView imageAvatar;
    private TextInputEditText inputFullName;
    private TextInputEditText inputUsername;
    private TextInputEditText inputEmail;
    private TextInputEditText inputBio;
    private MaterialButton buttonChangeAvatar;
    private MaterialButton buttonSaveProfile;
    private MaterialButton buttonChangePassword;
    private TextView textProfileMessage;
    private ProgressBar progressBar;

    private AuthManager authManager;
    private UserApi userApi;
    private ActivityResultLauncher<String> pickImageLauncher;
    private UserProfileResponse currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        authManager = new AuthManager(this);
        if (!authManager.isLoggedIn()) {
            finish();
            return;
        }

        userApi = ApiClient.getUserApi();
        initializeViews();
        setupToolbar();
        setupImagePicker();
        setupListeners();
        fetchProfile();
    }

    private void initializeViews() {
        imageAvatar = findViewById(R.id.imageAvatar);
        inputFullName = findViewById(R.id.inputFullName);
        inputUsername = findViewById(R.id.inputUsername);
        inputEmail = findViewById(R.id.inputEmail);
        inputBio = findViewById(R.id.inputBio);
        buttonChangeAvatar = findViewById(R.id.buttonChangeAvatar);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        textProfileMessage = findViewById(R.id.textProfileMessage);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                imageAvatar.setImageURI(uri);
                uploadAvatar(uri);
            }
        });
    }

    private void setupListeners() {
        buttonChangeAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        buttonSaveProfile.setOnClickListener(v -> submitProfileChanges());
        buttonChangePassword.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));
    }

    private void fetchProfile() {
        String header = buildAuthHeader();
        if (header == null) {
            redirectToLogin();
            return;
        }
        setLoading(true);
        userApi.getProfile(header).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    bindProfile(currentProfile);
                } else {
                    handleAuthOrError(response, textProfileMessage);
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                setLoading(false);
                showMessage(textProfileMessage, getString(R.string.error_network), true);
            }
        });
    }

    private void bindProfile(UserProfileResponse profile) {
        inputFullName.setText(profile.getFullName());
        inputUsername.setText(profile.getUsername());
        inputEmail.setText(profile.getEmail());
        inputBio.setText(profile.getBio());
        
        // Convert relative path to full URL for Glide
        String avatarUrl = profile.getAvatarUrl();
        if (avatarUrl != null && avatarUrl.startsWith("/api/")) {
            avatarUrl = "http://10.0.2.2:8080" + avatarUrl;
        }
        
        android.util.Log.d("EditProfile", "Loading avatar URL: " + avatarUrl);
        Glide.with(this)
                .load(avatarUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        android.util.Log.e("EditProfile", "Glide load failed for: " + model, e);
                        return false;
                    }
                    
                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        android.util.Log.d("EditProfile", "Glide load success for: " + model);
                        return false;
                    }
                })
                .into(imageAvatar);
        
        // Save full URL to cache, not relative path
        authManager.updateCachedProfile(
                profile.getFullName(),
                profile.getEmail(),
                profile.getUsername(),
                avatarUrl, // Use converted full URL
                profile.getBio());
    }

    private void submitProfileChanges() {
        hideMessages();
        String fullName = valueOrNull(inputFullName);
        String username = valueOrNull(inputUsername);
        String bioValue = valueOrEmpty(inputBio);
        String bio;
        if (bioValue.isEmpty()) {
            boolean previouslyHadBio = currentProfile != null && currentProfile.getBio() != null && !currentProfile.getBio().isEmpty();
            bio = previouslyHadBio && inputBio.getText() != null ? "" : null;
        } else {
            bio = bioValue;
        }

        String header = buildAuthHeader();
        if (header == null) {
            redirectToLogin();
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest(fullName, username, bio);
        setLoading(true);
        userApi.updateProfile(header, request).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    bindProfile(currentProfile);
                    showMessage(textProfileMessage, getString(R.string.profile_update_success), false);
                } else {
                    handleAuthOrError(response, textProfileMessage);
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                setLoading(false);
                showMessage(textProfileMessage, getString(R.string.error_network), true);
            }
        });
    }

    private void uploadAvatar(Uri uri) {
        String header = buildAuthHeader();
        if (header == null) {
            redirectToLogin();
            return;
        }
        
        // Create file from URI instead of loading into memory
        java.io.File file = createTempFileFromUri(uri);
        if (file == null) {
            Toast.makeText(this, R.string.error_unexpected, Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String mimeType = getContentResolver().getType(uri);
            
            // Log để debug
            android.util.Log.d("EditProfile", "Upload avatar - size: " + file.length() + " bytes, mime: " + mimeType);
            
            MediaType mediaType = MediaType.parse(mimeType != null ? mimeType : "image/jpeg");
            RequestBody requestBody = RequestBody.create(mediaType, file);
            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "file",
                    file.getName(),
                    requestBody);
            setLoading(true);
            userApi.uploadAvatar(header, body).enqueue(new Callback<UserProfileResponse>() {
                @Override
                public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                    setLoading(false);
                    file.delete(); // Clean up temp file
                    
                    android.util.Log.d("EditProfile", "Upload response code: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        currentProfile = response.body();
                        bindProfile(currentProfile);
                        showMessage(textProfileMessage, getString(R.string.avatar_update_success), false);
                        android.util.Log.d("EditProfile", "Avatar updated: " + currentProfile.getAvatarUrl());
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            android.util.Log.e("EditProfile", "Upload failed: " + errorBody);
                        } catch (Exception e) {
                            android.util.Log.e("EditProfile", "Upload failed", e);
                        }
                        handleAuthOrError(response, textProfileMessage);
                    }
                }

                @Override
                public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                    setLoading(false);
                    file.delete(); // Clean up temp file
                    android.util.Log.e("EditProfile", "Upload network error", t);
                    showMessage(textProfileMessage, getString(R.string.error_network), true);
                }
            });
        } catch (Exception e) {
            file.delete(); // Clean up temp file
            android.util.Log.e("EditProfile", "Exception during upload", e);
            showMessage(textProfileMessage, getString(R.string.error_unexpected), true);
        }
    }

    private void hideMessages() {
        textProfileMessage.setVisibility(View.GONE);
    }

    private void showMessage(TextView view, String message, boolean isError) {
        if (message == null) {
            view.setVisibility(View.GONE);
            return;
        }
        int color = ContextCompat.getColor(this,
                isError ? android.R.color.holo_red_dark : android.R.color.holo_green_dark);
        view.setTextColor(color);
        view.setText(message);
        view.setVisibility(View.VISIBLE);
    }

    private String buildAuthHeader() {
        String token = authManager.getToken();
        if (token == null) {
            return null;
        }
        return "Bearer " + token;
    }

    private void handleAuthOrError(Response<?> response, TextView targetView) {
        if (response.code() == 401) {
            redirectToLogin();
            return;
        }
        String message = parseErrorMessage(response);
        showMessage(targetView, message, true);
    }

    private String parseErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                ErrorResponse error = new Gson().fromJson(raw, ErrorResponse.class);
                if (error != null && error.getMessage() != null) {
                    return error.getMessage();
                }
            }
        } catch (IOException ignored) {
        }
        return getString(R.string.error_unexpected);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonSaveProfile.setEnabled(!loading);
        buttonChangePassword.setEnabled(!loading);
        buttonChangeAvatar.setEnabled(!loading);
    }

    private String valueOrNull(TextInputEditText input) {
        String value = valueOrEmpty(input);
        return value.isEmpty() ? null : value;
    }

    private String valueOrEmpty(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }
    
    private java.io.File createTempFileFromUri(Uri uri) {
        try {
            String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";
            java.io.File file = new java.io.File(getCacheDir(), fileName);
            
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            outputStream.close();
            inputStream.close();
            
            return file;
        } catch (IOException e) {
            android.util.Log.e("EditProfile", "Error creating temp file", e);
            return null;
        }
    }

    private byte[] readAllBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int n;
        while ((n = stream.read(data)) != -1) {
            buffer.write(data, 0, n);
        }
        return buffer.toByteArray();
    }

    private void redirectToLogin() {
        authManager.logout();
        Toast.makeText(this, R.string.error_unexpected, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
