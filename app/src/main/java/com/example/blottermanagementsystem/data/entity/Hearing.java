package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "hearings",
    foreignKeys = @ForeignKey(
        entity = BlotterReport.class,
        parentColumns = "id",
        childColumns = "blotterReportId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("blotterReportId")}
)
public class Hearing {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int blotterReportId;
    private String hearingDate;
    private String hearingTime;
    private String location;
    private String purpose;
    private String status;
    private long createdAt;
    public Hearing() {
        this.status = "Scheduled";
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public Hearing(int blotterReportId, String hearingDate, String hearingTime, String location, String purpose) {
        this.blotterReportId = blotterReportId;
        this.hearingDate = hearingDate;
        this.hearingTime = hearingTime;
        this.location = location;
        this.purpose = purpose;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBlotterReportId() { return blotterReportId; }
    public void setBlotterReportId(int blotterReportId) { this.blotterReportId = blotterReportId; }
    public String getHearingDate() { return hearingDate; }
    public void setHearingDate(String hearingDate) { this.hearingDate = hearingDate; }
    public String getHearingTime() { return hearingTime; }
    public void setHearingTime(String hearingTime) { this.hearingTime = hearingTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    // Alias for compatibility
    public String getTitle() { return purpose != null ? purpose : "Hearing"; }
}
