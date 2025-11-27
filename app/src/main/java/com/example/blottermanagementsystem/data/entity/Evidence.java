package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "evidence",
    foreignKeys = @ForeignKey(
        entity = BlotterReport.class,
        parentColumns = "id",
        childColumns = "blotterReportId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("blotterReportId")}
)
public class Evidence {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int blotterReportId;
    private String evidenceType;
    private String description;
    private String filePath;
    private long collectedDate;
    private String collectedBy;
    private String locationFound;
    private String chainOfCustodyNotes;
    private String photoUris;
    private String videoUris;
    private String capturedBy;
    private long captureTimestamp;

    public Evidence() {
        this.collectedDate = System.currentTimeMillis();
        this.photoUris = "";
        this.videoUris = "";
        this.captureTimestamp = System.currentTimeMillis();
    }

    @Ignore
    public Evidence(int blotterReportId, String evidenceType, String description, String filePath) {
        this.blotterReportId = blotterReportId;
        this.evidenceType = evidenceType;
        this.description = description;
        this.filePath = filePath;
        this.collectedDate = System.currentTimeMillis();
        this.photoUris = "";
        this.videoUris = "";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBlotterReportId() { return blotterReportId; }
    public void setBlotterReportId(int blotterReportId) { this.blotterReportId = blotterReportId; }
    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public long getCollectedDate() { return collectedDate; }
    public void setCollectedDate(long collectedDate) { this.collectedDate = collectedDate; }
    public String getCollectedBy() { return collectedBy; }
    public void setCollectedBy(String collectedBy) { this.collectedBy = collectedBy; }
    public String getLocationFound() { return locationFound; }
    public void setLocationFound(String locationFound) { this.locationFound = locationFound; }
    public String getChainOfCustodyNotes() { return chainOfCustodyNotes; }
    public void setChainOfCustodyNotes(String chainOfCustodyNotes) { this.chainOfCustodyNotes = chainOfCustodyNotes; }
    public String getPhotoUris() { return photoUris; }
    public void setPhotoUris(String photoUris) { this.photoUris = photoUris; }
    public String getVideoUris() { return videoUris; }
    public void setVideoUris(String videoUris) { this.videoUris = videoUris; }
    public String getCapturedBy() { return capturedBy; }
    public void setCapturedBy(String capturedBy) { this.capturedBy = capturedBy; }
    public long getCaptureTimestamp() { return captureTimestamp; }
    public void setCaptureTimestamp(long captureTimestamp) { this.captureTimestamp = captureTimestamp; }
    
    // Alias for compatibility
    public void setPhotoUri(String photoUri) { this.photoUris = photoUri; }
    public String getPhotoUri() { return photoUris; }
}
