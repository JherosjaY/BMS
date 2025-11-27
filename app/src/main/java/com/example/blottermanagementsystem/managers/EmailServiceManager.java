package com.example.blottermanagementsystem.managers;

import android.content.Context;
import android.util.Log;
import com.example.blottermanagementsystem.config.EmailConfig;
import com.example.blottermanagementsystem.utils.ApiClient;
import java.util.Properties;
import java.util.Random;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * ✅ EMAIL SERVICE MANAGER
 * Handles automatic email sending for officer credentials and password resets
 */
public class EmailServiceManager {
    private static final String TAG = "EmailServiceManager";
    private Context context;
    
    public EmailServiceManager(Context context) {
        this.context = context;
    }
    
    /**
     * Send password reset email with auto-generated code
     */
    public void sendPasswordResetEmail(String toEmail, ApiCallback<String> callback) {
        new Thread(() -> {
            try {
                // Auto-generate reset code
                String resetCode = generateResetCode();
                
                String subject = "BMS - Password Reset Request";
                String body = "Your password reset code is: " + resetCode + "\n\n" +
                             "This code will expire in 15 minutes.\n\n" +
                             "If you didn't request this, please ignore this email.\n\n" +
                             "Blotter Management System";
                
                sendEmail(toEmail, subject, body);
                
                Log.d(TAG, "✅ Reset code sent to " + toEmail);
                callback.onSuccess("Reset code: " + resetCode); // For testing
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to send reset email: " + e.getMessage(), e);
                callback.onError("Failed to send reset email: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Send officer credentials email with auto-generated temporary password
     */
    public void sendOfficerCredentialsEmail(String toEmail, String officerName, 
                                           String tempPassword, ApiCallback<String> callback) {
        new Thread(() -> {
            try {
                String subject = "BMS - Officer Account Credentials";
                String body = "Dear " + officerName + ",\n\n" +
                             "Your officer account has been created in the Blotter Management System.\n\n" +
                             "Login Credentials:\n" +
                             "Email: " + toEmail + "\n" +
                             "Temporary Password: " + tempPassword + "\n\n" +
                             "Please login and change your password immediately.\n\n" +
                             "Welcome to the team!\n\n" +
                             "Blotter Management System";
                
                sendEmail(toEmail, subject, body);
                
                Log.d(TAG, "✅ Officer credentials sent to " + toEmail);
                callback.onSuccess("Officer credentials sent to " + toEmail);
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to send officer credentials: " + e.getMessage(), e);
                callback.onError("Failed to send officer credentials: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Generic email sending method
     */
    public void sendEmail(String toEmail, String subject, String body, ApiCallback<String> callback) {
        new Thread(() -> {
            try {
                sendEmail(toEmail, subject, body);
                callback.onSuccess("Email sent to " + toEmail);
            } catch (Exception e) {
                Log.e(TAG, "❌ Email send failed: " + e.getMessage(), e);
                callback.onError("Email send failed: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Internal email sending implementation
     */
    private void sendEmail(String toEmail, String subject, String body) throws Exception {
        Properties props = EmailConfig.getSMTPProperties();
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    EmailConfig.EMAIL_USERNAME,
                    EmailConfig.EMAIL_PASSWORD
                );
            }
        });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EmailConfig.FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(body);
        
        Transport.send(message);
        
        Log.d(TAG, "📧 Email sent successfully to " + toEmail);
    }
    
    /**
     * Generate 6-digit reset code
     */
    public String generateResetCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
    
    /**
     * Generate temporary password for officers
     */
    public String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
    
    /**
     * Callback interface
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
