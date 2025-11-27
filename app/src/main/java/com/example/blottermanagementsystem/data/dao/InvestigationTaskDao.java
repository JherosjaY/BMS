package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.InvestigationTask;
import java.util.List;

@Dao
public interface InvestigationTaskDao {
    
    @Query("SELECT * FROM investigation_tasks WHERE reportId = :reportId ORDER BY priority ASC, createdDate ASC")
    List<InvestigationTask> getTasksByReportId(int reportId);
    
    @Query("SELECT * FROM investigation_tasks WHERE reportId = :reportId AND isCompleted = 0")
    List<InvestigationTask> getIncompleteTasksByReportId(int reportId);
    
    @Query("SELECT * FROM investigation_tasks WHERE reportId = :reportId AND isCompleted = 1")
    List<InvestigationTask> getCompletedTasksByReportId(int reportId);
    
    @Query("SELECT * FROM investigation_tasks WHERE id = :taskId")
    InvestigationTask getTaskById(int taskId);
    
    @Query("SELECT COUNT(*) FROM investigation_tasks WHERE reportId = :reportId")
    int getTaskCountByReportId(int reportId);
    
    @Query("SELECT COUNT(*) FROM investigation_tasks WHERE reportId = :reportId AND isCompleted = 1")
    int getCompletedTaskCountByReportId(int reportId);
    
    @Query("SELECT COUNT(*) FROM investigation_tasks WHERE reportId = :reportId AND isCompleted = 0")
    int getIncompleteTaskCountByReportId(int reportId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTask(InvestigationTask task);
    
    @Insert
    void insertTasks(List<InvestigationTask> tasks);
    
    @Update
    void updateTask(InvestigationTask task);
    
    @Delete
    void deleteTask(InvestigationTask task);
    
    @Query("DELETE FROM investigation_tasks WHERE reportId = :reportId")
    void deleteTasksByReportId(int reportId);
}
