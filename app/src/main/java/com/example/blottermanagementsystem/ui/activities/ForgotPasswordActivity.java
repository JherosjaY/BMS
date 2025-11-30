package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.services.EmailAuthService;
import com.example.blottermanagementsystem.utils.NetworkConnectivityManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.security.MessageDigest;
import java.util.Random;
import java.util.concurrent.Executors;

public class ForgotPasswordActivity extends BaseActivity {
    
    private ImageView btnBack;
    private TextView tvSubtitle, tvResetCode, tvCodeExpiry, tvError;
    private TextInputEditText etEmail, etResetCode, etNewPassword, etConfirmPassword;
    private MaterialButton btnSendCode, btnResetPassword;
    private LinearLayout layoutEmailStep, layoutCodeStep;
    private CardView cardResetCode;
    private ProgressBar progressBar;
    
    // 📧 NEW: Email authentication service
    private EmailAuthService emailAuthService;
    private NetworkConnectivityManager networkConnectivityManager;
    private String userEmail;
    private android.os.Handler countdownHandler;
    private Runnable countdownRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        
        // 📧 NEW: Initialize services
        emailAuthService = new EmailAuthService();
        networkConnectivityManager = new NetworkConnectivityManager(this);
        android.util.Log.d("ForgotPasswordActivity", "✅ EmailAuthService initialized");
        
        initViews();
        setupListeners();
        animateViews();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvResetCode = findViewById(R.id.tvResetCode);
        tvCodeExpiry = findViewById(R.id.tvCodeExpiry);
        tvError = findViewById(R.id.tvError);
        
        etEmail = findViewById(R.id.etEmail);
        etResetCode = findViewById(R.id.etResetCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        btnSendCode = findViewById(R.id.btnSendCode);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        
        layoutEmailStep = findViewById(R.id.layoutEmailStep);
        layoutCodeStep = findViewById(R.id.layoutCodeStep);
        cardResetCode = findViewById(R.id.cardResetCode);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnSendCode.setOnClickListener(v -> sendResetCode());
        
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }
    
    private void sendResetCode() {
        String email = etEmail.getText().toString().trim();
        
        if (email.isEmpty()) {
            showError("Please enter your email address");
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return;
        }
        
        // 📧 NEW: Check internet connection
        if (!networkConnectivityManager.isConnectedToInternet()) {
            showError("❌ No internet connection\n\nThis app requires internet to reset password.");
            return;
        }
        
        hideError();
        userEmail = email;
        
        // 📧 NEW: Use EmailAuthService to request password reset
        android.util.Log.d("ForgotPasswordActivity", "📧 Requesting password reset for: " + email);
        emailAuthService.requestPasswordReset(email, new EmailAuthService.AuthCallback() {
            @Override
            public void onSuccess(String message, String userId) {
                android.util.Log.d("ForgotPasswordActivity", "✅ Reset code sent!");
                Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                
                // Show code entry step
                showResetCodeStep();
                startCountdownTimer();
            }
            
            @Override
            public void onError(String errorMessage) {
                android.util.Log.e("ForgotPasswordActivity", "❌ Reset request failed: " + errorMessage);
                showError("Failed to send reset code: " + errorMessage);
            }
            
            @Override
            public void onLoading() {
                android.util.Log.d("ForgotPasswordActivity", "🔄 Sending reset code...");
            }
        });
    }
    
    private void resetPassword() {
        String code = etResetCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        if (code.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        
        if (code.length() != 6) {
            showError("Reset code must be 6 digits");
            return;
        }
        
        // Strict password validation
        String passwordError = validateStrongPassword(newPassword);
        if (passwordError != null) {
            showError(passwordError);
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        // 📧 NEW: Check internet connection
        if (!networkConnectivityManager.isConnectedToInternet()) {
            showError("❌ No internet connection\n\nThis app requires internet to reset password.");
            return;
        }
        
        hideError();
        
        // 📧 NEW: Use EmailAuthService to reset password with code
        android.util.Log.d("ForgotPasswordActivity", "📧 Resetting password with code");
        emailAuthService.resetPasswordWithCode(userEmail, code, newPassword, 
            new EmailAuthService.AuthCallback() {
                @Override
                public void onSuccess(String message, String userId) {
                    android.util.Log.d("ForgotPasswordActivity", "✅ Password reset successful!");
                    Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                    
                    // Navigate back to login
                    Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                
                @Override
                public void onError(String errorMessage) {
                    android.util.Log.e("ForgotPasswordActivity", "❌ Password reset failed: " + errorMessage);
                    showError("Password reset failed: " + errorMessage);
                }
                
                @Override
                public void onLoading() {
                    android.util.Log.d("ForgotPasswordActivity", "🔄 Resetting password...");
                }
            });
    }
    
    private String generateResetCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return password; // Fallback to plain text (not recommended)
        }
    }
    
    private void showResetCodeStep() {
        // Hide email step
        layoutEmailStep.setVisibility(View.GONE);
        
        // Show code step
        layoutCodeStep.setVisibility(View.VISIBLE);
        
        // HIDE the reset code display card (code sent via email)
        cardResetCode.setVisibility(View.GONE);
        
        // Update subtitle
        tvSubtitle.setText("Check your email for the reset code");
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSendCode.setEnabled(!show);
        btnResetPassword.setEnabled(!show);
        etEmail.setEnabled(!show);
        etResetCode.setEnabled(!show);
        etNewPassword.setEnabled(!show);
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
        View resetCard = findViewById(R.id.resetCard);
        
        if (resetCard != null) {
            resetCard.setAlpha(0f);
            resetCard.setTranslationY(50f);
            resetCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .start();
        }
    }
    
    /**
     * Format code with spaces (e.g., "123456" -> "1 2 3 4 5 6")
     */
    private String formatCodeWithSpaces(String code) {
        if (code == null || code.isEmpty()) {
            return "";
        }
        
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            formatted.append(code.charAt(i));
            if (i < code.length() - 1) {
                formatted.append(" ");
            }
        }
        return formatted.toString();
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
     * Start countdown timer for code expiry
     */
    private void startCountdownTimer() {
        if (countdownHandler == null) {
            countdownHandler = new android.os.Handler();
        }
        
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long remainingTime = codeExpiryTime - System.currentTimeMillis();
                
                if (remainingTime <= 0) {
                    // Code expired
                    tvCodeExpiry.setText("Code expired");
                    tvCodeExpiry.setTextColor(0xFFef4444); // Red
                    return;
                }
                
                // Calculate minutes and seconds
                long minutes = (remainingTime / 1000) / 60;
                long seconds = (remainingTime / 1000) % 60;
                
                // Update UI
                String timeText = String.format("Expires in %d:%02d", minutes, seconds);
                tvCodeExpiry.setText(timeText);
                
                // Change color based on time remaining
                if (minutes < 2) {
                    tvCodeExpiry.setTextColor(0xFFef4444); // Red if less than 2 minutes
                } else if (minutes < 3) {
                    tvCodeExpiry.setTextColor(0xFFfbbf24); // Yellow/Orange if less than 3 minutes
                } else {
                    tvCodeExpiry.setTextColor(0xFF10b981); // Green if 3+ minutes
                }
                
                // Schedule next update in 1 second
                countdownHandler.postDelayed(this, 1000);
            }
        };
        
        // Start the countdown
        countdownHandler.post(countdownRunnable);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop countdown timer
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }
}
