package com.example.blottermanagementsystem.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

/**
 * Helper class for checking and managing permissions across the app.
 * This ensures consistent permission handling logic everywhere.
 */
public class PermissionHelper {
    
    /**
     * Check if camera permission is granted
     */
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if storage/media permission is granted
     * Handles Android 13+ (READ_MEDIA_IMAGES) and older versions (READ_EXTERNAL_STORAGE)
     */
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) 
                == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * Get the appropriate storage permission string for the current Android version
     */
    public static String getStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            return Manifest.permission.READ_EXTERNAL_STORAGE;
        }
    }
    
    /**
     * Check if audio recording permission is granted
     */
    public static boolean hasAudioPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if SMS permission is granted
     */
    public static boolean hasSmsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if notification permission is granted (Android 13+)
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                == PackageManager.PERMISSION_GRANTED;
        }
        // Notifications don't need permission on older Android versions
        return true;
    }
    
    /**
     * Check if location permission is granted
     */
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Check if we should show rationale for a permission
     * Returns true if user denied permission but didn't select "Don't ask again"
     */
    public static boolean shouldShowRationale(Activity activity, String permission) {
        return activity.shouldShowRequestPermissionRationale(permission);
    }
    
    /**
     * Get user-friendly permission name
     */
    public static String getPermissionName(String permission) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                return "Camera";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.READ_MEDIA_IMAGES:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Storage";
            case Manifest.permission.RECORD_AUDIO:
                return "Microphone";
            case Manifest.permission.SEND_SMS:
                return "SMS";
            case Manifest.permission.POST_NOTIFICATIONS:
                return "Notifications";
            case Manifest.permission.ACCESS_FINE_LOCATION:
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "Location";
            default:
                return "Permission";
        }
    }
    
    /**
     * Get user-friendly permission description
     */
    public static String getPermissionDescription(String permission) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                return "Camera permission is needed to take photos for evidence and documentation.";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.READ_MEDIA_IMAGES:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "Storage permission is needed to access and save photos.";
            case Manifest.permission.RECORD_AUDIO:
                return "Microphone permission is needed to record audio statements and evidence.";
            case Manifest.permission.SEND_SMS:
                return "SMS permission is needed to send notifications via text message.";
            case Manifest.permission.POST_NOTIFICATIONS:
                return "Notification permission is needed to keep you updated about your cases.";
            case Manifest.permission.ACCESS_FINE_LOCATION:
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return "Location permission is needed to record incident locations.";
            default:
                return "This permission is required for the app to function properly.";
        }
    }
}
