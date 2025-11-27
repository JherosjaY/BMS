package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "respondents",
    foreignKeys = {
        @ForeignKey(entity = BlotterReport.class, parentColumns = "id", childColumns = "blotterReportId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Person.class, parentColumns = "id", childColumns = "personId", onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index("blotterReportId"), @Index("personId")}
)
public class Respondent {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int blotterReportId;
    private int personId;
    private String accusation;
    private String relationshipToComplainant;
    private boolean hasEvidence;
    private String status;
    private String contactNumber;
    private boolean notificationSent;
    private Long notificationSentDate;
    private boolean smsDelivered;
    private String cooperationStatus;
    private Long acknowledgedDate;
    private boolean appearedInPerson;
    private Long appearanceDate;
    private boolean statementGiven;
    private Long statementDate;
    private String statement;
    private boolean hearingScheduled;
    private Long hearingDate;
    private boolean attendedHearing;
    private long dateAccused;
    private String notes;

    public Respondent() {
        this.hasEvidence = false;
        this.status = "Accused";
        this.notificationSent = false;
        this.smsDelivered = false;
        this.cooperationStatus = "Not Contacted";
        this.appearedInPerson = false;
        this.hearingScheduled = false;
        this.attendedHearing = false;
        this.dateAccused = System.currentTimeMillis();
    }

    @Ignore
    public Respondent(int blotterReportId, int personId, String accusation, String contactNumber) {
        this.blotterReportId = blotterReportId;
        this.personId = personId;
        this.accusation = accusation;
        this.contactNumber = contactNumber;
        this.hasEvidence = false;
        this.notificationSent = false;
        this.smsDelivered = false;
        this.cooperationStatus = "Not Contacted";
        this.appearedInPerson = false;
        this.statementGiven = false;
        this.hearingScheduled = false;
        this.attendedHearing = false;
        this.dateAccused = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBlotterReportId() { return blotterReportId; }
    public void setBlotterReportId(int blotterReportId) { this.blotterReportId = blotterReportId; }
    public int getPersonId() { return personId; }
    public void setPersonId(int personId) { this.personId = personId; }
    public String getAccusation() { return accusation; }
    public void setAccusation(String accusation) { this.accusation = accusation; }
    public String getRelationshipToComplainant() { return relationshipToComplainant; }
    public void setRelationshipToComplainant(String relationshipToComplainant) { this.relationshipToComplainant = relationshipToComplainant; }
    public boolean isHasEvidence() { return hasEvidence; }
    public void setHasEvidence(boolean hasEvidence) { this.hasEvidence = hasEvidence; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public boolean isNotificationSent() { return notificationSent; }
    public void setNotificationSent(boolean notificationSent) { this.notificationSent = notificationSent; }
    public Long getNotificationSentDate() { return notificationSentDate; }
    public void setNotificationSentDate(Long notificationSentDate) { this.notificationSentDate = notificationSentDate; }
    public boolean isSmsDelivered() { return smsDelivered; }
    public void setSmsDelivered(boolean smsDelivered) { this.smsDelivered = smsDelivered; }
    public String getCooperationStatus() { return cooperationStatus; }
    public void setCooperationStatus(String cooperationStatus) { this.cooperationStatus = cooperationStatus; }
    public Long getAcknowledgedDate() { return acknowledgedDate; }
    public void setAcknowledgedDate(Long acknowledgedDate) { this.acknowledgedDate = acknowledgedDate; }
    public boolean isAppearedInPerson() { return appearedInPerson; }
    public void setAppearedInPerson(boolean appearedInPerson) { this.appearedInPerson = appearedInPerson; }
    public Long getAppearanceDate() { return appearanceDate; }
    public void setAppearanceDate(Long appearanceDate) { this.appearanceDate = appearanceDate; }
    public boolean isStatementGiven() { return statementGiven; }
    public void setStatementGiven(boolean statementGiven) { this.statementGiven = statementGiven; }
    public Long getStatementDate() { return statementDate; }
    public void setStatementDate(Long statementDate) { this.statementDate = statementDate; }
    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }
    public boolean isHearingScheduled() { return hearingScheduled; }
    public void setHearingScheduled(boolean hearingScheduled) { this.hearingScheduled = hearingScheduled; }
    public Long getHearingDate() { return hearingDate; }
    public void setHearingDate(Long hearingDate) { this.hearingDate = hearingDate; }
    public boolean isAttendedHearing() { return attendedHearing; }
    public void setAttendedHearing(boolean attendedHearing) { this.attendedHearing = attendedHearing; }
    public long getDateAccused() { return dateAccused; }
    public void setDateAccused(long dateAccused) { this.dateAccused = dateAccused; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Alias methods for compatibility
    public int getReportId() { return blotterReportId; }
    public void setReportId(int reportId) { this.blotterReportId = reportId; }

    public long getCreatedAt() { return dateAccused; }
    public void setCreatedAt(long createdAt) { this.dateAccused = createdAt; }
}
