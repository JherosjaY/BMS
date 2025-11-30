package com.example.blottermanagementsystem.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
// ❌ REMOVED: import com.example.blottermanagementsystem.services.PersonHistoryService; (Pure online mode)
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Map;

public class PersonHistoryActivity extends BaseActivity {
    private static final String TAG = "PersonHistory";

    private Toolbar toolbar;
    private TextInputEditText etSearchPerson;
    private MaterialButton btnSearch;
    private LinearLayout layoutHistoryFound, layoutNoHistory;
    private TextView tvPersonName, tvRiskLevel, tvNoHistory;
    private RecyclerView rvCriminalRecords, rvCaseInvolvements;
    private Button btnAddRecord, btnCreateProfile, btnSearchAgain;

    // ❌ REMOVED: private PersonHistoryService personService; (Pure online mode)
    private String currentUserRole;
    private String currentUserId;
    private String currentPersonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_history);

        // ❌ REMOVED: personService = new PersonHistoryService(this); (Pure online mode)
        currentUserRole = getSharedPreferences("UserSession", MODE_PRIVATE).getString("role", "User");
        currentUserId = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userId", "");

        initializeViews();
        setupToolbar();
        setupListeners();
        setupRoleBasedUI();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearchPerson = findViewById(R.id.etSearchPerson);
        btnSearch = findViewById(R.id.btnSearch);
        layoutHistoryFound = findViewById(R.id.layoutHistoryFound);
        layoutNoHistory = findViewById(R.id.layoutNoHistory);
        tvPersonName = findViewById(R.id.tvPersonName);
        tvRiskLevel = findViewById(R.id.tvRiskLevel);
        tvNoHistory = findViewById(R.id.tvNoHistory);
        rvCriminalRecords = findViewById(R.id.rvCriminalRecords);
        rvCaseInvolvements = findViewById(R.id.rvCaseInvolvements);
        btnAddRecord = findViewById(R.id.btnAddRecord);
        btnCreateProfile = findViewById(R.id.btnCreateProfile);
        btnSearchAgain = findViewById(R.id.btnSearchAgain);

        rvCriminalRecords.setLayoutManager(new LinearLayoutManager(this));
        rvCaseInvolvements.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Person History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> searchPerson());
        btnAddRecord.setOnClickListener(v -> showAddRecordDialog());
        btnCreateProfile.setOnClickListener(v -> showCreateProfileDialog());
        btnSearchAgain.setOnClickListener(v -> resetSearch());
    }

    private void setupRoleBasedUI() {
        // ONLY OFFICERS CAN ADD/MODIFY RECORDS
        if (!"Officer".equalsIgnoreCase(currentUserRole)) {
            btnAddRecord.setVisibility(View.GONE);
            btnCreateProfile.setVisibility(View.GONE);
        }
    }

    private void searchPerson() {
        String searchTerm = etSearchPerson.getText().toString().trim();
        
        if (searchTerm.isEmpty()) {
            Toast.makeText(this, "Please enter a person name", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "🔍 Searching for: " + searchTerm);
        Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();

        personService.searchPersonHistory(searchTerm, new PersonHistoryService.PersonHistoryCallback() {
            @Override
            public void onSuccess(Map<String, Object> results) {
                Log.d(TAG, "✅ Search successful");
                // For now, assume single result - in production, handle multiple results
                if (results != null && results.containsKey("person_id")) {
                    currentPersonId = (String) results.get("person_id");
                    loadCompletePersonHistory(currentPersonId);
                } else {
                    showNoHistoryFound("No person found with name: " + searchTerm);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Search error: " + error);
                showNoHistoryFound(error);
            }
        });
    }

    private void loadCompletePersonHistory(String personId) {
        Log.d(TAG, "📋 Loading complete history for: " + personId);
        
        personService.getPersonCompleteHistory(personId, new PersonHistoryService.CompleteHistoryCallback() {
            @Override
            public void onSuccess(Map<String, Object> history) {
                Log.d(TAG, "✅ History loaded successfully");
                displayPersonHistory(history);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ History load error: " + error);
                showNoHistoryFound(error);
            }
        });
    }

    private void displayPersonHistory(Map<String, Object> history) {
        layoutNoHistory.setVisibility(View.GONE);
        layoutHistoryFound.setVisibility(View.VISIBLE);

        // DISPLAY PERSON INFO
        String firstName = (String) history.getOrDefault("first_name", "Unknown");
        String lastName = (String) history.getOrDefault("last_name", "");
        String riskLevel = (String) history.getOrDefault("risk_level", "Low");

        tvPersonName.setText(firstName + " " + lastName);
        tvRiskLevel.setText("⚠️ Risk Level: " + riskLevel);

        Log.d(TAG, "📊 Displaying person: " + firstName + " " + lastName);
    }

    private void showNoHistoryFound(String message) {
        layoutHistoryFound.setVisibility(View.GONE);
        layoutNoHistory.setVisibility(View.VISIBLE);
        tvNoHistory.setText(message);

        // ONLY OFFICERS CAN CREATE NEW PROFILES
        btnCreateProfile.setVisibility("Officer".equalsIgnoreCase(currentUserRole) ? View.VISIBLE : View.GONE);
    }

    private void resetSearch() {
        etSearchPerson.setText("");
        layoutHistoryFound.setVisibility(View.GONE);
        layoutNoHistory.setVisibility(View.GONE);
        currentPersonId = null;
    }

    private void showAddRecordDialog() {
        if (currentPersonId == null) {
            Toast.makeText(this, "Please search for a person first", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_criminal_record, null);
        TextInputEditText etCrimeType = dialogView.findViewById(R.id.etCrimeType);
        TextInputEditText etCrimeDescription = dialogView.findViewById(R.id.etCrimeDescription);
        TextInputEditText etDateCommitted = dialogView.findViewById(R.id.etDateCommitted);

        new MaterialAlertDialogBuilder(this)
            .setTitle("Add Criminal Record")
            .setView(dialogView)
            .setPositiveButton("ADD", (dialog, which) -> {
                String crimeType = etCrimeType.getText().toString().trim();
                String crimeDescription = etCrimeDescription.getText().toString().trim();
                String dateCommitted = etDateCommitted.getText().toString().trim();

                if (crimeType.isEmpty()) {
                    Toast.makeText(this, "Please enter crime type", Toast.LENGTH_SHORT).show();
                    return;
                }

                addCriminalRecord(crimeType, crimeDescription, dateCommitted);
            })
            .setNegativeButton("CANCEL", null)
            .show();
    }

    private void addCriminalRecord(String crimeType, String crimeDescription, String dateCommitted) {
        Toast.makeText(this, "Adding criminal record...", Toast.LENGTH_SHORT).show();

        personService.addCriminalRecord(
            currentPersonId,
            crimeType,
            crimeDescription,
            dateCommitted,
            "",
            currentUserId,
            new PersonHistoryService.DataOperationCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "✅ " + message);
                    Toast.makeText(PersonHistoryActivity.this, message, Toast.LENGTH_SHORT).show();
                    loadCompletePersonHistory(currentPersonId);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ " + error);
                    Toast.makeText(PersonHistoryActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void showCreateProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_person_profile, null);
        TextInputEditText etFirstName = dialogView.findViewById(R.id.etFirstName);
        TextInputEditText etLastName = dialogView.findViewById(R.id.etLastName);
        TextInputEditText etAlias = dialogView.findViewById(R.id.etAlias);
        TextInputEditText etDateOfBirth = dialogView.findViewById(R.id.etDateOfBirth);
        TextInputEditText etGender = dialogView.findViewById(R.id.etGender);

        new MaterialAlertDialogBuilder(this)
            .setTitle("Create Person Profile")
            .setView(dialogView)
            .setPositiveButton("CREATE", (dialog, which) -> {
                String firstName = etFirstName.getText().toString().trim();
                String lastName = etLastName.getText().toString().trim();
                String alias = etAlias.getText().toString().trim();
                String dateOfBirth = etDateOfBirth.getText().toString().trim();
                String gender = etGender.getText().toString().trim();

                if (firstName.isEmpty() || lastName.isEmpty()) {
                    Toast.makeText(this, "Please enter first and last name", Toast.LENGTH_SHORT).show();
                    return;
                }

                createPersonProfile(firstName, lastName, alias, dateOfBirth, gender);
            })
            .setNegativeButton("CANCEL", null)
            .show();
    }

    private void createPersonProfile(String firstName, String lastName, String alias, 
                                    String dateOfBirth, String gender) {
        Toast.makeText(this, "Creating person profile...", Toast.LENGTH_SHORT).show();

        personService.createPersonProfile(
            firstName,
            lastName,
            alias,
            dateOfBirth,
            gender,
            "low",
            currentUserId,
            new PersonHistoryService.DataOperationCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "✅ " + message);
                    Toast.makeText(PersonHistoryActivity.this, message, Toast.LENGTH_SHORT).show();
                    resetSearch();
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ " + error);
                    Toast.makeText(PersonHistoryActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
}
