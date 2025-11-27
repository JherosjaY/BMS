package com.example.blottermanagementsystem.ui.activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.Hearing;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HearingsActivity extends BaseActivity {
    
    private RecyclerView recyclerHearings;
    private TextView tvEmpty;
    private BlotterDatabase database;
    private List<Hearing> hearingsList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearing_calendar);
        
        database = BlotterDatabase.getDatabase(this);
        
        setupToolbar();
        initViews();
        setupRecyclerView();
        loadHearings();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hearings");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void initViews() {
        recyclerHearings = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
    }
    
    private void setupRecyclerView() {
        recyclerHearings.setLayoutManager(new LinearLayoutManager(this));
        // Simple adapter for now - will show empty list
    }
    
    private void loadHearings() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Hearing> hearings = database.hearingDao().getAllHearings();
            
            runOnUiThread(() -> {
                hearingsList.clear();
                hearingsList.addAll(hearings);
                
                if (hearings.isEmpty()) {
                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(android.view.View.VISIBLE);
                    }
                    recyclerHearings.setVisibility(android.view.View.GONE);
                } else {
                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(android.view.View.GONE);
                    }
                    recyclerHearings.setVisibility(android.view.View.VISIBLE);
                }
            });
        });
    }
    
    // Quiet loading method to prevent black screen flicker
    private void loadHearingsQuietly() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Hearing> hearings = database.hearingDao().getAllHearings();
                
                // Only update if data actually changed
                if (hearings.size() != hearingsList.size()) {
                    runOnUiThread(() -> {
                        hearingsList.clear();
                        hearingsList.addAll(hearings);
                        
                        if (hearings.isEmpty()) {
                            if (tvEmpty != null) {
                                tvEmpty.setVisibility(android.view.View.VISIBLE);
                            }
                            if (recyclerHearings != null) recyclerHearings.setVisibility(android.view.View.GONE);
                        } else {
                            if (tvEmpty != null) {
                                tvEmpty.setVisibility(android.view.View.GONE);
                            }
                            if (recyclerHearings != null) recyclerHearings.setVisibility(android.view.View.VISIBLE);
                        }
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("HearingsActivity", "Error in quiet loading: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHearingsQuietly(); // Use quiet refresh to prevent black screen flicker
    }
}
