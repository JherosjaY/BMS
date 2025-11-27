package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    
    public static String copyImageToInternalStorage(Context context, Uri uri, int userId) {
        try {
            File profileDir = new File(context.getFilesDir(), "profile_images");
            if (!profileDir.exists()) {
                profileDir.mkdirs();
            }
            
            String fileName = "profile_" + userId + ".jpg";
            File destFile = new File(profileDir, fileName);
            
            InputStream input = context.getContentResolver().openInputStream(uri);
            if (input != null) {
                FileOutputStream output = new FileOutputStream(destFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                input.close();
                output.close();
                
                Log.d(TAG, "Image copied to: " + destFile.getAbsolutePath());
                return destFile.getAbsolutePath();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error copying image", e);
        }
        return null;
    }
    
    public static boolean deleteProfileImage(Context context, int userId) {
        try {
            File profileDir = new File(context.getFilesDir(), "profile_images");
            String fileName = "profile_" + userId + ".jpg";
            File file = new File(profileDir, fileName);
            
            if (file.exists()) {
                boolean deleted = file.delete();
                Log.d(TAG, "Profile image deleted: " + deleted);
                return deleted;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting image", e);
        }
        return false;
    }
    
    public static String getProfileImagePath(Context context, int userId) {
        File profileDir = new File(context.getFilesDir(), "profile_images");
        String fileName = "profile_" + userId + ".jpg";
        File file = new File(profileDir, fileName);
        
        return file.exists() ? file.getAbsolutePath() : null;
    }
}
