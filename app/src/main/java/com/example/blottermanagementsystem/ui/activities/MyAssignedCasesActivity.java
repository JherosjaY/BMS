package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.data.entity.Officer;
import com.example.blottermanagementsystem.ui.adapters.ReportAdapter;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MyAssignedCasesActivity extends BaseActivity {
    
    private RecyclerView recyclerCases;
    private LinearLayout emptyState;
    private BlotterDatabase database;
    private PreferencesManager preferencesManager;
    private ReportAdapter adapter;
    private List<BlotterReport> casesList = new ArrayList<>();
    private int officerId = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assigned_cases);
        
        database = BlotterDatabase.getDatabase(this);
        preferencesManager = new PreferencesManager(this);
        
        setupToolbar();
        initViews();
        setupRecyclerView();
        
        // STEP 1: Get officer ID from logged-in user
        loadOfficerAndCases();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Assigned Cases");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void initViews() {
        recyclerCases = findViewById(R.id.recyclerReports);
        emptyState = findViewById(R.id.emptyState);
    }
    
    private void setupRecyclerView() {
        adapter = new ReportAdapter(casesList, report -> {
            Intent intent = new Intent(this, OfficerCaseDetailActivity.class);
            intent.putExtra("REPORT_ID", report.getId());
            startActivity(intent);
        });
        recyclerCases.setLayoutManager(new LinearLayoutManager(this));
        recyclerCases.setAdapter(adapter);
    }
    
    private void loadOfficerAndCases() {
        // Get USER ID from login session
        int userId = preferencesManager.getUserId();
        android.util.Log.d("MyAssignedCases", "👤 User ID from preferences: " + userId);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // STEP 2: Find officer record for this user
                Officer officer = database.officerDao().getOfficerByUserId(userId);
                
                if (officer != null) {
                    officerId = officer.getId();
                    android.util.Log.d("MyAssignedCases", "✅ Found Officer - ID: " + officerId + ", Name: " + officer.getName());
                    
                    // STEP 3: Now load cases assigned to this officer
                    loadAssignedCases();
                } else {
                    android.util.Log.e("MyAssignedCases", "❌ No officer found for user ID: " + userId);
                    runOnUiThread(() -> {
                        showEmptyState("Officer Not Found", "Your officer account is not properly set up.");
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("MyAssignedCases", "❌ Database error: " + e.getMessage());
                runOnUiThread(() -> {
                    showEmptyState("Error", "Failed to load officer data.");
                });
            }
        });
    }
    
    private void loadAssignedCases() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Get ALL reports from database
                List<BlotterReport> allReports = database.blotterReportDao().getAllReports();
                List<BlotterReport> assignedReports = new ArrayList<>();
                
                android.util.Log.d("MyAssignedCases", "🔍 Scanning " + allReports.size() + " total reports for officer ID: " + officerId);
                
                int singleAssignments = 0;
                int multipleAssignments = 0;
                
                // STEP 4: Check each report if assigned to current officer
                for (BlotterReport report : allReports) {
                    boolean isAssignedToMe = false;
                    String assignmentType = "NONE";
                    
                    // CHECK 1: Single officer assignment
                    if (report.getAssignedOfficerId() != null && report.getAssignedOfficerId().intValue() == officerId) {
                        isAssignedToMe = true;
                        assignmentType = "SINGLE";
                        singleAssignments++;
                        android.util.Log.d("MyAssignedCases", "   ✅ SINGLE: " + report.getCaseNumber() + " | Status: " + report.getStatus());
                    }
                    
                    // CHECK 2: Multiple officers assignment
                    if (!isAssignedToMe && report.getAssignedOfficerIds() != null && !report.getAssignedOfficerIds().isEmpty()) {
                        String[] officerIds = report.getAssignedOfficerIds().split(",");
                        android.util.Log.d("MyAssignedCases", "   🔍 MULTIPLE: " + report.getCaseNumber() + " | Officer IDs: " + report.getAssignedOfficerIds());
                        
                        for (String idStr : officerIds) {
                            try {
                                int assignedOfficerId = Integer.parseInt(idStr.trim());
                                if (assignedOfficerId == officerId) {
                                    isAssignedToMe = true;
                                    assignmentType = "MULTIPLE";
                                    multipleAssignments++;
                                    android.util.Log.d("MyAssignedCases", "   ✅ MULTIPLE: " + report.getCaseNumber() + " | Status: " + report.getStatus());
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                android.util.Log.w("MyAssignedCases", "   ⚠️ Invalid officer ID in multiple assignment: " + idStr);
                            }
                        }
                    }
                    
                    if (isAssignedToMe) {
                        assignedReports.add(report);
                        android.util.Log.d("MyAssignedCases", "   📌 ADDED: " + report.getCaseNumber() + " | Assignment: " + assignmentType + " | Status: " + report.getStatus());
                    }
                }
                
                android.util.Log.d("MyAssignedCases", "📊 ASSIGNMENT SUMMARY:");
                android.util.Log.d("MyAssignedCases", "   - Total reports scanned: " + allReports.size());
                android.util.Log.d("MyAssignedCases", "   - Single assignments: " + singleAssignments);
                android.util.Log.d("MyAssignedCases", "   - Multiple assignments: " + multipleAssignments);
                android.util.Log.d("MyAssignedCases", "   - Total assigned to me: " + assignedReports.size());
                
                // STEP 5: Update UI with results
                runOnUiThread(() -> {
                    casesList.clear();
                    casesList.addAll(assignedReports);
                    
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    
                    if (assignedReports.isEmpty()) {
                        showEmptyState("No Assigned Cases", "You don't have any assigned cases yet.");
                    } else {
                        hideEmptyState();
                    }
                });
                
            } catch (Exception e) {
                android.util.Log.e("MyAssignedCases", "❌ Error loading cases: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showEmptyState("Loading Error", "Failed to load assigned cases.");
                });
            }
        });
    }
    
    private void showEmptyState(String title, String message) {
        if (emptyState != null) {
            emptyState.setVisibility(android.view.View.VISIBLE);
        }
        if (recyclerCases != null) {
            recyclerCases.setVisibility(android.view.View.GONE);
        }
    }
    
    private void hideEmptyState() {
        if (emptyState != null) {
            emptyState.setVisibility(android.view.View.GONE);
        }
        if (recyclerCases != null) {
            recyclerCases.setVisibility(android.view.View.VISIBLE);
        }
    }
    
    // Quiet refresh without UI flicker
    private void loadAssignedCasesQuietly() {
        if (officerId == -1) {
            loadOfficerAndCases();
            return;
        }
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<BlotterReport> allReports = database.blotterReportDao().getAllReports();
                List<BlotterReport> assignedReports = new ArrayList<>();
                
                for (BlotterReport report : allReports) {
                    boolean isAssignedToMe = false;
                    
                    // Check both assignment types
                    if (report.getAssignedOfficerId() != null && report.getAssignedOfficerId().intValue() == officerId) {
                        isAssignedToMe = true;
                    }
                    
                    if (!isAssignedToMe && report.getAssignedOfficerIds() != null && !report.getAssignedOfficerIds().isEmpty()) {
                        String[] officerIds = report.getAssignedOfficerIds().split(",");
                        for (String idStr : officerIds) {
                            try {
                                if (Integer.parseInt(idStr.trim()) == officerId) {
                                    isAssignedToMe = true;
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                // Ignore invalid IDs
                            }
                        }
                    }
                    
                    if (isAssignedToMe) {
                        assignedReports.add(report);
                    }
                }
                
                // Only update UI if data changed
                if (assignedReports.size() != casesList.size()) {
                    runOnUiThread(() -> {
                        casesList.clear();
                        casesList.addAll(assignedReports);
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        
                        if (assignedReports.isEmpty()) {
                            showEmptyState("No Assigned Cases", "You don't have any assigned cases yet.");
                        } else {
                            hideEmptyState();
                        }
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("MyAssignedCases", "Quiet refresh error: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssignedCasesQuietly();
    }
}
