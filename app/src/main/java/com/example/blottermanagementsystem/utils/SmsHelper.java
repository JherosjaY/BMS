package com.example.blottermanagementsystem.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import androidx.core.content.ContextCompat;

public class SmsHelper {
    
    public static boolean hasSmsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    public static boolean sendSms(String phoneNumber, String message) {
        try {
            if (!isValidPhilippineNumber(phoneNumber)) {
                return false;
            }
            
            SmsManager smsManager = SmsManager.getDefault();
            
            if (message.length() > 160) {
                smsManager.sendMultipartTextMessage(phoneNumber, null, 
                    smsManager.divideMessage(message), null, null);
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean isValidPhilippineNumber(String number) {
        String cleaned = number.replaceAll("[^0-9+]", "");
        
        if (cleaned.startsWith("+63") && cleaned.length() == 13) return true;
        if (cleaned.startsWith("0") && cleaned.length() == 11) return true;
        return cleaned.length() == 10;
    }
    
    public static String formatPhilippineNumber(String number) {
        String cleaned = number.replaceAll("[^0-9+]", "");
        
        if (cleaned.startsWith("+63")) return cleaned;
        if (cleaned.startsWith("0")) return "+63" + cleaned.substring(1);
        if (cleaned.length() == 10) return "+63" + cleaned;
        return cleaned;
    }
    
    public static String generateInitialNotice(String caseNumber, String respondentName, String accusation) {
        return "BARANGAY BLOTTER NOTICE\n\n" +
               "Dear " + respondentName + ",\n\n" +
               "You are being notified regarding Case #" + caseNumber + ".\n\n" +
               "Accusation: " + accusation + "\n\n" +
               "You are requested to appear at the Barangay Hall within 3 days.\n\n" +
               "Please bring a valid ID.\n\n" +
               "- Barangay Hall";
    }
    
    public static String generateHearingNotice(String caseNumber, String respondentName, 
                                              String hearingDate, String hearingTime) {
        return "HEARING SCHEDULED\n\n" +
               "Dear " + respondentName + ",\n\n" +
               "Case #" + caseNumber + "\n\n" +
               "A hearing has been scheduled:\n" +
               "Date: " + hearingDate + "\n" +
               "Time: " + hearingTime + "\n" +
               "Venue: Barangay Hall\n\n" +
               "Your presence is REQUIRED.\n\n" +
               "- Barangay";
    }
    
    public static String generateReminder(String caseNumber, String respondentName, int daysRemaining) {
        return "REMINDER\n\n" +
               "Dear " + respondentName + ",\n\n" +
               "This is a reminder for Case #" + caseNumber + ".\n\n" +
               "You have " + daysRemaining + " day(s) remaining to appear at the Barangay Hall.\n\n" +
               "- Barangay";
    }
}
