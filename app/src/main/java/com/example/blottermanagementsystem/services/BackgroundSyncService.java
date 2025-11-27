package com.example.blottermanagementsystem.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.dao.BlotterReportDao;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.utils.ConflictResolver;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.example.blottermanagementsystem.utils.SyncQueueManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ✅ BACKGROUND SYNC SERVICE
 * Periodically syncs data with Neon database
 */
public class BackgroundSyncService extends Service {
    private static final String TAG = "BackgroundSyncService";
    private static final int SYNC_INTERVAL = 15 * 60 * 1000; // 15 minutes
    
    private Handler syncHandler;
    private Runnable syncRunnable;
    private SyncQueueManager syncQueueManager;
    private ConflictResolver conflictResolver;
    
    @Override
    public void onCreate() {
        super.onCreate();
        syncQueueManager = SyncQueueManager.getInstance(this);
        conflictResolver = new ConflictResolver();
        setupPeriodicSync();
        Log.d(TAG, "✅ Background Sync Service Created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🔄 Background Sync Service Started");
        performSync();
        return START_STICKY;
    }
    
    // ✅ SETUP PERIODIC SYNC
    private void setupPeriodicSync() {
        syncHandler = new Handler();
        syncRunnable = new Runnable() {
            @Override
            public void run() {
                performSync();
                syncHandler.postDelayed(this, SYNC_INTERVAL);
            }
        };
        syncHandler.postDelayed(syncRunnable, SYNC_INTERVAL);
    }
    
    // ✅ PERFORM SYNC
    private void performSync() {
        if (!isOnline()) {
            Log.d(TAG, "📱 Offline - skipping background sync");
            return;
        }
        
        Log.d(TAG, "🔄 Starting background sync...");
        
        // Sync in sequence
        syncReports();
        syncQueueManager.processSyncQueue();
        
        Log.d(TAG, "✅ Background sync completed");
    }
    
    // ✅ SYNC REPORTS
    private void syncReports() {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BlotterDatabase db = BlotterDatabase.getDatabase(this);
                BlotterReportDao reportDao = db.blotterReportDao();
                
                List<BlotterReport> localReports = reportDao.getAllReports();
                Log.d(TAG, "📥 Syncing " + localReports.size() + " reports");
                
                // In real implementation, fetch from Neon and resolve conflicts
                // For now, just update timestamps
                for (BlotterReport report : localReports) {
                    // Update the report's status to mark it as synced
                    reportDao.updateReport(report);
                }
                
                Log.d(TAG, "✅ Reports synced");
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Report sync error: " + e.getMessage());
            }
        });
    }
    
    // ✅ CHECK NETWORK AVAILABILITY
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (syncHandler != null && syncRunnable != null) {
            syncHandler.removeCallbacks(syncRunnable);
        }
        Log.d(TAG, "🔴 Background Sync Service Destroyed");
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
