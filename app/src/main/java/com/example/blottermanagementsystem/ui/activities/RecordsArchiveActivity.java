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
import com.example.blottermanagementsystem.ui.adapters.ReportAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class RecordsArchiveActivity extends BaseActivity {
    
    private RecyclerView recyclerArchive;
    private LinearLayout emptyState;
    private BlotterDatabase database;
    private ReportAdapter adapter;
    private List<BlotterReport> archivedReports = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);
        
        database = BlotterDatabase.getDatabase(this);
        
        setupToolbar();
        initViews();
        setupRecyclerView();
        loadArchivedReports();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Records Archive");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void initViews() {
        recyclerArchive = findViewById(R.id.recyclerReports);
        emptyState = findViewById(R.id.emptyState);
    }
    
    private void setupRecyclerView() {
        adapter = new ReportAdapter(archivedReports, report -> {
            Intent intent = new Intent(this, ReportDetailActivity.class);
            intent.putExtra("REPORT_ID", report.getId());
            startActivity(intent);
        });
        recyclerArchive.setLayoutManager(new LinearLayoutManager(this));
        recyclerArchive.setAdapter(adapter);
    }
    
    private void loadArchivedReports() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Get resolved/closed reports (archived)
            List<BlotterReport> reports = database.blotterReportDao().getReportsByStatus("Resolved");
            List<BlotterReport> closedReports = database.blotterReportDao().getReportsByStatus("Closed");
            reports.addAll(closedReports);
            
            runOnUiThread(() -> {
                archivedReports.clear();
                archivedReports.addAll(reports);
                adapter.updateReports(archivedReports);
                
                if (reports.isEmpty()) {
                    emptyState.setVisibility(android.view.View.VISIBLE);
                    recyclerArchive.setVisibility(android.view.View.GONE);
                } else {
                    emptyState.setVisibility(android.view.View.GONE);
                    recyclerArchive.setVisibility(android.view.View.VISIBLE);
                }
            });
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadArchivedReports();
    }
}
