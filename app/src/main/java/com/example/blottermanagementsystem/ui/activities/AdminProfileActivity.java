package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.User;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.Executors;

public class AdminProfileActivity extends BaseActivity {
    
    private PreferencesManager preferencesManager;
    private BlotterDatabase database;
    private User currentUser;
    
    // Views
    private Toolbar toolbar;
    private TextView tvAdminName, tvUsername, tvFirstName, tvLastName, tvUsernameValue, tvRole;
    private CardView cardChangePassword, cardSystemSettings;
    private MaterialButton btnLogout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);
        
        preferencesManager = new PreferencesManager(this);
        database = BlotterDatabase.getDatabase(this);
        
        initViews();
        setupToolbar();
        setupListeners();
        loadUserData();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvAdminName = findViewById(R.id.tvAdminName);
        tvUsername = findViewById(R.id.tvUsername);
        tvFirstName = findViewById(R.id.tvFirstName);
        tvLastName = findViewById(R.id.tvLastName);
        tvUsernameValue = findViewById(R.id.tvUsernameValue);
        tvRole = findViewById(R.id.tvRole);
        
        cardChangePassword = findViewById(R.id.cardChangePassword);
        cardSystemSettings = findViewById(R.id.cardSystemSettings);
        
        btnLogout = findViewById(R.id.btnLogout);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Admin Profile");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupListeners() {
        cardChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        cardSystemSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
        
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }
    
    private void loadUserData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentUser = database.userDao().getUserById(preferencesManager.getUserId());
            
            runOnUiThread(() -> {
                if (currentUser != null) {
                    String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();
                    tvAdminName.setText(fullName);
                    tvUsername.setText("@" + currentUser.getUsername());
                    tvFirstName.setText(currentUser.getFirstName());
                    tvLastName.setText(currentUser.getLastName());
                    tvUsernameValue.setText(currentUser.getUsername());
                    tvRole.setText(currentUser.getRole());
                }
            });
        });
    }
    
    private void showEditProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("✏️ Edit Profile")
            .setView(dialogView)
            .setPositiveButton("SAVE", null)
            .setNegativeButton("CANCEL", null)
            .create();
        
        TextInputEditText etFirstName = dialogView.findViewById(R.id.etFirstName);
        TextInputEditText etLastName = dialogView.findViewById(R.id.etLastName);
        TextInputEditText etUsername = dialogView.findViewById(R.id.etUsername);
        
        etFirstName.setText(currentUser.getFirstName());
        etLastName.setText(currentUser.getLastName());
        etUsername.setText(currentUser.getUsername());
        
        dialog.setOnShowListener(dialogInterface -> {
            MaterialButton positiveButton = (MaterialButton) dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String firstName = etFirstName.getText().toString().trim();
                String lastName = etLastName.getText().toString().trim();
                String username = etUsername.getText().toString().trim();
                
                if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty()) {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                updateProfile(firstName, lastName, username);
                dialog.dismiss();
            });
        });
        
        dialog.show();
    }
    
    private void updateProfile(String firstName, String lastName, String username) {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            currentUser.setUsername(username);
            database.userDao().updateUser(currentUser);
            
            preferencesManager.setFirstName(firstName);
            preferencesManager.setLastName(lastName);
            
            runOnUiThread(() -> {
                loadUserData();
            });
        });
    }
    
    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        
        androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create();
        
        // Make dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        MaterialButton btnChange = dialogView.findViewById(R.id.btnChangePassword);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelPassword);
        
        btnChange.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!currentPassword.equals(currentUser.getPassword())) {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            
            changePassword(newPassword);
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private void changePassword(String newPassword) {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentUser.setPassword(newPassword);
            database.userDao().updateUser(currentUser);
            
            runOnUiThread(() -> {
            });
        });
    }
    
    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("LOGOUT", (dialog, which) -> {
                performLogout();
            })
            .setNegativeButton("CANCEL", null)
            .show();
    }
    
    private void performLogout() {
        preferencesManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showDeleteAccountDialog() {
        EditText etConfirmation = new EditText(this);
        etConfirmation.setHint("Type DELETE MY ACCOUNT to confirm");
        etConfirmation.setTextColor(getResources().getColor(R.color.text_primary, null));
        etConfirmation.setHintTextColor(getResources().getColor(R.color.text_secondary, null));
        
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 40, 50, 10);
        container.addView(etConfirmation);
        
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Delete Account?")
            .setMessage("This action cannot be undone. Type DELETE MY ACCOUNT to confirm:")
            .setView(container)
            .setPositiveButton("DELETE ACCOUNT", null)
            .setNegativeButton("CANCEL", null)
            .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            MaterialButton positiveButton = (MaterialButton) dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String confirmation = etConfirmation.getText().toString().trim();
                if (confirmation.equals("DELETE MY ACCOUNT")) {
                    performDeleteAccount();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Please type DELETE MY ACCOUNT to confirm", Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        dialog.show();
    }
    
    private void performDeleteAccount() {
        Executors.newSingleThreadExecutor().execute(() -> {
            database.userDao().deleteUser(currentUser);
            
            runOnUiThread(() -> {
                preferencesManager.logout();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
