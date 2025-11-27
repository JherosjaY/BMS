package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.ui.adapters.BlotterReportAdapter;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class OfficerMyCasesActivity extends BaseActivity {
    
    private RecyclerView recyclerView;
    private BlotterReportAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvTotalCases;
    private LinearLayout emptyStateCard;
    private SearchView searchView;
    private ChipGroup chipGroupFilter;
    private ImageButton btnBack;
    private Chip chipAll, chipPending, chipOngoing, chipResolved;
    
    // Statistics TextViews
    private TextView tvTotalCount, tvPendingCount, tvOngoingCount, tvResolvedCount;
    
    private BlotterDatabase database;
    private PreferencesManager preferencesManager;
    private List<BlotterReport> allCases = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_view_all_reports_new);
        
        database = BlotterDatabase.getDatabase(this);
        preferencesManager = new PreferencesManager(this);
        
        initViews();
        setupRecyclerView();
        setupListeners();
        loadMyCases();
        startPeriodicRefresh();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerReports);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        tvTotalCases = findViewById(R.id.tvTotalCount);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        btnBack = findViewById(R.id.btnBack);
        searchView = findViewById(R.id.etSearch);
        progressBar = findViewById(R.id.progressBar);
        
        // Initialize individual chips
        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipOngoing = findViewById(R.id.chipOngoing);
        chipResolved = findViewById(R.id.chipResolved);
        
        // Initialize statistics TextViews
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvOngoingCount = findViewById(R.id.tvOngoingCount);
        tvResolvedCount = findViewById(R.id.tvResolvedCount);
        
        // Log for debugging
        android.util.Log.d("OfficerMyCases", "recyclerView: " + (recyclerView != null ? "OK" : "NULL"));
        android.util.Log.d("OfficerMyCases", "emptyStateCard: " + (emptyStateCard != null ? "OK" : "NULL"));
        android.util.Log.d("OfficerMyCases", "tvTotalCases: " + (tvTotalCases != null ? "OK" : "NULL"));
        android.util.Log.d("OfficerMyCases", "chipGroupFilter: " + (chipGroupFilter != null ? "OK" : "NULL"));
        android.util.Log.d("OfficerMyCases", "chipAll: " + (chipAll != null ? "OK" : "NULL"));
        android.util.Log.d("OfficerMyCases", "chipPending: " + (chipPending != null ? "OK" : "NULL"));
        android.util.Log.d("OfficerMyCases", "chipOngoing: " + (chipOngoing != null ? "OK" : "NULL"));
        android.util.Log.d("OfficerMyCases", "chipResolved: " + (chipResolved != null ? "OK" : "NULL"));
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BlotterReportAdapter(new ArrayList<>(), report -> {
            // Open officer case detail activity (VIEW-ONLY with officer functions)
            Intent intent = new Intent(this, OfficerCaseDetailActivity.class);
            intent.putExtra("reportId", report.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // ✅ Just finish this activity - system will go back to Officer Dashboard
                finish();
            });
        }
        
        // Search functionality
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterCases(query);
                    return true;
                }
                
                @Override
                public boolean onQueryTextChange(String newText) {
                    filterCases(newText);
                    return true;
                }
            });
        }
        
        // Filter chips - Use individual OnCheckedChangeListener for each chip (WORKING PATTERN)
        if (chipAll != null) {
            chipAll.setChecked(true); // Set initial selection
            chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    android.util.Log.d("OfficerMyCases", "All chip selected");
                    applyFilter();
                    updateFilterCounts();
                }
            });
        }
        
        if (chipPending != null) {
            chipPending.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    android.util.Log.d("OfficerMyCases", "Assigned chip selected");
                    applyFilter();
                    updateFilterCounts();
                }
            });
        }
        
        if (chipOngoing != null) {
            chipOngoing.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    android.util.Log.d("OfficerMyCases", "Ongoing chip selected");
                    applyFilter();
                    updateFilterCounts();
                }
            });
        }
        
        if (chipResolved != null) {
            chipResolved.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    android.util.Log.d("OfficerMyCases", "Resolved chip selected");
                    applyFilter();
                    updateFilterCounts();
                }
            });
        }
    }
    
    private void loadMyCases() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyStateCard != null) emptyStateCard.setVisibility(View.GONE);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                int userId = preferencesManager.getUserId();
                // Get the officer record for this user
                com.example.blottermanagementsystem.data.entity.Officer officer = database.officerDao().getOfficerByUserId(userId);
                int officerId = (officer != null) ? officer.getId() : -1;
                
                List<BlotterReport> reports = database.blotterReportDao().getAllReports();
                
                android.util.Log.d("OfficerMyCases", "Loading cases for user ID: " + userId + ", Officer ID: " + officerId);
                android.util.Log.d("OfficerMyCases", "Total reports in database: " + reports.size());
                
                // Filter only officer's assigned cases
                allCases.clear();
                for (BlotterReport report : reports) {
                    // Check if officer is assigned (either single or multiple officers)
                    boolean isAssignedToOfficer = false;
                    
                    // Check single officer assignment
                    if (report.getAssignedOfficerId() != null && report.getAssignedOfficerId().intValue() == officerId) {
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
                        allCases.add(report);
                        android.util.Log.d("OfficerMyCases", "Found assigned case: " + report.getCaseNumber());
                    }
                }
                
                android.util.Log.d("OfficerMyCases", "Total assigned cases: " + allCases.size());
                
                runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    
                    // Background CardView always stays visible
                    // Only toggle between empty state content and recycler view
                    if (allCases.isEmpty()) {
                        if (emptyStateCard != null) emptyStateCard.setVisibility(View.VISIBLE);
                        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
                        if (tvTotalCases != null) tvTotalCases.setText("0 Cases");
                    } else {
                        if (emptyStateCard != null) emptyStateCard.setVisibility(View.GONE);
                        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
                        if (tvTotalCases != null) tvTotalCases.setText(allCases.size() + " Cases");
                        updateStatistics();
                        updateFilterCounts();
                        applyFilter();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("OfficerMyCases", "Error loading cases: " + e.getMessage(), e);
            }
        });
    }
    
    private void updateStatistics() {
        // Count cases by status
        int totalCount = allCases.size();
        int assignedCount = 0;
        int ongoingCount = 0;
        int resolvedCount = 0;
        
        for (BlotterReport report : allCases) {
            String status = report.getStatus();
            if (status != null) {
                if ("Assigned".equals(status)) {
                    assignedCount++;
                } else if ("Ongoing".equals(status) || "In Progress".equals(status)) {
                    ongoingCount++;
                } else if ("Resolved".equals(status) || "Closed".equals(status)) {
                    resolvedCount++;
                }
            }
        }
        
        // Update statistics display (if you have TextViews for these)
        android.util.Log.d("OfficerMyCases", "Statistics - Total: " + totalCount + 
            ", Assigned: " + assignedCount + ", Ongoing: " + ongoingCount + ", Resolved: " + resolvedCount);
    }
    
    
    private void updateFilterCounts() {
        // Count cases by status for display on chips
        int totalCount = allCases.size();
        int assignedCount = 0;
        int ongoingCount = 0;
        int resolvedCount = 0;
        
        for (BlotterReport report : allCases) {
            String status = report.getStatus();
            if (status != null) {
                if ("Assigned".equals(status)) {
                    assignedCount++;
                } else if ("Ongoing".equals(status) || "In Progress".equals(status)) {
                    ongoingCount++;
                } else if ("Resolved".equals(status) || "Closed".equals(status)) {
                    resolvedCount++;
                }
            }
        }
        
        // Update statistics display
        if (tvTotalCount != null) tvTotalCount.setText(String.valueOf(totalCount));
        if (tvPendingCount != null) tvPendingCount.setText(String.valueOf(assignedCount));
        if (tvOngoingCount != null) tvOngoingCount.setText(String.valueOf(ongoingCount));
        if (tvResolvedCount != null) tvResolvedCount.setText(String.valueOf(resolvedCount));
        
        android.util.Log.d("OfficerMyCases", "Filter counts updated - Total: " + totalCount + 
            ", Assigned: " + assignedCount + ", Ongoing: " + ongoingCount + ", Resolved: " + resolvedCount);
    }
    
    private void filterCases(String query) {
        if (query.isEmpty()) {
            applyFilter();
            return;
        }
        
        // Dynamically determine selected filter by checking which chip is checked (WORKING PATTERN)
        String selectedFilter = "All";
        if (chipPending != null && chipPending.isChecked()) selectedFilter = "Assigned";
        else if (chipOngoing != null && chipOngoing.isChecked()) selectedFilter = "Ongoing";
        else if (chipResolved != null && chipResolved.isChecked()) selectedFilter = "Resolved";
        
        List<BlotterReport> filtered = new ArrayList<>();
        
        for (BlotterReport report : allCases) {
            String status = report.getStatus() != null ? report.getStatus().toUpperCase() : "";
            boolean matchesFilter = false;
            
            // Apply status filter first
            if ("All".equals(selectedFilter)) {
                matchesFilter = true;
            } else if ("Assigned".equals(selectedFilter) && "ASSIGNED".equals(status)) {
                matchesFilter = true;
            } else if ("Ongoing".equals(selectedFilter) && 
                       ("ONGOING".equals(status) || "IN PROGRESS".equals(status))) {
                matchesFilter = true;
            } else if ("Resolved".equals(selectedFilter) && 
                       ("RESOLVED".equals(status) || "CLOSED".equals(status))) {
                matchesFilter = true;
            }
            
            // Then apply search query
            boolean matchesSearch = (report.getCaseNumber() != null && report.getCaseNumber().toLowerCase().contains(query.toLowerCase())) ||
                                   (report.getIncidentType() != null && report.getIncidentType().toLowerCase().contains(query.toLowerCase())) ||
                                   (report.getNarrative() != null && report.getNarrative().toLowerCase().contains(query.toLowerCase()));
            
            if (matchesFilter && matchesSearch) {
                filtered.add(report);
            }
        }
        
        // Update UI
        if (filtered.isEmpty()) {
            if (emptyStateCard != null) emptyStateCard.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            if (tvTotalCases != null) tvTotalCases.setText("0 Cases");
        } else {
            if (emptyStateCard != null) emptyStateCard.setVisibility(View.GONE);
            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
            if (adapter != null) adapter.updateReports(filtered);
            if (tvTotalCases != null) tvTotalCases.setText(filtered.size() + " Cases");
        }
    }
    
    private void applyFilter() {
        // ✅ CLEAN REWRITE - Determine which chip is selected
        FilterType filterType = getSelectedFilterType();
        
        // ✅ Filter cases based on selected chip
        List<BlotterReport> filtered = filterCasesByType(filterType);
        
        // ✅ Update UI with filtered results
        updateUIWithFilteredCases(filtered);
    }
    
    /**
     * ✅ Determine which filter chip is currently selected
     */
    private FilterType getSelectedFilterType() {
        if (chipAll != null && chipAll.isChecked()) {
            return FilterType.ALL;
        } else if (chipPending != null && chipPending.isChecked()) {
            return FilterType.ASSIGNED;
        } else if (chipOngoing != null && chipOngoing.isChecked()) {
            return FilterType.ONGOING;
        } else if (chipResolved != null && chipResolved.isChecked()) {
            return FilterType.RESOLVED;
        }
        // Default to ALL if nothing is checked
        return FilterType.ALL;
    }
    
    /**
     * ✅ Filter cases based on the selected filter type
     */
    private List<BlotterReport> filterCasesByType(FilterType filterType) {
        List<BlotterReport> filtered = new ArrayList<>();
        
        android.util.Log.d("OfficerMyCases", "🔍 Filtering by: " + filterType.name());
        
        for (BlotterReport report : allCases) {
            if (matchesFilter(report, filterType)) {
                filtered.add(report);
            }
        }
        
        android.util.Log.d("OfficerMyCases", "✅ Filtered result: " + filtered.size() + " cases");
        return filtered;
    }
    
    /**
     * ✅ Check if a single case matches the filter criteria
     */
    private boolean matchesFilter(BlotterReport report, FilterType filterType) {
        String status = report.getStatus() != null ? report.getStatus().toUpperCase() : "";
        
        switch (filterType) {
            case ALL:
                // Show all cases
                return true;
                
            case ASSIGNED:
                // Show only ASSIGNED status
                return "ASSIGNED".equals(status);
                
            case ONGOING:
                // Show ONGOING or IN PROGRESS status
                return "ONGOING".equals(status) || "IN PROGRESS".equals(status);
                
            case RESOLVED:
                // Show RESOLVED or CLOSED status
                return "RESOLVED".equals(status) || "CLOSED".equals(status);
                
            default:
                return false;
        }
    }
    
    /**
     * ✅ Update UI with filtered cases
     */
    private void updateUIWithFilteredCases(List<BlotterReport> filtered) {
        if (filtered.isEmpty()) {
            // Show empty state
            if (emptyStateCard != null) emptyStateCard.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            if (tvTotalCases != null) tvTotalCases.setText("0 Cases");
        } else {
            // Show cases
            if (emptyStateCard != null) emptyStateCard.setVisibility(View.GONE);
            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
            if (adapter != null) adapter.updateReports(filtered);
            if (tvTotalCases != null) tvTotalCases.setText(filtered.size() + " Cases");
        }
    }
    
    /**
     * ✅ Enum for filter types
     */
    private enum FilterType {
        ALL,
        ASSIGNED,
        ONGOING,
        RESOLVED
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadMyCases();
    }
    
    // Add method to refresh data when status changes
    public void refreshData() {
        loadMyCases();
    }
    
    // Add periodic refresh for real-time updates
    private void startPeriodicRefresh() {
        android.os.Handler handler = new android.os.Handler();
        Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Show subtle loading indicator
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                
                // Silent background refresh - no toast messages
                loadMyCases();
                
                handler.postDelayed(this, 15000); // Refresh every 15 seconds (faster)
            }
        };
        handler.postDelayed(refreshRunnable, 15000);
    }
    
    private void navigateToChipActivity(String chipText) {
        Intent intent;
        
        switch (chipText) {
            case "All":
                // Stay on current activity or navigate to OfficerViewAllReportsActivity
                intent = new Intent(this, OfficerViewAllReportsActivity_New.class);
                break;
            case "Pending":
                intent = new Intent(this, OfficerViewAssignedReportsActivity_New.class);
                break;
            case "Active":
                intent = new Intent(this, OfficerViewOngoingReportsActivity_New.class);
                break;
            case "Resolved":
                intent = new Intent(this, OfficerViewResolvedReportsActivity_New.class);
                break;
            default:
                return;
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
