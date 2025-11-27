package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(
    tableName = "investigation_tasks",
    foreignKeys = @ForeignKey(
        entity = BlotterReport.class,
        parentColumns = "id",
        childColumns = "reportId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("reportId")
)
public class InvestigationTask {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int reportId;
    public String taskName;
    public String description;
    public boolean isCompleted;
    public long completedDate;
    public String notes;
    public int priority; // 1=High, 2=Medium, 3=Low
    public long createdDate;
    public long updatedDate;
    
    // Constructor
    public InvestigationTask() {
    }
    
    @Ignore
    public InvestigationTask(int reportId, String taskName, String description, int priority) {
        this.reportId = reportId;
        this.taskName = taskName;
        this.description = description;
        this.priority = priority;
        this.isCompleted = false;
        this.createdDate = System.currentTimeMillis();
        this.updatedDate = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getReportId() {
        return reportId;
    }
    
    public void setReportId(int reportId) {
        this.reportId = reportId;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
        if (completed) {
            this.completedDate = System.currentTimeMillis();
        }
    }
    
    public long getCompletedDate() {
        return completedDate;
    }
    
    public void setCompletedDate(long completedDate) {
        this.completedDate = completedDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public long getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }
    
    public long getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }
}
