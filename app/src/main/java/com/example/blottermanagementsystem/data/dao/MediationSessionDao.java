package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.MediationSession;
import java.util.List;

@Dao
public interface MediationSessionDao {
    @Query("SELECT * FROM mediation_sessions WHERE blotterReportId = :reportId ORDER BY sessionDate DESC")
    List<MediationSession> getSessionsByReportId(int reportId);
    
    @Query("SELECT * FROM mediation_sessions ORDER BY sessionDate DESC")
    List<MediationSession> getAllSessions();
    
    @Query("SELECT * FROM mediation_sessions WHERE id = :sessionId")
    MediationSession getSessionById(int sessionId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSession(MediationSession session);
    
    @Update
    void updateSession(MediationSession session);
    
    @Delete
    void deleteSession(MediationSession session);
}
