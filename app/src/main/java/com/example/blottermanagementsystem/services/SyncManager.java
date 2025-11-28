package com.example.blottermanagementsystem.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private Context context;
    private ScheduledExecutorService syncScheduler;
    private List<Map<String, Object>> syncQueue;
    private static final int SYNC_INTERVAL_MINUTES = 5;

    public SyncManager(Context context) {
        this.context = context;
        this.syncQueue = new ArrayList<>();
        this.syncScheduler = Executors.newScheduledThreadPool(1);
        startBackgroundSync();
    }

    public boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        } catch (Exception e) {
            Log.e(TAG, "Error checking online status", e);
            return false;
        }
    }

    public void queueForSync(String dataType, Object data) {
        Map<String, Object> syncItem = new HashMap<>();
        syncItem.put("dataType", dataType);
        syncItem.put("data", data);
        syncItem.put("timestamp", System.currentTimeMillis());
        syncItem.put("synced", false);
        
        syncQueue.add(syncItem);
        Log.d(TAG, "📦 Queued " + dataType + " for sync. Queue size: " + syncQueue.size());
    }

    private void startBackgroundSync() {
        syncScheduler.scheduleAtFixedRate(() -> {
            if (isOnline() && !syncQueue.isEmpty()) {
                Log.d(TAG, "🔄 Starting background sync...");
                syncQueuedItems();
            }
        }, 0, SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    private void syncQueuedItems() {
        List<Map<String, Object>> itemsToRemove = new ArrayList<>();
        
        for (Map<String, Object> item : syncQueue) {
            try {
                String dataType = (String) item.get("dataType");
                Object data = item.get("data");
                
                Log.d(TAG, "📤 Syncing: " + dataType);
                
                // Simulate sync - in real implementation, call API
                item.put("synced", true);
                itemsToRemove.add(item);
                
                Log.d(TAG, "✅ Synced: " + dataType);
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Sync error", e);
            }
        }
        
        syncQueue.removeAll(itemsToRemove);
        Log.d(TAG, "🔄 Background sync completed. Remaining queue: " + syncQueue.size());
    }

    public void forceSyncNow() {
        if (isOnline()) {
            Log.d(TAG, "⚡ Force syncing now...");
            syncQueuedItems();
        } else {
            Log.w(TAG, "⚠️ Cannot force sync - device is offline");
        }
    }

    public int getPendingSyncCount() {
        return syncQueue.size();
    }

    public void shutdown() {
        if (syncScheduler != null && !syncScheduler.isShutdown()) {
            syncScheduler.shutdown();
            Log.d(TAG, "🛑 SyncManager shutdown");
        }
    }
}
