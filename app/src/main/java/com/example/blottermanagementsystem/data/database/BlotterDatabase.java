package com.example.blottermanagementsystem.data.database;

import android.content.Context;
import android.util.Log;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.blottermanagementsystem.data.dao.*;
import com.example.blottermanagementsystem.data.entity.*;

/**
 * 🚀 PURE NEON ONLINE-ONLY MODE
 * 
 * BlotterDatabase is now a STUB/NO-OP implementation
 * All data operations go directly to Neon via REST API
 * Local SQLite is DISABLED for simplified debugging
 * 
 * Once Neon works perfectly, we'll add offline support back
 */
@Database(
    entities = {
        User.class, BlotterReport.class, Suspect.class, Witness.class, Evidence.class,
        Hearing.class, StatusHistory.class, Resolution.class, Officer.class, InvestigationTask.class,
        Notification.class, Status.class, Person.class, Respondent.class, PersonHistory.class,
        SmsNotification.class, RespondentStatement.class, Summons.class, KPForm.class,
        MediationSession.class, CaseTimeline.class, CaseTemplate.class, SyncQueue.class,
        ConnectedDevice.class, LegalDocument.class, CloudinaryImage.class,
        PendingSync.class
    },
    version = 14,
    exportSchema = false
)
public abstract class BlotterDatabase extends RoomDatabase {
    
    public abstract UserDao userDao();
    public abstract BlotterReportDao blotterReportDao();
    public abstract SuspectDao suspectDao();
    public abstract WitnessDao witnessDao();
    public abstract EvidenceDao evidenceDao();
    public abstract HearingDao hearingDao();
    public abstract StatusHistoryDao statusHistoryDao();
    public abstract ResolutionDao resolutionDao();
    public abstract OfficerDao officerDao();
    public abstract NotificationDao notificationDao();
    public abstract StatusDao statusDao();
    public abstract PersonDao personDao();
    public abstract RespondentDao respondentDao();
    public abstract PersonHistoryDao personHistoryDao();
    public abstract SmsNotificationDao smsNotificationDao();
    public abstract RespondentStatementDao respondentStatementDao();
    public abstract SummonsDao summonsDao();
    public abstract KPFormDao kpFormDao();
    public abstract MediationSessionDao mediationSessionDao();
    public abstract CaseTimelineDao caseTimelineDao();
    public abstract CaseTemplateDao caseTemplateDao();
    public abstract SyncQueueDao syncQueueDao();
    public abstract ConnectedDeviceDao connectedDeviceDao();
    public abstract LegalDocumentDao legalDocumentDao();
    public abstract InvestigationTaskDao investigationTaskDao();
    public abstract CloudinaryImageDao cloudinaryImageDao();
    public abstract PendingSyncDao pendingSyncDao();
    
    private static volatile BlotterDatabase INSTANCE;
    
    /**
     * 🚀 STUB IMPLEMENTATION - PURE NEON MODE
     * Returns null - all data comes from Neon via REST API
     * Local SQLite is completely disabled
     */
    public static BlotterDatabase getDatabase(final Context context) {
        Log.d("BlotterDatabase", "⚠️ PURE NEON MODE: Local SQLite is DISABLED");
        Log.d("BlotterDatabase", "✅ All data operations use Neon REST API");
        return null; // ✅ NO local database in pure Neon mode
    }
    
    /**
     * 🚀 STUB - NOT USED IN PURE NEON MODE
     * This method is completely disabled - all data comes from Neon backend
     */
    private static void populateDatabase(Context context) {
        Log.d("BlotterDatabase", "⚠️ populateDatabase() called but DISABLED in pure Neon mode");
        // ✅ NO local database operations in pure Neon mode
    }
}
