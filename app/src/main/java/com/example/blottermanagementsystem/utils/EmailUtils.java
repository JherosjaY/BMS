package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class EmailUtils {
    
    private static final String TAG = "EmailUtils";
    
    /**
     * Send officer credentials via email
     * Currently uses device's email app (offline-capable)
     * TODO: Implement cloud-based email sending (SendGrid, Firebase, etc.)
     */
    public static void sendOfficerCredentials(
        Context context,
        String officerEmail,
        String officerName,
        String username,
        String temporaryPassword,
        String rank,
        String badgeNumber
    ) {
        String subject = "Your Blotter Management System Account";
        String body = buildCredentialsEmail(officerName, username, temporaryPassword, rank, badgeNumber);
        
        // Use device's email app
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + officerEmail));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        
        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send credentials via email"));
            Log.d(TAG, "Email intent launched successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch email intent: " + e.getMessage());
        }
    }
    
    /**
     * Build the email body with officer credentials
     */
    private static String buildCredentialsEmail(
        String officerName,
        String username,
        String temporaryPassword,
        String rank,
        String badgeNumber
    ) {
        return "Dear Officer " + officerName + ",\n\n" +
               "Your account has been created for the Blotter Management System.\n\n" +
               "Account Details:\n" +
               "- Rank: " + rank + "\n" +
               "- Badge Number: " + badgeNumber + "\n\n" +
               "Login Credentials:\n" +
               "- Username: " + username + "\n" +
               "- Temporary Password: " + temporaryPassword + "\n\n" +
               "IMPORTANT SECURITY NOTICE:\n" +
               "For security reasons, you will be required to change your password upon first login. " +
               "Please keep these credentials confidential and do not share them with anyone.\n\n" +
               "Login Instructions:\n" +
               "1. Open the Blotter Management System app\n" +
               "2. Enter your username and temporary password\n" +
               "3. You will be prompted to create a new secure password\n" +
               "4. Choose a strong password (minimum 8 characters with uppercase, lowercase, number, and special character)\n\n" +
               "If you did not request this account or have any questions, please contact your administrator immediately.\n\n" +
               "Best regards,\n" +
               "Blotter Management System\n" +
               "Police Department";
    }
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    /**
     * Build credentials summary for display
     */
    public static String buildCredentialsSummary(
        String officerName,
        String username,
        String temporaryPassword,
        String rank,
        String badgeNumber
    ) {
        return "Officer: " + officerName + "\n" +
               "Rank: " + rank + "\n" +
               "Badge: " + badgeNumber + "\n\n" +
               "Username: " + username + "\n" +
               "Password: " + temporaryPassword + "\n\n" +
               "Note: Officer must change password on first login.";
    }
}
