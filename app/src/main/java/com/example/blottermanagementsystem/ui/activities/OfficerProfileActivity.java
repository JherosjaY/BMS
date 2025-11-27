package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.User;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;
import java.util.concurrent.Executors;

public class OfficerProfileActivity extends BaseActivity {
    
    private PreferencesManager preferencesManager;
    private BlotterDatabase database;
    private User currentUser;
    
    // Views
    private Toolbar toolbar;
    private ImageView ivProfilePicture;
    private TextView tvProfileEmoji;
    private CardView cardProfilePic;
    private TextView tvName, tvUsername, tvFirstName, tvLastName, tvUsernameInfo, tvEmailInfo;
    private Chip chipRole;
    private CardView btnEditProfile, btnChangePassword, btnSettings;
    private MaterialButton btnDeleteAccount, btnLogout;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnChangePhoto;
    
    // Image picker launcher
    private ActivityResultLauncher<String> imagePickerLauncher;
    private String selectedImageUri;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        preferencesManager = new PreferencesManager(this);
        database = BlotterDatabase.getDatabase(this);
        
        setupImagePicker();
        initViews();
        setupToolbar();
        loadUserData();
        setupListeners();
    }
    
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // Try to grant persistent permission
                    try {
                        getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception e) {
                        // Ignore if not supported
                    }
                    
                    selectedImageUri = uri.toString();
                    android.util.Log.d("OfficerProfileActivity", "=== PROFILE PICTURE SELECTED ===");
                    android.util.Log.d("OfficerProfileActivity", "Selected URI: " + selectedImageUri);
                    android.util.Log.d("OfficerProfileActivity", "currentUser is null? " + (currentUser == null));
                    
                    preferencesManager.setProfileImageUri(selectedImageUri);
                    
                    // Also update database so dashboard can see the change
                    if (currentUser != null) {
                        currentUser.setProfilePhotoUri(selectedImageUri);
                        Executors.newSingleThreadExecutor().execute(() -> {
                            database.userDao().updateUser(currentUser);
                            android.util.Log.d("OfficerProfileActivity", "✅ Profile photo saved to database: " + selectedImageUri);
                        });
                    } else {
                        android.util.Log.e("OfficerProfileActivity", "❌ ERROR: Cannot save profile photo - currentUser is NULL!");
                        // Try to load user and save
                        int userId = preferencesManager.getUserId();
                        Executors.newSingleThreadExecutor().execute(() -> {
                            User user = database.userDao().getUserById(userId);
                            if (user != null) {
                                user.setProfilePhotoUri(selectedImageUri);
                                database.userDao().updateUser(user);
                                currentUser = user;
                                android.util.Log.d("OfficerProfileActivity", "✅ Profile photo saved after loading user");
                            } else {
                                android.util.Log.e("OfficerProfileActivity", "❌ Still cannot find user with ID: " + userId);
                            }
                        });
                    }
                    
                    loadProfilePicture();
                    Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvProfileEmoji = findViewById(R.id.tvProfileEmoji);
        cardProfilePic = findViewById(R.id.cardProfilePic);
        tvName = findViewById(R.id.tvName);
        tvUsername = findViewById(R.id.tvUsername);
        tvFirstName = findViewById(R.id.tvFirstName);
        tvLastName = findViewById(R.id.tvLastName);
        tvUsernameInfo = findViewById(R.id.tvUsernameInfo);
        tvEmailInfo = findViewById(R.id.tvEmailInfo);
        chipRole = findViewById(R.id.chipRole);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnSettings = findViewById(R.id.btnSettings);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnLogout = findViewById(R.id.btnLogout);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Officer Profile");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void loadUserData() {
        int userId = preferencesManager.getUserId();
        android.util.Log.d("OfficerProfileActivity", "=== LOADING USER DATA ===");
        android.util.Log.d("OfficerProfileActivity", "UserID from PreferencesManager: " + userId);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            // Check total users in database
            List<User> allUsers = database.userDao().getAllUsers();
            android.util.Log.d("OfficerProfileActivity", "Total users in database: " + allUsers.size());
            for (User u : allUsers) {
                android.util.Log.d("OfficerProfileActivity", "  User: ID=" + u.getId() + ", Username=" + u.getUsername() + ", FirstName=" + u.getFirstName());
            }
            
            currentUser = database.userDao().getUserById(userId);
            
            android.util.Log.d("OfficerProfileActivity", "User loaded: " + (currentUser != null ? "YES" : "NULL"));
            if (currentUser != null) {
                android.util.Log.d("OfficerProfileActivity", "FirstName: " + currentUser.getFirstName());
                android.util.Log.d("OfficerProfileActivity", "LastName: " + currentUser.getLastName());
                android.util.Log.d("OfficerProfileActivity", "Username: " + currentUser.getUsername());
            } else {
                android.util.Log.e("OfficerProfileActivity", "ERROR: User with ID " + userId + " NOT FOUND!");
            }
            
            runOnUiThread(() -> {
                if (currentUser != null) {
                    displayUserInfo();
                    loadProfilePicture();
                } else {
                    Toast.makeText(this, "ERROR: User not found in database!", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
    
    private void displayUserInfo() {
        // Get data from database
        String firstName = currentUser.getFirstName() != null ? currentUser.getFirstName() : "";
        String lastName = currentUser.getLastName() != null ? currentUser.getLastName() : "";
        String username = currentUser.getUsername() != null ? currentUser.getUsername() : "";
        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "Not provided";
        
        // Display name
        String fullName = (firstName + " " + lastName).trim();
        if (fullName.isEmpty()) {
            fullName = "Officer Account";
        }
        tvName.setText(fullName);
        tvUsername.setText("@" + username);
        
        // Display in cards
        tvFirstName.setText(firstName);
        tvLastName.setText(lastName);
        tvUsernameInfo.setText(username);
        tvEmailInfo.setText(email);
        
        // Set role chip for Officer
        chipRole.setText("Officer");
        chipRole.setChipBackgroundColorResource(android.R.color.transparent);
        chipRole.setChipStrokeColorResource(R.color.info_blue);
        chipRole.setTextColor(getResources().getColor(R.color.info_blue, null));
        
        // Show gender-appropriate emoji for officers
        showOfficerEmoji();
    }
    
    private void showOfficerEmoji() {
        String gender = preferencesManager.getGender();
        if ("Female".equalsIgnoreCase(gender)) {
            tvProfileEmoji.setText("👮‍♀️");
        } else {
            tvProfileEmoji.setText("👮‍♂️");
        }
        tvProfileEmoji.setVisibility(View.VISIBLE);
        ivProfilePicture.setVisibility(View.GONE);
        btnChangePhoto.setVisibility(View.GONE);
    }
    
    private void loadProfilePicture() {
        // Officers use emoji instead of profile pictures
        showOfficerEmoji();
    }
    
    private void setupListeners() {
        // Change Photo - Hidden for officers
        btnChangePhoto.setVisibility(View.GONE);
        
        // Edit Profile - Show dialog instead of new activity
        btnEditProfile.setOnClickListener(v -> {
            showEditProfileDialog();
        });
        
        // Change Password - Show dialog instead of new activity
        btnChangePassword.setOnClickListener(v -> {
            showChangePasswordDialog();
        });
        
        // Settings
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
        
        // Delete Account
        btnDeleteAccount.setOnClickListener(v -> {
            showDeleteAccountDialog();
        });
        
        // Logout (Last)
        btnLogout.setOnClickListener(v -> {
            showLogoutDialog();
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
        // Sign out from Google if user was signed in with Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Clear app session
            preferencesManager.clearSession();
            
            // Navigate to login screen (not welcome screen)
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    private void showDeleteAccountDialog() {
        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_account, null);
        com.google.android.material.button.MaterialButton btnCancelDelete = dialogView.findViewById(R.id.btnCancelDelete);
        com.google.android.material.button.MaterialButton btnConfirmDelete = dialogView.findViewById(R.id.btnConfirmDelete);
        EditText etDeleteConfirmation = dialogView.findViewById(R.id.etDeleteConfirmation);
        
        // Create dialog
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();
        
        // Set button listeners
        btnCancelDelete.setOnClickListener(v -> dialog.dismiss());
        
        btnConfirmDelete.setOnClickListener(v -> {
            String userInput = etDeleteConfirmation.getText().toString().trim();
            if ("DELETE MY ACCOUNT".equals(userInput)) {
                dialog.dismiss();
                performAccountDeletion();
            } else {
                Toast.makeText(this, "Please type exactly: DELETE MY ACCOUNT", Toast.LENGTH_SHORT).show();
                etDeleteConfirmation.requestFocus();
            }
        });
        
        // Make dialog background transparent
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }
    
    private void performAccountDeletion() {
        int userId = preferencesManager.getUserId();
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Delete all user's reports
                database.blotterReportDao().deleteReportsByUserId(userId);
                
                // Delete all user's notifications
                database.notificationDao().deleteAllByUserId(userId);
                
                // Delete corresponding Officer record (if exists)
                // Get officer by user ID or name
                String username = currentUser.getUsername();
                List<com.example.blottermanagementsystem.data.entity.Officer> officers = 
                    database.officerDao().getAllOfficers();
                
                for (com.example.blottermanagementsystem.data.entity.Officer officer : officers) {
                    // Match by name or other identifier
                    if (officer.getName() != null && 
                        officer.getName().equals(currentUser.getFirstName() + " " + currentUser.getLastName())) {
                        database.officerDao().deleteOfficer(officer);
                        android.util.Log.d("OfficerProfileActivity", "✅ Officer record deleted: " + officer.getName());
                        break;
                    }
                }
                
                // Delete user account
                database.userDao().deleteUser(currentUser);
                
                runOnUiThread(() -> {
                    // Clear session
                    preferencesManager.clearSession();
                    
                    // Navigate to login (not welcome screen)
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error deleting account: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                    android.util.Log.e("OfficerProfileActivity", "Error deleting account", e);
                });
            }
        });
    }
    
    private void showEditProfileDialog() {
        if (currentUser == null) {
            Toast.makeText(this, "User data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText etFirstName = dialogView.findViewById(R.id.etFirstName);
        TextInputEditText etLastName = dialogView.findViewById(R.id.etLastName);
        com.google.android.material.button.MaterialButton btnCancelEdit = dialogView.findViewById(R.id.btnCancelEdit);
        com.google.android.material.button.MaterialButton btnSaveEdit = dialogView.findViewById(R.id.btnSaveEdit);
        
        // Pre-fill with current data
        etFirstName.setText(currentUser.getFirstName());
        etLastName.setText(currentUser.getLastName());
        
        // Create dialog
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();
        
        // Set button listeners
        btnCancelEdit.setOnClickListener(v -> dialog.dismiss());
        
        btnSaveEdit.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            
            if (firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Update user
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            
            Executors.newSingleThreadExecutor().execute(() -> {
                database.userDao().updateUser(currentUser);
                runOnUiThread(() -> {
                    loadUserData();
                    dialog.dismiss();
                });
            });
        });
        
        // Make dialog background transparent
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }
    
    private void showChangePasswordDialog() {
        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        com.google.android.material.button.MaterialButton btnCancelPassword = dialogView.findViewById(R.id.btnCancelPassword);
        com.google.android.material.button.MaterialButton btnChangePassword = dialogView.findViewById(R.id.btnChangePassword);
        
        // Create dialog
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();
        
        // Set button listeners
        btnCancelPassword.setOnClickListener(v -> dialog.dismiss());
        
        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!com.example.blottermanagementsystem.utils.SecurityUtils.verifyPassword(currentPassword, currentUser.getPassword())) {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Update password with proper hashing
            String hashedNewPassword = com.example.blottermanagementsystem.utils.SecurityUtils.hashPassword(newPassword);
            currentUser.setPassword(hashedNewPassword);
            
            Executors.newSingleThreadExecutor().execute(() -> {
                database.userDao().updateUser(currentUser);
                runOnUiThread(() -> {
                    dialog.dismiss();
                });
            });
        });
        
        // Make dialog background transparent
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload user data when returning from edit screens
        loadUserData();
        loadProfilePicture();
    }
}
