package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "resolutions",
    foreignKeys = @ForeignKey(
        entity = BlotterReport.class,
        parentColumns = "id",
        childColumns = "blotterReportId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("blotterReportId")}
)
public class Resolution {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int blotterReportId;
    private String resolutionType;
    private String resolutionDetails;
    private int resolvedBy;
    private long resolvedDate;
    private long createdAt;

    public Resolution(int blotterReportId, String resolutionType, String resolutionDetails, int resolvedBy) {
        this.blotterReportId = blotterReportId;
        this.resolutionType = resolutionType;
        this.resolutionDetails = resolutionDetails;
        this.resolvedBy = resolvedBy;
        this.resolvedDate = System.currentTimeMillis();
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBlotterReportId() { return blotterReportId; }
    public void setBlotterReportId(int blotterReportId) { this.blotterReportId = blotterReportId; }
    public String getResolutionType() { return resolutionType; }
    public void setResolutionType(String resolutionType) { this.resolutionType = resolutionType; }
    public String getResolutionDetails() { return resolutionDetails; }
    public void setResolutionDetails(String resolutionDetails) { this.resolutionDetails = resolutionDetails; }
    public int getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(int resolvedBy) { this.resolvedBy = resolvedBy; }
    public long getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(long resolvedDate) { this.resolvedDate = resolvedDate; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
