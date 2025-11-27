package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.util.Log;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.data.entity.User;
import com.example.blottermanagementsystem.data.repository.ApiRepository;
import java.util.List;
import java.util.concurrent.Executors;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private final Context context;
    private final BlotterDatabase database;
    private final ApiRepository apiRepository;
    private final PreferencesManager preferencesManager;
    
    public SyncManager(Context context) {
        this.context = context;
        this.database = BlotterDatabase.getDatabase(context);
        this.apiRepository = new ApiRepository();
        this.preferencesManager = new PreferencesManager(context);
    }
    
    public void syncAll(SyncCallback callback) {
        NetworkMonitor networkMonitor = new NetworkMonitor(context);
        
        if (!networkMonitor.isNetworkAvailable()) {
            callback.onError("No internet connection");
            return;
        }
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Sync reports from cloud
                apiRepository.getAllReports(new ApiRepository.ApiCallback<List<BlotterReport>>() {
                    @Override
                    public void onSuccess(List<BlotterReport> cloudReports) {
                        for (BlotterReport report : cloudReports) {
                            BlotterReport existing = database.blotterReportDao().getReportById(report.getId());
                            if (existing == null) {
                                database.blotterReportDao().insertReport(report);
                            } else {
                                database.blotterReportDao().updateReport(report);
                            }
                        }
                        
                        // Sync users
                        syncUsers(callback);
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Sync failed: " + error);
                        callback.onError(error);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Sync error", e);
                callback.onError(e.getMessage());
            }
        });
    }
    
    private void syncUsers(SyncCallback callback) {
        apiRepository.getAllUsers(new ApiRepository.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> cloudUsers) {
                for (User user : cloudUsers) {
                    User existing = database.userDao().getUserById(user.getId());
                    if (existing == null) {
                        database.userDao().insertUser(user);
                    } else {
                        database.userDao().updateUser(user);
                    }
                }
                
                preferencesManager.saveString("last_sync", String.valueOf(System.currentTimeMillis()));
                callback.onSuccess();
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    public interface SyncCallback {
        void onSuccess();
        void onError(String error);
    }
}
