package com.example.blottermanagementsystem.ui.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.User;
import com.example.blottermanagementsystem.utils.PreferencesManager;

public class EditProfileActivity extends AppCompatActivity {
    
    private TextInputEditText etFirstName, etLastName;
    private MaterialButton btnSave, btnCancel;
    private PreferencesManager preferencesManager;
    private int userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
        // Initialize
        preferencesManager = new PreferencesManager(this);
        userId = preferencesManager.getUserId();
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Find views
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        
        // Load current data
        loadCurrentData();
        
        // Setup buttons
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfile());
    }
    
    private void loadCurrentData() {
        new Thread(() -> {
            BlotterDatabase db = BlotterDatabase.getDatabase(this);
            User user = db.userDao().getUserById(userId);
            
            runOnUiThread(() -> {
                if (user != null) {
                    etFirstName.setText(user.getFirstName());
                    etLastName.setText(user.getLastName());
                }
            });
        }).start();
    }
    
    private void saveProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        
        // Validate
        if (firstName.isEmpty()) {
            etFirstName.setError("Required");
            etFirstName.requestFocus();
            return;
        }
        
        if (lastName.isEmpty()) {
            etLastName.setError("Required");
            etLastName.requestFocus();
            return;
        }
        
        // Show loading for profile update
        com.example.blottermanagementsystem.utils.GlobalLoadingManager.show(this, "Updating profile...");
        
        new Thread(() -> {
            try {
            BlotterDatabase db = BlotterDatabase.getDatabase(this);
            User user = db.userDao().getUserById(userId);
            
            if (user != null) {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                db.userDao().updateUser(user);
                
                runOnUiThread(() -> {
                    com.example.blottermanagementsystem.utils.GlobalLoadingManager.hide();
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    com.example.blottermanagementsystem.utils.GlobalLoadingManager.hide();
                    Toast.makeText(this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
