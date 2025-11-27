package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.util.Log;

import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.CloudinaryImage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * CloudinaryImageManager - Manages Cloudinary image metadata in local database
 * Syncs images across devices when user logs in
 */
public class CloudinaryImageManager {
    
    private static final String TAG = "CloudinaryImageManager";
    private final BlotterDatabase database;
    private final Gson gson;
    
    public CloudinaryImageManager(Context context) {
        this.database = BlotterDatabase.getDatabase(context);
        this.gson = new Gson();
    }
    
    /**
     * Save image metadata to local database
     * This allows images to be synced across devices
     */
    public void saveImageMetadata(int userId, String publicId, String secureUrl, String fileName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                CloudinaryImage image = new CloudinaryImage();
                image.setUserId(userId);
                image.setPublicId(publicId);
                image.setSecureUrl(secureUrl);
                image.setFileName(fileName);
                image.setUploadedAt(System.currentTimeMillis());
                
                database.cloudinaryImageDao().insertImage(image);
                Log.d(TAG, "✅ Image metadata saved: " + publicId);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error saving image metadata: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Get all images for a user (synced across devices)
     */
    public void getUserImages(int userId, ImageListCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<CloudinaryImage> images = database.cloudinaryImageDao().getImagesByUserId(userId);
                Log.d(TAG, "✅ Retrieved " + images.size() + " images for user " + userId);
                callback.onSuccess(images);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error retrieving images: " + e.getMessage(), e);
                callback.onError("Error retrieving images: " + e.getMessage());
            }
        });
    }
    
    /**
     * Delete image metadata from local database
     */
    public void deleteImageMetadata(String publicId, DeleteCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                database.cloudinaryImageDao().deleteImageByPublicId(publicId);
                Log.d(TAG, "✅ Image metadata deleted: " + publicId);
                callback.onSuccess("Image deleted");
            } catch (Exception e) {
                Log.e(TAG, "❌ Error deleting image: " + e.getMessage(), e);
                callback.onError("Error deleting image: " + e.getMessage());
            }
        });
    }
    
    /**
     * Sync images from cloud (when user logs in from different device)
     */
    public void syncUserImages(int userId, SyncCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<CloudinaryImage> images = database.cloudinaryImageDao().getImagesByUserId(userId);
                Log.d(TAG, "✅ Synced " + images.size() + " images for user " + userId);
                callback.onSync(images);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error syncing images: " + e.getMessage(), e);
                callback.onError("Error syncing images: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get image URLs as JSON string (for storing in reports)
     */
    public String getImageUrlsAsJson(List<String> urls) {
        return gson.toJson(urls);
    }
    
    /**
     * Parse image URLs from JSON string
     */
    public List<String> parseImageUrlsFromJson(String json) {
        try {
            Type type = new TypeToken<List<String>>(){}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing image URLs: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Callback for image list operations
     */
    public interface ImageListCallback {
        void onSuccess(List<CloudinaryImage> images);
        void onError(String errorMessage);
    }
    
    /**
     * Callback for delete operations
     */
    public interface DeleteCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }
    
    /**
     * Callback for sync operations
     */
    public interface SyncCallback {
        void onSync(List<CloudinaryImage> images);
        void onError(String errorMessage);
    }
}
