package com.example.blottermanagementsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.User;
import com.example.blottermanagementsystem.ui.activities.BaseActivity;
import com.example.blottermanagementsystem.ui.activities.OnboardingActivity;
import com.example.blottermanagementsystem.ui.activities.AdminDashboardActivity;
import com.example.blottermanagementsystem.ui.activities.OfficerDashboardActivity;
import com.example.blottermanagementsystem.ui.activities.UserDashboardActivity;
import com.example.blottermanagementsystem.ui.activities.ProfilePictureSelectionActivity;
import com.example.blottermanagementsystem.utils.PreferencesManager;
// import com.example.blottermanagementsystem.services.BackgroundSyncService; // 🚀 DISABLED: Pure Neon mode
import com.example.blottermanagementsystem.websocket.WebSocketManager;
import com.example.blottermanagementsystem.websocket.RealtimeListener;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity implements RealtimeListener {
    
    private PreferencesManager preferencesManager;
    private BlotterDatabase database;
    private WebSocketManager webSocketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 🚀 DISABLED: Background Sync Service (Pure Neon mode - no local sync needed)
        // Intent syncIntent = new Intent(this, BackgroundSyncService.class);
        // startService(syncIntent);
        android.util.Log.d("MainActivity", "⚠️ Background Sync Service DISABLED - Pure Neon mode");
        
        // 🔌 INITIALIZE WEBSOCKET FOR REAL-TIME UPDATES
        webSocketManager = new WebSocketManager(this);
        webSocketManager.addListener(this);
        android.util.Log.d("MainActivity", "🔌 WebSocket Manager initialized");
        
        // MainActivity is just a router - no layout needed
        android.util.Log.d("MainActivity", "🚀 MainActivity started - routing to appropriate screen");
        
        preferencesManager = new PreferencesManager(this);
        database = BlotterDatabase.getDatabase(this);
        createAdminAccountIfNotExists();
        
        // CRITICAL FIX: On first launch after clear data, ensure flags are properly initialized
        boolean isOnboardingCompleted = preferencesManager.isOnboardingCompleted();
        boolean isPermissionsGranted = preferencesManager.isPermissionsGranted();
        boolean isLoggedIn = preferencesManager.isLoggedIn();
        
        android.util.Log.d("MainActivity", "Initial state - Onboarding: " + isOnboardingCompleted + ", Permissions: " + isPermissionsGranted + ", LoggedIn: " + isLoggedIn);
        
        // CRITICAL: If onboarding is NOT completed, we're in a fresh/reset state
        // Reset ALL flags to false to ensure clean onboarding flow
        if (!isOnboardingCompleted) {
            android.util.Log.d("MainActivity", "⚠️ FRESH STATE DETECTED - Onboarding not completed");
            android.util.Log.d("MainActivity", "🔄 Resetting ALL flags to ensure clean onboarding flow");
            preferencesManager.setOnboardingCompleted(false);
            preferencesManager.setPermissionsGranted(false);
            preferencesManager.setLoggedIn(false);
            preferencesManager.setHasSelectedProfilePicture(false);
            isOnboardingCompleted = false;
            isPermissionsGranted = false;
            isLoggedIn = false;
            android.util.Log.d("MainActivity", "✅ All flags reset to false - Onboarding will show");
        }
        
        // Determine start destination - SYNCED WITH KOTLIN VERSION (MainActivity.kt lines 231-243)
        // Flags naturally default to false and persist across app launches
        // They only reset when user clears app data (which is correct behavior)
        android.util.Log.d("MainActivity", "🔍 === APP START ROUTING ===");
        android.util.Log.d("MainActivity", "Onboarding completed: " + isOnboardingCompleted);
        android.util.Log.d("MainActivity", "Permissions granted: " + isPermissionsGranted);
        android.util.Log.d("MainActivity", "Logged in: " + isLoggedIn);
        android.util.Log.d("MainActivity", "User Role: " + preferencesManager.getUserRole());
        // KOTLIN LOGIC: Check flags in order (using local variables to ensure consistency)
        if (!isOnboardingCompleted) {
            // 1. Show onboarding first
            android.util.Log.d("MainActivity", "🎬 ONBOARDING NOT COMPLETED - Launching OnboardingActivity");
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        } else if (!isPermissionsGranted) {
            // 2. Then show permissions
            android.util.Log.d("MainActivity", "🔐 PERMISSIONS NOT GRANTED - Launching PermissionsSetupActivity");
            startActivity(new Intent(this, com.example.blottermanagementsystem.ui.activities.PermissionsSetupActivity.class));
            finish();
        } else if (!isLoggedIn) {
            // 3. Then show login/welcome
            android.util.Log.d("MainActivity", "🔓 NOT LOGGED IN - Going to WelcomeActivity (Login/Register)");
            startActivity(new Intent(this, com.example.blottermanagementsystem.ui.activities.WelcomeActivity.class));
            finish();
        } else {
            // 4. User is logged in - check role and profile picture requirement
            String role = preferencesManager.getUserRole();
            android.util.Log.d("MainActivity", "✅ USER LOGGED IN - User role: " + role);
            
            // Admin and Officer roles require re-login when app is reopened
            // They must go to WelcomeActivity (Login page) for security
            if ("Admin".equals(role)) {
                android.util.Log.d("MainActivity", "👨‍💼 ADMIN ROLE - Requiring re-login, going to WelcomeActivity");
                // Clear login flag to force re-login
                preferencesManager.setLoggedIn(false);
                startActivity(new Intent(this, com.example.blottermanagementsystem.ui.activities.WelcomeActivity.class));
                finish();
            } else if ("Officer".equals(role)) {
                android.util.Log.d("MainActivity", "👮 OFFICER ROLE - Requiring re-login, going to WelcomeActivity");
                // Clear login flag to force re-login
                preferencesManager.setLoggedIn(false);
                startActivity(new Intent(this, com.example.blottermanagementsystem.ui.activities.WelcomeActivity.class));
                finish();
            } else {
                // User role - check if profile picture selected
                if (!preferencesManager.hasSelectedProfilePicture()) {
                    android.util.Log.d("MainActivity", "🖼️ USER ROLE - PROFILE PICTURE NOT SELECTED - Going to ProfilePictureSelectionActivity");
                    startActivity(new Intent(this, ProfilePictureSelectionActivity.class));
                    finish();
                } else {
                    android.util.Log.d("MainActivity", "✅ USER ROLE - PROFILE PICTURE SELECTED - Going to UserDashboard");
                    startActivity(new Intent(this, UserDashboardActivity.class));
                    finish();
                }
            }
        }
    }
    
    private void checkProfilePictureAndNavigate() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                int userId = preferencesManager.getUserId();
                User user = database.userDao().getUserById(userId);
                boolean hasProfilePhotoInDb = user != null && user.getProfilePhotoUri() != null && !user.getProfilePhotoUri().isEmpty();
                
                // If user has profile photo in DB, set the flag to true
                if (hasProfilePhotoInDb) {
                    preferencesManager.setHasSelectedProfilePicture(true);
                    android.util.Log.d("MainActivity", "✅ User has profile photo in DB, setting flag to TRUE");
                }
                
                boolean hasSelectedPfp = preferencesManager.hasSelectedProfilePicture();
                android.util.Log.d("MainActivity", "User hasSelectedProfilePicture: " + hasSelectedPfp);
                
                // Navigate on UI thread
                runOnUiThread(() -> {
                    Intent intent;
                    if (!hasSelectedPfp) {
                        android.util.Log.d("MainActivity", "→ Going to ProfilePictureSelectionActivity");
                        intent = new Intent(this, ProfilePictureSelectionActivity.class);
                    } else {
                        android.util.Log.d("MainActivity", "→ Going to UserDashboardActivity");
                        intent = new Intent(this, UserDashboardActivity.class);
                    }
                    startActivity(intent);
                    finish();
                });
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error checking profile picture: " + e.getMessage());
                e.printStackTrace();
                // Fallback: go to dashboard
                runOnUiThread(() -> {
                    startActivity(new Intent(this, UserDashboardActivity.class));
                    finish();
                });
            }
        });
    }
    
    private void createAdminAccountIfNotExists() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Check if admin account exists
            User existingAdmin = database.userDao().getUserByUsername("admin");
            
            if (existingAdmin == null) {
                // Create built-in admin account with hashed password
                String hashedPassword = hashPassword("BMS2025");
                User admin = new User("System", "Administrator", "admin", hashedPassword, "Admin");
                admin.setActive(true);
                database.userDao().insertUser(admin);
                android.util.Log.d("MainActivity", "✅ Default admin account created: admin/BMS2025");
            } else {
                // Update existing admin password to hashed version if it's still plain text
                if (existingAdmin.getPassword().equals("admin123")) {
                    String hashedPassword = hashPassword("BMS2025");
                    existingAdmin.setPassword(hashedPassword);
                    database.userDao().updateUser(existingAdmin);
                    android.util.Log.d("MainActivity", "✅ Admin password updated to hashed version");
                }
            }
        });
    }
    
    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error hashing password", e);
            return password; // Fallback to plain text (not recommended)
        }
    }
    
    /**
     * DEBUG METHOD: Reset all flags to see the full onboarding flow
     * Call this to reset: onboarding, permissions, and login flags
     */
    private void resetAllFlags() {
        android.util.Log.d("MainActivity", "🔄 RESETTING ALL FLAGS FOR TESTING");
        preferencesManager.setOnboardingCompleted(false);
        preferencesManager.setPermissionsGranted(false);
        preferencesManager.setLoggedIn(false);
        android.util.Log.d("MainActivity", "✅ All flags reset!");
        android.util.Log.d("MainActivity", "   - onboarding_completed: false");
        android.util.Log.d("MainActivity", "   - permissions_granted: false");
        android.util.Log.d("MainActivity", "   - is_logged_in: false");
    }
    
    // ============================================================================
    // WEBSOCKET REAL-TIME UPDATES
    // ============================================================================
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Connect WebSocket when user is logged in
        if (preferencesManager.isLoggedIn()) {
            String userId = String.valueOf(preferencesManager.getUserId());
            String userRole = preferencesManager.getUserRole();
            
            if (webSocketManager != null && !webSocketManager.isConnected()) {
                android.util.Log.d("MainActivity", "🔌 Connecting WebSocket for user: " + userId);
                webSocketManager.connect(userId, userRole);
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Keep WebSocket connected in background for real-time updates
        // Only disconnect in onDestroy
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Disconnect WebSocket when app is destroyed
        if (webSocketManager != null) {
            android.util.Log.d("MainActivity", "🔌 Disconnecting WebSocket");
            webSocketManager.disconnect();
        }
    }
    
    /**
     * Handle real-time updates from WebSocket
     */
    @Override
    public void onRealtimeUpdate(String eventType, Object data) {
        android.util.Log.d("MainActivity", "📨 Real-time update: " + eventType);
        
        switch (eventType) {
            case "connected":
                android.util.Log.d("MainActivity", "✅ WebSocket connected");
                break;
                
            case "authenticated":
                android.util.Log.d("MainActivity", "✅ WebSocket authenticated");
                break;
                
            case "hearing_update":
                android.util.Log.d("MainActivity", "📅 Hearing update received");
                // Broadcast to all listening activities
                broadcastUpdate("hearing_update", data);
                break;
                
            case "case_update":
                android.util.Log.d("MainActivity", "📋 Case update received");
                broadcastUpdate("case_update", data);
                break;
                
            case "person_update":
                android.util.Log.d("MainActivity", "👤 Person update received");
                broadcastUpdate("person_update", data);
                break;
                
            case "notification":
                android.util.Log.d("MainActivity", "🔔 Notification received");
                broadcastUpdate("notification", data);
                break;
                
            case "disconnected":
                android.util.Log.d("MainActivity", "⚠️ WebSocket disconnected");
                break;
                
            case "error":
                android.util.Log.e("MainActivity", "❌ WebSocket error: " + data);
                break;
        }
    }
    
    /**
     * Broadcast real-time updates to other activities
     */
    private void broadcastUpdate(String eventType, Object data) {
        Intent intent = new Intent("com.example.blottermanagementsystem.REALTIME_UPDATE");
        intent.putExtra("eventType", eventType);
        if (data instanceof String) {
            intent.putExtra("data", (String) data);
        }
        sendBroadcast(intent);
        android.util.Log.d("MainActivity", "📢 Broadcast sent: " + eventType);
    }
}
