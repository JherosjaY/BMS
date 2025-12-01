package com.example.blottermanagementsystem.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.utils.ApiClient;
import com.example.blottermanagementsystem.utils.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ForgotPasswordActivity - Password reset flow
 * Step 1: Enter email → Send reset code
 * Step 2: Enter code → Verify code
 * Step 3: Enter new password → Reset password
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailInput, codeInput, newPasswordInput, confirmPasswordInput;
    private Button sendCodeBtn, verifyCodeBtn, resetPasswordBtn;
    private ProgressBar progressBar;
    private ApiService apiService;

    private int currentStep = 1; // 1: Email, 2: Code, 3: Password

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        codeInput = findViewById(R.id.codeInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        sendCodeBtn = findViewById(R.id.sendCodeBtn);
        verifyCodeBtn = findViewById(R.id.verifyCodeBtn);
        resetPasswordBtn = findViewById(R.id.resetPasswordBtn);
        progressBar = findViewById(R.id.progressBar);

        // Initialize API
        apiService = ApiClient.getApiService();

        // Hide steps 2 and 3
        codeInput.setVisibility(android.view.View.GONE);
        verifyCodeBtn.setVisibility(android.view.View.GONE);
        newPasswordInput.setVisibility(android.view.View.GONE);
        confirmPasswordInput.setVisibility(android.view.View.GONE);
        resetPasswordBtn.setVisibility(android.view.View.GONE);

        // Step 1: Send reset code
        sendCodeBtn.setOnClickListener(v -> sendResetCode());

        // Step 2: Verify code
        verifyCodeBtn.setOnClickListener(v -> verifyCode());

        // Step 3: Reset password
        resetPasswordBtn.setOnClickListener(v -> resetPassword());
    }

    /**
     * Step 1: Send password reset code to email
     */
    private void sendResetCode() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(android.view.View.VISIBLE);
        sendCodeBtn.setEnabled(false);

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("email", email);

        apiService.sendPasswordResetCode(requestData).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                progressBar.setVisibility(android.view.View.GONE);
                sendCodeBtn.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    Boolean success = (Boolean) body.get("success");

                    if (success != null && success) {
                        Toast.makeText(ForgotPasswordActivity.this, "Reset code sent to email", Toast.LENGTH_SHORT).show();

                        // Move to step 2
                        currentStep = 2;
                        emailInput.setEnabled(false);
                        sendCodeBtn.setVisibility(android.view.View.GONE);

                        codeInput.setVisibility(android.view.View.VISIBLE);
                        verifyCodeBtn.setVisibility(android.view.View.VISIBLE);
                    } else {
                        String message = (String) body.get("message");
                        Toast.makeText(ForgotPasswordActivity.this, message != null ? message : "Error sending code", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                sendCodeBtn.setEnabled(true);
                Toast.makeText(ForgotPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Step 2: Verify reset code
     */
    private void verifyCode() {
        String email = emailInput.getText().toString().trim();
        String code = codeInput.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Please enter the reset code", Toast.LENGTH_SHORT).show();
            return;
        }

        if (code.length() != 6) {
            Toast.makeText(this, "Code must be 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(android.view.View.VISIBLE);
        verifyCodeBtn.setEnabled(false);

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("email", email);
        requestData.put("resetCode", code);

        apiService.verifyResetCode(requestData).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                progressBar.setVisibility(android.view.View.GONE);
                verifyCodeBtn.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    Boolean success = (Boolean) body.get("success");

                    if (success != null && success) {
                        Toast.makeText(ForgotPasswordActivity.this, "Code verified", Toast.LENGTH_SHORT).show();

                        // Move to step 3
                        currentStep = 3;
                        codeInput.setEnabled(false);
                        verifyCodeBtn.setVisibility(android.view.View.GONE);

                        newPasswordInput.setVisibility(android.view.View.VISIBLE);
                        confirmPasswordInput.setVisibility(android.view.View.VISIBLE);
                        resetPasswordBtn.setVisibility(android.view.View.VISIBLE);
                    } else {
                        String message = (String) body.get("message");
                        Toast.makeText(ForgotPasswordActivity.this, message != null ? message : "Invalid code", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                verifyCodeBtn.setEnabled(true);
                Toast.makeText(ForgotPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Step 3: Reset password with verified code
     */
    private void resetPassword() {
        String email = emailInput.getText().toString().trim();
        String code = codeInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Please enter new password", Toast.LENGTH_SHORT).show();
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

        progressBar.setVisibility(android.view.View.VISIBLE);
        resetPasswordBtn.setEnabled(false);

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("email", email);
        requestData.put("resetCode", code);
        requestData.put("newPassword", newPassword);

        apiService.resetPassword(requestData).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                progressBar.setVisibility(android.view.View.GONE);
                resetPasswordBtn.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    Boolean success = (Boolean) body.get("success");

                    if (success != null && success) {
                        Toast.makeText(ForgotPasswordActivity.this, "Password reset successful!", Toast.LENGTH_SHORT).show();

                        // Redirect to LoginActivity
                        finish();
                    } else {
                        String message = (String) body.get("message");
                        Toast.makeText(ForgotPasswordActivity.this, message != null ? message : "Error resetting password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                resetPasswordBtn.setEnabled(true);
                Toast.makeText(ForgotPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
