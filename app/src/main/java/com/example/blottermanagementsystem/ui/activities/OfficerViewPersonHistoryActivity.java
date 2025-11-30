package com.example.blottermanagementsystem.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.PersonHistory;
import com.example.blottermanagementsystem.services.PersonHistoryService;
// ❌ REMOVED: import com.example.blottermanagementsystem.services.SyncManager; (Pure online mode)
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class OfficerViewPersonHistoryActivity extends BaseActivity {
    
    private static final String EXTRA_PERSON_ID = "person_id";
    private static final String EXTRA_PERSON_NAME = "person_name";
    
    private static final String TAG = "OfficerViewPersonHistory";
    
    private Toolbar toolbar;
    private TextView tvPersonName, tvTotalCases, tvSuspectCount, tvRespondentCount;
    private RecyclerView recyclerHistory;
    private LinearLayout emptyState;
    private Chip chipAll, chipSuspect, chipRespondent;
    private FloatingActionButton fabAddRecord, fabSync;
    
    private BlotterDatabase database;
    private PersonHistoryService personHistoryService;
    // ❌ REMOVED: private SyncManager syncManager; (Pure online mode)
    private int personId;
    private String personName;
    private List<PersonHistory> allHistory = new ArrayList<>();
    private List<PersonHistory> filteredHistory = new ArrayList<>();
    private String currentFilter = "All";
    private String currentUserId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_officer_view_person_history);
        
        try {
            database = BlotterDatabase.getDatabase(this);
            personHistoryService = new PersonHistoryService(this);
            currentUserId = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userId", "");
            
            // Get person data from intent
            personId = getIntent().getIntExtra(EXTRA_PERSON_ID, -1);
            personName = getIntent().getStringExtra(EXTRA_PERSON_NAME);
            
            if (personId == -1) {
                Log.e(TAG, "Invalid person ID");
                finish();
                return;
            }
            
            initializeViews();
            setupToolbar();
            setupListeners();
            loadPersonHistory();
            syncWithCloud();
            
            Log.d(TAG, "✅ Activity created for person: " + personName);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
        }
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvPersonName = findViewById(R.id.tvPersonName);
        tvTotalCases = findViewById(R.id.tvTotalCases);
        tvSuspectCount = findViewById(R.id.tvSuspectCount);
        tvRespondentCount = findViewById(R.id.tvRespondentCount);
        recyclerHistory = findViewById(R.id.recyclerHistory);
        emptyState = findViewById(R.id.emptyState);
        chipAll = findViewById(R.id.chipAll);
        chipSuspect = findViewById(R.id.chipSuspect);
        chipRespondent = findViewById(R.id.chipRespondent);
        
        // Setup RecyclerView
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        
        // Set person name
        tvPersonName.setText(personName != null ? personName : "Unknown Person");
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    private void setupListeners() {
        try {
            chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentFilter = "All";
                    filterAndDisplayHistory();
                }
            });
            
            chipSuspect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentFilter = "Suspect";
                    filterAndDisplayHistory();
                }
            });
            
            chipRespondent.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentFilter = "Respondent";
                    filterAndDisplayHistory();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners: " + e.getMessage());
        }
    }
    
    private void loadPersonHistory() {
        try {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    List<PersonHistory> history = database.personHistoryDao().getHistoryByPersonId(personId);
                    
                    runOnUiThread(() -> {
                        allHistory.clear();
                        allHistory.addAll(history);
                        updateStatistics();
                        filterAndDisplayHistory();
                    });
                } catch (Exception e) {
                    android.util.Log.e("OfficerViewPersonHistory", "Error loading history: " + e.getMessage());
                    runOnUiThread(this::updateEmptyState);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("OfficerViewPersonHistory", "Error in loadPersonHistory: " + e.getMessage());
        }
    }
    
    private void updateStatistics() {
        try {
            int totalCases = allHistory.size();
            int suspectCount = 0;
            int respondentCount = 0;
            
            for (PersonHistory history : allHistory) {
                String activityType = history.getActivityType();
                if (activityType != null) {
                    if (activityType.contains("Suspect")) {
                        suspectCount++;
                    } else if (activityType.contains("Respondent")) {
                        respondentCount++;
                    }
                }
            }
            
            tvTotalCases.setText(String.valueOf(totalCases));
            tvSuspectCount.setText(String.valueOf(suspectCount));
            tvRespondentCount.setText(String.valueOf(respondentCount));
            
            android.util.Log.d("OfficerViewPersonHistory", "Stats - Total: " + totalCases + ", Suspect: " + suspectCount + ", Respondent: " + respondentCount);
        } catch (Exception e) {
            android.util.Log.e("OfficerViewPersonHistory", "Error updating statistics: " + e.getMessage());
        }
    }
    
    private void filterAndDisplayHistory() {
        try {
            filteredHistory.clear();
            
            for (PersonHistory history : allHistory) {
                if (currentFilter.equals("All")) {
                    filteredHistory.add(history);
                } else if (currentFilter.equals("Suspect") && history.getActivityType() != null && history.getActivityType().contains("Suspect")) {
                    filteredHistory.add(history);
                } else if (currentFilter.equals("Respondent") && history.getActivityType() != null && history.getActivityType().contains("Respondent")) {
                    filteredHistory.add(history);
                }
            }
            
            updateEmptyState();
        } catch (Exception e) {
            android.util.Log.e("OfficerViewPersonHistory", "Error filtering history: " + e.getMessage());
        }
    }
    
    private void updateEmptyState() {
        try {
            if (filteredHistory.isEmpty()) {
                recyclerHistory.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                recyclerHistory.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating empty state: " + e.getMessage());
        }
    }
    
    // ============ CLOUD SYNC METHODS ============
    
    private void syncWithCloud() {
        Log.d(TAG, "☁️ Syncing with cloud for person: " + personName);
        Toast.makeText(this, "Syncing with cloud...", Toast.LENGTH_SHORT).show();
        
        personHistoryService.getPersonCompleteHistory(personName, new PersonHistoryService.CompleteHistoryCallback() {
            @Override
            public void onSuccess(Map<String, Object> history) {
                Log.d(TAG, "✅ Cloud sync successful");
                Toast.makeText(OfficerViewPersonHistoryActivity.this, "✅ Sync complete", Toast.LENGTH_SHORT).show();
                loadPersonHistory();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Cloud sync error: " + error);
                Toast.makeText(OfficerViewPersonHistoryActivity.this, "Sync failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showAddRecordDialog() {
        Log.d(TAG, "➕ Showing add record dialog");
        Toast.makeText(this, "Add Record feature coming soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement add record dialog
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "📋 Activity resumed - refreshing data");
        loadPersonHistory();
    }
}
