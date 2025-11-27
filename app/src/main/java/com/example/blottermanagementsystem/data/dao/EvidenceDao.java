package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.Evidence;
import java.util.List;

@Dao
public interface EvidenceDao {
    @Query("SELECT * FROM evidence WHERE blotterReportId = :reportId")
    List<Evidence> getEvidenceByReportId(int reportId);
    
    @Query("SELECT * FROM evidence ORDER BY id DESC")
    List<Evidence> getAllEvidence();
    
    @Query("SELECT * FROM evidence WHERE id = :evidenceId")
    Evidence getEvidenceById(int evidenceId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertEvidence(Evidence evidence);
    
    @Update
    void updateEvidence(Evidence evidence);
    
    @Delete
    void deleteEvidence(Evidence evidence);
    
    @Query("DELETE FROM evidence WHERE blotterReportId = :reportId")
    void deleteEvidenceByReportId(int reportId);
    
    @Query("SELECT COUNT(*) FROM evidence WHERE blotterReportId = :reportId")
    int getEvidenceCountByReport(int reportId);
    
    @Query("SELECT * FROM evidence WHERE blotterReportId = :reportId")
    List<Evidence> getEvidenceByReport(int reportId);
}
