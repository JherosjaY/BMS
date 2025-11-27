package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.CaseTimeline;
import java.util.List;

@Dao
public interface CaseTimelineDao {
    @Query("SELECT * FROM case_timeline WHERE blotterReportId = :reportId ORDER BY timestamp DESC")
    List<CaseTimeline> getTimelineByReportId(int reportId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTimeline(CaseTimeline timeline);
    
    @Delete
    void deleteTimeline(CaseTimeline timeline);
}
