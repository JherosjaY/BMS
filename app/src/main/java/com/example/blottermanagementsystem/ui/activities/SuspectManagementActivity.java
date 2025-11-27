package com.example.blottermanagementsystem.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.managers.CasePersonManager;
import com.example.blottermanagementsystem.utils.ApiClient;
import java.util.List;
import java.util.Map;

/**
 * ✅ SUSPECT MANAGEMENT ACTIVITY
 * Manage suspects for a specific case
 */
public class SuspectManagementActivity extends AppCompatActivity {
    
    private int caseId;
    private CasePersonManager casePersonManager;
    private RecyclerView suspectsRecyclerView;
    private Button addSuspectButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create simple layout programmatically
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setContentView(recyclerView);
        
        caseId = getIntent().getIntExtra("case_id", -1);
        casePersonManager = new CasePersonManager(this);
        
        suspectsRecyclerView = recyclerView;
        loadSuspects();
    }
    
    private void initializeViews() {
        // Views initialized in onCreate
    }
    
    private void loadSuspects() {
        ApiClient.getCaseSuspects(caseId, new ApiClient.ApiCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> suspects) {
                android.util.Log.d("SuspectManagement", "✅ Loaded " + suspects.size() + " suspects");
                // TODO: Set adapter with suspects list
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(SuspectManagementActivity.this, 
                    "Failed to load suspects: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showAddSuspectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Suspect");
        
        // Create dialog fields programmatically
        EditText etName = new EditText(this);
        etName.setHint("Suspect Name");
        EditText etContact = new EditText(this);
        etContact.setHint("Contact");
        EditText etAddress = new EditText(this);
        etAddress.setHint("Address");
        EditText etDescription = new EditText(this);
        etDescription.setHint("Description");
        
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String contact = etContact.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            
            if (name.isEmpty()) {
                Toast.makeText(this, "Suspect name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            addSuspect(name, contact, address, description);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void addSuspect(String name, String contact, String address, String description) {
        Map<String, Object> suspectData = casePersonManager.createSuspectData(name, contact, description, address);
        
        ApiClient.addSuspectToCase(caseId, suspectData, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(SuspectManagementActivity.this, "Suspect added successfully", Toast.LENGTH_SHORT).show();
                loadSuspects(); // Refresh list
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(SuspectManagementActivity.this, 
                    "Failed to add suspect: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
