package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.Hearing;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

public class ViewUpcomingHearingsActivity extends BaseActivity {
    
    private RecyclerView recyclerHearings;
    private View emptyState;
    private androidx.cardview.widget.CardView emptyStateCard;
    private Chip chipAll, chipUpcoming, chipCompleted;
    private EditText etSearch;
    private ImageView emptyStateIcon;
    private TextView emptyStateTitle, emptyStateMessage;
    
    private List<Hearing> allHearings = new ArrayList<>();
    private List<Hearing> filteredHearings = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_view_upcoming_hearings);
            
            initializeViews();
            setupToolbar();
            setupChips();
            setupSearch();
            loadHearings();
            startPeriodicRefresh();
            
        } catch (Exception e) {
            android.util.Log.e("ViewUpcomingHearings", "Error in onCreate: " + e.getMessage(), e);
            android.widget.Toast.makeText(this, "Error loading hearings screen: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            // Show error state instead of finishing to prevent black flicker
            if (emptyStateCard != null) { emptyStateCard.setVisibility(View.VISIBLE); }
            if (emptyState != null) { emptyState.setVisibility(View.VISIBLE); }
            if (emptyStateTitle != null) { emptyStateTitle.setText("Error Loading"); }
            if (emptyStateMessage != null) { emptyStateMessage.setText("Please try again or\\ncontact support if issue persists."); }
        }
    }
    
    private void initializeViews() {
        recyclerHearings = findViewById(R.id.recyclerHearings);
        emptyState = findViewById(R.id.emptyState);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        chipAll = findViewById(R.id.chipAll);
        chipUpcoming = findViewById(R.id.chipUpcoming);
        chipCompleted = findViewById(R.id.chipCompleted);
        etSearch = findViewById(R.id.etSearch);
        emptyStateIcon = findViewById(R.id.emptyStateIcon);
        emptyStateTitle = findViewById(R.id.emptyStateTitle);
        emptyStateMessage = findViewById(R.id.emptyStateMessage);
        
        // Setup RecyclerView
        if (recyclerHearings != null) {
            recyclerHearings.setLayoutManager(new LinearLayoutManager(this));
        }
    }
    
    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Upcoming Hearings");
            }
            toolbar.setNavigationOnClickListener(v -> {
                android.util.Log.d("ViewUpcomingHearings", "Back button clicked");
                finish();
            });
        } else {
            android.util.Log.e("ViewUpcomingHearings", "Toolbar not found!");
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        android.util.Log.d("ViewUpcomingHearings", "onSupportNavigateUp called");
        finish();
        return true;
    }
    
    private void setupChips() {
        // Upcoming chip is selected by default
        if (chipUpcoming != null) {
            chipUpcoming.setChecked(true);
            chipUpcoming.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Already on Upcoming screen, no navigation needed
                    android.util.Log.d("ViewUpcomingHearings", "Upcoming chip selected - staying on current screen");
                }
            });
        }
        
        if (chipAll != null) {
            chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    navigateToScreen(ViewAllHearingsActivity.class);
                }
            });
        }
        
        if (chipCompleted != null) {
            chipCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    navigateToScreen(ViewCompletedHearingsActivity.class);
                }
            });
        }
    }
    
    private void navigateToScreen(Class<?> activityClass) {
        android.util.Log.d("ViewUpcomingHearings", "Navigating to: " + activityClass.getSimpleName());
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
    
    private void setupSearch() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterHearings(s.toString());
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }
    
    private void loadHearings() {
        android.util.Log.d("ViewUpcomingHearings", "Loading upcoming hearings");
        
        new Thread(() -> {
            try {
                BlotterDatabase db = BlotterDatabase.getDatabase(this);
                List<Hearing> hearings = db.hearingDao().getUpcomingHearings();
                
                runOnUiThread(() -> {
                    allHearings.clear();
                    allHearings.addAll(hearings);
                    filterHearings("");
                    android.util.Log.d("ViewUpcomingHearings", "Loaded " + hearings.size() + " upcoming hearings");
                });
            } catch (Exception e) {
                android.util.Log.e("ViewUpcomingHearings", "Error loading hearings: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "Error loading hearings", android.widget.Toast.LENGTH_SHORT).show();
                    filterHearings("");
                });
            }
        }).start();
    }
    
    private void filterHearings(String searchQuery) {
        filteredHearings.clear();
        
        if (searchQuery.isEmpty()) {
            filteredHearings.addAll(allHearings);
        } else {
            for (Hearing hearing : allHearings) {
                if ((hearing.getTitle() != null && hearing.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (hearing.getPurpose() != null && hearing.getPurpose().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (hearing.getLocation() != null && hearing.getLocation().toLowerCase().contains(searchQuery.toLowerCase()))) {
                    filteredHearings.add(hearing);
                }
            }
        }
        
        updateEmptyState();
    }
    
    private void updateEmptyState() {
        if (filteredHearings.isEmpty()) {
            if (emptyStateCard != null) emptyStateCard.setVisibility(View.VISIBLE);
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            if (recyclerHearings != null) recyclerHearings.setVisibility(View.GONE);
            
            // Update empty state content for Upcoming hearings
            if (emptyStateIcon != null) {
                emptyStateIcon.setImageResource(R.drawable.ic_clock_filled);
            }
            if (emptyStateTitle != null) {
                emptyStateTitle.setText("No Upcoming Hearings");
            }
            if (emptyStateMessage != null) {
                emptyStateMessage.setText("No hearings are scheduled\\nfor the near future.");
            }
        } else {
            // Keep CardView visible, only hide empty state content
            if (emptyStateCard != null) emptyStateCard.setVisibility(View.VISIBLE);
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            if (recyclerHearings != null) recyclerHearings.setVisibility(View.VISIBLE);
        }
    }
    
    private void startPeriodicRefresh() {
        android.os.Handler handler = new android.os.Handler();
        Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Only refresh if activity is visible and not finishing
                if (!isFinishing() && !isDestroyed()) {
                    loadHearingsQuietly(); // Use quiet refresh to prevent flicker
                }
                handler.postDelayed(this, 15000);
            }
        };
        handler.postDelayed(refreshRunnable, 15000);
    }
    
    private void loadHearingsQuietly() {
        // Background refresh without UI disruption
        new Thread(() -> {
            try {
                BlotterDatabase db = BlotterDatabase.getDatabase(this);
                List<Hearing> hearings = db.hearingDao().getUpcomingHearings();
                
                // Only update if data actually changed
                if (hearings.size() != allHearings.size()) {
                    runOnUiThread(() -> {
                        allHearings.clear();
                        allHearings.addAll(hearings);
                        filterHearings("");
                        updateEmptyState();
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("ViewUpcomingHearings", "Error in quiet refresh: " + e.getMessage());
            }
        }).start();
    }
}
