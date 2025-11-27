package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
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
 * ✅ WITNESS MANAGEMENT ACTIVITY
 * Manage witnesses for a specific case
 */
public class WitnessManagementActivity extends AppCompatActivity {
    
    private int caseId;
    private CasePersonManager casePersonManager;
    private RecyclerView witnessesRecyclerView;
    private Button addWitnessButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create simple layout programmatically
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setContentView(recyclerView);
        
        caseId = getIntent().getIntExtra("case_id", -1);
        casePersonManager = new CasePersonManager(this);
        
        witnessesRecyclerView = recyclerView;
        loadWitnesses();
    }
    
    private void initializeViews() {
        // Views initialized in onCreate
    }
    
    private void loadWitnesses() {
        ApiClient.getCaseWitnesses(caseId, new ApiClient.ApiCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> witnesses) {
                android.util.Log.d("WitnessManagement", "✅ Loaded " + witnesses.size() + " witnesses");
                // TODO: Set adapter with witnesses list
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(WitnessManagementActivity.this, 
                    "Failed to load witnesses: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showAddWitnessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Witness");
        
        // Create dialog fields programmatically
        EditText etName = new EditText(this);
        etName.setHint("Witness Name");
        EditText etContact = new EditText(this);
        etContact.setHint("Contact");
        EditText etAddress = new EditText(this);
        etAddress.setHint("Address");
        EditText etStatement = new EditText(this);
        etStatement.setHint("Statement");
        
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String contact = etContact.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String statement = etStatement.getText().toString().trim();
            
            if (name.isEmpty()) {
                Toast.makeText(this, "Witness name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            addWitness(name, contact, address, statement);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void addWitness(String name, String contact, String address, String statement) {
        Map<String, Object> witnessData = casePersonManager.createWitnessData(name, contact, statement, address);
        
        ApiClient.addWitnessToCase(caseId, witnessData, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(WitnessManagementActivity.this, "Witness added successfully", Toast.LENGTH_SHORT).show();
                loadWitnesses(); // Refresh list
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(WitnessManagementActivity.this, 
                    "Failed to add witness: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
