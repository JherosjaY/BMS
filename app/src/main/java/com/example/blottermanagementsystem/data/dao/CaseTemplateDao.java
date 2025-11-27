package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.CaseTemplate;
import java.util.List;

@Dao
public interface CaseTemplateDao {
    @Query("SELECT * FROM case_templates WHERE isActive = 1 ORDER BY usageCount DESC")
    List<CaseTemplate> getAllActiveTemplates();
    
    @Query("SELECT * FROM case_templates WHERE id = :templateId")
    CaseTemplate getTemplateById(int templateId);
    
    @Query("SELECT * FROM case_templates WHERE incidentType = :incidentType AND isActive = 1")
    List<CaseTemplate> getTemplatesByIncidentType(String incidentType);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTemplate(CaseTemplate template);
    
    @Update
    void updateTemplate(CaseTemplate template);
    
    @Delete
    void deleteTemplate(CaseTemplate template);
    
    @Query("UPDATE case_templates SET usageCount = usageCount + 1 WHERE id = :templateId")
    void incrementUsageCount(int templateId);
    
    @Query("UPDATE case_templates SET isActive = :isActive WHERE id = :templateId")
    void updateTemplateStatus(int templateId, boolean isActive);
}
