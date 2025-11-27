package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "sms_notifications",
    foreignKeys = {
        @ForeignKey(entity = Respondent.class, parentColumns = "id", childColumns = "respondentId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = BlotterReport.class, parentColumns = "id", childColumns = "blotterReportId", onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index("respondentId"), @Index("blotterReportId")}
)
public class SmsNotification {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int respondentId;
    private int blotterReportId;
    private String messageType;
    private String messageContent;
    private String recipientNumber;
    private long sentDate;
    private String deliveryStatus;
    private String respondentReply;
    private Long replyDate;

    public SmsNotification(int respondentId, int blotterReportId, String messageType, String messageContent, String recipientNumber) {
        this.respondentId = respondentId;
        this.blotterReportId = blotterReportId;
        this.messageType = messageType;
        this.messageContent = messageContent;
        this.recipientNumber = recipientNumber;
        this.sentDate = System.currentTimeMillis();
        this.deliveryStatus = "Pending";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getRespondentId() { return respondentId; }
    public void setRespondentId(int respondentId) { this.respondentId = respondentId; }
    public int getBlotterReportId() { return blotterReportId; }
    public void setBlotterReportId(int blotterReportId) { this.blotterReportId = blotterReportId; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }
    public String getRecipientNumber() { return recipientNumber; }
    public void setRecipientNumber(String recipientNumber) { this.recipientNumber = recipientNumber; }
    public long getSentDate() { return sentDate; }
    public void setSentDate(long sentDate) { this.sentDate = sentDate; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public String getRespondentReply() { return respondentReply; }
    public void setRespondentReply(String respondentReply) { this.respondentReply = respondentReply; }
    public Long getReplyDate() { return replyDate; }
    public void setReplyDate(Long replyDate) { this.replyDate = replyDate; }
}
