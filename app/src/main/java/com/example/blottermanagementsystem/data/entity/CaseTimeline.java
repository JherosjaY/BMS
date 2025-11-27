package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "case_timeline", foreignKeys = @ForeignKey(entity = BlotterReport.class, parentColumns = "id", childColumns = "blotterReportId", onDelete = ForeignKey.CASCADE), indices = {@Index("blotterReportId")})
public class CaseTimeline {
    @PrimaryKey(autoGenerate = true) private int id;
    private int blotterReportId; private String eventType; private String eventTitle; private String eventDescription; private String performedBy; private String performedByRole; private long timestamp; private String metadata;

    public CaseTimeline(int blotterReportId, String eventType, String eventTitle, String eventDescription, String performedBy, String performedByRole) {
        this.blotterReportId = blotterReportId; this.eventType = eventType; this.eventTitle = eventTitle; this.eventDescription = eventDescription; this.performedBy = performedBy; this.performedByRole = performedByRole; this.timestamp = System.currentTimeMillis();
    }

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public int getBlotterReportId() { return blotterReportId; } public void setBlotterReportId(int blotterReportId) { this.blotterReportId = blotterReportId; }
    public String getEventType() { return eventType; } public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEventTitle() { return eventTitle; } public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
    public String getEventDescription() { return eventDescription; } public void setEventDescription(String eventDescription) { this.eventDescription = eventDescription; }
    public String getPerformedBy() { return performedBy; } public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public String getPerformedByRole() { return performedByRole; } public void setPerformedByRole(String performedByRole) { this.performedByRole = performedByRole; }
    public long getTimestamp() { return timestamp; } public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getMetadata() { return metadata; } public void setMetadata(String metadata) { this.metadata = metadata; }
    
    // Alias for compatibility
    public String getDescription() { return eventDescription; }
}
