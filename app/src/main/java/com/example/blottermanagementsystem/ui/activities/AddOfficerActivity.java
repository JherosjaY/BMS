package com.example.blottermanagementsystem.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.Officer;
import com.example.blottermanagementsystem.utils.PhoneNumberValidator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Random;
import java.util.concurrent.Executors;

public class AddOfficerActivity extends BaseActivity {
    
    private TextInputEditText etFirstName, etLastName, etContactNumber, etEmail, etBadgeNumber;
    private Spinner spinnerRank, spinnerGender;
    private MaterialButton btnAddOfficer;
    
    private BlotterDatabase database;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_officer);
        
        database = BlotterDatabase.getDatabase(this);
        
        setupToolbar();
        initViews();
        generateBadgeNumber();
        setupSpinners();
        setupListeners();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void initViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etContactNumber = findViewById(R.id.etContactNumber);
        etEmail = findViewById(R.id.etEmail);
        etBadgeNumber = findViewById(R.id.etBadgeNumber);
        spinnerRank = findViewById(R.id.spinnerRank);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnAddOfficer = findViewById(R.id.btnAddOfficer);
    }
    
    private void setupSpinners() {
        // Gender spinner (with empty default)
        String[] genders = {"Select Gender", "Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_dropdown_item, genders);
        spinnerGender.setAdapter(genderAdapter);
        spinnerGender.setSelection(0); // Set to "Select Gender" by default
        
        // Rank spinner - Philippine PNP Ranks (with empty default)
        String[] ranks = {
            "Select Rank", // Default empty option
            "Patrolman (PTLM)",
            "Patrolwoman (PTLW)",
            "Police Officer 1 (PO1)",
            "Police Officer 2 (PO2)",
            "Police Officer 3 (PO3)",
            "Senior Police Officer 1 (SPO1)",
            "Senior Police Officer 2 (SPO2)",
            "Senior Police Officer 3 (SPO3)",
            "Senior Police Officer 4 (SPO4)",
            "Police Inspector (PINSP)",
            "Police Senior Inspector (PSINSP)",
            "Police Chief Inspector (PCINSP)",
            "Police Superintendent (PSUPT)",
            "Police Senior Superintendent (PSSUPT)",
            "Police Chief Superintendent (PCSUPT)",
            "Police Director (PDIR)",
            "Police Deputy Director General (PDDG)",
            "Police Director General (PDG)"
        };
        ArrayAdapter<String> rankAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_dropdown_item, ranks);
        spinnerRank.setAdapter(rankAdapter);
        spinnerRank.setSelection(0); // Set to "Select Rank" by default
    }
    
    private void setupListeners() {
        btnAddOfficer.setOnClickListener(v -> addOfficer());
    }
    
    private void addOfficer() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String contactNumber = etContactNumber.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String badgeNumber = etBadgeNumber.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String rank = spinnerRank.getSelectedItem().toString();
        
        // Validation
        if (firstName.isEmpty()) {
            etFirstName.setError("Required");
            return;
        }
        if (rank.equals("Select Rank")) {
            Toast.makeText(this, "Please select a rank", Toast.LENGTH_SHORT).show();
            return;
        }
        if (gender.equals("Select Gender")) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }
        if (lastName.isEmpty()) {
            etLastName.setError("Required");
            return;
        }
        if (contactNumber.isEmpty()) {
            etContactNumber.setError("Required");
            return;
        }
        
        // Validate Philippine mobile number
        if (!PhoneNumberValidator.isValidPhilippineNumber(contactNumber)) {
            etContactNumber.setError(PhoneNumberValidator.getErrorMessage(contactNumber));
            etContactNumber.requestFocus();
            Toast.makeText(this, "Invalid contact number. " + 
                PhoneNumberValidator.getSupportedNetworks(), Toast.LENGTH_LONG).show();
            return;
        }
        // Badge number is auto-generated, no validation needed
        
        btnAddOfficer.setEnabled(false);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Create officer with required constructor parameters
                String fullName = firstName + " " + lastName;
                Officer officer = new Officer(fullName, rank, badgeNumber);
                officer.setContactNumber(contactNumber);
                officer.setEmail(email);
                officer.setGender(gender);
                officer.setActive(true);
                
                long officerId = database.officerDao().insertOfficer(officer);
                
                // Generate credentials for the officer
                String username = com.example.blottermanagementsystem.utils.PasswordGenerator
                    .generateUsername(firstName, lastName);
                String temporaryPassword = com.example.blottermanagementsystem.utils.PasswordGenerator
                    .generateOfficerPassword(rank, badgeNumber);
                
                android.util.Log.d("AddOfficer", "=== OFFICER CREDENTIALS ===");
                android.util.Log.d("AddOfficer", "Username: " + username);
                android.util.Log.d("AddOfficer", "Password (plain): " + temporaryPassword);
                
                // Check if username already exists
                com.example.blottermanagementsystem.data.entity.User existingUser = 
                    database.userDao().getUserByUsername(username);
                
                if (existingUser == null) {
                    try {
                        // Hash the password before saving
                        String hashedPassword = com.example.blottermanagementsystem.utils.SecurityUtils.hashPassword(temporaryPassword);
                        android.util.Log.d("AddOfficer", "Password (plain): " + temporaryPassword);
                        android.util.Log.d("AddOfficer", "Password (hashed): " + hashedPassword);
                        
                        // Verify hash is correct length (SHA-256 = 64 hex chars)
                        if (hashedPassword.length() != 64) {
                            android.util.Log.e("AddOfficer", "❌ ERROR: Hash length is " + hashedPassword.length() + ", expected 64!");
                            throw new Exception("Password hash is invalid length: " + hashedPassword.length());
                        }
                        
                        com.example.blottermanagementsystem.data.entity.User officerUser = 
                            new com.example.blottermanagementsystem.data.entity.User(
                                firstName, lastName, username, hashedPassword, "Officer"
                            );
                        officerUser.setActive(true);
                        officerUser.setProfileCompleted(true);
                        officerUser.setGender(gender);
                        officerUser.setEmail(email);
                        officerUser.setPhoneNumber(contactNumber);
                        officerUser.setMustChangePassword(true); // Force password change on first login
                        
                        android.util.Log.d("AddOfficer", "=== ABOUT TO INSERT USER ===");
                        android.util.Log.d("AddOfficer", "Username: " + officerUser.getUsername());
                        android.util.Log.d("AddOfficer", "Password hash: " + officerUser.getPassword());
                        android.util.Log.d("AddOfficer", "Active: " + officerUser.isActive());
                        android.util.Log.d("AddOfficer", "Role: " + officerUser.getRole());
                        
                        long userId = database.userDao().insertUser(officerUser);
                        android.util.Log.d("AddOfficer", "✅ Officer User created! UserID: " + userId);
                        
                        // Verify user was actually saved
                        com.example.blottermanagementsystem.data.entity.User savedUser = database.userDao().getUserByUsername(username);
                        if (savedUser == null) {
                            throw new Exception("User was not saved to database!");
                        }
                        android.util.Log.d("AddOfficer", "✅ Verified user exists in database");
                        android.util.Log.d("AddOfficer", "Saved hash: " + savedUser.getPassword());
                        
                        // Link officer to user
                        officer.setId((int) officerId);
                        officer.setUserId((int) userId);
                        database.officerDao().updateOfficer(officer);
                        android.util.Log.d("AddOfficer", "✅ Officer linked to user! OfficerID: " + officerId);
                    } catch (Exception e) {
                        android.util.Log.e("AddOfficer", "❌ ERROR creating officer user: " + e.getMessage(), e);
                        e.printStackTrace();
                        throw e; // Re-throw to be caught by outer try-catch
                    }
                } else {
                    android.util.Log.e("AddOfficer", "❌ Username already exists: " + username);
                }
                
                runOnUiThread(() -> {
                    // Check internet connection and show appropriate dialog
                    handleCredentialsDelivery(fullName, username, temporaryPassword, rank, badgeNumber, email);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnAddOfficer.setEnabled(true);
                });
            }
        });
    }
    
    private void handleCredentialsDelivery(String officerName, String username, String password, 
                                           String rank, String badgeNumber, String email) {
        try {
            android.util.Log.d("AddOfficer", "Showing success dialog");
            android.util.Log.d("AddOfficer", "Email: " + email);
            
            // ALWAYS show the success dialog first (regardless of internet/email)
            showOfficerCreatedSuccessDialog(officerName, email, username, password, rank, badgeNumber);
        } catch (Exception e) {
            android.util.Log.e("AddOfficer", "Error in handleCredentialsDelivery", e);
            Toast.makeText(this, "Error showing dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Fallback to credentials dialog
            showCredentialsDialog(officerName, username, password, rank, badgeNumber);
        }
    }
    
    private void showOfficerCreatedSuccessDialog(String officerName, String email, String username, 
                                                 String password, String rank, String badgeNumber) {
        try {
            // Inflate custom dialog layout
            android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_officer_created, null);
            
            // Get views
            android.widget.TextView tvOfficerName = dialogView.findViewById(R.id.tvOfficerName);
            android.widget.TextView tvEmail = dialogView.findViewById(R.id.tvEmail);
            MaterialButton btnSendEmail = dialogView.findViewById(R.id.btnSendEmail);
            MaterialButton btnShowCredentials = dialogView.findViewById(R.id.btnShowCredentials);
            MaterialButton btnDone = dialogView.findViewById(R.id.btnDone);
            
            // Set data
            tvOfficerName.setText(officerName + "\n" + rank + " | Badge: " + badgeNumber);
            tvEmail.setText(email);
            
            // Create dialog
            AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
            
            // Set button listeners
            btnSendEmail.setOnClickListener(v -> {
                // Disable button to prevent multiple clicks
                btnSendEmail.setEnabled(false);
                
                // AUTOMATIC loading with email operation
                com.example.blottermanagementsystem.utils.AutoLoadingInterceptor.executeEmailWithLoading(this, () -> {
                    // Use the new EmailHelper with cloud-ready implementation
                    com.example.blottermanagementsystem.utils.EmailHelper.sendOfficerCredentialsEmail(
                        this, officerName, email, username, password
                    );
                    
                    // Show success toast on main thread
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(this, 
                            "✅ Credentials sent to " + email + "\nPlease check email app.", 
                            android.widget.Toast.LENGTH_LONG).show();
                        
                        // Re-enable button
                        btnSendEmail.setEnabled(true);
                    });
                    
                    // Log the action
                    android.util.Log.d("AddOfficer", "📧 Email credentials sent to: " + email);
                    android.util.Log.d("AddOfficer", "Officer: " + officerName + " | Username: " + username);
                });
            });
            
            btnShowCredentials.setOnClickListener(v -> {
                dialog.dismiss();
                showCredentialsDialog(officerName, username, password, rank, badgeNumber);
            });
            
            btnDone.setOnClickListener(v -> {
                dialog.dismiss();
                finish();
            });
            
            // Apply custom background
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            
            dialog.show();
        } catch (Exception e) {
            android.util.Log.e("AddOfficer", "Error showing dialog", e);
            showCredentialsDialog(officerName, username, password, rank, badgeNumber);
        }
    }
    
    private void showCredentialsDialog(String officerName, String username, String password,
                                       String rank, String badgeNumber) {
        try {
            // Inflate custom dialog layout
            android.view.LayoutInflater inflater = getLayoutInflater();
            android.view.View dialogView = inflater.inflate(R.layout.dialog_officer_credentials, null);
            
            if (dialogView == null) {
                throw new Exception("Dialog view is null");
            }
            
            // Get views from dialog
            android.widget.TextView tvOfficerName = dialogView.findViewById(R.id.tvOfficerName);
            android.widget.TextView tvUsername = dialogView.findViewById(R.id.tvUsername);
            android.widget.TextView tvPassword = dialogView.findViewById(R.id.tvPassword);
            MaterialButton btnCopyCredentials = dialogView.findViewById(R.id.btnCopyCredentials);
            MaterialButton btnDone = dialogView.findViewById(R.id.btnDone);
            
            if (tvOfficerName == null || tvUsername == null || tvPassword == null || 
                btnCopyCredentials == null || btnDone == null) {
                throw new Exception("One or more views are null");
            }
            
            // Set data
            tvOfficerName.setText(officerName + "\n" + rank + " | Badge: " + badgeNumber);
            tvUsername.setText(username); // Username already has "Off." prefix
            tvPassword.setText(password);
            
            // Create dialog
            AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
            
            // Set button listeners
            btnCopyCredentials.setOnClickListener(v -> {
                // Copy both username and password
                String credentials = "Username: " + username + "\nPassword: " + password;
                copyToClipboard("Credentials", credentials);
                Toast.makeText(this, "Credentials copied to clipboard", Toast.LENGTH_SHORT).show();
            });
            
            btnDone.setOnClickListener(v -> {
                dialog.dismiss();
                finish();
            });
            
            // Show dialog
            dialog.show();
            
            // Dim the background
            if (dialog.getWindow() != null) {
                dialog.getWindow().setDimAmount(0.7f); // 70% dim
            }
            
            android.util.Log.d("AddOfficer", "Dialog shown successfully");
        } catch (Exception e) {
            android.util.Log.e("AddOfficer", "Error showing dialog: " + e.getMessage(), e);
            e.printStackTrace();
            finish();
        }
    }
    
    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }
    
    private void generateBadgeNumber() {
        // Generate badge number format: PNP-YYYY-XXXXX
        int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        Random random = new Random();
        int randomNum = 10000 + random.nextInt(90000); // 5-digit random number
        
        String badgeNumber = "PNP-" + year + "-" + randomNum;
        etBadgeNumber.setText(badgeNumber);
    }
}
