package com.example.blottermanagementsystem.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.example.blottermanagementsystem.utils.PermissionHelper;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class PermissionsSetupActivity extends BaseActivity {
    
    private MaterialButton btnAllowAll;
    private MaterialButton btnSkip;
    private PreferencesManager preferencesManager;
    
    // These match exactly the 5 permissions shown in the UI
    private List<String> getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        
        // 1. Notifications (Android 13+ only)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        
        // 2. Camera
        permissions.add(Manifest.permission.CAMERA);
        
        // 3. Photos and Videos (Storage)
        permissions.add(PermissionHelper.getStoragePermission());
        
        // 4. SMS
        permissions.add(Manifest.permission.SEND_SMS);
        
        return permissions;
    }
    
    private final ActivityResultLauncher<String[]> permissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            // Count how many permissions were granted
            int grantedCount = 0;
            int totalCount = result.size();
            
            for (Boolean granted : result.values()) {
                if (granted) {
                    grantedCount++;
                }
            }
            
            // Always proceed - features will request permissions when needed
            // No toast needed - user is moving to login screen
            proceedToLogin();
        });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_setup);
        
        android.util.Log.d("PermissionsSetupActivity", "✅ PermissionsSetupActivity.onCreate() - Screen is displaying!");
        
        preferencesManager = new PreferencesManager(this);
        
        initViews();
        setupPermissionItems(); // FIX: Call this to set up permission icons/text
        setupListeners();
        animateShieldIcon(); // Only animate shield icon
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // If permissions are already granted, skip this screen
        if (preferencesManager.isPermissionsGranted()) {
            android.util.Log.d("PermissionsSetupActivity", "⚠️ Permissions already granted - skipping to WelcomeActivity");
            startActivity(new Intent(this, com.example.blottermanagementsystem.ui.activities.WelcomeActivity.class));
            finish();
        }
    }
    
    private void animateShieldIcon() {
        // Find the shield icon CardView
        androidx.cardview.widget.CardView shieldCard = findViewById(R.id.shieldIconCard);
        
        if (shieldCard != null) {
            // Start invisible and slightly small (like Welcome screen)
            shieldCard.setAlpha(0f);
            shieldCard.setScaleX(0.9f);
            shieldCard.setScaleY(0.9f);
            
            // Animate smooth fade + scale (like Welcome screen)
            shieldCard.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setStartDelay(200)
                .start();
        }
    }
    
    private void initViews() {
        btnAllowAll = findViewById(R.id.btnAllowAll);
        btnSkip = findViewById(R.id.btnSkip);
    }
    
    private void setupPermissionItems() {
        // Notifications
        android.widget.ImageView ivNotifIcon = findViewById(R.id.permissionNotifications).findViewById(R.id.ivPermissionIcon);
        TextView tvNotifTitle = findViewById(R.id.permissionNotifications).findViewById(R.id.tvPermissionTitle);
        TextView tvNotifDesc = findViewById(R.id.permissionNotifications).findViewById(R.id.tvPermissionDescription);
        ivNotifIcon.setImageResource(R.drawable.ic_permission_notification);
        tvNotifTitle.setText("Notifications");
        tvNotifDesc.setText("Receive important updates about your cases and hearings");
        
        // Camera
        android.widget.ImageView ivCameraIcon = findViewById(R.id.permissionCamera).findViewById(R.id.ivPermissionIcon);
        TextView tvCameraTitle = findViewById(R.id.permissionCamera).findViewById(R.id.tvPermissionTitle);
        TextView tvCameraDesc = findViewById(R.id.permissionCamera).findViewById(R.id.tvPermissionDescription);
        ivCameraIcon.setImageResource(R.drawable.ic_permission_camera);
        tvCameraTitle.setText("Camera");
        tvCameraDesc.setText("Take photos for evidence and documentation");
        
        // Photos
        android.widget.ImageView ivPhotosIcon = findViewById(R.id.permissionPhotos).findViewById(R.id.ivPermissionIcon);
        TextView tvPhotosTitle = findViewById(R.id.permissionPhotos).findViewById(R.id.tvPermissionTitle);
        TextView tvPhotosDesc = findViewById(R.id.permissionPhotos).findViewById(R.id.tvPermissionDescription);
        ivPhotosIcon.setImageResource(R.drawable.ic_permission_gallery);
        tvPhotosTitle.setText("Photos and Videos");
        tvPhotosDesc.setText("Upload images from your gallery as evidence");
        
        // SMS
        android.widget.ImageView ivSmsIcon = findViewById(R.id.permissionSms).findViewById(R.id.ivPermissionIcon);
        TextView tvSmsTitle = findViewById(R.id.permissionSms).findViewById(R.id.tvPermissionTitle);
        TextView tvSmsDesc = findViewById(R.id.permissionSms).findViewById(R.id.tvPermissionDescription);
        ivSmsIcon.setImageResource(R.drawable.ic_permission_sms);
        tvSmsTitle.setText("SMS");
        tvSmsDesc.setText("Send notifications via text message");
    }
    
    private void setupListeners() {
        btnAllowAll.setOnClickListener(v -> requestPermissions());
        btnSkip.setOnClickListener(v -> {
            // Mark as granted even if skipped (to avoid showing again) - SYNCED WITH KOTLIN
            preferencesManager.setPermissionsGranted(true);
            proceedToLogin();
        });
    }
    
    private void requestPermissions() {
        List<String> allPermissions = getRequiredPermissions();
        List<String> permissionsToRequest = new ArrayList<>();
        
        // Check which permissions are not granted yet
        for (String permission : allPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            // All permissions already granted - proceed directly
            proceedToLogin();
        }
    }
    
    private void proceedToLogin() {
        android.util.Log.d("PermissionsSetupActivity", "✅ proceedToLogin() - Setting permissions_granted = true");
        preferencesManager.setPermissionsGranted(true);
        android.util.Log.d("PermissionsSetupActivity", "🔓 proceedToLogin() - Launching WelcomeActivity");
        
        // Go to WelcomeActivity (3-screen auth flow)
        Intent intent = new Intent(this, com.example.blottermanagementsystem.ui.activities.WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
