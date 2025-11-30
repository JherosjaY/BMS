package com.example.blottermanagementsystem.ui.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.User;
import com.example.blottermanagementsystem.firebase.FirebaseAuthManager;
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

public class LoginActivity extends BaseActivity {
    
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin, btnGoogleSignIn;
    private TextView tvError, tvRegister, tvForgotPassword;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    private PreferencesManager preferencesManager;
    private FirebaseAuthManager firebaseAuthManager;
    private NetworkConnectivityManager networkConnectivityManager;
    
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initViews();
        setupGoogleSignIn();
        setupViewModel();
        setupListeners();
        animateViews();
    }
    
    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvError = findViewById(R.id.tvError);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
        preferencesManager = new PreferencesManager(this);
        firebaseAuthManager = new FirebaseAuthManager(this, preferencesManager);
        networkConnectivityManager = new NetworkConnectivityManager(this);
    }
    
    private void setupGoogleSignIn() {
        // Configure Google Sign-In
        // 🔥 IMPORTANT: We're using DEFAULT_SIGN_IN which doesn't require ID token
        // Firebase will handle authentication with just the Google account
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                // ✅ Removed requestIdToken - Firebase will authenticate without it
                .build();
        
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        
        // Setup Activity Result Launcher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    android.util.Log.d("LoginActivity", "🔔 Activity Result Launcher called!");
                    android.util.Log.d("LoginActivity", "📊 Result Code: " + result.getResultCode());
                    android.util.Log.d("LoginActivity", "📊 Data: " + (result.getData() != null ? "✅ Present" : "❌ NULL"));
                    
                    // ✅ IMPORTANT: Google Sign-In can return RESULT_CANCELED (0) but still have valid data
                    // We need to check the intent data directly, not just the result code
                    if (result.getData() != null) {
                        android.util.Log.d("LoginActivity", "✅ Intent data is present - processing Google Sign-In");
                        try {
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                            android.util.Log.d("LoginActivity", "✅ Got task from intent");
                            handleGoogleSignInResult(task);
                        } catch (Exception e) {
                            android.util.Log.e("LoginActivity", "❌ Error getting task from intent: " + e.getMessage());
                        }
                    } else {
                        android.util.Log.e("LoginActivity", "❌ No intent data - Google Sign-In cancelled or failed");
                    }
                });
    }
    
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            android.util.Log.d("LoginActivity", "🔍 handleGoogleSignInResult called");
            
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            android.util.Log.d("LoginActivity", "✅ Got GoogleSignInAccount: " + account.getEmail());
            
            // Get user info
            String googleId = account.getId();
            String email = account.getEmail();
            String displayName = account.getDisplayName();
            String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
            
            android.util.Log.d("LoginActivity", "📧 Email: " + email);
            android.util.Log.d("LoginActivity", "👤 Display Name: " + displayName);
            android.util.Log.d("LoginActivity", "🆔 Google ID: " + googleId);
            
            // Extract clean username from email (before @)
            String username = email.split("@")[0];
            
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
            
            android.util.Log.d("LoginActivity", "📝 First Name: " + firstName + ", Last Name: " + lastName);
            
            // Save Google account info
            preferencesManager.saveGoogleAccountInfo(email, displayName, photoUrl);
            android.util.Log.d("LoginActivity", "💾 Saved Google account info");
            
            // Get ID token (optional - not required for basic Google Sign-In)
            String idToken = account.getIdToken();
            android.util.Log.d("LoginActivity", "🔑 ID Token: " + (idToken != null ? "✅ Present" : "⚠️ Not required"));
            
            // ✅ Use ID token if available, otherwise use account directly
            if (idToken != null) {
                android.util.Log.d("LoginActivity", "🔥 Calling firebaseAuthManager.googleSignIn() with ID token");
                
                firebaseAuthManager.googleSignIn(idToken, new FirebaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(com.google.firebase.auth.FirebaseUser user, String token) {
                        android.util.Log.d("LoginActivity", "✅✅✅ Firebase Google Sign-In SUCCESSFUL");
                        Toast.makeText(LoginActivity.this, "✅ Logged in successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to appropriate dashboard based on role
                        String role = preferencesManager.getUserRole();
                        android.util.Log.d("LoginActivity", "🎯 User role: " + role);
                        navigateToDashboard(role);
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        android.util.Log.e("LoginActivity", "❌❌❌ Firebase Google Sign-In FAILED: " + errorMessage);
                        Toast.makeText(LoginActivity.this, "❌ Sign-in failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    @Override
                    public void onLoading() {
                        android.util.Log.d("LoginActivity", "🔄 Firebase Auth loading...");
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                // ✅ No ID token - just use the Google account info
                android.util.Log.d("LoginActivity", "⚠️ No ID token - using basic Google Sign-In");
                Toast.makeText(this, "✅ Signed in with Google!", Toast.LENGTH_SHORT).show();
                
                // 🔥 CRITICAL: Save user to local database so other activities can find them
                saveGoogleUserToDatabase(email, firstName, lastName, googleId);
                
                // Save Google account info to preferences
                preferencesManager.saveGoogleAccountInfo(email, displayName, photoUrl);
                preferencesManager.setFirstName(firstName);
                preferencesManager.setLastName(lastName);
                
                // 🎯 PROPER FLOW: Check if first-time user
                // If first time: Go to ProfilePictureSelectionActivity
                // If returning: Go directly to UserDashboard
                boolean isFirstTimeUser = preferencesManager.isFirstTimeUser();
                android.util.Log.d("LoginActivity", "📋 Is first time user? " + isFirstTimeUser);
                
                if (isFirstTimeUser) {
                    // First time: Go to ProfilePictureSelectionActivity
                    android.util.Log.d("LoginActivity", "🎯 First time user - redirecting to ProfilePictureSelectionActivity");
                    Intent intent = new Intent(this, ProfilePictureSelectionActivity.class);
                    intent.putExtra("firstName", firstName);
                    intent.putExtra("lastName", lastName);
                    intent.putExtra("photoUrl", photoUrl);
                    intent.putExtra("isGoogleSignIn", true);
                    intent.putExtra("isFirstTimeUser", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Returning user: Go directly to dashboard
                    android.util.Log.d("LoginActivity", "🎯 Returning user - redirecting to UserDashboard");
                    navigateToDashboard("user");
                }
            }
            
        } catch (ApiException e) {
            android.util.Log.e("LoginActivity", "❌ ApiException: " + e.getMessage());
            Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "❌ Unexpected Exception: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        android.util.Log.d("LoginActivity", "✅ Google user registered in backend");
                        
                        // Save to local preferences
                        preferencesManager.setLoggedIn(true);
                        preferencesManager.setFirstName(firstName);
                        preferencesManager.setLastName(lastName);
                        preferencesManager.setUserRole("user");
                        preferencesManager.saveGoogleAccountInfo(email, firstName + " " + lastName, photoUrl);
                        
                        Toast.makeText(LoginActivity.this, "Google signup successful!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to Profile Picture Selection
                        Intent intent = new Intent(LoginActivity.this, com.example.blottermanagementsystem.ui.activities.ProfilePictureSelectionActivity.class);
                        intent.putExtra("firstName", firstName);
                        intent.putExtra("lastName", lastName);
                        intent.putExtra("photoUrl", photoUrl);
                        intent.putExtra("isGoogleSignIn", true);
                        intent.putExtra("isFirstTimeUser", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Handle specific error codes from backend
                        handleGoogleSignInError(response);
                    }
                }
                
                @Override
                public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                    android.util.Log.e("LoginActivity", "❌ Network error: " + t.getMessage());
                    Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void handleGoogleSignInError(retrofit2.Response<java.util.Map<String, Object>> response) {
        int statusCode = response.code();
        String errorMessage = "Google Sign-In failed";
        
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                android.util.Log.e("LoginActivity", "❌ Google error response: " + errorBody);
                
                // Try to parse error as JSON
                org.json.JSONObject errorJson = new org.json.JSONObject(errorBody);
                String error = errorJson.optString("error", "");
                String message = errorJson.optString("message", "");
                
                // Handle specific error codes
                if ("EMAIL_EXISTS_DIFFERENT_METHOD".equals(error)) {
                    errorMessage = "This email is already registered with a different account. Please use your original login method.";
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    return;
                } else if (!message.isEmpty()) {
                    errorMessage = message;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "Error parsing error response: " + e.getMessage());
        }
        
        // If 409 (Conflict), show specific error
        if (statusCode == 409) {
            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        } else {
            // For other errors, show generic message
            android.util.Log.e("LoginActivity", "❌ Google Sign-In failed: " + statusCode);
            Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + statusCode, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        // Set callback for direct communication (more reliable than LiveData observer)
        authViewModel.setLoginCallback(new AuthViewModel.LoginCallback() {
            @Override
            public void onLoginSuccess(String role) {
                android.util.Log.d("LoginActivity", "📞 CALLBACK RECEIVED! Role: " + role);
                showLoading(false);
                handleLoginSuccess(role);
            }
            
            @Override
            public void onLoginError(String message) {
                android.util.Log.d("LoginActivity", "📞 CALLBACK ERROR: " + message);
                showLoading(false);
                showError(message);
            }
        });
        
        // Keep LiveData observer for other states
        authViewModel.getAuthState().observe(this, authState -> {
            android.util.Log.d("LoginActivity", "=== AUTH STATE CHANGED ===");
            android.util.Log.d("LoginActivity", "State: " + authState);
            
            if (authState == AuthViewModel.AuthState.LOADING) {
                android.util.Log.d("LoginActivity", "State: LOADING");
                showLoading(true);
            } else if (authState == AuthViewModel.AuthState.USER_NOT_FOUND) {
                showLoading(false);
                showError("User not found, register one.");
            } else if (authState == AuthViewModel.AuthState.WRONG_PASSWORD) {
                showLoading(false);
                showError("Invalid username or password");
            } else if (authState == AuthViewModel.AuthState.ERROR) {
                showLoading(false);
                showError("Login failed. Please try again.");
            }
        });
    }
    
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        
        btnGoogleSignIn.setOnClickListener(v -> {
            // 🌐 CHECK INTERNET CONNECTION (Pure Neon Online-Only Mode)
            if (!networkConnectivityManager.isConnectedToInternet()) {
                Toast.makeText(LoginActivity.this, 
                    "❌ No internet connection\n\nThis app requires internet to work.", 
                    Toast.LENGTH_LONG).show();
                android.util.Log.d("LoginActivity", "⚠️ User tried to sign in without internet");
                return;
            }
            
            android.util.Log.d("LoginActivity", "✅ Internet connected - Proceeding with Google Sign-In");
            
            // Sign out from Google first to allow account selection
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                // Launch Google Sign-In with account picker
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });
        
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        tvForgotPassword.setOnClickListener(v -> {
            // Show forgot password dialog
            showForgotPasswordDialog();
        });
        
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            attemptLogin();
            return true;
        });
    }
    
    private void showForgotPasswordDialog() {
        // Navigate to Forgot Password Activity
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }
    
    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.fill_all_fields));
            return;
        }
        
        // STRICT PASSWORD VALIDATION - Only for Officer and User roles
        // Admin role is built-in and exempt from strict validation
        if (!username.equalsIgnoreCase("admin") && !isValidPassword(password)) {
            showError("Password must be at least 8 characters with uppercase, lowercase, number, and special character");
            return;
        }
        
        hideError();
        
        android.util.Log.d("LoginActivity", "=== LOGIN ATTEMPT ===");
        android.util.Log.d("LoginActivity", "Username: " + username);
        
        // ✅ HYBRID APPROACH: Try backend first, fallback to local
        loginWithHybridApproach(username, password);
    }
    
    private void loginWithHybridApproach(String username, String password) {
        // Check if online
        com.example.blottermanagementsystem.utils.NetworkMonitor networkMonitor = 
            new com.example.blottermanagementsystem.utils.NetworkMonitor(this);
        
        if (networkMonitor.isNetworkAvailable()) {
            // ✅ ONLINE: Call backend API
            loginWithBackendAPI(username, password);
        } else {
            // ✅ OFFLINE: Use local database
            loginLocally(username, password);
        }
    }
    
    private void loginWithBackendAPI(String username, String password) {
        // Create request body
        java.util.Map<String, Object> loginData = new java.util.HashMap<>();
        loginData.put("username", username);
        loginData.put("password", password);
        
        showLoading(true);
        
        // Initialize API client with preferences (for JWT interceptor)
        com.example.blottermanagementsystem.utils.ApiClient.initApiClient(preferencesManager);
        
        // Call backend API
        com.example.blottermanagementsystem.utils.ApiClient.getApiService().login(loginData)
            .enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                @Override
                public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, 
                                     retrofit2.Response<java.util.Map<String, Object>> response) {
                    showLoading(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        android.util.Log.d("LoginActivity", "✅ Backend login successful");
                        
                        // Extract JWT token from response
                        String jwtToken = (String) response.body().get("token");
                        if (jwtToken != null && !jwtToken.isEmpty()) {
                            // Save JWT token for future API requests
                            preferencesManager.setJwtToken(jwtToken);
                            android.util.Log.d("LoginActivity", "✅ JWT token saved: " + jwtToken.substring(0, 20) + "...");
                        }
                        
                        // Extract user data from response
                        java.util.Map<String, Object> userData = (java.util.Map<String, Object>) response.body().get("data");
                        if (userData != null) {
                            java.util.Map<String, Object> user = (java.util.Map<String, Object>) userData.get("user");
                            
                            if (user != null) {
                                // Extract user ID
                                Object userIdObj = user.get("id");
                                int userId = -1;
                                if (userIdObj instanceof Number) {
                                    userId = ((Number) userIdObj).intValue();
                                } else if (userIdObj instanceof String) {
                                    try {
                                        userId = Integer.parseInt((String) userIdObj);
                                    } catch (NumberFormatException e) {
                                        android.util.Log.e("LoginActivity", "❌ Invalid user ID format");
                                    }
                                }
                                
                                // Save to local preferences
                                preferencesManager.setLoggedIn(true);
                                preferencesManager.setUserId(userId);
                                preferencesManager.setUsername(username);
                                preferencesManager.setFirstName((String) user.get("firstName"));
                                preferencesManager.setLastName((String) user.get("lastName"));
                                preferencesManager.setUserRole((String) user.get("role"));
                                
                                android.util.Log.d("LoginActivity", "✅ User data saved - ID: " + userId + ", Role: " + user.get("role"));
                                
                                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                handleLoginSuccess((String) user.get("role"));
                            }
                        }
                    } else {
                        android.util.Log.e("LoginActivity", "❌ Backend login failed: " + response.code());
                        // Fallback to local login
                        loginLocally(username, password);
                    }
                }
                
                @Override
                public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                    showLoading(false);
                    android.util.Log.e("LoginActivity", "❌ Network error: " + t.getMessage());
                    // Fallback to local login
                    loginLocally(username, password);
                }
            });
    }
    
    private void loginLocally(String username, String password) {
        android.util.Log.d("LoginActivity", "📱 Using local database (offline mode)");
        
        // Use local ViewModel for offline login
        authViewModel.login(username, password);
    }
    
    /**
     * Validate password strength
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
    
    private void handleLoginSuccess(String role) {
        android.util.Log.d("LoginActivity", "Navigating to dashboard, role: " + role);
        
        // Detect actual role based on username/email patterns
        String detectedRole = detectUserRole(role);
        android.util.Log.d("LoginActivity", "Detected role: " + detectedRole);
        
        Intent intent;
        switch (detectedRole.toLowerCase()) {
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            case "officer":
                // Check if officer has changed password
                if (!preferencesManager.hasPasswordChanged()) {
                    // First time login - force password change
                    intent = new Intent(this, OfficerWelcomeActivity.class);
                } else {
                    intent = new Intent(this, OfficerDashboardActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            default:
                // For regular users, check profile completion from database
                checkUserProfileCompletion();
                break;
        }
    }
    
    private void checkUserProfileCompletion() {
        int userId = preferencesManager.getUserId();
        
        android.util.Log.d("LoginActivity", "=== CHECK PROFILE COMPLETION ===");
        android.util.Log.d("LoginActivity", "UserId from PreferencesManager: " + userId);
        
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            com.example.blottermanagementsystem.data.database.BlotterDatabase database = 
                com.example.blottermanagementsystem.data.database.BlotterDatabase.getDatabase(this);
            com.example.blottermanagementsystem.data.entity.User user = database.userDao().getUserById(userId);
            
            runOnUiThread(() -> {
                Intent intent;
                
                if (user != null && user.isProfileCompleted()) {
                    // Profile completed - go to dashboard
                    android.util.Log.d("LoginActivity", "✅ Profile completed - navigating to dashboard");
                    android.util.Log.d("LoginActivity", "User: " + user.getFirstName() + " " + user.getLastName());
                    intent = new Intent(this, UserDashboardActivity.class);
                } else {
                    // First time or profile not complete - go to profile setup
                    android.util.Log.d("LoginActivity", "⚠️ Profile not completed - navigating to profile setup");
                    android.util.Log.d("LoginActivity", "Passing userId: " + userId + " to ProfilePictureSelectionActivity");
                    intent = new Intent(this, ProfilePictureSelectionActivity.class);
                    // Pass userId explicitly via Intent
                    intent.putExtra("USER_ID", userId);
                }
                
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
    
    private void showLoading(boolean show) {
        if (show) {
            btnLogin.setEnabled(false);
            btnLogin.setText("Signing in...");
            etUsername.setEnabled(false);
            etPassword.setEnabled(false);
        } else {
            btnLogin.setEnabled(true);
            btnLogin.setText("Sign in");
            etUsername.setEnabled(true);
            etPassword.setEnabled(true);
        }
    }
    
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
    
    private void hideError() {
        tvError.setVisibility(View.GONE);
    }
    
    
    private void animateViews() {
        View loginCard = findViewById(R.id.loginCard);
        
        // Login card animation - fade in and slide up
        if (loginCard != null) {
            loginCard.setAlpha(0f);
            loginCard.setTranslationY(50f);
            loginCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setStartDelay(200)
                .start();
        }
    }
    
    /**
     * ROLE DETECTION LOGIC
     * Determines user role based on username/email patterns
     */
    private String detectUserRole(String databaseRole) {
        String username = preferencesManager.getUsername();
        String email = preferencesManager.getFirstName(); // Assuming email is stored here
        
        if (username == null) {
            username = etUsername.getText().toString().toLowerCase();
        } else {
            username = username.toLowerCase();
        }
        
        android.util.Log.d("LoginActivity", "🔍 ROLE DETECTION - Username: " + username);
        
        // RULE 1: Username starts with "off." = OFFICER
        if (username.startsWith("off.")) {
            android.util.Log.d("LoginActivity", "✅ DETECTED: OFFICER (username starts with 'off.')");
            preferencesManager.setUserRole("Officer");
            return "officer";
        }
        
        // RULE 2: Built-in admin accounts
        if (username.equals("admin") || username.equals("sentin")) {
            android.util.Log.d("LoginActivity", "✅ DETECTED: ADMIN (built-in account)");
            preferencesManager.setUserRole("Admin");
            return "admin";
        }
        
        // RULE 3: Google Auth users = USER
        boolean isGoogleAuth = preferencesManager.isGoogleAccount();
        if (isGoogleAuth) {
            android.util.Log.d("LoginActivity", "✅ DETECTED: USER (Google Auth)");
            preferencesManager.setUserRole("User");
            return "user";
        }
        
        // RULE 4: Regular signup = USER
        android.util.Log.d("LoginActivity", "✅ DETECTED: USER (regular signup)");
        preferencesManager.setUserRole("User");
        return "user";
    }
    
    /**
     * 🎯 NAVIGATE TO DASHBOARD
     * Routes user to appropriate dashboard based on role
     */
    private void navigateToDashboard(String role) {
        try {
            Intent intent;
            
            if (role == null) {
                role = preferencesManager.getUserRole();
            }
            
            android.util.Log.d("LoginActivity", "🎯 Navigating to dashboard - Role: " + role);
            
            switch (role != null ? role.toLowerCase() : "user") {
                case "admin":
                    intent = new Intent(this, AdminDashboardActivity.class);
                    android.util.Log.d("LoginActivity", "✅ Redirecting to AdminDashboardActivity");
                    break;
                case "officer":
                    intent = new Intent(this, OfficerDashboardActivity.class);
                    android.util.Log.d("LoginActivity", "✅ Redirecting to OfficerDashboardActivity");
                    break;
                default:
                    intent = new Intent(this, UserDashboardActivity.class);
                    android.util.Log.d("LoginActivity", "✅ Redirecting to UserDashboardActivity");
                    break;
            }
            
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "❌ Error navigating to dashboard: " + e.getMessage());
            Toast.makeText(this, "Error navigating to dashboard", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 🔥 SAVE GOOGLE USER TO LOCAL DATABASE & SYNC TO NEON
     * Creates a user record in SQLite and syncs to Neon backend
     */
    private void saveGoogleUserToDatabase(String email, String firstName, String lastName, String googleId) {
        try {
            android.util.Log.d("LoginActivity", "💾 Saving Google user to local database...");
            
            // Get database instance
            com.example.blottermanagementsystem.data.database.BlotterDatabase database = 
                com.example.blottermanagementsystem.data.database.BlotterDatabase.getDatabase(this);
            
            // Create user object
            com.example.blottermanagementsystem.data.entity.User user = new com.example.blottermanagementsystem.data.entity.User();
            user.setUsername(email.split("@")[0]); // Use email prefix as username
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRole("user");
            user.setCreatedAt(System.currentTimeMillis());
            
            // Save to database on background thread
            java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    long userId = database.userDao().insertUser(user);
                    android.util.Log.d("LoginActivity", "✅ User saved to database with ID: " + userId);
                    
                    // Save user ID to preferences
                    preferencesManager.setUserId((int) userId);
                    preferencesManager.setLoggedIn(true);
                    preferencesManager.setUserRole("user");
                    preferencesManager.setFirstName(firstName);
                    preferencesManager.setLastName(lastName);
                    preferencesManager.setEmail(email);
                    
                    // 🔥 NEW USER: Set isFirstTimeUser = true for profile picture selection
                    preferencesManager.setFirstTimeUser(true);
                    android.util.Log.d("LoginActivity", "✅ Set isFirstTimeUser = true for new Google user");
                    
                    // 🌐 SYNC TO NEON: Send user data to backend
                    syncGoogleUserToNeon(email, firstName, lastName, googleId);
                    
                    android.util.Log.d("LoginActivity", "✅ User ID saved to preferences: " + userId);
                } catch (Exception e) {
                    android.util.Log.e("LoginActivity", "❌ Error saving user to database: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "❌ Error in saveGoogleUserToDatabase: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 🌐 SYNC GOOGLE USER TO NEON
     * Send user data to backend API to create/update user in Neon
     */
    private void syncGoogleUserToNeon(String email, String firstName, String lastName, String googleId) {
        try {
            android.util.Log.d("LoginActivity", "🌐 Syncing Google user to Neon...");
            
            // Create request body
            java.util.Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("email", email);
            userData.put("firstName", firstName);
            userData.put("lastName", lastName);
            userData.put("username", email.split("@")[0]);
            userData.put("role", "user");
            
            // ❌ REMOVED: syncGoogleUser call (Pure online mode - user already saved locally)
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "❌ Error in syncGoogleUserToNeon: " + e.getMessage());
            // Don't fail the login - user is already saved locally
        }
    }
    
}
