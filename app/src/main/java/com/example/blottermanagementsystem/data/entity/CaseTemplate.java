package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "case_templates")
public class CaseTemplate {
    @PrimaryKey(autoGenerate = true) private int id;
    private String templateName; private String incidentType; private String descriptionTemplate; private String commonQuestions; private boolean isActive; private int usageCount; private String createdBy; private long createdDate;

    public CaseTemplate(String templateName, String incidentType, String descriptionTemplate, String commonQuestions, String createdBy) {
        this.templateName = templateName; this.incidentType = incidentType; this.descriptionTemplate = descriptionTemplate; this.commonQuestions = commonQuestions; this.createdBy = createdBy; this.isActive = true; this.usageCount = 0; this.createdDate = System.currentTimeMillis();
    }

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public String getTemplateName() { return templateName; } public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getIncidentType() { return incidentType; } public void setIncidentType(String incidentType) { this.incidentType = incidentType; }
    public String getDescriptionTemplate() { return descriptionTemplate; } public void setDescriptionTemplate(String descriptionTemplate) { this.descriptionTemplate = descriptionTemplate; }
    public String getCommonQuestions() { return commonQuestions; } public void setCommonQuestions(String commonQuestions) { this.commonQuestions = commonQuestions; }
    public boolean isActive() { return isActive; } public void setActive(boolean active) { isActive = active; }
    public int getUsageCount() { return usageCount; } public void setUsageCount(int usageCount) { this.usageCount = usageCount; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedDate() { return createdDate; } public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }
}
