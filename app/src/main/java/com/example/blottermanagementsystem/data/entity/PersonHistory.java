package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "person_history",
    foreignKeys = {
        @ForeignKey(entity = Person.class, parentColumns = "id", childColumns = "personId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = BlotterReport.class, parentColumns = "id", childColumns = "blotterReportId", onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index("personId"), @Index("blotterReportId")}
)
public class PersonHistory {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int personId;
    private Integer blotterReportId;
    private String activityType;
    private String description;
    private Integer performedByPersonId;
    private String oldValue;
    private String newValue;
    private long timestamp;
    private String metadata;

    public PersonHistory(int personId, String activityType, String description) {
        this.personId = personId;
        this.activityType = activityType;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPersonId() { return personId; }
    public void setPersonId(int personId) { this.personId = personId; }
    public Integer getBlotterReportId() { return blotterReportId; }
    public void setBlotterReportId(Integer blotterReportId) { this.blotterReportId = blotterReportId; }
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPerformedByPersonId() { return performedByPersonId; }
    public void setPerformedByPersonId(Integer performedByPersonId) { this.performedByPersonId = performedByPersonId; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
