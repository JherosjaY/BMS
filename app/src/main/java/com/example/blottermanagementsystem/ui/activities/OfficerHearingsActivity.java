package com.example.blottermanagementsystem.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.Hearing;
import com.example.blottermanagementsystem.ui.adapters.HearingAdapter;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class OfficerHearingsActivity extends BaseActivity {
    
    private RecyclerView recyclerView;
    private HearingAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvTotalHearings;
    private LinearLayout emptyStateCard;
    private SearchView searchView;
    private ChipGroup chipGroupFilter;
    private ImageButton btnBack;
    
    private BlotterDatabase database;
    private PreferencesManager preferencesManager;
    private List<Hearing> allHearings = new ArrayList<>();
    private String currentFilter = "All";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_hearings);
        
        database = BlotterDatabase.getDatabase(this);
        preferencesManager = new PreferencesManager(this);
        
        initViews();
        setupRecyclerView();
        setupListeners();
        loadHearings();
        startPeriodicRefresh();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerHearings);
        progressBar = findViewById(R.id.progressBar);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        tvTotalHearings = findViewById(R.id.tvTotalHearings);
        searchView = findViewById(R.id.searchView);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HearingAdapter(hearing -> {
            // Open hearing detail - placeholder for now
            android.widget.Toast.makeText(this, "Hearing ID: " + hearing.getId(), android.widget.Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterHearings(query);
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                filterHearings(newText);
                return true;
            }
        });
        
        // Filter chips
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int chipId = checkedIds.get(0);
                Chip chip = findViewById(chipId);
                if (chip != null) {
                    currentFilter = chip.getText().toString();
                    // Refresh data from database when filter changes to ensure real-time updates
                    loadHearings();
                }
            }
        });
    }
    
    private void loadHearings() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateCard.setVisibility(View.GONE);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Hearing> hearings = database.hearingDao().getAllHearings();
            
            // Store all hearings for filtering
            allHearings.clear();
            allHearings.addAll(hearings);
            
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                
                // Background CardView always stays visible
                // Only toggle between empty state content and recycler view
                if (allHearings.isEmpty()) {
                    emptyStateCard.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    tvTotalHearings.setText("0 Hearings");
                } else {
                    emptyStateCard.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    applyFilter();
                }
            });
        });
    }
    
    private void filterHearings(String query) {
        if (query.isEmpty()) {
            applyFilter();
            return;
        }
        
        List<Hearing> filtered = new ArrayList<>();
        for (Hearing hearing : allHearings) {
            String purpose = hearing.getPurpose() != null ? hearing.getPurpose().toLowerCase() : "";
            String location = hearing.getLocation() != null ? hearing.getLocation().toLowerCase() : "";
            String hearingDate = hearing.getHearingDate() != null ? hearing.getHearingDate().toLowerCase() : "";
            
            if (purpose.contains(query.toLowerCase()) ||
                location.contains(query.toLowerCase()) ||
                hearingDate.contains(query.toLowerCase())) {
                filtered.add(hearing);
            }
        }
        
        adapter.setHearings(filtered);
        tvTotalHearings.setText(filtered.size() + " Hearings");
    }
    
    private void applyFilter() {
        List<Hearing> filtered = new ArrayList<>();
        
        for (Hearing hearing : allHearings) {
            if (currentFilter.equals("All")) {
                filtered.add(hearing);
            } else if (currentFilter.equals("Upcoming")) {
                // Filter upcoming hearings (you can add date logic here)
                if ("Scheduled".equals(hearing.getStatus())) {
                    filtered.add(hearing);
                }
            } else if (currentFilter.equals("Completed")) {
                if ("Completed".equals(hearing.getStatus())) {
                    filtered.add(hearing);
                }
            } else if (currentFilter.equals("Cancelled")) {
                if ("Cancelled".equals(hearing.getStatus())) {
                    filtered.add(hearing);
                }
            }
        }
        
        // Update UI based on filtered results - background CardView always stays visible
        if (filtered.isEmpty()) {
            emptyStateCard.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvTotalHearings.setText("0 Hearings");
        } else {
            emptyStateCard.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setHearings(filtered);
            tvTotalHearings.setText(filtered.size() + " Hearings");
        }
    }
    
    // Add method to refresh data when status changes
    public void refreshData() {
        loadHearings();
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
                loadHearings();
                
                handler.postDelayed(this, 15000); // Refresh every 15 seconds (faster)
            }
        };
        handler.postDelayed(refreshRunnable, 15000);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadHearings();
    }
}
