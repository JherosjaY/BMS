package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.PendingSync;
import java.util.List;

/**
 * ✅ PENDING SYNC DAO
 * Manages offline sync queue operations
 */
@Dao
public interface PendingSyncDao {
    
    @Query("SELECT * FROM pending_sync ORDER BY createdAt ASC")
    List<PendingSync> getPendingSyncs();
    
    @Query("SELECT * FROM pending_sync WHERE status = 'PENDING' ORDER BY createdAt ASC")
    List<PendingSync> getPendingSyncsByStatus();
    
    @Query("SELECT COUNT(*) FROM pending_sync WHERE status = 'PENDING'")
    int getPendingSyncCount();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPendingSync(PendingSync pendingSync);
    
    @Update
    void updatePendingSync(PendingSync pendingSync);
    
    @Delete
    void deletePendingSync(PendingSync pendingSync);
    
    @Query("DELETE FROM pending_sync WHERE id = :id")
    void deletePendingSyncById(int id);
    
    @Query("DELETE FROM pending_sync WHERE status = 'FAILED' AND retryCount >= 3")
    void cleanupFailedSyncs();
    
    @Query("DELETE FROM pending_sync")
    void deleteAllPendingSyncs();
}
