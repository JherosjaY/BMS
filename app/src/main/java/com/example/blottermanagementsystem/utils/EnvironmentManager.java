package com.example.blottermanagementsystem.utils;

import android.util.Log;

/**
 * ✅ ENVIRONMENT MANAGER
 * Manages environment-specific configurations (dev vs production)
 */
public class EnvironmentManager {
    private static final String TAG = "EnvironmentManager";
    
    // Production URLs
    private static final String PRODUCTION_BASE_URL = "https://blotter-backend.onrender.com/api/";
    private static final String PRODUCTION_FIREBASE_URL = "https://your-firebase-project.firebaseio.com";
    
    // Development URLs
    private static final String DEVELOPMENT_BASE_URL = "http://10.0.2.2:3000/api/";
    private static final String DEVELOPMENT_FIREBASE_URL = "http://10.0.2.2:9000";
    
    /**
     * Get API base URL based on build type
     */
    public static String getApiBaseUrl() {
        String url = isProduction() ? PRODUCTION_BASE_URL : DEVELOPMENT_BASE_URL;
        Log.d(TAG, "📡 Using API URL: " + url);
        return url;
    }
    
    /**
     * Get Firebase URL based on build type
     */
    public static String getFirebaseUrl() {
        String url = isProduction() ? PRODUCTION_FIREBASE_URL : DEVELOPMENT_FIREBASE_URL;
        Log.d(TAG, "🔥 Using Firebase URL: " + url);
        return url;
    }
    
    /**
     * Check if running in production
     */
    public static boolean isProduction() {
        // Check if app is debuggable (debug builds are not production)
        return !isDebugBuild();
    }
    
    /**
     * Check if running in development
     */
    public static boolean isDevelopment() {
        return isDebugBuild();
    }
    
    /**
     * Check if this is a debug build
     */
    private static boolean isDebugBuild() {
        try {
            // Try to access BuildConfig.DEBUG
            Class<?> buildConfig = Class.forName("com.example.blottermanagementsystem.BuildConfig");
            java.lang.reflect.Field debugField = buildConfig.getField("DEBUG");
            return (Boolean) debugField.get(null);
        } catch (Exception e) {
            // If BuildConfig is not available, assume development
            Log.w(TAG, "Could not determine build type, assuming development");
            return true;
        }
    }
    
    /**
     * Get environment name
     */
    public static String getEnvironmentName() {
        return isProduction() ? "PRODUCTION" : "DEVELOPMENT";
    }
    
    /**
     * Get build variant
     */
    public static String getBuildVariant() {
        return isDevelopment() ? "debug" : "release";
    }
    
    /**
     * Get app version
     */
    public static String getAppVersion() {
        return "1.0.0"; // Update this with your actual version
    }
    
    /**
     * Get app version code
     */
    public static int getAppVersionCode() {
        return 1; // Update this with your actual version code
    }
    
    /**
     * Log environment info
     */
    public static void logEnvironmentInfo() {
        Log.i(TAG, "========== ENVIRONMENT INFO ==========");
        Log.i(TAG, "Environment: " + getEnvironmentName());
        Log.i(TAG, "Build Type: " + getBuildVariant());
        Log.i(TAG, "App Version: " + getAppVersion() + " (" + getAppVersionCode() + ")");
        Log.i(TAG, "API URL: " + getApiBaseUrl());
        Log.i(TAG, "Firebase URL: " + getFirebaseUrl());
        Log.i(TAG, "======================================");
    }
    
    /**
     * Get timeout duration based on environment
     */
    public static int getApiTimeoutSeconds() {
        // Longer timeout for production to account for network latency
        return isProduction() ? 30 : 15;
    }
    
    /**
     * Get retry count based on environment
     */
    public static int getMaxRetries() {
        // More retries in production for reliability
        return isProduction() ? 5 : 3;
    }
    
    /**
     * Get sync interval in milliseconds
     */
    public static long getSyncIntervalMs() {
        // More frequent sync in development
        return isDevelopment() ? 5 * 60 * 1000 : 15 * 60 * 1000; // 5 min dev, 15 min prod
    }
    
    /**
     * Check if logging is enabled
     */
    public static boolean isLoggingEnabled() {
        return isDevelopment();
    }
    
    /**
     * Check if crash reporting is enabled
     */
    public static boolean isCrashReportingEnabled() {
        return isProduction();
    }
}
