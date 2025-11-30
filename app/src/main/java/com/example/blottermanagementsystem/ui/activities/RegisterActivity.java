package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.User;
import com.example.blottermanagementsystem.services.EmailAuthService;
import com.example.blottermanagementsystem.utils.NetworkConnectivityManager;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.example.blottermanagementsystem.viewmodel.AuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends BaseActivity {
    
    private TextInputEditText etUsernameField, etUsername, etPassword, etConfirmPassword;
    private TextInputEditText etFirstName, etLastName; // 📧 NEW: Name fields for email sign-up
    private MaterialButton btnRegister;
    private TextView tvError, tvLogin;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private PreferencesManager preferencesManager;
    
    // 📧 NEW: Email authentication service
    private EmailAuthService emailAuthService;
    private NetworkConnectivityManager networkConnectivityManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        preferencesManager = new PreferencesManager(this);
        setupGoogleSignIn();
        initViews();
        setupViewModel();
        setupListeners();
        animateViews();
    }
    
    private void setupGoogleSignIn() {
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build();
        
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        
        // Register for activity result
        googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleGoogleSignInResult(task);
            }
        );
    }
    
    private void initViews() {
        etUsernameField = findViewById(R.id.etUsernameField);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvError = findViewById(R.id.tvError);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
        
        // 📧 NEW: Initialize name fields (if they exist in layout)
        try {
            etFirstName = findViewById(R.id.etFirstName);
            etLastName = findViewById(R.id.etLastName);
        } catch (Exception e) {
            android.util.Log.w("RegisterActivity", "⚠️ Name fields not found in layout");
        }
        
        // 📧 NEW: Initialize services
        emailAuthService = new EmailAuthService();
        networkConnectivityManager = new NetworkConnectivityManager(this);
        android.util.Log.d("RegisterActivity", "✅ EmailAuthService and NetworkConnectivityManager initialized");
    }
    
    private void setupViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        // Observe registerState instead of authState for registration
        authViewModel.getRegisterState().observe(this, authState -> {
            if (authState == AuthViewModel.AuthState.LOADING) {
                showLoading(true);
            } else if (authState == AuthViewModel.AuthState.SUCCESS) {
                showLoading(false);
                handleRegisterSuccess();
            } else if (authState == AuthViewModel.AuthState.EMAIL_EXISTS) {
                showLoading(false);
                showError("This email is already registered. Please use a different email or sign in.");
            } else if (authState == AuthViewModel.AuthState.ERROR) {
                showLoading(false);
                showError("Registration failed. Username may already exist.");
            } else {
                showLoading(false);
            }
        });
    }
    
    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        
        tvLogin.setOnClickListener(v -> {
            // Go back to Login screen
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
    
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            
            // Get user info
            String googleId = account.getId(); // ✅ Use actual Google ID
            String email = account.getEmail();
            String displayName = account.getDisplayName();
            String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
            
            // Parse display name into first and last name
            final String firstName;
            final String lastName;
            if (displayName != null && !displayName.isEmpty()) {
                String[] nameParts = displayName.split(" ", 2);
                firstName = nameParts[0];
                if (nameParts.length > 1) {
                    lastName = nameParts[1];
                } else {
                    lastName = "Account";
                }
            } else {
                firstName = "User";
                lastName = "Account";
            }
            
            // Save Google account info
            preferencesManager.saveGoogleAccountInfo(email, displayName, photoUrl);
            
            // ✅ Call backend API to register Google user
            registerGoogleUserViaBackend(googleId, email, firstName, lastName, photoUrl);
            
        } catch (ApiException e) {
            Toast.makeText(this, "Google Sign-Up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void registerGoogleUserViaBackend(String googleId, String email, String firstName, String lastName, String photoUrl) {
        // Create request body for backend
        java.util.Map<String, Object> googleAuthData = new java.util.HashMap<>();
        googleAuthData.put("googleId", googleId); // ✅ Use actual Google ID
        googleAuthData.put("email", email);
        googleAuthData.put("firstName", firstName);
        googleAuthData.put("lastName", lastName);
        googleAuthData.put("profilePictureUrl", photoUrl != null ? photoUrl : "");
        
        // Call backend API
        com.example.blottermanagementsystem.utils.ApiClient.getApiService().googleSignIn(googleAuthData)
            .enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                @Override
                public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, 
                                     retrofit2.Response<java.util.Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        android.util.Log.d("RegisterActivity", "✅ Google user registered in backend");
                        
                        // Save to local preferences
                        preferencesManager.setLoggedIn(true);
                        preferencesManager.setFirstName(firstName);
                        preferencesManager.setLastName(lastName);
                        preferencesManager.setUserRole("user");
                        
                        Toast.makeText(RegisterActivity.this, "Google signup successful!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to Profile Picture Selection
                        Intent intent = new Intent(RegisterActivity.this, ProfilePictureSelectionActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        android.util.Log.e("RegisterActivity", "❌ Backend registration failed: " + response.code());
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                    android.util.Log.e("RegisterActivity", "❌ Network error: " + t.getMessage());
                    Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void attemptRegister() {
        String username = etUsernameField.getText().toString().trim();
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String firstName = etFirstName != null ? etFirstName.getText().toString().trim() : "";
        String lastName = etLastName != null ? etLastName.getText().toString().trim() : "";
        
        if (username.isEmpty() || email.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty() ||
            firstName.isEmpty() || lastName.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        // Strict password validation
        String passwordError = validateStrongPassword(password);
        if (passwordError != null) {
            showError(passwordError);
            return;
        }
        
        // 📧 NEW: Check internet connection
        if (!networkConnectivityManager.isConnectedToInternet()) {
            showError("❌ No internet connection\n\nThis app requires internet to sign up.");
            return;
        }
        
        hideError();
        
        // Show loading on button (disable and show progress)
        btnRegister.setEnabled(false);
        btnRegister.setText("Creating account...");
        
        // 📧 NEW: Use EmailAuthService for sign-up
        android.util.Log.d("RegisterActivity", "📧 Attempting email sign-up with: " + email);
        emailAuthService.signUpWithEmail(email, password, firstName, lastName, 
            new EmailAuthService.AuthCallback() {
                @Override
                public void onSuccess(String message, String userId) {
                    android.util.Log.d("RegisterActivity", "✅ Sign-up successful!");
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                    
                    // Redirect to LoginActivity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("email", email); // Pre-fill email on login
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
                
                @Override
                public void onError(String errorMessage) {
                    android.util.Log.e("RegisterActivity", "❌ Sign-up failed: " + errorMessage);
                    showError("Sign-up failed: " + errorMessage);
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Create Account");
                }
                
                @Override
                public void onLoading() {
                    android.util.Log.d("RegisterActivity", "🔄 Signing up...");
                }
            });
    }
    
    private void registerWithHybridApproach(String username, String email, String password) {
        // Check if online
        com.example.blottermanagementsystem.utils.NetworkMonitor networkMonitor = 
            new com.example.blottermanagementsystem.utils.NetworkMonitor(this);
        
        if (networkMonitor.isNetworkAvailable()) {
            // ✅ ONLINE: Call backend API
            registerWithBackendAPI(username, email, password);
        } else {
            // ✅ OFFLINE: Save to local database
            registerLocally(username, email, password);
        }
    }
    
    private void registerWithBackendAPI(String username, String email, String password) {
        // Create request body
        java.util.Map<String, Object> registrationData = new java.util.HashMap<>();
        registrationData.put("username", username);
        registrationData.put("email", email);
        registrationData.put("password", password);
        registrationData.put("firstName", "User");
        registrationData.put("lastName", "Account");
        
        // Initialize API client with preferences
        com.example.blottermanagementsystem.utils.ApiClient.initApiClient(preferencesManager);
        
        // Call backend API
        com.example.blottermanagementsystem.utils.ApiClient.getApiService().register(registrationData)
            .enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                @Override
                public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, 
                                     retrofit2.Response<java.util.Map<String, Object>> response) {
                    com.example.blottermanagementsystem.utils.GlobalLoadingManager.hide();
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Create account");
                    
                    if (response.isSuccessful() && response.body() != null) {
                        android.util.Log.d("RegisterActivity", "✅ Backend registration successful");
                        
                        // ✅ PROFESSIONAL APPROACH: DO NOT auto-login
                        // User must manually login with their credentials
                        // This is more secure and professional
                        
                        Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Show credentials dialog and redirect to login
                        handleRegisterSuccess(username, email, password);
                    } else {
                        // Handle specific error codes from backend
                        handleRegistrationError(response);
                    }
                }
                
                @Override
                public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                    android.util.Log.e("RegisterActivity", "❌ Network error: " + t.getMessage());
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Create account");
                    // Fallback to local registration
                    registerLocally(username, email, password);
                }
            });
    }
    
    private void handleRegistrationError(retrofit2.Response<java.util.Map<String, Object>> response) {
        int statusCode = response.code();
        String errorMessage = "Registration failed";
        
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                android.util.Log.e("RegisterActivity", "❌ Error response: " + errorBody);
                
                // Try to parse error as JSON
                org.json.JSONObject errorJson = new org.json.JSONObject(errorBody);
                String error = errorJson.optString("error", "");
                String message = errorJson.optString("message", "");
                
                // Handle specific error codes
                if ("DUPLICATE_EMAIL".equals(error)) {
                    errorMessage = "Email already registered. Please use another email.";
                    showError(errorMessage);
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Create account");
                    return;
                } else if ("DUPLICATE_USERNAME".equals(error)) {
                    errorMessage = "Username already taken. Please choose another.";
                    showError(errorMessage);
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Create account");
                    return;
                } else if ("EMAIL_EXISTS_DIFFERENT_METHOD".equals(error)) {
                    errorMessage = "Email already registered with a different account.";
                    showError(errorMessage);
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Create account");
                    return;
                } else if (!message.isEmpty()) {
                    errorMessage = message;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("RegisterActivity", "Error parsing error response: " + e.getMessage());
        }
        
        // If 409 (Conflict), show specific error
        if (statusCode == 409) {
            showError(errorMessage);
            btnRegister.setEnabled(true);
            btnRegister.setText("Create account");
        } else {
            // For other errors, fallback to local registration
            android.util.Log.e("RegisterActivity", "❌ Backend registration failed: " + statusCode);
            registerLocally(etUsernameField.getText().toString().trim(), 
                          etUsername.getText().toString().trim(), 
                          etPassword.getText().toString().trim());
        }
    }
    
    private void registerLocally(String username, String email, String password) {
        android.util.Log.d("RegisterActivity", "📱 Saving to local database (offline mode)");
        
        // Hash the password
        String hashedPassword = hashPassword(password);
        
        // Create user for local database
        User newUser = new User("User", "Account", username, hashedPassword, "User");
        newUser.setEmail(email);
        
        // Save to local database via ViewModel
        authViewModel.register(newUser);
    }
    
    private void handleRegisterSuccess() {
        // Get user details from input fields
        String username = etUsernameField.getText().toString().trim();
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // Show credentials dialog
        showCredentialsDialog(username, email, password);
    }
    
    private void handleRegisterSuccess(String username, String email, String password) {
        // Show credentials dialog with provided credentials
        showCredentialsDialog(username, email, password);
    }
    
    private void showCredentialsDialog(String username, String email, String password) {
        try {
            // Inflate custom dialog layout
            android.view.LayoutInflater inflater = getLayoutInflater();
            android.view.View dialogView = inflater.inflate(R.layout.dialog_user_credentials, null);
            
            // Get views from dialog
            android.widget.TextView tvUserName = dialogView.findViewById(R.id.tvUserName);
            android.widget.TextView tvUsername = dialogView.findViewById(R.id.tvUsername);
            android.widget.TextView tvPassword = dialogView.findViewById(R.id.tvPassword);
            MaterialButton btnCopyCredentials = dialogView.findViewById(R.id.btnCopyCredentials);
            MaterialButton btnDone = dialogView.findViewById(R.id.btnDone);
            
            // Set data - Show username and password for login
            tvUserName.setText("Account Created Successfully!");
            tvUsername.setText(username);
            tvPassword.setText(password);
            
            android.util.Log.d("RegisterActivity", "✅ Showing credentials dialog - Username: " + username + ", Email: " + email);
            
            // Create dialog
            AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
            
            // Set button listeners
            btnCopyCredentials.setOnClickListener(v -> {
                // Copy both username and password for easy login
                String credentials = "Username: " + username + "\nPassword: " + password;
                copyToClipboard("Login Credentials", credentials);
                Toast.makeText(this, "Credentials copied to clipboard", Toast.LENGTH_SHORT).show();
            });
            
            btnDone.setOnClickListener(v -> {
                dialog.dismiss();
                android.util.Log.d("RegisterActivity", "✅ User confirmed credentials - redirecting to LoginActivity");
                
                // ✅ PROFESSIONAL APPROACH: Redirect to LoginActivity for manual login
                // User must enter their credentials to verify they saved them
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
            
            // Show dialog
            dialog.show();
            
            android.util.Log.d("RegisterActivity", "✅ Credentials dialog shown successfully");
        } catch (Exception e) {
            android.util.Log.e("RegisterActivity", "❌ Error showing credentials dialog", e);
            // Navigate to Login screen without showing dialog
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
    
    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }
    
    private void showLoading(boolean show) {
        if (show) {
            btnRegister.setEnabled(false);
            btnRegister.setText("Creating account...");
        } else {
            btnRegister.setEnabled(true);
            btnRegister.setText("Create account");
        }
        etUsernameField.setEnabled(!show);
        etUsername.setEnabled(!show);
        etPassword.setEnabled(!show);
        etConfirmPassword.setEnabled(!show);
    }
    
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
    
    private void hideError() {
        tvError.setVisibility(View.GONE);
    }
    
    private void animateViews() {
        View registerCard = findViewById(R.id.registerCard);
        
        // Register card animation - fade in and slide up
        if (registerCard != null) {
            registerCard.setAlpha(0f);
            registerCard.setTranslationY(50f);
            registerCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setStartDelay(200)
                .start();
        }
    }
    
    /**
     * Validate strong password requirements
     * Returns null if valid, error message if invalid
     */
    private String validateStrongPassword(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }
        
        if (!hasUpperCase) {
            return "Password must contain at least 1 uppercase letter";
        }
        if (!hasLowerCase) {
            return "Password must contain at least 1 lowercase letter";
        }
        if (!hasDigit) {
            return "Password must contain at least 1 number";
        }
        if (!hasSpecialChar) {
            return "Password must contain at least 1 special character (@, #, $, etc.)";
        }
        
        return null; // Password is valid
    }
    
    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            android.util.Log.e("RegisterActivity", "Error hashing password", e);
            return password; // Fallback to plain text (not recommended)
        }
    }
}
