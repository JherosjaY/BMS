package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import java.io.File;

public class MediaUploadHelper {
    private static final String TAG = "MediaUploadHelper";
    
    public static void uploadMedia(Context context, Uri mediaUri, String mediaType, UploadCallback callback) {
        try {
            String optimizedPath = null;
            
            if ("image".equals(mediaType)) {
                optimizedPath = ImageOptimizer.optimizeImage(context, mediaUri, 
                    "upload_" + System.currentTimeMillis() + ".jpg");
            }
            
            if (optimizedPath != null) {
                CloudinaryUploader.getInstance().uploadImage(optimizedPath, new CloudinaryUploader.UploadCallback() {
                    @Override
                    public void onSuccess(String url) {
                        callback.onSuccess(url);
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
            } else {
                callback.onError("Failed to optimize media");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error uploading media", e);
            callback.onError(e.getMessage());
        }
    }
    
    public interface UploadCallback {
        void onSuccess(String url);
        void onError(String error);
    }
}
