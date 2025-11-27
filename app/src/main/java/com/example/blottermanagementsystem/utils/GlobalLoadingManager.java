package com.example.blottermanagementsystem.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.blottermanagementsystem.R;

/**
 * Global Loading Manager - One implementation for all screens!
 * Usage: GlobalLoadingManager.show(context, "Loading..."); 
 *        GlobalLoadingManager.hide();
 */
public class GlobalLoadingManager {
    
    private static Dialog loadingDialog;
    private static boolean isShowing = false;
    private static Handler timeoutHandler = new Handler();
    private static Runnable stage2Runnable; // 15s timeout
    private static Runnable stage3Runnable; // 20s timeout
    private static TextView currentMessageView;
    private static android.view.View emergencyOverlay;
    
    /**
     * Show loading dialog with custom message
     */
    public static void show(Context context, String message) {
        if (context == null || !(context instanceof Activity)) return;
        
        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) return;
        
        try {
            hide(); // Hide any existing dialog first
            
            // Create loading dialog
            loadingDialog = new Dialog(context);
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            loadingDialog.setCancelable(false);
            loadingDialog.setCanceledOnTouchOutside(false);
            
            // Inflate custom loading layout
            View loadingView = LayoutInflater.from(context).inflate(R.layout.dialog_global_loading, null);
            loadingDialog.setContentView(loadingView);
            
            // Set message and store reference
            currentMessageView = loadingView.findViewById(R.id.tvLoadingMessage);
            if (currentMessageView != null) {
                currentMessageView.setText(message);
            }
            
            // Dimmed background with transparency
            if (loadingDialog.getWindow() != null) {
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                loadingDialog.getWindow().setDimAmount(0.7f); // 70% dim
                loadingDialog.getWindow().setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                    android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
                );
            }
            
            loadingDialog.show();
            isShowing = true;
            
            // Show ProgressBar spinner
            try {
                ProgressBar progressBar = loadingView.findViewById(R.id.progressBar);
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    // ProgressBar animation starts automatically
                }
            } catch (Exception e) {
                android.util.Log.e("GlobalLoading", "❌ Error showing progress bar: " + e.getMessage());
            }
            
            // Smart timeout system - Progressive messages
            setupSmartTimeouts();
            
        } catch (Exception e) {
            android.util.Log.e("GlobalLoading", "❌ Error showing loading dialog: " + e.getMessage());
            e.printStackTrace();
            
            // If dialog completely fails, try to show a simple overlay
            showEmergencyOverlay(context, message);
        }
    }
    
    /**
     * Show loading with default message
     */
    public static void show(Context context) {
        show(context, "Loading...");
    }
    
    /**
     * Show loading for cloud operations
     */
    public static void showCloud(Context context, String operation) {
        show(context, "☁️ " + operation + "...");
    }
    
    /**
     * Show loading for database operations
     */
    public static void showDatabase(Context context, String operation) {
        show(context, "💾 " + operation + "...");
    }
    
    /**
     * Show loading for CloudBase/Network operations
     */
    public static void showCloudBase(Context context, String operation) {
        show(context, "☁️ " + operation + "...");
    }
    
    /**
     * Show loading for login operations
     */
    public static void showLogin(Context context) {
        show(context, "🔐 Signing in...");
    }
    
    /**
     * Show loading for upload operations
     */
    public static void showUpload(Context context, String fileType) {
        show(context, "📤 Uploading " + fileType + "...");
    }
    
    /**
     * Show loading for sync operations
     */
    public static void showSync(Context context) {
        show(context, "🔄 Syncing data...");
    }
    
    /**
     * Show loading for email operations
     */
    public static void showEmail(Context context) {
        show(context, "📧 Sending email...");
    }
    
    /**
     * Hide loading dialog and emergency overlay
     */
    public static void hide() {
        try {
            // Hide dialog
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            
            // Hide emergency overlay if exists
            hideEmergencyOverlay();
            
            isShowing = false;
            
            // Cancel all timeout callbacks
            clearTimeouts();
        } catch (Exception e) {
            android.util.Log.e("GlobalLoading", "Error hiding loading dialog: " + e.getMessage());
        }
    }
    
    /**
     * Setup smart timeout system with progressive messages
     */
    private static void setupSmartTimeouts() {
        // Clear any existing timeouts
        clearTimeouts();
        
        // Stage 2: After 15 seconds - "Taking longer than expected..."
        stage2Runnable = () -> {
            if (isShowing && currentMessageView != null) {
                currentMessageView.setText("Taking longer than expected...\nPlease wait while we connect.");
                // Stage 2: Slow connection detected
            }
        };
        timeoutHandler.postDelayed(stage2Runnable, 15000); // 15 seconds
        
        // Stage 3: After 20 seconds - "Please check your internet connection"
        stage3Runnable = () -> {
            if (isShowing && currentMessageView != null) {
                currentMessageView.setText("Still connecting...\nPlease check your internet connection.");
                // Stage 3: Very slow connection detected
            }
        };
        timeoutHandler.postDelayed(stage3Runnable, 20000); // 20 seconds
    }
    
    /**
     * Clear all timeout callbacks
     */
    private static void clearTimeouts() {
        if (stage2Runnable != null) {
            timeoutHandler.removeCallbacks(stage2Runnable);
        }
        if (stage3Runnable != null) {
            timeoutHandler.removeCallbacks(stage3Runnable);
        }
    }
    
    /**
     * Check if loading is currently showing
     */
    public static boolean isShowing() {
        return isShowing && loadingDialog != null && loadingDialog.isShowing();
    }
    /**
     * Auto-hide after specified milliseconds
     */
    public static void showWithTimeout(Context context, String message, long timeoutMs) {
        show(context, message);
        
        new android.os.Handler().postDelayed(() -> {
            hide();
        }, timeoutMs);
    }
    
    
    /**
     * Emergency overlay when dialog completely fails
     */
    private static void showEmergencyOverlay(Context context, String message) {
        try {
            if (!(context instanceof Activity)) return;
            Activity activity = (Activity) context;
            
            // Create simple overlay programmatically
            android.widget.FrameLayout overlay = new android.widget.FrameLayout(context);
            overlay.setBackgroundColor(0xCC000000); // Semi-transparent black
            overlay.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
            
            // Add progress indicator
            android.widget.LinearLayout content = new android.widget.LinearLayout(context);
            content.setOrientation(android.widget.LinearLayout.VERTICAL);
            content.setGravity(android.view.Gravity.CENTER);
            
            // System progress bar
            android.widget.ProgressBar progressBar = new android.widget.ProgressBar(context);
            progressBar.setIndeterminate(true);
            content.addView(progressBar);
            
            // Message
            android.widget.TextView messageView = new android.widget.TextView(context);
            messageView.setText(message);
            messageView.setTextColor(0xFFFFFFFF);
            messageView.setTextSize(16);
            messageView.setGravity(android.view.Gravity.CENTER);
            messageView.setPadding(32, 32, 32, 32);
            content.addView(messageView);
            
            android.widget.FrameLayout.LayoutParams contentParams = 
                new android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
            contentParams.gravity = android.view.Gravity.CENTER;
            overlay.addView(content, contentParams);
            
            // Add to activity's root view
            android.view.ViewGroup rootView = activity.findViewById(android.R.id.content);
            if (rootView != null) {
                rootView.addView(overlay);
                emergencyOverlay = overlay; // Store reference for cleanup
                // Emergency overlay displayed
            }
            
        } catch (Exception e) {
            android.util.Log.e("GlobalLoading", "❌ Emergency overlay failed: " + e.getMessage());
        }
    }
    
    /**
     * Hide emergency overlay
     */
    private static void hideEmergencyOverlay() {
        try {
            if (emergencyOverlay != null && emergencyOverlay.getParent() != null) {
                android.view.ViewGroup parent = (android.view.ViewGroup) emergencyOverlay.getParent();
                parent.removeView(emergencyOverlay);
                emergencyOverlay = null;
                // Emergency overlay removed
            }
        } catch (Exception e) {
            android.util.Log.e("GlobalLoading", "❌ Error hiding emergency overlay: " + e.getMessage());
        }
    }
}
