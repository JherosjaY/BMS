package com.example.blottermanagementsystem.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.util.Log;

public class PerformanceOptimizer {
    private static final String TAG = "PerformanceOptimizer";
    
    public static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    public static long getMaxMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.maxMemory();
    }
    
    public static int getMemoryUsagePercent() {
        long used = getUsedMemory();
        long max = getMaxMemory();
        return (int) ((used * 100) / max);
    }
    
    public static void logMemoryUsage() {
        long usedMB = getUsedMemory() / (1024 * 1024);
        long maxMB = getMaxMemory() / (1024 * 1024);
        int percent = getMemoryUsagePercent();
        
        Log.d(TAG, String.format("Memory: %d/%d MB (%d%%)", usedMB, maxMB, percent));
    }
    
    public static void clearCache(Context context) {
        try {
            context.getCacheDir().delete();
            Log.d(TAG, "Cache cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing cache", e);
        }
    }
    
    public static boolean isLowMemory(Context context) {
        ActivityManager activityManager = 
            (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.lowMemory;
    }
    
    public static void requestGarbageCollection() {
        System.gc();
        Log.d(TAG, "Garbage collection requested");
    }
}
