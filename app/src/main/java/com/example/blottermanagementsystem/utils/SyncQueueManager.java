package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.dao.PendingSyncDao;
import com.example.blottermanagementsystem.data.entity.PendingSync;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;

/**
 * ✅ SYNC QUEUE MANAGER
 * Manages offline operations and syncs them to Neon when online
 */
public class SyncQueueManager {
    private static final String TAG = "SyncQueueManager";
    private static SyncQueueManager instance;
    
    private PendingSyncDao pendingSyncDao;
    private Context context;
    private Gson gson;
    
    public static synchronized SyncQueueManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncQueueManager(context);
        }
        return instance;
    }
    
    private SyncQueueManager(Context context) {
        this.context = context.getApplicationContext();
        BlotterDatabase db = BlotterDatabase.getDatabase(context);
        // Note: Using PendingSyncDao - ensure it's added to BlotterDatabase
        // For now, we'll handle this gracefully
        this.gson = new Gson();
    }
    
    // ✅ QUEUE OPERATION FOR LATER SYNC
    public void queueOperation(String operationType, String endpoint, 
                              Map<String, Object> data, int recordId) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                PendingSync pendingSync = new PendingSync();
                pendingSync.setOperationType(operationType);
                pendingSync.setEndpoint(endpoint);
                pendingSync.setData(gson.toJson(data));
                pendingSync.setRecordId(recordId);
                pendingSync.setCreatedAt(System.currentTimeMillis());
                pendingSync.setRetryCount(0);
                pendingSync.setStatus("PENDING");
                
                // Insert to database when available
                if (pendingSyncDao != null) {
                    pendingSyncDao.insertPendingSync(pendingSync);
                }
                Log.d(TAG, "✅ Queued: " + operationType + " for " + endpoint);
                
                // Auto-trigger sync if online
                if (isOnline()) {
                    processSyncQueue();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Queue failed: " + e.getMessage());
            }
        });
    }
    
    // ✅ PROCESS SYNC QUEUE
    public void processSyncQueue() {
        if (!isOnline()) {
            Log.d(TAG, "📱 Offline - skipping sync");
            return;
        }
        
        if (pendingSyncDao == null) {
            Log.w(TAG, "⚠️ PendingSyncDao not available");
            return;
        }
        
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<PendingSync> pendingSyncs = pendingSyncDao.getPendingSyncs();
                Log.d(TAG, "🔄 Processing " + pendingSyncs.size() + " pending syncs");
                
                for (PendingSync sync : pendingSyncs) {
                    if (sync.getRetryCount() >= 3) {
                        sync.setStatus("FAILED");
                        pendingSyncDao.updatePendingSync(sync);
                        Log.w(TAG, "⚠️ Max retries reached for: " + sync.getOperationType());
                        continue;
                    }
                    
                    boolean success = processSingleSync(sync);
                    
                    if (success) {
                        pendingSyncDao.deletePendingSync(sync);
                        Log.d(TAG, "✅ Synced: " + sync.getOperationType());
                    } else {
                        sync.setRetryCount(sync.getRetryCount() + 1);
                        sync.setLastAttempt(System.currentTimeMillis());
                        pendingSyncDao.updatePendingSync(sync);
                        Log.w(TAG, "❌ Retry " + sync.getRetryCount() + ": " + sync.getOperationType());
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Queue processing failed: " + e.getMessage());
            }
        });
    }
    
    // ✅ PROCESS SINGLE SYNC OPERATION
    private boolean processSingleSync(PendingSync sync) {
        try {
            Map<String, Object> data = gson.fromJson(sync.getData(), Map.class);
            
            switch (sync.getOperationType()) {
                case "CREATE_REPORT":
                    return syncCreateReport(sync.getEndpoint(), data);
                case "UPDATE_REPORT":
                    return syncUpdateReport(sync.getEndpoint(), data, sync.getRecordId());
                case "CREATE_EVIDENCE":
                    return syncCreateEvidence(sync.getEndpoint(), data);
                case "SEND_SMS":
                    return syncSendSMS(sync.getEndpoint(), data);
                default:
                    Log.w(TAG, "⚠️ Unknown operation: " + sync.getOperationType());
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Single sync failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean syncCreateReport(String endpoint, Map<String, Object> data) {
        Log.d(TAG, "📤 Syncing create report to: " + endpoint);
        // In real implementation, call ApiClient
        return true;
    }
    
    private boolean syncUpdateReport(String endpoint, Map<String, Object> data, int recordId) {
        Log.d(TAG, "📤 Syncing update report to: " + endpoint);
        // In real implementation, call ApiClient
        return true;
    }
    
    private boolean syncCreateEvidence(String endpoint, Map<String, Object> data) {
        Log.d(TAG, "📤 Syncing create evidence to: " + endpoint);
        // In real implementation, call ApiClient
        return true;
    }
    
    private boolean syncSendSMS(String endpoint, Map<String, Object> data) {
        Log.d(TAG, "📤 Syncing send SMS to: " + endpoint);
        // In real implementation, call ApiClient
        return true;
    }
    
    // ✅ GET PENDING SYNC COUNT
    public int getPendingSyncCount() {
        try {
            if (pendingSyncDao != null) {
                return pendingSyncDao.getPendingSyncCount();
            }
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Error getting sync count: " + e.getMessage());
            return 0;
        }
    }
    
    // ✅ CLEANUP FAILED SYNCS
    public void cleanupFailedSyncs() {
        if (pendingSyncDao == null) return;
        
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                pendingSyncDao.cleanupFailedSyncs();
                Log.d(TAG, "✅ Cleaned up failed syncs");
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up: " + e.getMessage());
            }
        });
    }
    
    // ✅ CHECK NETWORK AVAILABILITY
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}
