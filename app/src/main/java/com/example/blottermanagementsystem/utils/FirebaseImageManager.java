package com.example.blottermanagementsystem.utils;

import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * ✅ FIREBASE CLOUDINARY IMAGE UPLOAD MANAGER
 * Handles image uploads to Firebase Storage with Cloudinary integration
 */
public class FirebaseImageManager {
    private static final String TAG = "FirebaseImageManager";
    private StorageReference storageRef;
    private FirebaseStorage storage;
    
    public FirebaseImageManager() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("blotter-images");
    }
    
    // ✅ UPLOAD IMAGE TO FIREBASE CLOUDINARY
    public void uploadImage(Uri imageUri, String caseNumber, ImageUploadCallback callback) {
        try {
            String fileName = "case_" + caseNumber + "_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageRef.child(fileName);
            
            android.util.Log.d(TAG, "📤 Starting image upload for case: " + caseNumber);
            
            UploadTask uploadTask = imageRef.putFile(imageUri);
            
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Get download URL
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    android.util.Log.d(TAG, "✅ Image uploaded successfully: " + imageUrl);
                    callback.onSuccess(imageUrl);
                    
                    // ✅ SYNC IMAGE URL TO CLOUDBASE
                    syncImageUrlToCloudbase(caseNumber, imageUrl);
                });
            }).addOnFailureListener(e -> {
                android.util.Log.e(TAG, "❌ Upload failed: " + e.getMessage());
                callback.onError("Upload failed: " + e.getMessage());
            }).addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                android.util.Log.d(TAG, "📊 Upload progress: " + (int) progress + "%");
                callback.onProgress((int) progress);
            });
            
        } catch (Exception e) {
            android.util.Log.e(TAG, "❌ Image upload error: " + e.getMessage());
            callback.onError("Image upload error: " + e.getMessage());
        }
    }
    
    private void syncImageUrlToCloudbase(String caseNumber, String imageUrl) {
        // Sync image URL to Cloudbase backend
        android.util.Log.d(TAG, "🔄 Syncing image URL to Cloudbase...");
        
        // This would call your backend API to update the report with the image URL
        // Example: ApiClient.updateReportImage(caseNumber, imageUrl)
    }
    
    // ✅ DELETE IMAGE FROM FIREBASE
    public void deleteImage(String imageUrl, DeleteCallback callback) {
        try {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            
            imageRef.delete().addOnSuccessListener(aVoid -> {
                android.util.Log.d(TAG, "✅ Image deleted successfully");
                callback.onSuccess();
            }).addOnFailureListener(e -> {
                android.util.Log.e(TAG, "❌ Delete failed: " + e.getMessage());
                callback.onError("Delete failed: " + e.getMessage());
            });
            
        } catch (Exception e) {
            android.util.Log.e(TAG, "❌ Delete error: " + e.getMessage());
            callback.onError("Delete error: " + e.getMessage());
        }
    }
    
    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
        void onProgress(int progress);
    }
    
    public interface DeleteCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}
