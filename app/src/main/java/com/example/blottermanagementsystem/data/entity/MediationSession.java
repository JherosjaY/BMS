package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "mediation_sessions", foreignKeys = @ForeignKey(entity = BlotterReport.class, parentColumns = "id", childColumns = "blotterReportId", onDelete = ForeignKey.CASCADE), indices = {@Index("blotterReportId")})
public class MediationSession {
    @PrimaryKey(autoGenerate = true) private int id;
    private int blotterReportId; private int sessionNumber; private long sessionDate; private String sessionTime; private String venue; private String mediatorName; private String mediatorPosition;
    private boolean complainantPresent; private String complainantRepresentative; private boolean respondentPresent; private String respondentRepresentative; private String luponMembersPresent; private String witnessesPresent;
    private String sessionType; private String discussionSummary; private String agreementsReached; private String nextSteps; private String outcome; private String settlementTerms; private String reasonForFailure;
    private boolean nextSessionScheduled; private Long nextSessionDate; private String nextSessionTime; private String minutesOfMeeting; private String attachments; private String recordedBy; private long recordedDate;
    private String complainantSignature; private String respondentSignature; private String mediatorSignature;

    public MediationSession(int blotterReportId, int sessionNumber, long sessionDate, String sessionTime, String mediatorName, String sessionType, String discussionSummary, String outcome, String recordedBy) {
        this.blotterReportId = blotterReportId; this.sessionNumber = sessionNumber; this.sessionDate = sessionDate; this.sessionTime = sessionTime; this.mediatorName = mediatorName; this.sessionType = sessionType;
        this.discussionSummary = discussionSummary; this.outcome = outcome; this.recordedBy = recordedBy; this.venue = "Barangay Hall"; this.mediatorPosition = "Lupon Chairman"; this.recordedDate = System.currentTimeMillis();
    }

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public int getBlotterReportId() { return blotterReportId; } public void setBlotterReportId(int blotterReportId) { this.blotterReportId = blotterReportId; }
    public int getSessionNumber() { return sessionNumber; } public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }
    public long getSessionDate() { return sessionDate; } public void setSessionDate(long sessionDate) { this.sessionDate = sessionDate; }
    public String getSessionTime() { return sessionTime; } public void setSessionTime(String sessionTime) { this.sessionTime = sessionTime; }
    public String getVenue() { return venue; } public void setVenue(String venue) { this.venue = venue; }
    public String getMediatorName() { return mediatorName; } public void setMediatorName(String mediatorName) { this.mediatorName = mediatorName; }
    public String getMediatorPosition() { return mediatorPosition; } public void setMediatorPosition(String mediatorPosition) { this.mediatorPosition = mediatorPosition; }
    public boolean isComplainantPresent() { return complainantPresent; } public void setComplainantPresent(boolean complainantPresent) { this.complainantPresent = complainantPresent; }
    public String getComplainantRepresentative() { return complainantRepresentative; } public void setComplainantRepresentative(String complainantRepresentative) { this.complainantRepresentative = complainantRepresentative; }
    public boolean isRespondentPresent() { return respondentPresent; } public void setRespondentPresent(boolean respondentPresent) { this.respondentPresent = respondentPresent; }
    public String getRespondentRepresentative() { return respondentRepresentative; } public void setRespondentRepresentative(String respondentRepresentative) { this.respondentRepresentative = respondentRepresentative; }
    public String getLuponMembersPresent() { return luponMembersPresent; } public void setLuponMembersPresent(String luponMembersPresent) { this.luponMembersPresent = luponMembersPresent; }
    public String getWitnessesPresent() { return witnessesPresent; } public void setWitnessesPresent(String witnessesPresent) { this.witnessesPresent = witnessesPresent; }
    public String getSessionType() { return sessionType; } public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public String getDiscussionSummary() { return discussionSummary; } public void setDiscussionSummary(String discussionSummary) { this.discussionSummary = discussionSummary; }
    public String getAgreementsReached() { return agreementsReached; } public void setAgreementsReached(String agreementsReached) { this.agreementsReached = agreementsReached; }
    public String getNextSteps() { return nextSteps; } public void setNextSteps(String nextSteps) { this.nextSteps = nextSteps; }
    public String getOutcome() { return outcome; } public void setOutcome(String outcome) { this.outcome = outcome; }
    public String getSettlementTerms() { return settlementTerms; } public void setSettlementTerms(String settlementTerms) { this.settlementTerms = settlementTerms; }
    public String getReasonForFailure() { return reasonForFailure; } public void setReasonForFailure(String reasonForFailure) { this.reasonForFailure = reasonForFailure; }
    public boolean isNextSessionScheduled() { return nextSessionScheduled; } public void setNextSessionScheduled(boolean nextSessionScheduled) { this.nextSessionScheduled = nextSessionScheduled; }
    public Long getNextSessionDate() { return nextSessionDate; } public void setNextSessionDate(Long nextSessionDate) { this.nextSessionDate = nextSessionDate; }
    public String getNextSessionTime() { return nextSessionTime; } public void setNextSessionTime(String nextSessionTime) { this.nextSessionTime = nextSessionTime; }
    public String getMinutesOfMeeting() { return minutesOfMeeting; } public void setMinutesOfMeeting(String minutesOfMeeting) { this.minutesOfMeeting = minutesOfMeeting; }
    public String getAttachments() { return attachments; } public void setAttachments(String attachments) { this.attachments = attachments; }
    public String getRecordedBy() { return recordedBy; } public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public long getRecordedDate() { return recordedDate; } public void setRecordedDate(long recordedDate) { this.recordedDate = recordedDate; }
    public String getComplainantSignature() { return complainantSignature; } public void setComplainantSignature(String complainantSignature) { this.complainantSignature = complainantSignature; }
    public String getRespondentSignature() { return respondentSignature; } public void setRespondentSignature(String respondentSignature) { this.respondentSignature = respondentSignature; }
    public String getMediatorSignature() { return mediatorSignature; } public void setMediatorSignature(String mediatorSignature) { this.mediatorSignature = mediatorSignature; }
    
    // Alias for compatibility
    public String getStatus() { return outcome != null ? outcome : "Scheduled"; }
}
