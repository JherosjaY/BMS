package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.google.android.material.chip.Chip;
import com.example.blottermanagementsystem.data.entity.Hearing;
import com.example.blottermanagementsystem.data.dao.HearingDao;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class OfficerViewCompletedHearingsActivity extends BaseActivity {
    
    private RecyclerView recyclerHearings;
    private View emptyState;
    private androidx.cardview.widget.CardView emptyStateCard;
    private Chip chipAll, chipUpcoming, chipCompleted, chipCanceled;
    private EditText etSearch;
    private ImageView emptyStateIcon;
    private TextView emptyStateTitle, emptyStateMessage;
    private List<Hearing> allHearings = new ArrayList<>();
    private List<Hearing> filteredHearings = new ArrayList<>();
    private PreferencesManager preferencesManager;
    private int officerId;
    private String searchQuery = "";
    private String currentFilter = "Completed";
    private HearingDao hearingDao;
    private BlotterDatabase database;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            android.util.Log.d("OfficerViewCompletedHearings", "Starting onCreate...");
            setContentView(R.layout.activity_officer_completed_hearings);
            
            preferencesManager = new PreferencesManager(this);
            officerId = preferencesManager.getUserId();
            database = BlotterDatabase.getDatabase(this);
            hearingDao = database.hearingDao();
            
            initializeViews();
            setupToolbar();
            setupListeners();
            loadHearings();
            startPeriodicRefresh();
            
            android.util.Log.d("OfficerViewCompletedHearings", "onCreate completed successfully");
        } catch (Exception e) {
            android.util.Log.e("OfficerViewCompletedHearings", "Error in onCreate: " + e.getMessage(), e);
            showErrorState();
        }
    }
    
    private void initializeViews() {
        try {
            recyclerHearings = findViewById(R.id.recyclerHearings);
            emptyState = findViewById(R.id.emptyState);
            emptyStateCard = findViewById(R.id.emptyStateCard);
            etSearch = findViewById(R.id.etSearch);
            chipAll = findViewById(R.id.chipAll);
            chipUpcoming = findViewById(R.id.chipUpcoming);
            chipCompleted = findViewById(R.id.chipCompleted);
            chipCanceled = findViewById(R.id.chipCanceled);
            emptyStateIcon = findViewById(R.id.emptyStateIcon);
            emptyStateTitle = findViewById(R.id.emptyStateTitle);
            emptyStateMessage = findViewById(R.id.emptyStateMessage);
            
            // Setup RecyclerView
            if (recyclerHearings != null) {
                recyclerHearings.setLayoutManager(new LinearLayoutManager(this));
            }
        } catch (Exception e) {
            android.util.Log.e("OfficerViewCompletedHearings", "Error initializing views: " + e.getMessage());
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
            // Search listener
            if (etSearch != null) {
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        searchQuery = s.toString().toLowerCase();
                        filterAndDisplayHearings();
                    }
                    
                    @Override
                    public void afterTextChanged(Editable s) {}
                });
            }
            
            // Chip listeners
            if (chipAll != null) {
                chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        currentFilter = "All";
                        filterAndDisplayHearings();
                    }
                });
            }
            
            if (chipUpcoming != null) {
                chipUpcoming.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        currentFilter = "Upcoming";
                        filterAndDisplayHearings();
                    }
                });
            }
            
            if (chipCompleted != null) {
                chipCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        currentFilter = "Completed";
                        filterAndDisplayHearings();
                    }
                });
            }
            
            if (chipCanceled != null) {
                chipCanceled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        currentFilter = "Canceled";
                        filterAndDisplayHearings();
                    }
                });
            }
        } catch (Exception e) {
            android.util.Log.e("OfficerViewCompletedHearings", "Error setting up listeners: " + e.getMessage());
        }
    }
    
    private void loadHearings() {
        try {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    List<Hearing> hearings = hearingDao.getCompletedHearings();
                    runOnUiThread(() -> {
                        allHearings.clear();
                        allHearings.addAll(hearings);
                        filterAndDisplayHearings();
                    });
                } catch (Exception e) {
                    android.util.Log.e("OfficerViewCompletedHearings", "Error loading hearings: " + e.getMessage());
                    runOnUiThread(this::updateEmptyState);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("OfficerViewCompletedHearings", "Error in loadHearings: " + e.getMessage());
            updateEmptyState();
        }
    }
    
    private void filterAndDisplayHearings() {
        try {
            filteredHearings.clear();
            
            // Filter by status
            for (Hearing hearing : allHearings) {
                if (currentFilter.equals("All") || currentFilter.equals("Completed")) {
                    filteredHearings.add(hearing);
                }
            }
            
            // Filter by search query
            if (!searchQuery.isEmpty()) {
                filteredHearings.removeIf(hearing -> {
                    String purpose = hearing.getPurpose() != null ? hearing.getPurpose().toLowerCase() : "";
                    String location = hearing.getLocation() != null ? hearing.getLocation().toLowerCase() : "";
                    return !purpose.contains(searchQuery) && !location.contains(searchQuery);
                });
            }
            
            updateEmptyState();
        } catch (Exception e) {
            android.util.Log.e("OfficerViewCompletedHearings", "Error filtering hearings: " + e.getMessage());
        }
    }
    
    private void updateEmptyState() {
        try {
            if (filteredHearings.isEmpty()) {
                if (recyclerHearings != null) {
                    recyclerHearings.setVisibility(View.GONE);
                }
                if (emptyState != null) {
                    emptyState.setVisibility(View.VISIBLE);
                }
                updateEmptyStateMessage();
            } else {
                if (recyclerHearings != null) {
                    recyclerHearings.setVisibility(View.VISIBLE);
                }
                if (emptyState != null) {
                    emptyState.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("OfficerViewCompletedHearings", "Error updating empty state: " + e.getMessage());
        }
    }
    
    private void updateEmptyStateMessage() {
        try {
            if (emptyStateTitle != null) {
                emptyStateTitle.setText("No Completed Hearings");
            }
            if (emptyStateMessage != null) {
                emptyStateMessage.setText("Finished hearings will appear here.");
            }
        } catch (Exception e) {
            android.util.Log.e("OfficerViewCompletedHearings", "Error updating empty state message: " + e.getMessage());
        }
    }
    
    private void startPeriodicRefresh() {
        // Periodic refresh every 15 seconds
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(15000);
                    loadHearings();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
    
    private void showErrorState() {
        try {
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
            }
            if (recyclerHearings != null) {
                recyclerHearings.setVisibility(View.GONE);
            }
            if (emptyStateTitle != null) {
                emptyStateTitle.setText("Error Loading Hearings");
            }
            if (emptyStateMessage != null) {
                emptyStateMessage.setText("Please try again later.");
            }
        } catch (Exception e) {
            android.util.Log.e("OfficerViewCompletedHearings", "Error showing error state: " + e.getMessage());
        }
    }
}
