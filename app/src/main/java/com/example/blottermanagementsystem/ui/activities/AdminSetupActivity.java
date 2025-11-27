package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
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

public class AdminSetupActivity extends AppCompatActivity {
    
    private TextInputEditText etFirstName, etLastName, etUsername, etPassword;
    private MaterialButton btnCreateAdmin;
    private BlotterDatabase database;
    private PreferencesManager preferencesManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_setup);
        
        database = BlotterDatabase.getDatabase(this);
        preferencesManager = new PreferencesManager(this);
        
        setupToolbar();
        initViews();
        setupListeners();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Setup");
        }
    }
    
    private void initViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnCreateAdmin = findViewById(R.id.btnCreateAdmin);
    }
    
    private void setupListeners() {
        btnCreateAdmin.setOnClickListener(v -> createAdmin());
    }
    
    private void createAdmin() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // Validation
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
        
        if (username.isEmpty()) {
            etUsername.setError("Required");
            etUsername.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            etPassword.setError("Required");
            etPassword.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            etPassword.setError("Min 6 characters");
            etPassword.requestFocus();
            return;
        }
        
        // Show loading for admin creation
        com.example.blottermanagementsystem.utils.GlobalLoadingManager.show(this, "Creating admin...");
        
        new Thread(() -> {
            try {
            User admin = new User("Admin", "User", "admin", "admin123", "Admin");
            admin.setCreatedAt(System.currentTimeMillis());
            
            long userId = database.userDao().insertUser(admin);
            
            runOnUiThread(() -> {
                com.example.blottermanagementsystem.utils.GlobalLoadingManager.hide();
                
                // Navigate to login
                Intent intent = new Intent(this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    com.example.blottermanagementsystem.utils.GlobalLoadingManager.hide();
                    Toast.makeText(this, "Error creating admin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    @Override
    public void onBackPressed() {
        // Prevent back navigation during setup
        Toast.makeText(this, "Please complete admin setup", Toast.LENGTH_SHORT).show();
    }
}
