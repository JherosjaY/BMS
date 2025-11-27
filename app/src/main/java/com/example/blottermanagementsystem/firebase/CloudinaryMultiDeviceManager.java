package com.example.blottermanagementsystem.firebase;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import com.cloudinary.Cloudinary;
import com.example.blottermanagementsystem.config.CloudinaryConfig;
import com.example.blottermanagementsystem.utils.ApiClient;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ CLOUDINARY MULTI-DEVICE MANAGER - COMPLETE IMPLEMENTATION
 * Real Cloudinary SDK integration with actual credentials
 */
public class CloudinaryMultiDeviceManager {
    private static final String TAG = "CloudinaryMultiDeviceManager";
    private Cloudinary cloudinary;
    private Context context;
    
    public CloudinaryMultiDeviceManager(Context context) {
        this.context = context;
        this.cloudinary = CloudinaryConfig.getCloudinary();
    }
    
    /**
     * Upload image from URI to Cloudinary
     */
    public void uploadImage(Uri imageUri, String contextType, UploadCallback callback) {
        new Thread(() -> {
            try {
                String filePath = getRealPathFromURI(imageUri);
                if (filePath == null) {
                    callback.onError("Could not get image path");
                    return;
                }
                
                File imageFile = new File(filePath);
                if (!imageFile.exists()) {
                    callback.onError("Image file not found");
                    return;
                }
                
                Log.d(TAG, "📤 Uploading image: " + imageFile.getName());
                
                // Select upload options based on context
                Map<String, Object> uploadOptions;
                switch (contextType) {
                    case "avatar":
                        uploadOptions = CloudinaryConfig.getAvatarUploadOptions();
                        break;
                    case "evidence":
                        uploadOptions = CloudinaryConfig.getEvidenceUploadOptions();
                        break;
                    case "attachment":
                        uploadOptions = CloudinaryConfig.getReportAttachmentOptions();
                        break;
                    default:
                        uploadOptions = CloudinaryConfig.getUploadOptions(contextType);
                }
                
                // Upload to Cloudinary
                Map<?, ?> uploadResult = cloudinary.uploader().upload(imageFile, uploadOptions);
                String imageUrl = (String) uploadResult.get("secure_url");
                
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Log.d(TAG, "✅ Cloudinary upload successful: " + imageUrl);
                    callback.onSuccess(imageUrl);
                } else {
                    callback.onError("Upload failed - no URL returned");
                }
                
            } catch (IOException e) {
                Log.e(TAG, "❌ Upload failed: " + e.getMessage(), e);
                callback.onError("Upload failed: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "❌ Error: " + e.getMessage(), e);
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Upload image from byte array
     */
    public void uploadImageFromBytes(byte[] imageData, String contextType, UploadCallback callback) {
        new Thread(() -> {
            try {
                // Create temporary file from bytes
                File tempFile = File.createTempFile("image_", ".jpg", context.getCacheDir());
                java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
                fos.write(imageData);
                fos.close();
                
                Map<String, Object> uploadOptions;
                switch (contextType) {
                    case "avatar":
                        uploadOptions = CloudinaryConfig.getAvatarUploadOptions();
                        break;
                    case "evidence":
                        uploadOptions = CloudinaryConfig.getEvidenceUploadOptions();
                        break;
                    default:
                        uploadOptions = CloudinaryConfig.getUploadOptions(contextType);
                }
                
                Map<?, ?> uploadResult = cloudinary.uploader().upload(tempFile, uploadOptions);
                String imageUrl = (String) uploadResult.get("secure_url");
                
                tempFile.delete(); // Clean up temp file
                
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Log.d(TAG, "✅ Image uploaded: " + imageUrl);
                    callback.onSuccess(imageUrl);
                } else {
                    callback.onError("Upload failed");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Error: " + e.getMessage(), e);
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Sync image URL to Neon database
     */
    public void syncImageToNeon(Map<String, Object> imageData, SyncCallback callback) {
        Log.d(TAG, "🔄 Syncing image to Neon database...");
        
        ApiClient.syncImageToNeon(imageData, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ Image synced to Neon: " + imageData.get("image_url"));
                callback.onSuccess();
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Neon sync failed: " + error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Upload and sync in one operation
     */
    public void uploadAndSyncImage(Uri imageUri, int userId, String context, UploadCallback callback) {
        uploadImage(imageUri, context, new UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d(TAG, "📤 Upload successful, syncing to Neon...");
                
                Map<String, Object> imageData = new HashMap<>();
                imageData.put("user_id", userId);
                imageData.put("image_url", imageUrl);
                imageData.put("context", context);
                imageData.put("uploaded_at", System.currentTimeMillis());
                
                syncImageToNeon(imageData, new SyncCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Image uploaded and synced");
                        callback.onSuccess(imageUrl);
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.w(TAG, "⚠️ Sync failed but image uploaded: " + error);
                        callback.onSuccess(imageUrl); // Still return URL even if sync fails
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Upload failed: " + error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Get user avatar from Neon
     */
    public void getUserAvatar(String userId, ImageLoadCallback callback) {
        Log.d(TAG, "👤 Loading avatar for user: " + userId);
        
        ApiClient.getUserImage(userId, "avatar", new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Log.d(TAG, "✅ Avatar loaded: " + imageUrl);
                    callback.onImageLoaded(imageUrl);
                } else {
                    Log.w(TAG, "⚠️ No avatar found");
                    callback.onImageNotFound();
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to load avatar: " + error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Get case evidence from Neon
     */
    public void getCaseEvidence(String userId, String caseNumber, ImageLoadCallback callback) {
        Log.d(TAG, "📸 Loading evidence for case: " + caseNumber);
        
        ApiClient.getUserImage(userId, "evidence_" + caseNumber, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Log.d(TAG, "✅ Evidence loaded: " + imageUrl);
                    callback.onImageLoaded(imageUrl);
                } else {
                    Log.w(TAG, "⚠️ No evidence found");
                    callback.onImageNotFound();
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to load evidence: " + error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Get real file path from URI
     */
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        
        if (cursor != null) {
            try {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String result = cursor.getString(column_index);
                cursor.close();
                return result;
            } catch (Exception e) {
                Log.e(TAG, "Error getting path: " + e.getMessage());
                return contentUri.getPath();
            }
        }
        return contentUri.getPath();
    }
    
    // ✅ CALLBACKS
    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }
    
    public interface SyncCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface ImageLoadCallback {
        void onImageLoaded(String imageUrl);
        void onImageNotFound();
        void onError(String error);
    }
}
