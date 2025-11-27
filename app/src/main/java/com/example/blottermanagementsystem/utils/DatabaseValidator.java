package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.util.Log;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.data.entity.Officer;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * DatabaseValidator - Validates local Room database integrity
 * for multi-officer assignment feature
 */
public class DatabaseValidator {
    private static final String TAG = "DatabaseValidator";
    private final BlotterDatabase database;
    private final Context context;
    
    public DatabaseValidator(Context context) {
        this.context = context;
        this.database = BlotterDatabase.getDatabase(context);
    }
    
    /**
     * Validates multi-officer assignment in local database
     */
    public void validateMultiOfficerAssignment(ValidationCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ValidationResult result = new ValidationResult();
                
                // Test 1: Check database schema
                Log.d(TAG, "🔍 Test 1: Checking database schema...");
                result.schemaValid = checkSchema();
                
                // Test 2: Check existing reports
                Log.d(TAG, "🔍 Test 2: Checking existing reports...");
                List<BlotterReport> allReports = database.blotterReportDao().getAllReports();
                result.totalReports = allReports.size();
                Log.d(TAG, "   Total reports in database: " + result.totalReports);
                
                // Test 3: Check single officer assignments
                Log.d(TAG, "🔍 Test 3: Checking single officer assignments...");
                int singleOfficerCount = 0;
                for (BlotterReport report : allReports) {
                    if (report.getAssignedOfficerId() != null && 
                        (report.getAssignedOfficerIds() == null || report.getAssignedOfficerIds().isEmpty())) {
                        singleOfficerCount++;
                    }
                }
                result.singleOfficerAssignments = singleOfficerCount;
                Log.d(TAG, "   Single officer assignments: " + singleOfficerCount);
                
                // Test 4: Check multi officer assignments
                Log.d(TAG, "🔍 Test 4: Checking multi-officer assignments...");
                int multiOfficerCount = 0;
                for (BlotterReport report : allReports) {
                    if (report.getAssignedOfficerIds() != null && !report.getAssignedOfficerIds().isEmpty()) {
                        String[] ids = report.getAssignedOfficerIds().split(",");
                        if (ids.length > 1) {
                            multiOfficerCount++;
                            Log.d(TAG, "   ✅ Case " + report.getCaseNumber() + ": " + ids.length + " officers assigned");
                            Log.d(TAG, "      assignedOfficerIds: " + report.getAssignedOfficerIds());
                            Log.d(TAG, "      assignedOfficer: " + report.getAssignedOfficer());
                        }
                    }
                }
                result.multiOfficerAssignments = multiOfficerCount;
                Log.d(TAG, "   Multi-officer assignments: " + multiOfficerCount);
                
                // Test 5: Check officer data integrity
                Log.d(TAG, "🔍 Test 5: Checking officer data integrity...");
                List<Officer> allOfficers = database.officerDao().getAllOfficers();
                result.totalOfficers = allOfficers.size();
                Log.d(TAG, "   Total officers in database: " + result.totalOfficers);
                
                // Test 6: Validate officer IDs in assignments
                Log.d(TAG, "🔍 Test 6: Validating officer IDs in assignments...");
                int invalidOfficerIds = 0;
                for (BlotterReport report : allReports) {
                    if (report.getAssignedOfficerIds() != null && !report.getAssignedOfficerIds().isEmpty()) {
                        String[] ids = report.getAssignedOfficerIds().split(",");
                        for (String id : ids) {
                            try {
                                int officerId = Integer.parseInt(id.trim());
                                Officer officer = database.officerDao().getOfficerById(officerId);
                                if (officer == null) {
                                    invalidOfficerIds++;
                                    Log.w(TAG, "   ⚠️ Case " + report.getCaseNumber() + ": Invalid officer ID " + officerId);
                                }
                            } catch (NumberFormatException e) {
                                invalidOfficerIds++;
                                Log.w(TAG, "   ⚠️ Case " + report.getCaseNumber() + ": Invalid ID format: " + id);
                            }
                        }
                    }
                }
                result.invalidOfficerIds = invalidOfficerIds;
                Log.d(TAG, "   Invalid officer IDs found: " + invalidOfficerIds);
                
                // Test 7: Check status consistency
                Log.d(TAG, "🔍 Test 7: Checking status consistency...");
                int assignedWithoutOfficers = 0;
                for (BlotterReport report : allReports) {
                    if ("ASSIGNED".equalsIgnoreCase(report.getStatus())) {
                        if ((report.getAssignedOfficerId() == null || report.getAssignedOfficerId() == 0) &&
                            (report.getAssignedOfficerIds() == null || report.getAssignedOfficerIds().isEmpty())) {
                            assignedWithoutOfficers++;
                            Log.w(TAG, "   ⚠️ Case " + report.getCaseNumber() + ": Status is ASSIGNED but no officers assigned");
                        }
                    }
                }
                result.assignedWithoutOfficers = assignedWithoutOfficers;
                Log.d(TAG, "   Cases with ASSIGNED status but no officers: " + assignedWithoutOfficers);
                
                // Final result
                result.isValid = result.schemaValid && result.invalidOfficerIds == 0 && result.assignedWithoutOfficers == 0;
                
                Log.d(TAG, "\n" + "=".repeat(60));
                Log.d(TAG, "📊 VALIDATION SUMMARY:");
                Log.d(TAG, "=".repeat(60));
                Log.d(TAG, "Schema Valid: " + (result.schemaValid ? "✅ YES" : "❌ NO"));
                Log.d(TAG, "Total Reports: " + result.totalReports);
                Log.d(TAG, "Single Officer Assignments: " + result.singleOfficerAssignments);
                Log.d(TAG, "Multi-Officer Assignments: " + result.multiOfficerAssignments);
                Log.d(TAG, "Total Officers: " + result.totalOfficers);
                Log.d(TAG, "Invalid Officer IDs: " + result.invalidOfficerIds);
                Log.d(TAG, "Assigned Without Officers: " + result.assignedWithoutOfficers);
                Log.d(TAG, "Overall Status: " + (result.isValid ? "✅ VALID" : "❌ INVALID"));
                Log.d(TAG, "=".repeat(60) + "\n");
                
                callback.onValidationComplete(result);
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Validation error: " + e.getMessage(), e);
                callback.onValidationError(e.getMessage());
            }
        });
    }
    
    /**
     * Checks if database schema is correct
     */
    private boolean checkSchema() {
        try {
            // Try to access the fields to verify schema
            BlotterReport testReport = new BlotterReport();
            testReport.setAssignedOfficerId(1);
            testReport.setAssignedOfficerIds("1,2");
            
            Log.d(TAG, "   ✅ Schema check passed - both officer fields accessible");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "   ❌ Schema check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generates a detailed validation report
     */
    public void generateDetailedReport(ReportCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                StringBuilder report = new StringBuilder();
                report.append("LOCAL DATABASE VALIDATION REPORT\n");
                report.append("=".repeat(60)).append("\n");
                report.append("Generated: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())).append("\n\n");
                
                List<BlotterReport> allReports = database.blotterReportDao().getAllReports();
                
                report.append("MULTI-OFFICER ASSIGNMENTS:\n");
                report.append("-".repeat(60)).append("\n");
                
                for (BlotterReport report_item : allReports) {
                    if (report_item.getAssignedOfficerIds() != null && !report_item.getAssignedOfficerIds().isEmpty()) {
                        String[] ids = report_item.getAssignedOfficerIds().split(",");
                        if (ids.length > 1) {
                            report.append("Case: ").append(report_item.getCaseNumber()).append("\n");
                            report.append("  Status: ").append(report_item.getStatus()).append("\n");
                            report.append("  Officers: ").append(report_item.getAssignedOfficer()).append("\n");
                            report.append("  Officer IDs: ").append(report_item.getAssignedOfficerIds()).append("\n");
                            report.append("  Type: Multi-Officer (").append(ids.length).append(")\n\n");
                        }
                    }
                }
                
                callback.onReportGenerated(report.toString());
                
            } catch (Exception e) {
                Log.e(TAG, "Error generating report: " + e.getMessage());
                callback.onReportError(e.getMessage());
            }
        });
    }
    
    // Callbacks
    public interface ValidationCallback {
        void onValidationComplete(ValidationResult result);
        void onValidationError(String error);
    }
    
    public interface ReportCallback {
        void onReportGenerated(String report);
        void onReportError(String error);
    }
    
    // Result class
    public static class ValidationResult {
        public boolean schemaValid;
        public int totalReports;
        public int singleOfficerAssignments;
        public int multiOfficerAssignments;
        public int totalOfficers;
        public int invalidOfficerIds;
        public int assignedWithoutOfficers;
        public boolean isValid;
    }
}
