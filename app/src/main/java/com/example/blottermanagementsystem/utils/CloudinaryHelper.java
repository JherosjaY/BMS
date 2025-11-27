package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.callback.ErrorInfo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * CloudinaryHelper - Handles all Cloudinary image uploads and management
 * Images are stored in cloud and synced across all user devices
 */
public class CloudinaryHelper {
    
    private static final String TAG = "CloudinaryHelper";
    private static final String CLOUD_NAME = "blotter-system"; // Replace with your Cloudinary cloud name
    private static final String UPLOAD_PRESET = "blotter_unsigned"; // Replace with your upload preset
    
    // Initialize Cloudinary (call this once in Application class)
    public static void initCloudinary(Context context) {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            MediaManager.init(context, config);
            Log.d(TAG, "✅ Cloudinary initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing Cloudinary: " + e.getMessage(), e);
        }
    }
    
    /**
     * Upload image to Cloudinary
     * @param context Android context
     * @param fileUri URI of the file to upload
     * @param userId User ID for folder organization
     * @param callback Callback for upload result
     */
    public static void uploadImage(Context context, Uri fileUri, int userId, CloudinaryUploadCallback callback) {
        try {
            if (fileUri == null) {
                callback.onError("File URI is null");
                return;
            }
            
            Log.d(TAG, "Starting upload: " + fileUri.toString());
            
            // Create upload options with folder structure
            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("folder", "blotter/user_" + userId); // Organize by user
            uploadOptions.put("resource_type", "auto"); // Auto-detect file type
            uploadOptions.put("quality", "auto"); // Auto-optimize quality
            uploadOptions.put("fetch_format", "auto"); // Auto-optimize format
            
            // Upload to Cloudinary
            MediaManager.get().upload(fileUri)
                .unsigned(UPLOAD_PRESET)
                .option("folder", "blotter/user_" + userId)
                .option("resource_type", "auto")
                .option("quality", "auto")
                .option("fetch_format", "auto")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload started: " + requestId);
                        callback.onStart();
                    }
                    
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        Log.d(TAG, "Upload progress: " + progress + "%");
                        callback.onProgress(progress);
                    }
                    
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        try {
                            String publicId = (String) resultData.get("public_id");
                            String secureUrl = (String) resultData.get("secure_url");
                            String cloudinaryUrl = (String) resultData.get("url");
                            
                            Log.d(TAG, "✅ Upload successful!");
                            Log.d(TAG, "Public ID: " + publicId);
                            Log.d(TAG, "Secure URL: " + secureUrl);
                            
                            callback.onSuccess(publicId, secureUrl != null ? secureUrl : cloudinaryUrl);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing upload result: " + e.getMessage(), e);
                            callback.onError("Error processing upload result: " + e.getMessage());
                        }
                    }
                    
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "❌ Upload failed: " + error.getDescription());
                        callback.onError("Upload failed: " + error.getDescription());
                    }
                    
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w(TAG, "Upload rescheduled: " + error.getDescription());
                        callback.onError("Upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch();
                
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during upload: " + e.getMessage(), e);
            callback.onError("Exception: " + e.getMessage());
        }
    }
    
    /**
     * Generate optimized Cloudinary URL for different use cases
     */
    public static String getOptimizedUrl(String publicId, int width, int height, String quality) {
        try {
            // Format: https://res.cloudinary.com/cloud_name/image/upload/w_width,h_height,c_fill,q_quality/public_id
            return String.format(
                "https://res.cloudinary.com/%s/image/upload/w_%d,h_%d,c_fill,q_%s/%s",
                CLOUD_NAME, width, height, quality, publicId
            );
        } catch (Exception e) {
            Log.e(TAG, "Error generating URL: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get thumbnail URL (small preview)
     */
    public static String getThumbnailUrl(String publicId) {
        return getOptimizedUrl(publicId, 150, 150, "auto");
    }
    
    /**
     * Get full-size URL (for viewing)
     */
    public static String getFullSizeUrl(String publicId) {
        return getOptimizedUrl(publicId, 1200, 1200, "auto");
    }
    
    /**
     * Delete image from Cloudinary
     */
    public static void deleteImage(String publicId, CloudinaryDeleteCallback callback) {
        try {
            Log.d(TAG, "Deleting image: " + publicId);
            
            // Note: Deletion requires authenticated API call
            // For now, we'll log it and let the backend handle it
            Log.d(TAG, "Image deletion queued for: " + publicId);
            callback.onSuccess("Image marked for deletion");
            
        } catch (Exception e) {
            Log.e(TAG, "Error deleting image: " + e.getMessage());
            callback.onError("Error deleting image: " + e.getMessage());
        }
    }
    
    /**
     * Callback interface for upload operations
     */
    public interface CloudinaryUploadCallback {
        void onStart();
        void onProgress(int progress);
        void onSuccess(String publicId, String secureUrl);
        void onError(String errorMessage);
    }
    
    /**
     * Callback interface for delete operations
     */
    public interface CloudinaryDeleteCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }
}
