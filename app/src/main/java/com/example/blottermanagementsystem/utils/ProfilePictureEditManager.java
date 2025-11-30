package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.blottermanagementsystem.services.EmailAuthService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 📸 PROFILE PICTURE EDIT MANAGER
 * 
 * Handles profile picture updates in UserProfileActivity
 * - Upload new picture to Cloudinary
 * - Update URL in Neon database
 * - Multi-device sync support
 * - Real-time display update
 */
public class ProfilePictureEditManager {
    private static final String TAG = "ProfilePictureEditMgr";
    private Context context;
    private PreferencesManager preferencesManager;
    private com.example.blottermanagementsystem.utils.CloudinaryManager cloudinaryManager;
    
    // Callback interface for edit results
    public interface EditCallback {
        void onUpdateSuccess(String newCloudinaryUrl);
        void onUpdateError(String errorMessage);
        void onUploading();
    }
    
    public ProfilePictureEditManager(Context context, PreferencesManager preferencesManager) {
        this.context = context;
        this.preferencesManager = preferencesManager;
        this.cloudinaryManager = new com.example.blottermanagementsystem.utils.CloudinaryManager(context, preferencesManager);
    }
    
    /**
     * 📸 UPDATE PROFILE PICTURE
     * Upload new picture and update in Neon
     * Role-based: Only USER role can edit
     */
    public void updateProfilePicture(Uri imageUri, String userId, EditCallback callback) {
        if (callback != null) callback.onUploading();
        
        Log.d(TAG, "📸 Updating profile picture for userId: " + userId);
        
        // 🎯 ROLE-BASED CHECK: Only USER can edit profile picture
        String userRole = preferencesManager.getUserRole();
        if (userRole != null && !userRole.toLowerCase().equals("user")) {
            String errorMessage = "❌ Only USER role can edit profile picture. Your role: " + userRole;
            Log.e(TAG, errorMessage);
            if (callback != null) {
                callback.onUpdateError(errorMessage);
            }
            return;
        }
        
        try {
            // Upload to Cloudinary
            cloudinaryManager.uploadProfilePicture(imageUri, userId, 
                new com.example.blottermanagementsystem.utils.CloudinaryManager.UploadCallback() {
                    @Override
                    public void onUploadSuccess(String cloudinaryUrl) {
                        Log.d(TAG, "✅ Cloudinary upload successful!");
                        Log.d(TAG, "✅ New URL: " + cloudinaryUrl);
                        
                        // Update URL in Neon database
                        updateProfilePictureInNeon(userId, cloudinaryUrl, callback);
                    }
                    
                    @Override
                    public void onUploadError(String errorMessage) {
                        Log.e(TAG, "❌ Cloudinary upload failed: " + errorMessage);
                        if (callback != null) {
                            callback.onUpdateError("Upload failed: " + errorMessage);
                        }
                    }
                    
                    @Override
                    public void onUploading() {
                        Log.d(TAG, "🔄 Uploading to Cloudinary...");
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during update: " + e.getMessage());
            if (callback != null) {
                callback.onUpdateError(e.getMessage());
            }
        }
    }
    
    /**
     * 📸 UPDATE PROFILE PICTURE IN NEON
     * Store new Cloudinary URL in Neon database
     */
    private void updateProfilePictureInNeon(String userId, String cloudinaryUrl, EditCallback callback) {
        try {
            Log.d(TAG, "🔄 Updating profile picture URL in Neon...");
            
            // Prepare update data
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("userId", userId);
            updateData.put("profilePictureUrl", cloudinaryUrl);
            
            // Call backend API to update profile picture
            ApiClient.getApiService().updateProfilePicture(Integer.parseInt(userId), updateData)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "✅ Profile picture updated in Neon!");
                            
                            // Update local preferences
                            preferencesManager.setProfileImageUri(cloudinaryUrl);
                            
                            if (callback != null) {
                                callback.onUpdateSuccess(cloudinaryUrl);
                            }
                        } else {
                            String errorMessage = "Failed to update in Neon: " + response.code();
                            Log.e(TAG, "❌ " + errorMessage);
                            if (callback != null) {
                                callback.onUpdateError(errorMessage);
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        String errorMessage = t.getMessage() != null ? t.getMessage() : "Network error";
                        Log.e(TAG, "❌ Network error: " + errorMessage);
                        if (callback != null) {
                            callback.onUpdateError(errorMessage);
                        }
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception updating Neon: " + e.getMessage());
            if (callback != null) {
                callback.onUpdateError(e.getMessage());
            }
        }
    }
    
    /**
     * 📸 GET CURRENT PROFILE PICTURE URL
     * Fetch latest URL from preferences
     */
    public String getCurrentProfilePictureUrl() {
        String url = preferencesManager.getProfileImageUri();
        Log.d(TAG, "📸 Current profile picture URL: " + (url != null ? "✅" : "❌"));
        return url;
    }
    
    /**
     * 🎯 GET ROLE-BASED PROFILE PICTURE
     * Returns appropriate image based on user role
     */
    public String getRoleBasedProfilePicture(String gender) {
        String userRole = preferencesManager.getUserRole();
        
        if (userRole == null) {
            userRole = "user";
        }
        
        Log.d(TAG, "🎯 Getting role-based profile picture for role: " + userRole);
        
        switch (userRole.toLowerCase()) {
            case "admin":
                // ADMIN: Hardcoded shield icon with star (⭐🛡️)
                Log.d(TAG, "🛡️ Admin role - returning hardcoded shield icon");
                return "ADMIN_SHIELD"; // Special marker for hardcoded icon
                
            case "officer":
                // OFFICER: Dynamic gender-based icon
                if (gender != null && gender.toLowerCase().equals("female")) {
                    Log.d(TAG, "👮‍♀️ Officer role (Female) - returning female officer icon");
                    return "OFFICER_FEMALE"; // Special marker for female officer
                } else {
                    Log.d(TAG, "👮‍♂️ Officer role (Male) - returning male officer icon");
                    return "OFFICER_MALE"; // Special marker for male officer
                }
                
            default:
                // USER: Can have custom profile picture
                Log.d(TAG, "📸 User role - returning custom profile picture URL");
                return getCurrentProfilePictureUrl();
        }
    }
    
    /**
     * 🎯 CAN USER EDIT PROFILE PICTURE
     * Check if current user role can edit profile picture
     */
    public boolean canEditProfilePicture() {
        String userRole = preferencesManager.getUserRole();
        boolean canEdit = userRole != null && userRole.toLowerCase().equals("user");
        Log.d(TAG, "🎯 Can edit profile picture: " + canEdit + " (Role: " + userRole + ")");
        return canEdit;
    }
}
