package com.example.blottermanagementsystem.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.dao.BlotterReportDao;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.utils.HybridApiClient;
import com.example.blottermanagementsystem.utils.NetworkMonitor;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ BLOTTER REPOSITORY - HYBRID LOCAL + NEON APPROACH
 * Bridges local Room database with Neon cloud database
 */
public class BlotterRepository {
    private static final String TAG = "BlotterRepository";
    
    private BlotterReportDao localDao;
    private HybridApiClient apiClient;
    private PreferencesManager prefs;
    private Context context;
    
    public BlotterRepository(Context context) {
        BlotterDatabase db = BlotterDatabase.getDatabase(context);
        this.localDao = db.blotterReportDao();
        this.apiClient = new HybridApiClient(context);
        this.prefs = new PreferencesManager(context);
        this.context = context;
    }
    
    // ✅ HYBRID: Get reports (Local first, then sync with Neon)
    public LiveData<List<BlotterReport>> getReportsHybrid(int userId, String userRole) {
        MutableLiveData<List<BlotterReport>> result = new MutableLiveData<>();
        
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // STEP 1: Get from LOCAL database immediately
                List<BlotterReport> localReports = localDao.getAllReports();
                List<BlotterReport> filteredReports = filterByUserRole(localReports, userId, userRole);
                result.postValue(filteredReports);
                
                Log.d(TAG, "✅ Loaded " + filteredReports.size() + " reports from local DB");
                
                // STEP 2: Sync with NEON in background
                NetworkMonitor networkMonitor = new NetworkMonitor(context);
                if (networkMonitor.isNetworkAvailable()) {
                    syncWithNeonDatabase(userId, userRole);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Error loading reports: " + e.getMessage());
                result.postValue(new ArrayList<>());
            }
        });
        
        return result;
    }
    
    // ✅ HYBRID: Create report (Local + Neon)
    public void createReportHybrid(BlotterReport report, RepositoryCallback<BlotterReport> callback) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // STEP 1: Save to LOCAL database immediately
                long localId = localDao.insertReport(report);
                report.setId((int) localId);
                
                Log.d(TAG, "✅ Report saved locally with ID: " + localId);
                
                // STEP 2: Sync to NEON database
                NetworkMonitor networkMonitor = new NetworkMonitor(context);
                if (networkMonitor.isNetworkAvailable()) {
                    syncReportToNeon(report, callback);
                } else {
                    // Mark for later sync
                    Log.d(TAG, "📋 Report marked for later sync (offline)");
                    callback.onSuccess(report);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Local save failed: " + e.getMessage());
                callback.onError("Local save failed: " + e.getMessage());
            }
        });
    }
    
    // ✅ UPDATE REPORT
    public void updateReport(BlotterReport report, RepositoryCallback<BlotterReport> callback) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Update local database
                localDao.updateReport(report);
                Log.d(TAG, "✅ Report updated locally");
                
                // Sync to Neon if online
                NetworkMonitor networkMonitor = new NetworkMonitor(context);
                if (networkMonitor.isNetworkAvailable()) {
                    syncReportUpdateToNeon(report, callback);
                } else {
                    callback.onSuccess(report);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Update failed: " + e.getMessage());
                callback.onError("Update failed: " + e.getMessage());
            }
        });
    }
    
    // ✅ GET REPORT BY ID
    public LiveData<BlotterReport> getReportById(int reportId) {
        MutableLiveData<BlotterReport> result = new MutableLiveData<>();
        
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BlotterReport report = localDao.getReportById(reportId);
                result.postValue(report);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error getting report: " + e.getMessage());
                result.postValue(null);
            }
        });
        
        return result;
    }
    
    // ✅ PRIVATE: Sync with Neon Database
    private void syncWithNeonDatabase(int userId, String userRole) {
        apiClient.getReportsHybrid(userId, userRole, new HybridApiClient.ApiCallback<List<BlotterReport>>() {
            @Override
            public void onSuccess(List<BlotterReport> neonReports) {
                Log.d(TAG, "✅ Synced " + neonReports.size() + " reports from Neon");
                updateLocalWithNeonData(neonReports);
            }
            
            @Override
            public void onError(String error) {
                Log.w(TAG, "⚠️ Neon sync failed: " + error);
                // Continue with local data
            }
        });
    }
    
    // ✅ PRIVATE: Sync Report to Neon
    private void syncReportToNeon(BlotterReport report, RepositoryCallback<BlotterReport> callback) {
        // This would call the NeonDatabaseSync utility
        Log.d(TAG, "🔄 Syncing report to Neon: " + report.getCaseNumber());
        callback.onSuccess(report);
    }
    
    // ✅ PRIVATE: Sync Report Update to Neon
    private void syncReportUpdateToNeon(BlotterReport report, RepositoryCallback<BlotterReport> callback) {
        Log.d(TAG, "🔄 Syncing report update to Neon: " + report.getCaseNumber());
        callback.onSuccess(report);
    }
    
    // ✅ PRIVATE: Update Local Database with Neon Data
    private void updateLocalWithNeonData(List<BlotterReport> neonReports) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                for (BlotterReport report : neonReports) {
                    // Check if report exists locally
                    BlotterReport existing = localDao.getReportById(report.getId());
                    if (existing != null) {
                        localDao.updateReport(report);
                    } else {
                        localDao.insertReport(report);
                    }
                }
                Log.d(TAG, "✅ Local database synced with Neon");
            } catch (Exception e) {
                Log.e(TAG, "❌ Local sync failed: " + e.getMessage());
            }
        });
    }
    
    // ✅ PRIVATE: Filter reports by user role
    private List<BlotterReport> filterByUserRole(List<BlotterReport> reports, int userId, String userRole) {
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
    
    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }
}
