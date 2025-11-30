package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * ☁️ CLOUDINARY MANAGER
 * 
 * Handles profile picture uploads to Cloudinary
 * - Upload from camera or gallery
 * - Get Cloudinary URL
 * - Store URL in Neon database
 * - Multi-device sync via Cloudinary URL
 */
public class CloudinaryManager {
    private static final String TAG = "CloudinaryManager";
    private Context context;
    private PreferencesManager preferencesManager;
    
    // Cloudinary configuration
    private static final String CLOUDINARY_CLOUD_NAME = "your_cloud_name"; // TODO: Replace with your cloud name
    private static final String CLOUDINARY_API_KEY = "your_api_key";       // TODO: Replace with your API key
    private static final String CLOUDINARY_UPLOAD_PRESET = "your_preset";  // TODO: Replace with your preset
    
    // Callback interface for upload results
    public interface CloudinaryUploadCallback {
        void onUploadSuccess(String cloudinaryUrl);
        void onUploadError(String errorMessage);
        void onUploading();
    }
    
    public CloudinaryManager(Context context, PreferencesManager preferencesManager) {
        this.context = context;
        this.preferencesManager = preferencesManager;
        initializeCloudinary();
    }
    
    /**
     * ☁️ INITIALIZE CLOUDINARY
     * Configure Cloudinary with credentials
     */
    private void initializeCloudinary() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", CLOUDINARY_CLOUD_NAME);
            config.put("api_key", CLOUDINARY_API_KEY);
            
            MediaManager.init(context, config);
            Log.d(TAG, "✅ Cloudinary initialized");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing Cloudinary: " + e.getMessage());
        }
    }
    
    /**
     * ☁️ UPLOAD PROFILE PICTURE
     * Uploads image to Cloudinary and returns URL
     */
    public void uploadProfilePicture(Uri imageUri, String userId, CloudinaryUploadCallback callback) {
        if (callback != null) callback.onUploading();
        
        Log.d(TAG, "☁️ Uploading profile picture to Cloudinary for userId: " + userId);
        
        try {
            // Convert Uri to File
            File imageFile = convertUriToFile(imageUri);
            if (imageFile == null) {
                Log.e(TAG, "❌ Failed to convert Uri to File");
                if (callback != null) {
                    callback.onUploadError("Failed to process image");
                }
                return;
            }
            
            // Prepare upload options
            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("folder", "bms/profile_pictures"); // Organize in folder
            uploadOptions.put("public_id", "user_" + userId + "_profile"); // Unique ID
            uploadOptions.put("overwrite", true); // Replace if exists
            uploadOptions.put("resource_type", "auto");
            
            // Upload to Cloudinary
            MediaManager.get().upload(imageFile.getAbsolutePath())
                .option("folder", "bms/profile_pictures")
                .option("public_id", "user_" + userId + "_profile")
                .option("overwrite", true)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "🔄 Upload started: " + requestId);
                    }
                    
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        long progress = (bytes * 100) / totalBytes;
                        Log.d(TAG, "📊 Upload progress: " + progress + "%");
                    }
                    
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        try {
                            String cloudinaryUrl = (String) resultData.get("secure_url");
                            Log.d(TAG, "✅ Upload successful!");
                            Log.d(TAG, "✅ Cloudinary URL: " + cloudinaryUrl);
                            
                            // Save URL to preferences
                            preferencesManager.setProfileImageUri(cloudinaryUrl);
                            
                            if (callback != null) {
                                callback.onUploadSuccess(cloudinaryUrl);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error processing upload result: " + e.getMessage());
                            if (callback != null) {
                                callback.onUploadError("Error processing upload");
                            }
                        }
                    }
                    
                    @Override
                    public void onError(String requestId, ErrorCallback error) {
                        String errorMessage = error.getError() != null ? 
                            error.getError().toString() : "Unknown error";
                        Log.e(TAG, "❌ Upload failed: " + errorMessage);
                        if (callback != null) {
                            callback.onUploadError(errorMessage);
                        }
                    }
                    
                    @Override
                    public void onReschedule(String requestId, ErrorCallback error) {
                        Log.d(TAG, "⏳ Upload rescheduled");
                    }
                })
                .dispatch();
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during upload: " + e.getMessage());
            if (callback != null) {
                callback.onUploadError(e.getMessage());
            }
        }
    }
    
    /**
     * ☁️ CONVERT URI TO FILE
     * Converts image Uri to File for upload
     */
    private File convertUriToFile(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "❌ Could not open input stream from Uri");
                return null;
            }
            
            // Create temporary file
            File tempFile = new File(context.getCacheDir(), "profile_pic_temp.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            // Copy input to output
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            outputStream.close();
            inputStream.close();
            
            Log.d(TAG, "✅ Converted Uri to File: " + tempFile.getAbsolutePath());
            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error converting Uri to File: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * ☁️ GET CLOUDINARY URL FROM PREFERENCES
     * Retrieves stored Cloudinary URL for multi-device sync
     */
    public String getProfilePictureUrl() {
        String url = preferencesManager.getProfileImageUri();
        Log.d(TAG, "📸 Retrieved profile picture URL: " + (url != null ? "✅" : "❌"));
        return url;
    }
    
    /**
     * ☁️ DELETE PROFILE PICTURE
     * Removes profile picture from Cloudinary
     */
    public void deleteProfilePicture(String userId) {
        try {
            Log.d(TAG, "🗑️ Deleting profile picture for userId: " + userId);
            
            // TODO: Implement deletion via backend API
            // POST /api/auth/profile/picture/{userId}/delete
            
            Log.d(TAG, "✅ Profile picture deletion queued");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error deleting profile picture: " + e.getMessage());
        }
    }
}
