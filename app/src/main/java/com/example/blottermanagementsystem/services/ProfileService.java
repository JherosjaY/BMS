package com.example.blottermanagementsystem.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import com.example.blottermanagementsystem.utils.ApiClient;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ProfileService - Handles profile picture operations
 * - Save to local storage
 * - Upload to Neon database
 * - Load from local or remote
 */
public class ProfileService {
    private static final String TAG = "ProfileService";
    private Context context;
    private PreferencesManager preferencesManager;

    public ProfileService(Context context) {
        this.context = context;
        this.preferencesManager = new PreferencesManager(context);
    }

    /**
     * Upload profile picture to Neon database
     */
    public void uploadProfilePicture(String imageUri, ProfileCallback callback) {
        try {
            Log.d(TAG, "📤 Uploading profile picture: " + imageUri);
            
            // Save to local storage first
            saveImageToLocalStorage(imageUri);
            
            // Then upload to Neon DB
            int userId = preferencesManager.getUserId();
            java.util.Map<String, Object> pictureData = new java.util.HashMap<>();
            pictureData.put("profilePictureUrl", imageUri);
            
            ApiClient.getApiService()
                .updateProfilePicture(userId, pictureData)
                .enqueue(new Callback<java.util.Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<java.util.Map<String, Object>> call, 
                                         Response<java.util.Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "✅ Profile picture uploaded successfully");
                            callback.onSuccess("Profile picture uploaded successfully");
                        } else {
                            Log.e(TAG, "❌ Upload failed: " + response.code());
                            callback.onError("Upload failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                        Log.e(TAG, "❌ Network error: " + t.getMessage());
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error: " + e.getMessage());
            callback.onError("Error: " + e.getMessage());
        }
    }

    /**
     * Save image to local storage
     */
    private void saveImageToLocalStorage(String imageUri) {
        try {
            Log.d(TAG, "💾 Saving image to local storage");
            
            // Get bitmap from URI
            InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(imageUri));
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            if (bitmap != null) {
                // Save to app's files directory
                File file = new File(context.getFilesDir(), "profile_picture.jpg");
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.close();
                
                Log.d(TAG, "✅ Image saved to local storage: " + file.getAbsolutePath());
            }
            
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ Error saving to local storage: " + e.getMessage());
        }
    }

    /**
     * Load profile picture from local storage
     */
    public Bitmap loadProfilePictureFromLocal() {
        try {
            Log.d(TAG, "📂 Loading profile picture from local storage");
            
            File file = new File(context.getFilesDir(), "profile_picture.jpg");
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                Log.d(TAG, "✅ Profile picture loaded from local storage");
                return bitmap;
            } else {
                Log.d(TAG, "⚠️ No profile picture in local storage");
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading from local storage: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get user gender for officer profile
     */
    public static String getUserGender(Context context) {
        PreferencesManager prefs = new PreferencesManager(context);
        String gender = prefs.getGender();
        Log.d(TAG, "👥 User gender: " + gender);
        return gender != null ? gender : "Male"; // Default to Male
    }

    /**
     * Callback interface for profile operations
     */
    public interface ProfileCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}
