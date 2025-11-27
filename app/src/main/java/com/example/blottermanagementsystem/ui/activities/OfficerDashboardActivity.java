package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.ui.adapters.RecentCaseAdapter;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class OfficerDashboardActivity extends BaseActivity {

    private TextView tvWelcomeTop, tvTotalCases, tvActiveCases, tvResolvedCases, tvPendingCases, btnProfile;
    private ImageButton btnNotifications;
    private View notificationBadge;
    private CardView cardMyCases, cardHearings, cardExportExcel;
    private android.widget.LinearLayout emptyState;
    private androidx.cardview.widget.CardView emptyStateCard;
    private RecyclerView recyclerRecentCases;

    private PreferencesManager preferencesManager;
    private BlotterDatabase database;
    private List<BlotterReport> recentCases = new ArrayList<>();
    private RecentCaseAdapter recentCaseAdapter;
    private long backPressedTime = 0;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_dashboard);

        preferencesManager = new PreferencesManager(this);
        database = BlotterDatabase.getDatabase(this);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadData();
    }

    private void initViews() {
        tvWelcomeTop = findViewById(R.id.tvWelcomeTop);
        tvTotalCases = findViewById(R.id.tvTotalCases);
        tvActiveCases = findViewById(R.id.tvActiveCases);
        tvResolvedCases = findViewById(R.id.tvResolvedCases);
        tvPendingCases = findViewById(R.id.tvPendingCases);
        btnNotifications = findViewById(R.id.btnNotifications);
        notificationBadge = findViewById(R.id.notificationBadge);
        btnProfile = findViewById(R.id.btnProfile);
        cardMyCases = findViewById(R.id.cardMyCases);
        cardHearings = findViewById(R.id.cardHearings);
        cardExportExcel = findViewById(R.id.cardExportExcel);
        emptyState = findViewById(R.id.emptyState);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        recyclerRecentCases = findViewById(R.id.recyclerRecentCases);

        String firstName = preferencesManager.getFirstName();
        tvWelcomeTop.setText("Welcome, Officer " + firstName + "!");
        
        checkUnreadNotifications();
    }

    private void loadData() {
        com.example.blottermanagementsystem.utils.GlobalLoadingManager.show(this, "Loading dashboard...");

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                int userId = preferencesManager.getUserId();
                
                com.example.blottermanagementsystem.data.entity.Officer officer = database.officerDao().getOfficerByUserId(userId);
                int officerId = (officer != null) ? officer.getId() : userId;
                
                List<BlotterReport> allReports = database.blotterReportDao().getAllReports();

                int total = 0;
                int active = 0;
                int resolved = 0;
                int pending = 0;
                recentCases.clear();

                for (BlotterReport report : allReports) {
                    Integer assignedId = report.getAssignedOfficerId();
                    String status = report.getStatus() != null ? report.getStatus().toLowerCase() : "";
                    
                    // Check if officer is assigned (either single or multiple officers)
                    boolean isAssignedToOfficer = false;
                    
                    // Check single officer assignment
                    if (assignedId != null && assignedId.intValue() == officerId) {
                        isAssignedToOfficer = true;
                    }
                    
                    // Check multiple officers assignment
                    if (!isAssignedToOfficer && report.getAssignedOfficerIds() != null && !report.getAssignedOfficerIds().isEmpty()) {
                        String[] officerIds = report.getAssignedOfficerIds().split(",");
                        for (String id : officerIds) {
                            try {
                                if (Integer.parseInt(id.trim()) == officerId) {
                                    isAssignedToOfficer = true;
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                // Ignore invalid IDs
                            }
                        }
                    }
                    
                    if (isAssignedToOfficer) {
                        total++;
                        recentCases.add(report);
                        if ("pending".equals(status) || "assigned".equals(status)) {
                            pending++;
                        } else if ("ongoing".equals(status) || "in progress".equals(status)) {
                            active++;
                        } else if ("resolved".equals(status) || "closed".equals(status)) {
                            resolved++;
                        }
                    }
                }
                
                int finalTotal = total;
                int finalActive = active;
                int finalResolved = resolved;
                int finalPending = pending;

                runOnUiThread(() -> {
                    tvTotalCases.setText(String.valueOf(finalTotal));
                    tvActiveCases.setText(String.valueOf(finalActive));
                    tvResolvedCases.setText(String.valueOf(finalResolved));
                    tvPendingCases.setText(String.valueOf(finalPending));

                    if (recentCaseAdapter != null) {
                        recentCaseAdapter.updateCases(recentCases);
                    }

                    if (emptyStateCard != null) {
                        emptyStateCard.setVisibility(View.VISIBLE);
                    }

                    if (recentCases.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        recyclerRecentCases.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        recyclerRecentCases.setVisibility(View.VISIBLE);
                    }

                    com.example.blottermanagementsystem.utils.GlobalLoadingManager.hide();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    com.example.blottermanagementsystem.utils.GlobalLoadingManager.hide();
                });
            }
        });
    }

    private void setupRecyclerView() {
        recyclerRecentCases.setLayoutManager(new LinearLayoutManager(this));

        recentCaseAdapter = new RecentCaseAdapter(recentCases, report -> {
            Intent intent = new Intent(this, OfficerCaseDetailActivity.class);
            intent.putExtra("REPORT_ID", report.getId());
            startActivity(intent);
        });
        recyclerRecentCases.setAdapter(recentCaseAdapter);
    }

    private void setupListeners() {
        setupStatisticsCardListeners();

        cardMyCases.setOnClickListener(v -> {
            Intent intent = new Intent(this, OfficerViewAssignedReportsActivity_New.class);
            intent.putExtra("SELECTED_CHIP", "ALL");
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        cardHearings.setOnClickListener(v -> {
            Intent intent = new Intent(this, OfficerViewAllHearingsActivity.class);
            startActivity(intent);
        });

        if (cardExportExcel != null) {
            cardExportExcel.setOnClickListener(v -> {
                exportToExcel();
            });
        }

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, OfficerProfileActivity.class);
            startActivity(intent);
        });

        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
        });
    }

    private void setupStatisticsCardListeners() {
        try {
            View cardTotalCases = findViewById(R.id.cardTotalCases);
            if (cardTotalCases != null) {
                cardTotalCases.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(this, OfficerViewAssignedReportsActivity_New.class);
                        intent.putExtra("SELECTED_CHIP", "ALL");
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    } catch (Exception e) {
                        android.util.Log.e("OfficerDashboard", "Error opening ViewAssignedReports", e);
                    }
                });
            }

            View cardPending = findViewById(R.id.cardPending);
            if (cardPending != null) {
                cardPending.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(this, OfficerViewAssignedReportsActivity_New.class);
                        intent.putExtra("SELECTED_CHIP", "ASSIGNED");
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    } catch (Exception e) {
                        android.util.Log.e("OfficerDashboard", "Error opening ViewAssignedReports", e);
                    }
                });
            }

            View cardActive = findViewById(R.id.cardActive);
            if (cardActive != null) {
                cardActive.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(this, OfficerViewOngoingReportsActivity_New.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    } catch (Exception e) {
                        android.util.Log.e("OfficerDashboard", "Error opening ViewOngoingReports", e);
                    }
                });
            }

            View cardResolved = findViewById(R.id.cardResolved);
            if (cardResolved != null) {
                cardResolved.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(this, OfficerViewResolvedReportsActivity_New.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    } catch (Exception e) {
                        android.util.Log.e("OfficerDashboard", "Error opening ViewResolvedReports", e);
                    }
                });
            }
        } catch (Exception e) {
            android.util.Log.e("OfficerDashboard", "Error setting up statistics cards: " + e.getMessage());
        }
    }

    private void checkUnreadNotifications() {
        try {
            int userId = preferencesManager.getUserId();
            com.example.blottermanagementsystem.data.database.BlotterDatabase db = 
                com.example.blottermanagementsystem.data.database.BlotterDatabase.getDatabase(this);
            List<com.example.blottermanagementsystem.data.entity.Notification> unreadNotifications = 
                db.notificationDao().getUnreadNotificationsForUser(userId);
            
            boolean hasUnread = unreadNotifications != null && !unreadNotifications.isEmpty();
            
            runOnUiThread(() -> {
                if (notificationBadge != null) {
                    notificationBadge.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("OfficerDashboard", "Error checking notifications: " + e.getMessage());
        }
    }

    private void exportToExcel() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Create workbook and sheet
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Officer Cases");

                // Create header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Case ID", "Title", "Description", "Status", "Date Created", "Assigned Officer"};
                
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    
                    // Style header
                    CellStyle style = workbook.createCellStyle();
                    Font font = workbook.createFont();
                    font.setBold(true);
                    font.setColor(IndexedColors.WHITE.getIndex());
                    style.setFont(font);
                    style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    style.setAlignment(HorizontalAlignment.CENTER);
                    cell.setCellStyle(style);
                }

                // Add data rows
                int rowNum = 1;
                for (BlotterReport report : recentCases) {
                    Row row = sheet.createRow(rowNum++);
                    
                    row.createCell(0).setCellValue(String.valueOf(report.getId()));
                    row.createCell(1).setCellValue(report.getCaseNumber() != null ? report.getCaseNumber() : "");
                    row.createCell(2).setCellValue(report.getNarrative() != null ? report.getNarrative() : "");
                    row.createCell(3).setCellValue(report.getStatus() != null ? report.getStatus() : "");
                    row.createCell(4).setCellValue(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(report.getDateFiled())));
                    row.createCell(5).setCellValue(report.getAssignedOfficerId() != null ? report.getAssignedOfficerId().toString() : "");
                }

                // Set fixed column widths (autoSizeColumn not available on Android)
                sheet.setColumnWidth(0, 3000);  // ID
                sheet.setColumnWidth(1, 5000);  // Case Number
                sheet.setColumnWidth(2, 8000);  // Narrative
                sheet.setColumnWidth(3, 4000);  // Status
                sheet.setColumnWidth(4, 6000);  // Date Filed
                sheet.setColumnWidth(5, 4000);  // Officer ID

                // Get Download folder - works on all Android versions
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                
                // Create Downloads folder if it doesn't exist
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs();
                }

                // Save file
                String fileName = "Officer_Cases_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".xlsx";
                File excelFile = new File(downloadDir, fileName);
                
                try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                    workbook.write(fos);
                    workbook.close();
                    
                    runOnUiThread(() -> {
                        Toast.makeText(OfficerDashboardActivity.this, 
                            "✅ Excel exported to Downloads: " + fileName, 
                            Toast.LENGTH_LONG).show();
                        android.util.Log.d("ExcelExport", "File saved to: " + excelFile.getAbsolutePath());
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(OfficerDashboardActivity.this, 
                        "❌ Export failed: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    android.util.Log.e("ExcelExport", "Error exporting to Excel", e);
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Check for new notifications when returning to dashboard
        checkUnreadNotifications();
    }
    
    @Override
    public void onBackPressed() {
        // ✅ Double-tap to exit with vibration
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            // Double tap detected - exit app with vibration
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(200); // 200ms vibration on exit
            }
            
            if (backToast != null) {
                backToast.cancel();
            }
            
            finishAffinity(); // Close all activities and exit app
        } else {
            // First tap - vibrate and show message
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(100); // 100ms vibration on first tap
            }
            
            backPressedTime = System.currentTimeMillis();
            backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
    }
    
    @Override
    public void onUserLeaveHint() {
        // ✅ When home button is pressed, reset the back press timer
        backPressedTime = 0;
        super.onUserLeaveHint();
    }
}
