package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.SyncQueue;
import java.util.List;

@Dao
public interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE synced = 0 ORDER BY timestamp ASC")
    List<SyncQueue> getPendingSync();
    
    // Alias for getPendingSync() - used by HybridSyncManager
    @Query("SELECT * FROM sync_queue WHERE synced = 0 ORDER BY timestamp ASC")
    List<SyncQueue> getPendingSyncItems();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSyncQueue(SyncQueue syncQueue);
    
    // Alias for insertSyncQueue() - used by AdminCaseDetailActivity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSyncItem(SyncQueue syncItem);
    
    @Update
    void updateSyncQueue(SyncQueue syncQueue);
    
    // Alias for updateSyncQueue() - used by HybridSyncManager
    @Update
    void updateSyncItem(SyncQueue syncItem);
    
    @Delete
    void deleteSyncQueue(SyncQueue syncQueue);
    
    @Query("DELETE FROM sync_queue WHERE synced = 1 AND timestamp < :timestamp")
    void deleteOldSynced(long timestamp);
}
