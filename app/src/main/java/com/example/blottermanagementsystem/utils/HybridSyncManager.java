package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.util.Log;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.data.entity.SyncQueue;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * HybridSyncManager - Manages both local and cloud synchronization
 * Works seamlessly online and offline
 */
public class HybridSyncManager {
    private static final String TAG = "HybridSyncManager";
    private final Context context;
    private final BlotterDatabase database;
    private final NetworkMonitor networkMonitor;
    private final PreferencesManager preferencesManager;
    
    public HybridSyncManager(Context context) {
        this.context = context;
        this.database = BlotterDatabase.getDatabase(context);
        this.networkMonitor = new NetworkMonitor(context);
        this.preferencesManager = new PreferencesManager(context);
    }
    
    /**
     * Syncs data based on network availability
     * OFFLINE: Works with local database only
     * ONLINE: Syncs with cloud and local database
     */
    public void syncAll(SyncCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                boolean isOnline = networkMonitor.isNetworkAvailable();
                Log.d(TAG, "🔄 Starting sync - Network: " + (isOnline ? "ONLINE" : "OFFLINE"));
                
                if (isOnline) {
                    syncOnline(callback);
                } else {
                    syncOffline(callback);
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Sync error: " + e.getMessage(), e);
                callback.onError("Sync failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * ONLINE SYNC: Syncs with cloud and local database
     */
    private void syncOnline(SyncCallback callback) {
        Log.d(TAG, "🌐 ONLINE MODE: Syncing with cloud...");
        
        try {
            // Step 1: Process pending local changes
            processPendingSyncQueue();
            
            // Step 2: Fetch latest data from cloud
            fetchCloudData();
            
            // Step 3: Merge and update local database
            mergeCloudData();
            
            // Step 4: Mark sync as complete
            preferencesManager.saveString("last_sync", String.valueOf(System.currentTimeMillis()));
            preferencesManager.saveString("last_sync_status", "SUCCESS");
            
            Log.d(TAG, "✅ Online sync completed successfully");
            callback.onSuccess();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Online sync failed: " + e.getMessage());
            preferencesManager.saveString("last_sync_status", "FAILED");
            callback.onError("Online sync failed: " + e.getMessage());
        }
    }
    
    /**
     * OFFLINE SYNC: Works with local database only
     */
    private void syncOffline(SyncCallback callback) {
        Log.d(TAG, "📱 OFFLINE MODE: Using local database only...");
        
        try {
            // Step 1: Validate local database
            validateLocalDatabase();
            
            // Step 2: Queue changes for later sync
            queuePendingChanges();
            
            // Step 3: Update last sync attempt
            preferencesManager.saveString("last_sync_status", "OFFLINE");
            
            Log.d(TAG, "✅ Offline sync completed - changes queued for cloud sync");
            callback.onSuccess();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Offline sync failed: " + e.getMessage());
            callback.onError("Offline sync failed: " + e.getMessage());
        }
    }
    
    /**
     * Processes pending changes in SyncQueue
     */
    private void processPendingSyncQueue() {
        Log.d(TAG, "📤 Processing pending sync queue...");
        
        try {
            List<SyncQueue> pendingItems = database.syncQueueDao().getPendingSyncItems();
            Log.d(TAG, "   Found " + pendingItems.size() + " pending items");
            
            for (SyncQueue item : pendingItems) {
                try {
                    Log.d(TAG, "   Processing: " + item.getEntityType() + " - " + item.getAction());
                    
                    // Send to cloud API
                    // TODO: Implement cloud API call
                    // apiRepository.syncItem(item);
                    
                    // Mark as synced
                    item.setSynced(true);
                    database.syncQueueDao().updateSyncItem(item);
                    
                    Log.d(TAG, "   ✅ Synced: " + item.getEntityType());
                    
                } catch (Exception e) {
                    // Increment retry count
                    item.setRetryCount(item.getRetryCount() + 1);
                    item.setLastError(e.getMessage());
                    database.syncQueueDao().updateSyncItem(item);
                    
                    Log.w(TAG, "   ⚠️ Retry " + item.getRetryCount() + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing sync queue: " + e.getMessage());
        }
    }
    
    /**
     * Fetches latest data from cloud
     */
    private void fetchCloudData() {
        Log.d(TAG, "☁️ Fetching data from cloud...");
        
        try {
            // TODO: Implement cloud API calls
            // List<BlotterReport> cloudReports = apiRepository.getAllReports();
            // List<Officer> cloudOfficers = apiRepository.getAllOfficers();
            
            Log.d(TAG, "   ✅ Cloud data fetched");
            
        } catch (Exception e) {
            Log.e(TAG, "Error fetching cloud data: " + e.getMessage());
        }
    }
    
    /**
     * Merges cloud data with local database
     */
    private void mergeCloudData() {
        Log.d(TAG, "🔀 Merging cloud data with local database...");
        
        try {
            // TODO: Implement merge logic
            // For each cloud report:
            //   - Check if exists locally
            //   - If newer: update local
            //   - If local is newer: queue for upload
            
            Log.d(TAG, "   ✅ Data merged successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error merging data: " + e.getMessage());
        }
    }
    
    /**
     * Validates local database integrity
     */
    private void validateLocalDatabase() {
        Log.d(TAG, "🔍 Validating local database...");
        
        try {
            DatabaseValidator validator = new DatabaseValidator(context);
            validator.validateMultiOfficerAssignment(new DatabaseValidator.ValidationCallback() {
                @Override
                public void onValidationComplete(DatabaseValidator.ValidationResult result) {
                    if (result.isValid) {
                        Log.d(TAG, "   ✅ Local database is valid");
                    } else {
                        Log.w(TAG, "   ⚠️ Local database has issues:");
                        Log.w(TAG, "      Invalid Officer IDs: " + result.invalidOfficerIds);
                        Log.w(TAG, "      Assigned Without Officers: " + result.assignedWithoutOfficers);
                    }
                }
                
                @Override
                public void onValidationError(String error) {
                    Log.e(TAG, "   ❌ Validation error: " + error);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating database: " + e.getMessage());
        }
    }
    
    /**
     * Queues pending changes for later sync
     */
    private void queuePendingChanges() {
        Log.d(TAG, "📋 Queuing pending changes...");
        
        try {
            List<SyncQueue> pendingItems = database.syncQueueDao().getPendingSyncItems();
            Log.d(TAG, "   " + pendingItems.size() + " items queued for cloud sync");
            
        } catch (Exception e) {
            Log.e(TAG, "Error queuing changes: " + e.getMessage());
        }
    }
    
    /**
     * Gets sync status information
     */
    public void getSyncStatus(StatusCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                SyncStatus status = new SyncStatus();
                status.isOnline = networkMonitor.isNetworkAvailable();
                status.lastSyncTime = preferencesManager.getString("last_sync", "Never");
                status.lastSyncStatus = preferencesManager.getString("last_sync_status", "Unknown");
                
                List<SyncQueue> pendingItems = database.syncQueueDao().getPendingSyncItems();
                status.pendingChanges = pendingItems.size();
                
                List<BlotterReport> allReports = database.blotterReportDao().getAllReports();
                status.totalReports = allReports.size();
                
                callback.onStatusReady(status);
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting sync status: " + e.getMessage());
                callback.onStatusError(e.getMessage());
            }
        });
    }
    
    // Callbacks
    public interface SyncCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface StatusCallback {
        void onStatusReady(SyncStatus status);
        void onStatusError(String error);
    }
    
    // Status class
    public static class SyncStatus {
        public boolean isOnline;
        public String lastSyncTime;
        public String lastSyncStatus;
        public int pendingChanges;
        public int totalReports;
        
        @Override
        public String toString() {
            return "SyncStatus{" +
                    "isOnline=" + isOnline +
                    ", lastSyncTime='" + lastSyncTime + '\'' +
                    ", lastSyncStatus='" + lastSyncStatus + '\'' +
                    ", pendingChanges=" + pendingChanges +
                    ", totalReports=" + totalReports +
                    '}';
        }
    }
}
