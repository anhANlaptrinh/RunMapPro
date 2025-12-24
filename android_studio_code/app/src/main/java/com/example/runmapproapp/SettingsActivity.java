package com.example.runmapproapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.runmapproapp.auth.AuthManager;
import com.example.runmapproapp.ui.profile.UserProfileActivity;
import com.example.runmapproapp.utils.LocaleHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.runmapproapp.utils.BottomNavigationHelper;

public class SettingsActivity extends AppCompatActivity {

    private AuthManager authManager;
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        authManager = new AuthManager(this);

        setupToolbar();

        // Setup bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav, R.id.nav_settings);

        MaterialCardView buttonMyProfile = findViewById(R.id.buttonMyProfile);
        MaterialCardView buttonEditProfile = findViewById(R.id.buttonEditProfile);
        MaterialCardView buttonLanguage = findViewById(R.id.buttonLanguage);
        MaterialCardView buttonLogout = findViewById(R.id.buttonLogout);

        buttonMyProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
        });

        buttonEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });
        
        buttonLanguage.setOnClickListener(v -> showLanguageDialog());

        buttonLogout.setOnClickListener(v -> {
            authManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.settings);
        }
    }
    
    private void showLanguageDialog() {
        String currentLang = LocaleHelper.getLanguage(this);
        String[] languages = {
            getString(R.string.language_vietnamese),
            getString(R.string.language_english),
            getString(R.string.language_chinese)
        };
        String[] langCodes = {"vi", "en", "zh"};
        
        int selectedIndex = 0;
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLang)) {
                selectedIndex = i;
                break;
            }
        }
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_language)
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String selectedLang = langCodes[which];
                    if (!selectedLang.equals(currentLang)) {
                        LocaleHelper.setLocale(this, selectedLang);
                        // Restart app with MainActivity then back to Settings
                        Intent mainIntent = new Intent(this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Intent settingsIntent = new Intent(this, SettingsActivity.class);
                        startActivities(new Intent[]{mainIntent, settingsIntent});
                        finish();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.setupBottomNavigation(this, bottomNav, R.id.nav_settings);
    }

}
