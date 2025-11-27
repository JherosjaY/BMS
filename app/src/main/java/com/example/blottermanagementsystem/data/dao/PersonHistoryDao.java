package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.PersonHistory;
import java.util.List;

@Dao
public interface PersonHistoryDao {
    @Query("SELECT * FROM person_history WHERE personId = :personId ORDER BY timestamp DESC")
    List<PersonHistory> getHistoryByPersonId(int personId);
    
    @Query("SELECT * FROM person_history WHERE blotterReportId = :reportId ORDER BY timestamp DESC")
    List<PersonHistory> getHistoryByReportId(int reportId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPersonHistory(PersonHistory personHistory);
    
    @Delete
    void deletePersonHistory(PersonHistory personHistory);
}
