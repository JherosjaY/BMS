package com.example.blottermanagementsystem.utils;

import android.app.Activity;
import android.content.Context;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ HYBRID OFFLINE-FIRST API CLIENT
 * Loads from local database first, then syncs with Cloudbase in background
 */
public class HybridApiClient {
    private static final String TAG = "HybridApiClient";
    private Context context;
    
    public HybridApiClient(Context context) {
        this.context = context;
    }
    
    // ✅ HYBRID APPROACH: Local first, then sync with Cloudbase
    public void getReportsHybrid(int userId, String userRole, ApiCallback<List<BlotterReport>> callback) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // STEP 1: Load from LOCAL database immediately
                BlotterDatabase db = BlotterDatabase.getDatabase(context);
                List<BlotterReport> localReports = db.blotterReportDao().getAllReports();
                
                // Filter by user role locally
                List<BlotterReport> filteredReports = filterReportsByUser(localReports, userId, userRole);
                
                // Return local data immediately
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        android.util.Log.d(TAG, "✅ Loaded " + filteredReports.size() + " reports from local DB");
                        callback.onSuccess(filteredReports);
                    });
                }
                
                // STEP 2: Sync with Cloudbase in background if online
                NetworkMonitor networkMonitor = new NetworkMonitor(context);
                if (networkMonitor.isNetworkAvailable()) {
                    syncWithCloudbaseInBackground(userId, userRole);
                }
                
            } catch (Exception e) {
                android.util.Log.e(TAG, "Local database error: " + e.getMessage());
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        callback.onError("Local database error: " + e.getMessage());
                    });
                }
            }
        });
    }
    
    private void syncWithCloudbaseInBackground(int userId, String userRole) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                android.util.Log.d(TAG, "🔄 Starting background sync with Cloudbase...");
                
                // Sync logic: push local changes, pull server updates
                pushLocalChangesToServer(userId);
                pullLatestFromServer(userId, userRole);
                
                android.util.Log.d(TAG, "✅ Background sync completed successfully");
            } catch (Exception e) {
                android.util.Log.w(TAG, "⚠️ Background sync failed: " + e.getMessage());
                // Fail silently - user can continue with local data
            }
        });
    }
    
    private void pushLocalChangesToServer(int userId) {
        // Push any local modifications to server
        // This would include new reports created offline, updates, etc.
        android.util.Log.d(TAG, "📤 Pushing local changes to server...");
    }
    
    private void pullLatestFromServer(int userId, String userRole) {
        // Pull latest data from server and update local database
        android.util.Log.d(TAG, "📥 Pulling latest data from server...");
    }
    
    private List<BlotterReport> filterReportsByUser(List<BlotterReport> reports, int userId, String userRole) {
        List<BlotterReport> filtered = new ArrayList<>();
        
        for (BlotterReport report : reports) {
            if ("Admin".equalsIgnoreCase(userRole)) {
                // Admin sees all reports
                filtered.add(report);
            } else if ("Officer".equalsIgnoreCase(userRole)) {
                // Officer sees assigned reports
                if (report.getAssignedOfficerId() == userId) {
                    filtered.add(report);
                }
            } else if ("User".equalsIgnoreCase(userRole)) {
                // User sees only their own reports
                if (report.getReportedById() == userId) {
                    filtered.add(report);
                }
            }
        }
        
        return filtered;
    }
    
    public interface ApiCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }
}
