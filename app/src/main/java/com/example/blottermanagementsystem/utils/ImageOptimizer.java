package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageOptimizer {
    private static final String TAG = "ImageOptimizer";
    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 1024;
    private static final int QUALITY = 85;
    
    public static String optimizeImage(Context context, Uri imageUri, String outputFileName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();
            
            if (originalBitmap == null) return null;
            
            Bitmap optimizedBitmap = resizeBitmap(originalBitmap, MAX_WIDTH, MAX_HEIGHT);
            
            File optimizedDir = new File(context.getCacheDir(), "optimized_images");
            if (!optimizedDir.exists()) {
                optimizedDir.mkdirs();
            }
            
            File outputFile = new File(optimizedDir, outputFileName);
            FileOutputStream fos = new FileOutputStream(outputFile);
            optimizedBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, fos);
            fos.close();
            
            originalBitmap.recycle();
            optimizedBitmap.recycle();
            
            Log.d(TAG, "Image optimized: " + outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error optimizing image", e);
            return null;
        }
    }
    
    private static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }
        
        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    
    public static long getImageSize(String filePath) {
        File file = new File(filePath);
        return file.exists() ? file.length() : 0;
    }
}
