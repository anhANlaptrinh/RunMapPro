package com.example.runmapproapp.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.runmapproapp.R;
import com.example.runmapproapp.utils.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {

    private CardView cardLanguage;
    private TextView tvCurrentLanguage;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);

        setupToolbar();
        bindViews();
        setupListeners();
        updateLanguageDisplay();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.settings);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void bindViews() {
        cardLanguage = findViewById(R.id.cardLanguage);
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage);
    }

    private void setupListeners() {
        cardLanguage.setOnClickListener(v -> showLanguageDialog());
    }

    private void updateLanguageDisplay() {
        String currentLang = LocaleHelper.getLanguage(this);
        tvCurrentLanguage.setText(LocaleHelper.getLanguageName(currentLang));
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
                        recreate();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
