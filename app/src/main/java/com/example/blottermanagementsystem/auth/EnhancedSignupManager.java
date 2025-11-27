package com.example.blottermanagementsystem.auth;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.example.blottermanagementsystem.data.entity.User;
import com.example.blottermanagementsystem.firebase.CloudinaryMultiDeviceManager;
import com.example.blottermanagementsystem.utils.ApiClient;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ ENHANCED SIGNUP MANAGER
 * Handles user registration with Cloudinary profile picture upload
 */
public class EnhancedSignupManager {
    private static final String TAG = "EnhancedSignupManager";
    private CloudinaryMultiDeviceManager cloudinaryManager;
    private Context context;
    
    public EnhancedSignupManager(Context context) {
        this.context = context;
        this.cloudinaryManager = new CloudinaryMultiDeviceManager(context);
    }
    
    /**
     * Sign up with profile picture
     */
    public void signupWithProfilePicture(Map<String, Object> userData, Uri profileImageUri, 
                                        SignupCallback callback) {
        Log.d(TAG, "📝 Starting signup with profile picture...");
        
        if (profileImageUri != null) {
            // Upload profile picture first
            cloudinaryManager.uploadImage(profileImageUri, "avatar", 
                new CloudinaryMultiDeviceManager.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        Log.d(TAG, "✅ Profile picture uploaded: " + imageUrl);
                        userData.put("profile_picture_url", imageUrl);
                        
                        // Complete registration
                        completeUserRegistration(userData, imageUrl, callback);
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Profile picture upload failed: " + error);
                        callback.onError("Profile picture upload failed: " + error);
                    }
                });
        } else {
            // No profile picture, proceed with signup
            Log.d(TAG, "⚠️ No profile picture provided, proceeding without");
            completeUserRegistration(userData, null, callback);
        }
    }
    
    /**
     * Complete user registration
     */
    private void completeUserRegistration(Map<String, Object> userData, String imageUrl, 
                                         SignupCallback callback) {
        Log.d(TAG, "🔄 Completing user registration...");
        
        ApiClient.signupUser(userData, new ApiClient.ApiCallback<User>() {
            @Override
            public void onSuccess(User createdUser) {
                Log.d(TAG, "✅ User created: " + createdUser.getEmail());
                
                // If image was uploaded, sync it to Neon
                if (imageUrl != null) {
                    Map<String, Object> imageData = new HashMap<>();
                    imageData.put("user_id", createdUser.getId());
                    imageData.put("image_url", imageUrl);
                    imageData.put("context", "avatar");
                    imageData.put("uploaded_at", System.currentTimeMillis());
                    
                    cloudinaryManager.syncImageToNeon(imageData, 
                        new CloudinaryMultiDeviceManager.SyncCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "✅ Profile picture synced to Neon");
                                callback.onSuccess(createdUser);
                            }
                            
                            @Override
                            public void onError(String error) {
                                Log.w(TAG, "⚠️ Image sync failed but user created: " + error);
                                // User created successfully, sync can retry later
                                callback.onSuccess(createdUser);
                            }
                        });
                } else {
                    callback.onSuccess(createdUser);
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Registration failed: " + error);
                callback.onError("Registration failed: " + error);
            }
        });
    }
    
    /**
     * Sign up without profile picture
     */
    public void signup(Map<String, Object> userData, SignupCallback callback) {
        signupWithProfilePicture(userData, null, callback);
    }
    
    /**
     * Callback interface
     */
    public interface SignupCallback {
        void onSuccess(User user);
        void onError(String error);
    }
}
