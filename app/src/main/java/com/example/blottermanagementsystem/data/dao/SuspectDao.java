package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.Suspect;
import java.util.List;

@Dao
public interface SuspectDao {
    @Query("SELECT * FROM suspects WHERE blotterReportId = :reportId")
    List<Suspect> getSuspectsByReportId(int reportId);
    
    @Query("SELECT * FROM suspects WHERE id = :suspectId")
    Suspect getSuspectById(int suspectId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSuspect(Suspect suspect);
    
    @Update
    void updateSuspect(Suspect suspect);
    
    @Delete
    void deleteSuspect(Suspect suspect);
    
    @Query("DELETE FROM suspects WHERE blotterReportId = :reportId")
    void deleteSuspectsByReportId(int reportId);
    
    @Query("SELECT COUNT(*) FROM suspects WHERE blotterReportId = :reportId")
    int getSuspectCountByReport(int reportId);
    
    @Query("SELECT * FROM suspects WHERE blotterReportId = :reportId")
    List<Suspect> getSuspectsByReport(int reportId);
}
