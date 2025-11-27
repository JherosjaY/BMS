package com.example.blottermanagementsystem.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.utils.PreferencesManager;

public class OfficerSettingsActivity extends BaseActivity {
    
    private ImageButton btnBack;
    private SwitchMaterial switchPushNotifications, switchEmailNotifications, switchSmsNotifications;
    private LinearLayout layoutClearCache, layoutBackupData, layoutPrivacyPolicy;
    private PreferencesManager preferencesManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_settings);
        
        preferencesManager = new PreferencesManager(this);
        
        initViews();
        setupListeners();
        loadSettings();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        switchPushNotifications = findViewById(R.id.switchPushNotifications);
        switchEmailNotifications = findViewById(R.id.switchEmailNotifications);
        switchSmsNotifications = findViewById(R.id.switchSmsNotifications);
        layoutClearCache = findViewById(R.id.layoutClearCache);
        layoutBackupData = findViewById(R.id.layoutBackupData);
        layoutPrivacyPolicy = findViewById(R.id.layoutPrivacyPolicy);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        // Notification switches
        switchPushNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setPushNotificationsEnabled(isChecked);
        });
        
        switchEmailNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setEmailNotificationsEnabled(isChecked);
        });
        
        switchSmsNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setSmsNotificationsEnabled(isChecked);
        });
        
        // Data & Storage actions
        layoutClearCache.setOnClickListener(v -> clearCache());
        layoutBackupData.setOnClickListener(v -> backupData());
        layoutPrivacyPolicy.setOnClickListener(v -> showPrivacyPolicy());
    }
    
    private void loadSettings() {
        switchPushNotifications.setChecked(preferencesManager.isPushNotificationsEnabled());
        switchEmailNotifications.setChecked(preferencesManager.isEmailNotificationsEnabled());
        switchSmsNotifications.setChecked(preferencesManager.isSmsNotificationsEnabled());
    }
    
    private void clearCache() {
        // Clear app cache
        try {
            // Clear internal cache
            deleteDir(getCacheDir());
            
            // Clear external cache if available
            if (getExternalCacheDir() != null) {
                deleteDir(getExternalCacheDir());
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Failed to clear cache", Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean deleteDir(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new java.io.File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
    
    private void backupData() {
        // Placeholder for backup functionality
        Toast.makeText(this, "Backup feature coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void showPrivacyPolicy() {
        // Placeholder for privacy policy
        Toast.makeText(this, "Privacy Policy coming soon", Toast.LENGTH_SHORT).show();
    }
}
