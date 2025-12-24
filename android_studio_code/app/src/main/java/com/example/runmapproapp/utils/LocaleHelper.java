package com.example.runmapproapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {
    
    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";
    private static final String PREFS_NAME = "language_prefs";
    
    public static Context setLocale(Context context, String language) {
        persist(context, language);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }
        
        return updateResourcesLegacy(context, language);
    }
    
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(SELECTED_LANGUAGE, "en");
    }
    
    private static void persist(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }
    
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        
        return context.createConfigurationContext(configuration);
    }
    
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }
        
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        
        return context;
    }
    
    public static Context onAttach(Context context) {
        String language = getLanguage(context);
        return setLocale(context, language);
    }
    
    public static String getLanguageName(String languageCode) {
        switch (languageCode) {
            case "vi":
                return "Tiếng Việt";
            case "zh":
                return "中文";
            case "en":
            default:
                return "English";
        }
    }
}
