package com.example.blottermanagementsystem.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.MediationSession;
import com.example.blottermanagementsystem.ui.adapters.MediationSessionAdapter;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.example.blottermanagementsystem.utils.RoleAccessControl;
import java.util.List;

public class MediationSessionActivity extends BaseActivity {
    
    private RecyclerView recyclerView;
    private MediationSessionAdapter adapter;
    private TextView tvEmpty, tvTotalSessions, tvScheduledSessions, tvCompletedSessions;
    private FloatingActionButton fabAdd;
    private BlotterDatabase database;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PreferencesManager preferencesManager = new PreferencesManager(this);
        if (!RoleAccessControl.checkAnyRole(this, new String[]{"Admin", "Officer"}, preferencesManager)) {
            return;
        }
        
        setContentView(R.layout.activity_mediation_session);
        
        database = BlotterDatabase.getDatabase(this);
        
        setupToolbar();
        initViews();
        setupListeners();
        loadSessions();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mediation Sessions");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTotalSessions = findViewById(R.id.tvTotalSessions);
        tvScheduledSessions = findViewById(R.id.tvScheduledSessions);
        tvCompletedSessions = findViewById(R.id.tvCompletedSessions);
        fabAdd = findViewById(R.id.fabAdd);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MediationSessionAdapter(session -> {
            // Handle session click
        });
        recyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            // Add new mediation session
        });
    }
    
    private void loadSessions() {
        new Thread(() -> {
            List<MediationSession> sessions = database.mediationSessionDao().getAllSessions();
            
            int total = sessions.size();
            long scheduled = sessions.stream().filter(s -> "Scheduled".equals(s.getStatus())).count();
            long completed = sessions.stream().filter(s -> "Completed".equals(s.getStatus())).count();
            
            runOnUiThread(() -> {
                tvTotalSessions.setText(String.valueOf(total));
                tvScheduledSessions.setText(String.valueOf(scheduled));
                tvCompletedSessions.setText(String.valueOf(completed));
                
                if (sessions.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                    adapter.setSessions(sessions);
                }
            });
        }).start();
    }
}
