package com.example.runmapproapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.ui.social.FeedActivity;
import com.example.runmapproapp.ui.profile.UserProfileActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private AuthManager authManager;
    private TextView textWelcome;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = new AuthManager(this);
        android.util.Log.d("MainActivity", "onCreate - isLoggedIn: " + authManager.isLoggedIn());
        android.util.Log.d("MainActivity", "onCreate - token: " + authManager.getToken());
        
        if (!authManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        setupToolbar();
        textWelcome = findViewById(R.id.textWelcome);
        MaterialButton buttonFeed = findViewById(R.id.buttonFeed);
        MaterialButton buttonGroups = findViewById(R.id.buttonGroups);
        MaterialButton buttonChat = findViewById(R.id.buttonChat);
        MaterialButton buttonSettings = findViewById(R.id.buttonSettings);

        String name = authManager.getUserName();
        textWelcome.setText(getString(R.string.welcome_message, name == null ? "" : name));

        buttonFeed.setOnClickListener(v -> {
            Intent intent = new Intent(this, FeedActivity.class);
            startActivity(intent);
        });

        buttonGroups.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.runmapproapp.ui.groups.GroupListActivity.class);
            startActivity(intent);
        });

        buttonChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.runmapproapp.ui.chat.ChatListActivity.class);
            startActivity(intent);
        });

        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (authManager != null && !authManager.isLoggedIn()) {
            redirectToLogin();
        } else if (textWelcome != null) {
            String name = authManager.getUserName();
            textWelcome.setText(getString(R.string.welcome_message, name == null ? "" : name));
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.main_title);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}