package com.example.blottermanagementsystem.utils;

import android.content.Context;
import java.util.concurrent.Executors;

/**
 * AUTOMATIC Loading Interceptor - No manual coding needed!
 * This automatically shows/hides loading for common operations
 */
public class AutoLoadingInterceptor {
    
    /**
     * Automatically handle database operations with loading
     */
    public static void executeWithLoading(Context context, String operation, Runnable task) {
        GlobalLoadingManager.show(context, operation + "...");
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Execute the task
                task.run();
                
                // Auto-hide loading after task completes with delay
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    GlobalLoadingManager.hide();
                }, 500); // Small delay to show the animation
                
            } catch (Exception e) {
                // Auto-hide loading on error
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    GlobalLoadingManager.hide();
                });
                android.util.Log.e("AutoLoading", "Error in task: " + e.getMessage());
            }
        });
    }
    
    /**
     * Automatically handle cloud operations with loading
     */
    public static void executeCloudWithLoading(Context context, String operation, Runnable task) {
        GlobalLoadingManager.showCloud(context, operation);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                task.run();
                
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    GlobalLoadingManager.hide();
                });
                
            } catch (Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    GlobalLoadingManager.hide();
                });
                android.util.Log.e("AutoLoading", "Error in cloud task: " + e.getMessage());
            }
        });
    }
    
    /**
     * Automatically handle email operations with loading
     */
    public static void executeEmailWithLoading(Context context, Runnable task) {
        GlobalLoadingManager.show(context, "📧 Sending email...");
        
        // Simulate email delay (real operations will have natural delay)
        new android.os.Handler().postDelayed(() -> {
            try {
                task.run();
                GlobalLoadingManager.hide();
            } catch (Exception e) {
                GlobalLoadingManager.hide();
                android.util.Log.e("AutoLoading", "Error in email task: " + e.getMessage());
            }
        }, 1500);
    }
    
    /**
     * Show loading with auto-timeout (for operations without callbacks)
     */
    public static void showWithAutoTimeout(Context context, String message, long timeoutMs) {
        GlobalLoadingManager.show(context, message);
        
        new android.os.Handler().postDelayed(() -> {
            GlobalLoadingManager.hide();
        }, timeoutMs);
    }
}
