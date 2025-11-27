package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.InvestigationTask;
import com.example.blottermanagementsystem.ui.adapters.InvestigationTaskAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class InvestigationChecklistActivity extends BaseActivity {
    
    private int reportId;
    private BlotterDatabase database;
    private RecyclerView recyclerTasks;
    private InvestigationTaskAdapter adapter;
    private List<InvestigationTask> tasks = new ArrayList<>();
    
    private TextView tvProgress, tvTaskCount;
    private ProgressBar progressBar;
    private MaterialButton btnGenerateReport, btnCompleteInvestigation;
    private LinearLayout emptyState;
    private MaterialCardView cardProgress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investigation_checklist);
        
        database = BlotterDatabase.getDatabase(this);
        reportId = getIntent().getIntExtra("REPORT_ID", -1);
        
        if (reportId == -1) {
            Toast.makeText(this, "Invalid report ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        setupToolbar();
        loadTasks();
    }
    
    private void initializeViews() {
        recyclerTasks = findViewById(R.id.recyclerTasks);
        tvProgress = findViewById(R.id.tvProgress);
        tvTaskCount = findViewById(R.id.tvTaskCount);
        progressBar = findViewById(R.id.progressBar);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        btnCompleteInvestigation = findViewById(R.id.btnCompleteInvestigation);
        emptyState = findViewById(R.id.emptyState);
        cardProgress = findViewById(R.id.cardProgress);
        
        // Setup RecyclerView
        adapter = new InvestigationTaskAdapter(tasks, task -> {
            updateTaskCompletion(task);
        });
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerTasks.setAdapter(adapter);
        
        // Setup buttons
        btnGenerateReport.setOnClickListener(v -> generateInvestigationReport());
        btnCompleteInvestigation.setOnClickListener(v -> completeInvestigation());
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Investigation Checklist");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void loadTasks() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                tasks.clear();
                List<InvestigationTask> loadedTasks = database.investigationTaskDao().getTasksByReportId(reportId);
                
                // If no tasks exist, create default investigation tasks
                if (loadedTasks.isEmpty()) {
                    loadedTasks = createDefaultTasks();
                }
                
                tasks.addAll(loadedTasks);
                
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    updateProgress();
                    updateEmptyState();
                });
            } catch (Exception e) {
                android.util.Log.e("InvestigationChecklist", "Error loading tasks: " + e.getMessage());
            }
        });
    }
    
    private List<InvestigationTask> createDefaultTasks() {
        List<InvestigationTask> defaultTasks = new ArrayList<>();
        
        defaultTasks.add(new InvestigationTask(reportId, "Interview Complainant", "Conduct detailed interview with the complainant", 1));
        defaultTasks.add(new InvestigationTask(reportId, "Interview Respondent", "Conduct detailed interview with the respondent", 1));
        defaultTasks.add(new InvestigationTask(reportId, "Gather Evidence", "Collect and document all physical evidence", 1));
        defaultTasks.add(new InvestigationTask(reportId, "Document Scene", "Take photos/videos of the incident scene", 2));
        defaultTasks.add(new InvestigationTask(reportId, "Collect Witness Statements", "Obtain statements from witnesses", 2));
        defaultTasks.add(new InvestigationTask(reportId, "Review Evidence", "Analyze and review all collected evidence", 1));
        defaultTasks.add(new InvestigationTask(reportId, "Prepare Investigation Report", "Compile findings into formal report", 1));
        
        // Insert into database
        Executors.newSingleThreadExecutor().execute(() -> {
            for (InvestigationTask task : defaultTasks) {
                database.investigationTaskDao().insertTask(task);
            }
        });
        
        return defaultTasks;
    }
    
    private void updateTaskCompletion(InvestigationTask task) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                task.setCompleted(!task.isCompleted());
                task.setUpdatedDate(System.currentTimeMillis());
                database.investigationTaskDao().updateTask(task);
                
                runOnUiThread(() -> {
                    updateProgress();
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                android.util.Log.e("InvestigationChecklist", "Error updating task: " + e.getMessage());
            }
        });
    }
    
    private void updateProgress() {
        int total = tasks.size();
        int completed = 0;
        
        for (InvestigationTask task : tasks) {
            if (task.isCompleted()) {
                completed++;
            }
        }
        
        int progress = total > 0 ? (completed * 100) / total : 0;
        
        tvProgress.setText(progress + "%");
        tvTaskCount.setText(completed + " of " + total + " completed");
        progressBar.setProgress(progress);
        
        // Enable complete button only if all tasks are done
        if (btnCompleteInvestigation != null) {
            btnCompleteInvestigation.setEnabled(completed == total && total > 0);
            btnCompleteInvestigation.setAlpha(completed == total && total > 0 ? 1.0f : 0.5f);
        }
    }
    
    private void updateEmptyState() {
        if (tasks.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerTasks.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerTasks.setVisibility(View.VISIBLE);
        }
    }
    
    private void generateInvestigationReport() {
        Toast.makeText(this, "Generating PDF report...", Toast.LENGTH_SHORT).show();
        // TODO: Implement PDF generation
    }
    
    private void completeInvestigation() {
        // Check if all tasks are completed
        int completedCount = 0;
        for (InvestigationTask task : tasks) {
            if (task.isCompleted()) {
                completedCount++;
            }
        }
        
        if (completedCount == tasks.size() && tasks.size() > 0) {
            // All tasks completed - update report status to "Resolved"
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    BlotterDatabase db = BlotterDatabase.getDatabase(this);
                    com.example.blottermanagementsystem.data.entity.BlotterReport report = 
                        db.blotterReportDao().getReportById(reportId);
                    
                    if (report != null) {
                        report.setStatus("Resolved");
                        report.setUpdatedAt(System.currentTimeMillis());
                        db.blotterReportDao().updateReport(report);
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Investigation completed! Case marked as Resolved.", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("INVESTIGATION_COMPLETE", true);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error completing investigation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            Toast.makeText(this, "Please complete all investigation tasks first", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }
}
