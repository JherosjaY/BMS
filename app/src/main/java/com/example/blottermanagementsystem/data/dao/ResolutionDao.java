package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.Resolution;
import java.util.List;

@Dao
public interface ResolutionDao {
    @Query("SELECT * FROM resolutions WHERE blotterReportId = :reportId")
    List<Resolution> getResolutionsByReportId(int reportId);
    
    @Query("SELECT * FROM resolutions WHERE id = :resolutionId")
    Resolution getResolutionById(int resolutionId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertResolution(Resolution resolution);
    
    @Update
    void updateResolution(Resolution resolution);
    
    @Delete
    void deleteResolution(Resolution resolution);
    
    @Query("SELECT COUNT(*) FROM resolutions WHERE blotterReportId = :reportId")
    int getResolutionCountByReport(int reportId);
    
    @Query("SELECT * FROM resolutions WHERE blotterReportId = :reportId")
    List<Resolution> getResolutionsByReport(int reportId);
}
