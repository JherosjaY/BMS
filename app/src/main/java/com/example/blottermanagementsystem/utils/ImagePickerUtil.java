package com.example.blottermanagementsystem.utils;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

/**
 * ✅ IMAGE PICKER UTILITY
 * Handles image selection from gallery or camera
 */
public class ImagePickerUtil {
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImagePickerCallback callback;
    
    public interface ImagePickerCallback {
        void onImageSelected(Uri imageUri);
        void onError(String error);
    }
    
    /**
     * Initialize image picker with AppCompatActivity
     */
    public ImagePickerUtil(AppCompatActivity activity, ImagePickerCallback callback) {
        this.callback = callback;
        
        imagePickerLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        callback.onImageSelected(data.getData());
                    } else {
                        callback.onError("No image selected");
                    }
                } else {
                    callback.onError("Image selection cancelled");
                }
            }
        );
    }
    
    /**
     * Open gallery to pick image
     */
    public void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, 
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    
    /**
     * Open camera to take photo
     */
    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imagePickerLauncher.launch(intent);
    }
}
