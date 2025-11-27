package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.ui.adapters.ReportAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ReportOversightActivity extends BaseActivity {
    
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilters;
    private Chip chipAll, chipPending, chipActive, chipResolved;
    private RecyclerView recyclerReports;
    private android.widget.LinearLayout emptyState;
    private androidx.cardview.widget.CardView emptyStateCard;
    
    private BlotterDatabase database;
    private List<BlotterReport> reportsList = new ArrayList<>();
    private ReportAdapter adapter;
    private String currentFilter = "All";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_report_list);
            
            database = BlotterDatabase.getDatabase(this);
            
            // Check if filter was passed from dashboard
            String filter = getIntent().getStringExtra("filter");
            if (filter != null) {
                if (filter.equals("active")) {
                    currentFilter = "Active";
                } else if (filter.equals("archived")) {
                    currentFilter = "Resolved";
                }
            }
            
            setupToolbar();
            initViews();
            setupRecyclerView();
            setupListeners();
            loadReports();
            startPeriodicRefresh();
        } catch (Exception e) {
            android.util.Log.e("ReportOversight", "Error in onCreate: " + e.getMessage(), e);
            android.widget.Toast.makeText(this, "Error loading screen: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Report Oversight");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void initViews() {
        try {
            etSearch = findViewById(R.id.etSearch);
            recyclerReports = findViewById(R.id.recyclerViewReports);
            emptyState = findViewById(R.id.emptyState);
            emptyStateCard = findViewById(R.id.emptyStateCard);
            chipGroupFilters = findViewById(R.id.chipGroupFilters);
            chipAll = findViewById(R.id.chipAll);
            chipPending = findViewById(R.id.chipPending);
            chipActive = findViewById(R.id.chipInProgress);
            chipResolved = findViewById(R.id.chipResolved);
            
        } catch (Exception e) {
            android.util.Log.e("ReportOversight", "Error in initViews: " + e.getMessage(), e);
            throw e;
        }
    }
    
    private void setupRecyclerView() {
        try {
            if (recyclerReports == null) {
                android.util.Log.e("ReportOversight", "recyclerReports is null in setupRecyclerView!");
                return;
            }
            adapter = new ReportAdapter(reportsList, report -> {
                Intent intent = new Intent(this, ReportDetailActivity.class);
                intent.putExtra("REPORT_ID", report.getId());
                startActivity(intent);
            });
            recyclerReports.setLayoutManager(new LinearLayoutManager(this));
            recyclerReports.setAdapter(adapter);
        } catch (Exception e) {
            android.util.Log.e("ReportOversight", "Error in setupRecyclerView: " + e.getMessage(), e);
        }
    }
    
    private void setupListeners() {
        // Search functionality
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReports(s.toString());
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        // Filter chips navigate to admin report oversight screen
        if (chipAll != null) {
            chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Intent intent = new Intent(this, AdminReportOversightActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            });
        }
        
        if (chipPending != null) {
            chipPending.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Intent intent = new Intent(this, AdminReportOversightActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            });
        }
        
        if (chipActive != null) {
            chipActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Intent intent = new Intent(this, AdminReportOversightActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            });
        }
        
        if (chipResolved != null) {
            chipResolved.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Intent intent = new Intent(this, AdminReportOversightActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            });
        }
    }
    
    private void loadReports() {
        try {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    List<BlotterReport> reports = database.blotterReportDao().getAllReports();
                    
                    runOnUiThread(() -> {
                        try {
                            if (adapter == null) {
                                android.util.Log.e("ReportOversight", "adapter is null!");
                                return;
                            }
                            reportsList.clear();
                            reportsList.addAll(reports);
                            adapter.updateReports(reportsList);
                            
                            if (emptyStateCard != null) {
                                emptyStateCard.setVisibility(android.view.View.VISIBLE);
                            }
                            
                            if (emptyState != null && recyclerReports != null) {
                                if (reports.isEmpty()) {
                                    emptyState.setVisibility(android.view.View.VISIBLE);
                                    recyclerReports.setVisibility(android.view.View.GONE);
                                } else {
                                    emptyState.setVisibility(android.view.View.GONE);
                                    recyclerReports.setVisibility(android.view.View.VISIBLE);
                                }
                            }
                        } catch (Exception e) {
                            android.util.Log.e("ReportOversight", "Error updating UI: " + e.getMessage(), e);
                        }
                    });
                } catch (Exception e) {
                    android.util.Log.e("ReportOversight", "Error loading reports from DB: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("ReportOversight", "Error in loadReports: " + e.getMessage(), e);
        }
    }
    
    // Add periodic refresh for real-time updates (ANTI-FLICKER)
    private void startPeriodicRefresh() {
        android.os.Handler handler = new android.os.Handler();
        Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && !isDestroyed()) {
                    loadReportsQuietly(); // Use quiet refresh to prevent flickering
                }
                handler.postDelayed(this, 15000); // Refresh every 15 seconds
            }
        };
        handler.postDelayed(refreshRunnable, 15000);
    }
    
    // Quiet refresh method to prevent black flickering
    private void loadReportsQuietly() {
        new Thread(() -> {
            try {
                List<BlotterReport> reports = database.blotterReportDao().getAllReports();
                
                // Only update if data actually changed
                if (reports.size() != reportsList.size()) {
                    runOnUiThread(() -> {
                        reportsList.clear();
                        reportsList.addAll(reports);
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        updateEmptyState();
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("ReportOversight", "Error in quiet refresh: " + e.getMessage());
            }
        }).start();
    }
    
    // Update empty state method
    private void updateEmptyState() {
        if (reportsList.isEmpty()) {
            // Keep CardView visible, show empty state content
            if (emptyStateCard != null) emptyStateCard.setVisibility(android.view.View.VISIBLE);
            if (emptyState != null) emptyState.setVisibility(android.view.View.VISIBLE);
            if (recyclerReports != null) recyclerReports.setVisibility(android.view.View.GONE);
        } else {
            // Keep CardView visible, hide empty state content
            if (emptyStateCard != null) emptyStateCard.setVisibility(android.view.View.VISIBLE);
            if (emptyState != null) emptyState.setVisibility(android.view.View.GONE);
            if (recyclerReports != null) recyclerReports.setVisibility(android.view.View.VISIBLE);
        }
    }
    
    // Add method for filtering reports
    private void filterReports(String query) {
        // Implementation for search filtering
        if (adapter != null) {
            // Filter logic can be added here if needed
            loadReports(); // For now, just reload
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadReports();
    }
}
