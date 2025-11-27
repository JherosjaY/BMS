package com.example.blottermanagementsystem.utils;

import android.util.Patterns;
import java.util.regex.Pattern;

public class ValidationUtils {
    
    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null) return false;
        String cleaned = phone.replaceAll("[^0-9+]", "");
        return cleaned.length() >= 10 && cleaned.length() <= 13;
    }
    
    public static boolean isValidUsername(String username) {
        if (username == null || username.length() < 3) return false;
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
        return pattern.matcher(username).matches();
    }
    
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;
        
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasUpper && hasLower && hasDigit;
    }
    
    public static String getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) return "Empty";
        if (password.length() < 6) return "Too Short";
        if (password.length() < 8) return "Weak";
        if (!isStrongPassword(password)) return "Medium";
        return "Strong";
    }
    
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        Pattern pattern = Pattern.compile("^[a-zA-Z\\s]{2,50}$");
        return pattern.matcher(name.trim()).matches();
    }
    
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }
}
