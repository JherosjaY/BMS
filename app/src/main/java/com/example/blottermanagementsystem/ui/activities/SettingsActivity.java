package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {
    
    private SwitchMaterial switchPushNotifications, switchEmailNotifications, switchSmsNotifications;
    private LinearLayout btnClearCache, btnBackupData, btnPrivacyPolicy;
    private LinearLayout layoutNewFeatures;
    private CardView cardReportOversight;
    private PreferencesManager preferencesManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        preferencesManager = new PreferencesManager(this);
        
        setupToolbar();
        initViews();
        loadSettings();
        setupListeners();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void initViews() {
        switchPushNotifications = findViewById(R.id.switchPushNotifications);
        switchEmailNotifications = findViewById(R.id.switchEmailNotifications);
        switchSmsNotifications = findViewById(R.id.switchSmsNotifications);
        btnClearCache = findViewById(R.id.btnClearCache);
        btnBackupData = findViewById(R.id.btnBackupData);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
        
        // New Features section (only if exists in layout)
        // Note: These cards may not exist in all layouts
        // layoutNewFeatures = findViewById(R.id.layoutNewFeatures);
        // cardReportOversight = findViewById(R.id.cardReportOversight);
        
        setupRoleBasedFeatures();
    }
    
    private void setupRoleBasedFeatures() {
        String role = preferencesManager.getRole();
        
        if (layoutNewFeatures == null) return;
        
        // Show new features section
        layoutNewFeatures.setVisibility(View.VISIBLE);
        
        // Admin features
        if ("Admin".equals(role)) {
            showView(cardReportOversight);
        }
        // Clerk features
        else if ("Clerk".equals(role)) {
            // Clerk-specific features if any
        }
        // User features - no special features for regular users
        else {
            // Regular users don't have special feature cards
        }
    }
    
    private void showView(View view) {
        if (view != null) view.setVisibility(View.VISIBLE);
    }
    
    private void loadSettings() {
        // Load saved preferences
        switchPushNotifications.setChecked(preferencesManager.isPushNotificationsEnabled());
        switchEmailNotifications.setChecked(preferencesManager.isEmailNotificationsEnabled());
        switchSmsNotifications.setChecked(preferencesManager.isSmsNotificationsEnabled());
    }
    
    private void setupListeners() {
        switchPushNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setPushNotificationsEnabled(isChecked);
            Toast.makeText(this, "Push notifications " + (isChecked ? "enabled" : "disabled"), 
                Toast.LENGTH_SHORT).show();
        });
        
        switchEmailNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setEmailNotificationsEnabled(isChecked);
            Toast.makeText(this, "Email notifications " + (isChecked ? "enabled" : "disabled"), 
                Toast.LENGTH_SHORT).show();
        });
        
        switchSmsNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setSmsNotificationsEnabled(isChecked);
            Toast.makeText(this, "SMS notifications " + (isChecked ? "enabled" : "disabled"), 
                Toast.LENGTH_SHORT).show();
        });
        
        btnClearCache.setOnClickListener(v -> {
            // TODO: Clear cache
            Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show();
        });
        
        btnBackupData.setOnClickListener(v -> {
            // TODO: Backup data
            Toast.makeText(this, "Backup started", Toast.LENGTH_SHORT).show();
        });
        
        btnPrivacyPolicy.setOnClickListener(v -> {
            // TODO: Open privacy policy
            Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show();
        });
        
        // NEW FEATURES LISTENERS (only for cards that exist)
        if (cardReportOversight != null) {
            cardReportOversight.setOnClickListener(v -> startActivity(new Intent(this, AdminReportOversightActivity.class)));
        }
    }
}
