package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.google.firebase.functions.FirebaseFunctions;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class EmailHelper {
    
    private static FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
    
    public static void sendReportEmail(Context context, BlotterReport report, String recipientEmail) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        
        String subject = "Blotter Report: " + report.getCaseNumber();
        String body = "BLOTTER REPORT DETAILS\n\n" +
                     "Case Number: " + report.getCaseNumber() + "\n" +
                     "Incident Type: " + report.getIncidentType() + "\n" +
                     "Status: " + report.getStatus() + "\n" +
                     "Location: " + report.getLocation() + "\n" +
                     "Date: " + dateFormat.format(new Date(report.getIncidentDate())) + "\n\n" +
                     "Description:\n" + report.getDescription();
        
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipientEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        
        if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(emailIntent, "Send Email"));
        }
    }
    
    public static void sendHearingNoticeEmail(Context context, String recipientEmail, 
                                             String caseNumber, String hearingDate, String hearingTime) {
        String subject = "Hearing Notice - Case " + caseNumber;
        String body = "HEARING NOTICE\n\n" +
                     "You are hereby notified of a scheduled hearing:\n\n" +
                     "Case Number: " + caseNumber + "\n" +
                     "Date: " + hearingDate + "\n" +
                     "Time: " + hearingTime + "\n" +
                     "Venue: Barangay Hall\n\n" +
                     "Your presence is required.\n\n" +
                     "Please bring valid ID and any supporting documents.";
        
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipientEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        
        if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(emailIntent, "Send Email"));
        }
    }
    
    /**
     * Send Officer Credentials via Email (Cloud-Ready)
     * This method is ready for Firebase Cloud Functions integration
     */
    public static void sendOfficerCredentialsEmail(Context context, String officerName, 
                                                   String email, String username, String password) {
        // TODO: When Firebase is configured, uncomment this to use cloud function
        // sendOfficerCredentialsViaCloud(officerName, email, username, password);
        
        // For now, use local email intent
        sendOfficerCredentialsViaIntent(context, officerName, email, username, password);
    }
    
    /**
     * LOCAL METHOD: Send via email intent (opens email app)
     */
    private static void sendOfficerCredentialsViaIntent(Context context, String officerName, 
                                                       String email, String username, String password) {
        String subject = "Your Officer Account Credentials - Blotter Management System";
        String body = getOfficerCredentialsEmailBody(officerName, username, password);
        
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        
        if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(emailIntent, "Send Credentials via Email"));
        }
    }
    
    /**
     * CLOUD METHOD: Send via Firebase Cloud Function (Ready for implementation)
     * Uncomment when Firebase Cloud Functions are set up
     */
    /*
    private static void sendOfficerCredentialsViaCloud(String officerName, String email, 
                                                      String username, String password) {
        Map<String, Object> data = new HashMap<>();
        data.put("officerName", officerName);
        data.put("email", email);
        data.put("username", username);
        data.put("password", password);
        data.put("emailType", "officer_credentials");
        
        mFunctions
            .getHttpsCallable("sendEmail")
            .call(data)
            .addOnSuccessListener(result -> {
                // Email sent successfully via cloud
                android.util.Log.d("EmailHelper", "Email sent successfully to: " + email);
            })
            .addOnFailureListener(e -> {
                // Error sending email
                android.util.Log.e("EmailHelper", "Error sending email: " + e.getMessage());
            });
    }
    */
    
    /**
     * Get formatted email body for officer credentials
     */
    private static String getOfficerCredentialsEmailBody(String officerName, String username, String password) {
        return "═══════════════════════════════════════\n" +
               "  BLOTTER MANAGEMENT SYSTEM\n" +
               "  Officer Account Credentials\n" +
               "═══════════════════════════════════════\n\n" +
               "Dear Officer " + officerName + ",\n\n" +
               "Welcome to the Blotter Management System!\n\n" +
               "Your officer account has been successfully created.\n" +
               "Please find your login credentials below:\n\n" +
               "┌─────────────────────────────────────┐\n" +
               "│  LOGIN CREDENTIALS                  │\n" +
               "├─────────────────────────────────────┤\n" +
               "│  Username: " + username + "\n" +
               "│  Password: " + password + "\n" +
               "└─────────────────────────────────────┘\n\n" +
               "🔐 SECURITY REMINDER:\n" +
               "• Please change your password after first login\n" +
               "• Keep your credentials confidential\n" +
               "• Do not share your account with others\n" +
               "• Report any suspicious activity immediately\n\n" +
               "📱 HOW TO LOGIN:\n" +
               "1. Open the Blotter Management System app\n" +
               "2. Select 'Officer Login'\n" +
               "3. Enter your username and password\n" +
               "4. Click 'Login'\n\n" +
               "If you have any questions or need assistance,\n" +
               "please contact your administrator.\n\n" +
               "Best regards,\n" +
               "Blotter Management System\n" +
               "Barangay Administration\n\n" +
               "═══════════════════════════════════════\n" +
               "This is an automated message. Please do not reply.\n" +
               "═══════════════════════════════════════";
    }
    
    /**
     * Get HTML formatted email body for cloud function (prettier email)
     */
    public static String getOfficerCredentialsEmailHTML(String officerName, String username, String password) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<style>" +
               "body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }" +
               ".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
               ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; text-align: center; }" +
               ".credentials { background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 20px 0; }" +
               ".credential-item { margin: 10px 0; }" +
               ".credential-label { font-weight: bold; color: #667eea; }" +
               ".credential-value { font-family: monospace; background-color: #e9ecef; padding: 5px 10px; border-radius: 4px; display: inline-block; }" +
               ".security-tips { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }" +
               ".footer { text-align: center; color: #6c757d; font-size: 12px; margin-top: 30px; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<div class='header'>" +
               "<h1>🔐 Officer Account Created</h1>" +
               "<p>Blotter Management System</p>" +
               "</div>" +
               "<p>Dear Officer <strong>" + officerName + "</strong>,</p>" +
               "<p>Welcome to the Blotter Management System! Your officer account has been successfully created.</p>" +
               "<div class='credentials'>" +
               "<h3>📋 Login Credentials</h3>" +
               "<div class='credential-item'>" +
               "<span class='credential-label'>Username:</span> " +
               "<span class='credential-value'>" + username + "</span>" +
               "</div>" +
               "<div class='credential-item'>" +
               "<span class='credential-label'>Password:</span> " +
               "<span class='credential-value'>" + password + "</span>" +
               "</div>" +
               "</div>" +
               "<div class='security-tips'>" +
               "<h4>🔐 Security Reminder</h4>" +
               "<ul>" +
               "<li>Please change your password after first login</li>" +
               "<li>Keep your credentials confidential</li>" +
               "<li>Do not share your account with others</li>" +
               "<li>Report any suspicious activity immediately</li>" +
               "</ul>" +
               "</div>" +
               "<h4>📱 How to Login:</h4>" +
               "<ol>" +
               "<li>Open the Blotter Management System app</li>" +
               "<li>Select 'Officer Login'</li>" +
               "<li>Enter your username and password</li>" +
               "<li>Click 'Login'</li>" +
               "</ol>" +
               "<p>If you have any questions or need assistance, please contact your administrator.</p>" +
               "<p>Best regards,<br><strong>Blotter Management System</strong><br>Barangay Administration</p>" +
               "<div class='footer'>" +
               "<p>This is an automated message. Please do not reply.</p>" +
               "</div>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
    
    /**
     * Send Password Reset Code via Email (Cloud-Ready)
     */
    public static void sendPasswordResetEmail(Context context, String email, String resetCode) {
        // TODO: When Firebase is configured, uncomment this to use cloud function
        // sendPasswordResetViaCloud(email, resetCode);
        
        // For now, use local email intent
        sendPasswordResetViaIntent(context, email, resetCode);
    }
    
    /**
     * LOCAL METHOD: Send reset code via email intent
     */
    private static void sendPasswordResetViaIntent(Context context, String email, String resetCode) {
        String subject = "Password Reset Code - Blotter Management System";
        String body = getPasswordResetEmailBody(resetCode);
        
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        
        if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(emailIntent, "Send Reset Code via Email"));
        }
    }
    
    /**
     * CLOUD METHOD: Send reset code via Firebase Cloud Function (Ready for implementation)
     */
    /*
    private static void sendPasswordResetViaCloud(String email, String resetCode) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("resetCode", resetCode);
        data.put("emailType", "password_reset");
        
        mFunctions
            .getHttpsCallable("sendEmail")
            .call(data)
            .addOnSuccessListener(result -> {
                android.util.Log.d("EmailHelper", "Reset code sent to: " + email);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("EmailHelper", "Error sending reset code: " + e.getMessage());
            });
    }
    */
    
    /**
     * Get formatted email body for password reset
     */
    private static String getPasswordResetEmailBody(String resetCode) {
        return "═══════════════════════════════════════\n" +
               "  BLOTTER MANAGEMENT SYSTEM\n" +
               "  Password Reset Request\n" +
               "═══════════════════════════════════════\n\n" +
               "You have requested to reset your password.\n\n" +
               "Your password reset code is:\n\n" +
               "┌─────────────────────────────────────┐\n" +
               "│                                     │\n" +
               "│         " + resetCode + "         │\n" +
               "│                                     │\n" +
               "└─────────────────────────────────────┘\n\n" +
               "⏰ This code will expire in 5 minutes.\n\n" +
               "🔐 SECURITY TIPS:\n" +
               "• Do not share this code with anyone\n" +
               "• If you didn't request this, ignore this email\n" +
               "• Contact admin if you suspect unauthorized access\n\n" +
               "📱 HOW TO RESET:\n" +
               "1. Return to the app\n" +
               "2. Enter the reset code above\n" +
               "3. Create your new password\n" +
               "4. Confirm and save\n\n" +
               "If you have any questions, please contact\n" +
               "your administrator.\n\n" +
               "Best regards,\n" +
               "Blotter Management System\n" +
               "Barangay Administration\n\n" +
               "═══════════════════════════════════════\n" +
               "This is an automated message. Please do not reply.\n" +
               "═══════════════════════════════════════";
    }
    
    /**
     * Get HTML formatted email for password reset (prettier email)
     */
    public static String getPasswordResetEmailHTML(String resetCode) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<style>" +
               "body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }" +
               ".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
               ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; text-align: center; }" +
               ".reset-code { background-color: #f8f9fa; border: 3px dashed #667eea; padding: 30px; margin: 20px 0; text-align: center; }" +
               ".code { font-size: 48px; font-weight: bold; letter-spacing: 10px; color: #667eea; font-family: monospace; }" +
               ".expiry { color: #dc3545; font-weight: bold; margin-top: 10px; }" +
               ".security-tips { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }" +
               ".footer { text-align: center; color: #6c757d; font-size: 12px; margin-top: 30px; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<div class='header'>" +
               "<h1>🔑 Password Reset Request</h1>" +
               "<p>Blotter Management System</p>" +
               "</div>" +
               "<p>You have requested to reset your password.</p>" +
               "<p>Your password reset code is:</p>" +
               "<div class='reset-code'>" +
               "<div class='code'>" + resetCode + "</div>" +
               "<div class='expiry'>⏰ Expires in 5 minutes</div>" +
               "</div>" +
               "<div class='security-tips'>" +
               "<h4>🔐 Security Tips</h4>" +
               "<ul>" +
               "<li>Do not share this code with anyone</li>" +
               "<li>If you didn't request this, ignore this email</li>" +
               "<li>Contact admin if you suspect unauthorized access</li>" +
               "</ul>" +
               "</div>" +
               "<h4>📱 How to Reset:</h4>" +
               "<ol>" +
               "<li>Return to the app</li>" +
               "<li>Enter the reset code above</li>" +
               "<li>Create your new password</li>" +
               "<li>Confirm and save</li>" +
               "</ol>" +
               "<p>If you have any questions, please contact your administrator.</p>" +
               "<p>Best regards,<br><strong>Blotter Management System</strong><br>Barangay Administration</p>" +
               "<div class='footer'>" +
               "<p>This is an automated message. Please do not reply.</p>" +
               "</div>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
}
