package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.StatusHistory;
import java.util.List;

@Dao
public interface StatusHistoryDao {
    @Query("SELECT * FROM status_history WHERE blotterReportId = :reportId ORDER BY createdAt DESC")
    List<StatusHistory> getStatusHistoryByReportId(int reportId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertStatusHistory(StatusHistory statusHistory);
    
    @Delete
    void deleteStatusHistory(StatusHistory statusHistory);
}
