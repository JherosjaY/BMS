package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class LanguageManager {
    private static final String PREF_LANGUAGE = "app_language";
    private final PreferencesManager preferencesManager;
    
    public LanguageManager(Context context) {
        this.preferencesManager = new PreferencesManager(context);
    }
    
    public void setLanguage(Context context, String languageCode) {
        preferencesManager.saveString(PREF_LANGUAGE, languageCode);
        applyLanguage(context, languageCode);
    }
    
    public String getCurrentLanguage() {
        return preferencesManager.getString(PREF_LANGUAGE, "en");
    }
    
    public void applyLanguage(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        
        context.createConfigurationContext(config);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
    
    public void applySavedLanguage(Context context) {
        String languageCode = getCurrentLanguage();
        applyLanguage(context, languageCode);
    }
    
    public static String[] getSupportedLanguages() {
        return new String[]{"en", "fil", "ceb"};
    }
    
    public static String[] getSupportedLanguageNames() {
        return new String[]{"English", "Filipino", "Cebuano"};
    }
}
