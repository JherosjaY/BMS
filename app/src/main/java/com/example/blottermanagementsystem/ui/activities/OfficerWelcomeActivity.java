package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.User;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.example.blottermanagementsystem.utils.SecurityUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.concurrent.Executors;

public class OfficerWelcomeActivity extends AppCompatActivity {
    
    private ImageButton btnBack;
    private TextView tvOfficerIcon;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private TextInputLayout tilCurrentPassword, tilNewPassword, tilConfirmPassword;
    private MaterialButton btnChangePassword;
    private PreferencesManager preferencesManager;
    private BlotterDatabase database;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_welcome);
        
        // Set status bar and navigation bar colors
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark_blue));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary_dark_blue));
        }
        
        preferencesManager = new PreferencesManager(this);
        database = BlotterDatabase.getDatabase(this);
        
        initViews();
        setupListeners();
        loadOfficerGender();
    }
    
    @Override
    public void onBackPressed() {
        // Prevent going back - must change password
        Toast.makeText(this, "You must change your password to continue", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        // Handle home button press
        if (keyCode == android.view.KeyEvent.KEYCODE_HOME) {
            // Logout and go to login page
            performLogout();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void performLogout() {
        // Clear session
        preferencesManager.logout();
        
        // Navigate to login
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvOfficerIcon = findViewById(R.id.tvOfficerIcon);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tilCurrentPassword = findViewById(R.id.tilCurrentPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> performLogout());
        btnChangePassword.setOnClickListener(v -> changePassword());
    }
    
    private void loadOfficerGender() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int userId = preferencesManager.getUserId();
            User user = database.userDao().getUserById(userId);
            
            runOnUiThread(() -> {
                if (user != null && user.getGender() != null) {
                    String gender = user.getGender();
                    if (gender.equalsIgnoreCase("Female")) {
                        tvOfficerIcon.setText("👮‍♀️"); // Female officer emoji
                    } else {
                        tvOfficerIcon.setText("👮‍♂️"); // Male officer emoji
                    }
                } else {
                    tvOfficerIcon.setText("👮"); // Default officer emoji
                }
            });
        });
    }
    
    /**
     * Validate password strength - Same as LoginActivity
     * Requirements:
     * - At least 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }
    
    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // Clear previous errors
        tilCurrentPassword.setBoxStrokeColor(getResources().getColor(R.color.text_input_box_stroke));
        tilNewPassword.setBoxStrokeColor(getResources().getColor(R.color.text_input_box_stroke));
        tilConfirmPassword.setBoxStrokeColor(getResources().getColor(R.color.text_input_box_stroke));
        
        // Validate inputs
        if (currentPassword.isEmpty()) {
            Toast.makeText(this, "⚠️ Please enter current password", Toast.LENGTH_SHORT).show();
            tilCurrentPassword.setBoxStrokeColor(getResources().getColor(R.color.error_red));
            etCurrentPassword.requestFocus();
            return;
        }
        
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "⚠️ Please enter new password", Toast.LENGTH_SHORT).show();
            tilNewPassword.setBoxStrokeColor(getResources().getColor(R.color.error_red));
            etNewPassword.requestFocus();
            return;
        }
        
        // STRICT PASSWORD VALIDATION - Same as login screen
        if (!isValidPassword(newPassword)) {
            Toast.makeText(this, "⚠️ Password must be at least 8 characters with uppercase, lowercase, number, and special character", Toast.LENGTH_LONG).show();
            tilNewPassword.setBoxStrokeColor(getResources().getColor(R.color.error_red));
            etNewPassword.requestFocus();
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "⚠️ Passwords do not match", Toast.LENGTH_SHORT).show();
            tilConfirmPassword.setBoxStrokeColor(getResources().getColor(R.color.error_red));
            etConfirmPassword.requestFocus();
            return;
        }
        
        // Verify current password and update
        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("Changing...");
        
        Executors.newSingleThreadExecutor().execute(() -> {
            int userId = preferencesManager.getUserId();
            User user = database.userDao().getUserById(userId);
            
            runOnUiThread(() -> {
                if (user == null) {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    btnChangePassword.setEnabled(true);
                    btnChangePassword.setText("Change Password & Continue");
                    return;
                }
                
                // Verify current password - handle both hashed and plain text passwords
                String storedPassword = user.getPassword();
                boolean passwordValid = false;
                
                // Check if stored password is hashed (64 hex characters for SHA-256)
                if (SecurityUtils.isPasswordHashed(storedPassword)) {
                    // Stored password is hashed - use verifyPassword
                    passwordValid = SecurityUtils.verifyPassword(currentPassword, storedPassword);
                } else {
                    // Stored password is plain text - direct comparison
                    passwordValid = currentPassword.equals(storedPassword);
                }
                
                if (!passwordValid) {
                    android.util.Log.d("OfficerWelcome", "❌ Incorrect password attempt");
                    
                    // Show error message with red outline
                    Toast.makeText(this, "❌ Invalid password input", Toast.LENGTH_SHORT).show();
                    tilCurrentPassword.setBoxStrokeColor(getResources().getColor(R.color.error_red));
                    etCurrentPassword.requestFocus();
                    
                    // Reset button state
                    btnChangePassword.setEnabled(true);
                    btnChangePassword.setText("Change Password & Continue");
                    return;
                }
                
                // Update password (hash the new password!)
                String hashedNewPassword = SecurityUtils.hashPassword(newPassword);
                user.setPassword(hashedNewPassword);
                
                android.util.Log.d("OfficerWelcome", "✅ Password changed successfully!");
                
                Executors.newSingleThreadExecutor().execute(() -> {
                    database.userDao().updateUser(user);
                    
                    runOnUiThread(() -> {
                        // Mark as password changed FIRST
                        preferencesManager.setPasswordChanged(true);
                        
                        // Small delay to ensure preference is saved
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            // Navigate to Officer Dashboard
                            Intent intent = new Intent(this, OfficerDashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }, 500);
                    });
                });
            });
        });
    }
}
