package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.ui.adapters.ReportAdapter;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.example.blottermanagementsystem.utils.ApiClient;
import com.example.blottermanagementsystem.utils.NetworkMonitor;
import com.example.blottermanagementsystem.utils.GlobalLoadingManager;
import android.widget.Toast;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewAllReportsActivity extends BaseActivity {
    
    private RecyclerView recyclerReports;
    private ReportAdapter adapter;
    private View emptyState;
    private androidx.cardview.widget.CardView emptyStateCard;
    private Chip chipAll, chipPending, chipAssigned, chipOngoing, chipResolved;
    private EditText etSearch;
    private TextView tvTotalCount, tvPendingCount, tvOngoingCount, tvResolvedCount;
    private ImageButton btnSort;
    private ImageView emptyStateIcon;
    private TextView emptyStateTitle, emptyStateMessage;
    private android.widget.HorizontalScrollView chipScrollView;
    private List<BlotterReport> allReports = new ArrayList<>();
    private List<BlotterReport> filteredReports = new ArrayList<>();
    private PreferencesManager preferencesManager;
    private int userId;
    private String searchQuery = "";
    private String currentSort = "Newest First";
    private boolean isOfficerFilter = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            android.util.Log.d("ViewAllReports", "Starting onCreate...");
            setContentView(R.layout.activity_view_all_reports);
            
            preferencesManager = new PreferencesManager(this);
            userId = preferencesManager.getUserId();
            String userRole = preferencesManager.getUserRole();
            isOfficerFilter = getIntent().getBooleanExtra("officer_filter", false);
            
            // Ensure User role can only see their own reports
            if ("User".equalsIgnoreCase(userRole)) {
                isOfficerFilter = false; // Force user filtering
            }
            
            initializeViews();
            setupToolbar();
            setupListeners();
            loadReports();
            startPeriodicRefresh();
            
            // ✅ RESTORE SCROLL POSITION & AUTO-SELECT CHIP
            restoreScrollPosition();
            autoSelectCurrentChip();
            
            android.util.Log.d("ViewAllReports", "onCreate completed successfully");
        } catch (Exception e) {
            android.util.Log.e("ViewAllReports", "Error in onCreate: " + e.getMessage(), e);
            showErrorState();
        }
    }
    
    private void initializeViews() {
        try {
            recyclerReports = findViewById(R.id.recyclerReports);
            emptyState = findViewById(R.id.emptyState);
            emptyStateCard = findViewById(R.id.emptyStateCard);
            etSearch = findViewById(R.id.etSearch);
            tvTotalCount = findViewById(R.id.tvTotalCount);
            tvPendingCount = findViewById(R.id.tvPendingCount);
            tvOngoingCount = findViewById(R.id.tvOngoingCount);
            tvResolvedCount = findViewById(R.id.tvResolvedCount);
            btnSort = findViewById(R.id.btnSort);
            
            // ✅ COMPLETE CHIP INITIALIZATION
            chipAll = findViewById(R.id.chipAll);
            chipPending = findViewById(R.id.chipPending);
            chipAssigned = findViewById(R.id.chipAssigned);
            chipOngoing = findViewById(R.id.chipOngoing);
            chipResolved = findViewById(R.id.chipResolved);
            
            // ✅ SET CHIP ICONS DYNAMICALLY
            if (chipPending != null) chipPending.setChipIconResource(R.drawable.ic_pending_modern);
            if (chipAssigned != null) chipAssigned.setChipIconResource(R.drawable.ic_users_modern);
            if (chipOngoing != null) chipOngoing.setChipIconResource(R.drawable.ic_ongoing_modern);
            if (chipResolved != null) chipResolved.setChipIconResource(R.drawable.ic_resolved_modern);
            
            emptyStateIcon = findViewById(R.id.emptyStateIcon);
            emptyStateTitle = findViewById(R.id.emptyStateTitle);
            emptyStateMessage = findViewById(R.id.emptyStateMessage);
            
            // Scale empty state icon to be larger
            if (emptyStateIcon != null) {
                emptyStateIcon.setScaleX(1.5f);
                emptyStateIcon.setScaleY(1.5f);
            }
            
            // Setup RecyclerView
            if (recyclerReports != null) {
                adapter = new ReportAdapter(filteredReports, report -> {
                    try {
                        String userRole = preferencesManager.getUserRole();
                        Class<?> targetActivity;
                        
                        if ("Admin".equalsIgnoreCase(userRole)) {
                            targetActivity = AdminCaseDetailActivity.class;
                        } else if ("Officer".equalsIgnoreCase(userRole)) {
                            targetActivity = OfficerCaseDetailActivity.class;
                        } else {
                            targetActivity = ReportDetailActivity.class;
                        }
                        
                        Intent intent = new Intent(this, targetActivity);
                        intent.putExtra("REPORT_ID", report.getId());
                        startActivity(intent);
                    } catch (Exception e) {
                        android.util.Log.e("ViewAllReports", "Error opening report detail: " + e.getMessage());
                    }
                });
                recyclerReports.setLayoutManager(new LinearLayoutManager(this));
                recyclerReports.setAdapter(adapter);
            }
        } catch (Exception e) {
            android.util.Log.e("ViewAllReports", "Error initializing views: " + e.getMessage());
            throw e;
        }
    }
    
    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    private void setupListeners() {
        try {
            // Setup search
            setupSearchListener();
            
            // ✅ UNIFIED CHIP NAVIGATION
            setupChipNavigation();
            
            // Setup sort button
            if (btnSort != null) {
                btnSort.setOnClickListener(v -> showSortDialog());
            }
        } catch (Exception e) {
            android.util.Log.e("ViewAllReports", "Error setting up listeners: " + e.getMessage());
        }
    }
    
    private void setupSearchListener() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = s.toString().toLowerCase();
                    filterReports();
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }
    
    private void setupChipNavigation() {
        // ✅ CONSISTENT NAVIGATION FOR ALL CHIPS WITH LOADING ANIMATION
        if (chipAll != null) {
            chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    showLoadingAndNavigate(ViewAllReportsActivity.class, "Loading all reports...");
                }
            });
        }
        
        if (chipPending != null) {
            chipPending.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    showLoadingAndNavigate(ViewPendingReportsActivity.class, "Loading pending reports...");
                }
            });
        }
        
        if (chipAssigned != null) {
            chipAssigned.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    showLoadingAndNavigate(ViewAssignedReportsActivity.class, "Loading assigned reports...");
                }
            });
        }
        
        if (chipOngoing != null) {
            chipOngoing.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    showLoadingAndNavigate(ViewOngoingReportsActivity.class, "Loading ongoing reports...");
                }
            });
        }
        
        if (chipResolved != null) {
            chipResolved.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    showLoadingAndNavigate(ViewResolvedReportsActivity.class, "Loading resolved reports...");
                }
            });
        }
    }
    
    // ✅ SHOW LOADING ANIMATION DURING CHIP NAVIGATION
    private void showLoadingAndNavigate(Class<?> activityClass, String loadingMessage) {
        GlobalLoadingManager.show(this, loadingMessage);
        
        new android.os.Handler().postDelayed(() -> {
            navigateToScreen(activityClass);
            GlobalLoadingManager.hide();
        }, 300); // 300ms delay for smooth visual feedback
    }
    
    private void navigateToScreen(Class<?> activityClass) {
        try {
            // ✅ SAVE SCROLL POSITION FOR SMOOTH RETURN
            saveScrollPosition();
            
            Intent intent = new Intent(this, activityClass);
            intent.putExtra("officer_filter", isOfficerFilter);
            
            // ✅ SMOOTH ACTIVITY TRANSITION
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
            
        } catch (Exception e) {
            android.util.Log.e("ViewAllReports", "Navigation error: " + e.getMessage());
            Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveScrollPosition() {
        if (chipScrollView != null) {
            // ✅ SAVE CURRENT SCROLL POSITION
            int scrollX = chipScrollView.getScrollX();
            android.content.SharedPreferences prefs = getSharedPreferences("chip_scroll", MODE_PRIVATE);
            prefs.edit().putInt("scroll_position", scrollX).apply();
        }
    }
    
    private void restoreScrollPosition() {
        if (chipScrollView != null) {
            chipScrollView.post(() -> {
                try {
                    android.content.SharedPreferences prefs = getSharedPreferences("chip_scroll", MODE_PRIVATE);
                    int savedScrollX = prefs.getInt("scroll_position", 0);
                    
                    if (savedScrollX > 0) {
                        // ✅ SMOOTH SCROLL TO SAVED POSITION
                        chipScrollView.smoothScrollTo(savedScrollX, 0);
                        
                        // Clear after restoring
                        prefs.edit().remove("scroll_position").apply();
                    }
                } catch (Exception e) {
                    android.util.Log.e("ViewAllReports", "Error restoring scroll: " + e.getMessage());
                }
            });
        }
    }
    
    private void autoSelectCurrentChip() {
        // ✅ DELAY SLIGHTLY FOR SMOOTH VISUAL FEEDBACK
        new android.os.Handler().postDelayed(() -> {
            try {
                if (chipAll != null) {
                    chipAll.setChecked(true);
                }
            } catch (Exception e) {
                android.util.Log.e("ViewAllReports", "Error auto-selecting chip: " + e.getMessage());
            }
        }, 100);
    }
    
    private void loadReports() {
        // ✅ STEP 1: Load from LOCAL DATABASE FIRST (immediate UI update)
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BlotterDatabase db = BlotterDatabase.getDatabase(this);
                List<BlotterReport> reports = db.blotterReportDao().getAllReports();
                
                runOnUiThread(() -> {
                    filterReportsByUser(reports);
                    filterReports();
                    updateStatistics();
                    updateEmptyState();
                });
                
                android.util.Log.d("ViewAllReports", "✅ Local data loaded: " + reports.size() + " reports");
                
            } catch (Exception e) {
                android.util.Log.e("ViewAllReports", "❌ Error loading from database: " + e.getMessage());
            }
        });
        
        // ✅ STEP 2: Sync with API in background (silent update)
        NetworkMonitor networkMonitor = new NetworkMonitor(this);
        if (networkMonitor.isNetworkAvailable()) {
            java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                ApiClient.getAllReports(new ApiClient.ApiCallback<List<BlotterReport>>() {
                    @Override
                    public void onSuccess(List<BlotterReport> apiReports) {
                        // Update local database silently
                        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                            try {
                                BlotterDatabase db = BlotterDatabase.getDatabase(ViewAllReportsActivity.this);
                                
                                for (BlotterReport report : apiReports) {
                                    BlotterReport existing = db.blotterReportDao().getReportById(report.getId());
                                    if (existing == null) {
                                        db.blotterReportDao().insertReport(report);
                                    } else {
                                        db.blotterReportDao().updateReport(report);
                                    }
                                }
                                
                                android.util.Log.d("ViewAllReports", "✅ API sync completed: " + apiReports.size() + " reports");
                                
                                // ✅ Refresh UI with updated data (only if different)
                                runOnUiThread(() -> {
                                    if (hasDataChanged(apiReports, allReports)) {
                                        allReports.clear();
                                        allReports.addAll(apiReports);
                                        filterReports();
                                        updateStatistics();
                                        android.util.Log.d("ViewAllReports", "✅ UI refreshed with API data");
                                    }
                                });
                                
                            } catch (Exception e) {
                                android.util.Log.e("ViewAllReports", "❌ Error syncing API data: " + e.getMessage());
                            }
                        });
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        android.util.Log.w("ViewAllReports", "⚠️ API sync error: " + errorMessage);
                        // Continue with local data - no need to show error to user
                    }
                });
            });
        } else {
            android.util.Log.d("ViewAllReports", "📶 No network - using local data only");
        }
    }
    
    private void loadReportsFromApi() {
        // Show loading indicator
        runOnUiThread(() -> {
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        });
        
        // Fetch from API
        ApiClient.getAllReports(new ApiClient.ApiCallback<List<BlotterReport>>() {
            @Override
            public void onSuccess(List<BlotterReport> apiReports) {
                // Save to local database
                BlotterDatabase db = BlotterDatabase.getDatabase(ViewAllReportsActivity.this);
                new Thread(() -> {
                    try {
                        for (BlotterReport report : apiReports) {
                            BlotterReport existing = db.blotterReportDao().getReportById(report.getId());
                            if (existing == null) {
                                db.blotterReportDao().insertReport(report);
                            } else {
                                db.blotterReportDao().updateReport(report);
                            }
                        }
                        
                        // Update UI with API data
                        runOnUiThread(() -> {
                            filterReportsByUser(apiReports);
                            updateStatistics();
                            filterReports();
                        });
                    } catch (Exception e) {
                        android.util.Log.e("ViewAllReports", "Error saving API data: " + e.getMessage());
                        loadReportsFromDatabase();
                    }
                }).start();
            }
            
            @Override
            public void onError(String errorMessage) {
                // Fallback to local database
                android.util.Log.w("ViewAllReports", "API error: " + errorMessage);
                loadReportsFromDatabase();
            }
        });
    }
    
    private void loadReportsFromDatabase() {
        new Thread(() -> {
            try {
                BlotterDatabase db = BlotterDatabase.getDatabase(this);
                List<BlotterReport> reports = db.blotterReportDao().getAllReports();
                
                runOnUiThread(() -> {
                    filterReportsByUser(reports);
                    updateStatistics();
                    filterReports();
                });
            } catch (Exception e) {
                android.util.Log.e("ViewAllReports", "Error loading from database: " + e.getMessage());
            }
        }).start();
    }
    
    private void filterReportsByUser(List<BlotterReport> reports) {
        allReports.clear();
        String userRole = preferencesManager.getUserRole();
        
        for (BlotterReport report : reports) {
            // Admin sees ALL reports (no filtering by user)
            if ("Admin".equalsIgnoreCase(userRole)) {
                allReports.add(report);
            }
            // Officer sees reports assigned to them
            else if ("Officer".equalsIgnoreCase(userRole)) {
                boolean isAssignedToOfficer = false;
                
                if (report.getAssignedOfficerId() != null && report.getAssignedOfficerId().intValue() == userId) {
                    isAssignedToOfficer = true;
                }
                
                if (!isAssignedToOfficer && report.getAssignedOfficerIds() != null && !report.getAssignedOfficerIds().isEmpty()) {
                    String[] officerIds = report.getAssignedOfficerIds().split(",");
                    for (String id : officerIds) {
                        try {
                            if (Integer.parseInt(id.trim()) == userId) {
                                isAssignedToOfficer = true;
                                break;
                            }
                        } catch (NumberFormatException e) {
                            // Ignore
                        }
                    }
                }
                
                if (isAssignedToOfficer) {
                    allReports.add(report);
                }
            }
            // Regular User sees ONLY their own filed reports
            else {
                if (report.getReportedById() == userId) {
                    allReports.add(report);
                }
            }
        }
    }
    
    private void updateStatistics() {
        int total = allReports.size();
        int pending = 0;
        int ongoing = 0;
        int resolved = 0;
        
        for (BlotterReport report : allReports) {
            String status = report.getStatus();
            if (status != null) {
                status = status.toUpperCase();
                if ("PENDING".equals(status)) {
                    pending++;
                } else if ("ASSIGNED".equals(status)) {
                    pending++;
                } else if ("ONGOING".equals(status) || "IN PROGRESS".equals(status)) {
                    ongoing++;
                } else if ("RESOLVED".equals(status)) {
                    resolved++;
                }
            }
        }
        
        if (tvTotalCount != null) tvTotalCount.setText(String.valueOf(total));
        if (tvPendingCount != null) tvPendingCount.setText(String.valueOf(pending));
        if (tvOngoingCount != null) tvOngoingCount.setText(String.valueOf(ongoing));
        if (tvResolvedCount != null) tvResolvedCount.setText(String.valueOf(resolved));
    }
    
    private void filterReports() {
        filteredReports.clear();
        
        // Show ALL statuses - no status filtering for All chip
        if (searchQuery.isEmpty()) {
            filteredReports.addAll(allReports);
        } else {
            for (BlotterReport report : allReports) {
                String caseNumber = report.getCaseNumber() != null ? report.getCaseNumber().toLowerCase() : "";
                String incidentType = report.getIncidentType() != null ? report.getIncidentType().toLowerCase() : "";
                String complainant = report.getComplainantName() != null ? report.getComplainantName().toLowerCase() : "";
                
                if (caseNumber.contains(searchQuery) || 
                    incidentType.contains(searchQuery) || 
                    complainant.contains(searchQuery)) {
                    filteredReports.add(report);
                }
            }
        }
        
        sortReports();
        
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        
        updateEmptyState();
    }
    
    private void sortReports() {
        switch (currentSort) {
            case "Newest First":
                Collections.sort(filteredReports, (r1, r2) -> 
                    Long.compare(r2.getDateFiled(), r1.getDateFiled()));
                break;
            case "Oldest First":
                Collections.sort(filteredReports, (r1, r2) -> 
                    Long.compare(r1.getDateFiled(), r2.getDateFiled()));
                break;
        }
    }
    
    private void filterReportsByStatus(String status) {
        filteredReports.clear();
        
        for (BlotterReport report : allReports) {
            String reportStatus = report.getStatus() != null ? report.getStatus().toUpperCase().trim() : "";
            if (status.toUpperCase().trim().equals(reportStatus)) {
                filteredReports.add(report);
            }
        }
        
        sortReports();
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BlotterDatabase db = BlotterDatabase.getDatabase(this);
                List<BlotterReport> reports = db.blotterReportDao().getAllReports();
                
                runOnUiThread(() -> {
                    filterReportsByUser(reports);
                    updateStatistics();
                });
            } catch (Exception e) {
                android.util.Log.e("ViewAllReports", "Error updating statistics: " + e.getMessage());
            }
        });
    }
    
    private void startPeriodicRefresh() {
        android.os.Handler handler = new android.os.Handler();
        Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // ✅ Only refresh if activity is visible and not finishing
                if (!isFinishing() && !isDestroyed()) {
                    android.util.Log.d("ViewAllReports", "🔄 Periodic refresh triggered");
                    loadReportsQuietly(); // Use quiet refresh to prevent UI flicker
                }
                handler.postDelayed(this, 30000); // ✅ Refresh every 30 seconds
            }
        };
        handler.postDelayed(refreshRunnable, 30000);
    }
    
    private void loadReportsQuietly() {
        // ✅ Background refresh without UI disruption
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BlotterDatabase db = BlotterDatabase.getDatabase(this);
                List<BlotterReport> reports = db.blotterReportDao().getAllReports();
                
                // Filter by user role before comparing
                List<BlotterReport> filteredByUser = new ArrayList<>();
                for (BlotterReport report : reports) {
                    if (shouldShowReport(report)) {
                        filteredByUser.add(report);
                    }
                }
                
                // ✅ Deep comparison: check if data actually changed
                boolean dataChanged = hasDataChanged(filteredByUser, allReports);
                
                if (dataChanged) {
                    runOnUiThread(() -> {
                        allReports.clear();
                        allReports.addAll(filteredByUser);
                        filterReports();
                        updateStatistics();
                        android.util.Log.d("ViewAllReports", "✅ Quiet refresh - UI updated");
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("ViewAllReports", "❌ Error in quiet refresh: " + e.getMessage());
            }
        });
    }
    
    private boolean hasDataChanged(List<BlotterReport> newReports, List<BlotterReport> oldReports) {
        if (newReports.size() != oldReports.size()) {
            android.util.Log.d("ViewAllReports", "📊 Data changed: Different report count");
            return true;
        }
        
        // Deep comparison: check if data actually changed (not just size)
        for (int i = 0; i < newReports.size(); i++) {
            BlotterReport newReport = newReports.get(i);
            BlotterReport oldReport = oldReports.get(i);
            
            // Compare key fields that affect UI
            if (newReport.getId() != oldReport.getId() ||
                !equals(newReport.getStatus(), oldReport.getStatus()) ||
                !equals(newReport.getCaseNumber(), oldReport.getCaseNumber()) ||
                newReport.getDateFiled() != oldReport.getDateFiled() ||
                !equals(newReport.getIncidentType(), oldReport.getIncidentType())) {
                
                android.util.Log.d("ViewAllReports", "📊 Data changed: Report content modified");
                return true;
            }
        }
        
        android.util.Log.d("ViewAllReports", "📊 No data changes detected");
        return false;
    }
    
    // Helper method for null-safe string comparison
    private boolean equals(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equals(str2);
    }
    
    // Helper method for user role filtering
    private boolean shouldShowReport(BlotterReport report) {
        String userRole = preferencesManager.getUserRole();
        
        if ("Admin".equalsIgnoreCase(userRole)) {
            return true;
        } else if ("Officer".equalsIgnoreCase(userRole)) {
            // Officer sees assigned reports
            return isReportAssignedToOfficer(report);
        } else {
            // User sees only their own reports
            return report.getReportedById() == userId;
        }
    }
    
    private boolean isReportAssignedToOfficer(BlotterReport report) {
        if (report.getAssignedOfficerId() != null && report.getAssignedOfficerId().intValue() == userId) {
            return true;
        }
        
        if (report.getAssignedOfficerIds() != null && !report.getAssignedOfficerIds().isEmpty()) {
            String[] officerIds = report.getAssignedOfficerIds().split(",");
            for (String id : officerIds) {
                try {
                    if (Integer.parseInt(id.trim()) == userId) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        
        return false;
    }
    
    private void updateEmptyState() {
        if (filteredReports.isEmpty()) {
            if (emptyStateCard != null) emptyStateCard.setVisibility(View.VISIBLE);
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            if (recyclerReports != null) recyclerReports.setVisibility(View.GONE);
        } else {
            if (emptyStateCard != null) emptyStateCard.setVisibility(View.VISIBLE);
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            if (recyclerReports != null) recyclerReports.setVisibility(View.VISIBLE);
        }
    }
    
    private void showSortDialog() {
        String[] sortOptions = {"Newest First", "Oldest First"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle("Sort Reports")
            .setSingleChoiceItems(sortOptions, currentSort.equals("Newest First") ? 0 : 1, 
                (dialog, which) -> {
                    currentSort = sortOptions[which];
                    filterReports();
                    dialog.dismiss();
                })
            .setNegativeButton("Cancel", null);
        
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }
        dialog.show();
    }
    
    private void showErrorState() {
        if (emptyStateCard != null) emptyStateCard.setVisibility(View.VISIBLE);
        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        if (emptyStateTitle != null) emptyStateTitle.setText("Error Loading");
        if (emptyStateMessage != null) emptyStateMessage.setText("Please try again or\ncontact support if issue persists.");
    }
}