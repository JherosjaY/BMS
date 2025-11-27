package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.LegalDocument;
import com.example.blottermanagementsystem.ui.adapters.LegalDocumentAdapter;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.example.blottermanagementsystem.utils.RoleAccessControl;
import java.util.List;

public class LegalDocumentsDashboardActivity extends BaseActivity {
    
    private RecyclerView recyclerView;
    private LegalDocumentAdapter adapter;
    private TextView tvEmpty, tvTotalDocs, tvPendingDocs, tvCompletedDocs;
    private CardView cardKPForms, cardSummons, cardMediation;
    private FloatingActionButton fabAdd;
    private BlotterDatabase database;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PreferencesManager preferencesManager = new PreferencesManager(this);
        if (!RoleAccessControl.checkAnyRole(this, new String[]{"Admin", "Officer"}, preferencesManager)) {
            return;
        }
        
        // TODO: Create activity_legal_documents_dashboard.xml
        // KP Forms layout removed - now using floating dialog
        setContentView(R.layout.activity_report_detail); // Fallback layout
        
        database = BlotterDatabase.getDatabase(this);
        
        setupToolbar();
        initViews();
        setupListeners();
        loadDocuments();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Legal Documents");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        // TODO: Add IDs to layout
        // tvTotalDocs = findViewById(R.id.tvTotalDocs);
        // tvPendingDocs = findViewById(R.id.tvPendingDocs);
        // tvCompletedDocs = findViewById(R.id.tvCompletedDocs);
        cardKPForms = findViewById(R.id.cardKPForms);
        cardSummons = findViewById(R.id.cardSummons);
        cardMediation = findViewById(R.id.cardMediation);
        fabAdd = findViewById(R.id.fabAdd);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LegalDocumentAdapter(document -> {
            // Handle document click
        });
        recyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        cardKPForms.setOnClickListener(v -> {
            Toast.makeText(this, "KP Forms feature moved to Case Investigation screen", Toast.LENGTH_SHORT).show();
        });
        
        cardSummons.setOnClickListener(v -> {
            Toast.makeText(this, "Summons feature has been removed from the system", Toast.LENGTH_SHORT).show();
        });
        
        cardMediation.setOnClickListener(v -> {
            startActivity(new Intent(this, MediationSessionActivity.class));
        });
        
        fabAdd.setOnClickListener(v -> {
            // Add new legal document
        });
    }
    
    private void loadDocuments() {
        new Thread(() -> {
            List<LegalDocument> documents = database.legalDocumentDao().getAllDocuments();
            
            int total = documents.size();
            long pending = documents.stream().filter(d -> "Pending".equals(d.getStatus())).count();
            long completed = documents.stream().filter(d -> "Completed".equals(d.getStatus())).count();
            
            runOnUiThread(() -> {
                tvTotalDocs.setText(String.valueOf(total));
                tvPendingDocs.setText(String.valueOf(pending));
                tvCompletedDocs.setText(String.valueOf(completed));
                
                if (documents.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                    adapter.setDocuments(documents);
                }
            });
        }).start();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadDocuments();
    }
}
