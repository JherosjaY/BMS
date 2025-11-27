package com.example.blottermanagementsystem.utils;

import java.util.regex.Pattern;

/**
 * Utility class for validating Philippine mobile numbers
 * Supports all major networks: Globe, Smart, Sun, TM, TNT, DITO
 */
public class PhoneNumberValidator {

    // Philippine mobile number prefixes
    private static final String[] VALID_PREFIXES = {
            // Globe/TM
            "0905", "0906", "0915", "0916", "0917", "0926", "0927", "0935", "0936", "0937",
            "0945", "0953", "0954", "0955", "0956", "0965", "0966", "0967", "0975", "0976",
            "0977", "0978", "0979", "0995", "0996", "0997",
            
            // Smart/TNT
            "0907", "0908", "0909", "0910", "0911", "0912", "0913", "0914", "0918", "0919",
            "0920", "0921", "0928", "0929", "0930", "0938", "0939", "0946", "0947", "0948",
            "0949", "0950", "0951", "0961", "0962", "0963", "0964", "0968", "0969", "0970",
            "0971", "0980", "0981", "0989", "0991", "0992", "0993", "0994", "0998", "0999",
            
            // Sun Cellular
            "0922", "0923", "0924", "0925", "0931", "0932", "0933", "0934", "0940", "0941",
            "0942", "0943", "0944",
            
            // DITO Telecommunity
            "0895", "0896", "0897", "0898", "0991", "0992", "0993", "0994"
    };

    /**
     * Validates if the phone number is a valid Philippine mobile number
     * Format: 09XXXXXXXXX (11 digits starting with 09)
     *
     * @param phoneNumber The phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhilippineNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        // Remove spaces and dashes
        String cleanNumber = phoneNumber.replaceAll("[\\s-]", "");

        // Check if it's exactly 11 digits
        if (cleanNumber.length() != 11) {
            return false;
        }

        // Check if it starts with 09
        if (!cleanNumber.startsWith("09")) {
            return false;
        }

        // Check if it matches any valid prefix
        boolean hasValidPrefix = false;
        for (String prefix : VALID_PREFIXES) {
            if (cleanNumber.startsWith(prefix)) {
                hasValidPrefix = true;
                break;
            }
        }
        
        if (!hasValidPrefix) {
            return false;
        }
        
        // Validate that all characters are digits
        if (!Pattern.matches("\\d{11}", cleanNumber)) {
            return false;
        }
        
        // Reject sequential patterns (e.g., 09123456789, 09876543210)
        if (isSequentialPattern(cleanNumber)) {
            return false;
        }
        
        // Reject repetitive patterns (e.g., 09111111111, 09222222222)
        if (isRepetitivePattern(cleanNumber)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if the number contains a sequential pattern
     */
    private static boolean isSequentialPattern(String number) {
        // Check ascending sequence (e.g., 123456789)
        String digits = number.substring(2); // Skip "09" prefix
        int sequentialCount = 0;
        
        for (int i = 0; i < digits.length() - 1; i++) {
            int current = Character.getNumericValue(digits.charAt(i));
            int next = Character.getNumericValue(digits.charAt(i + 1));
            
            if (next == current + 1 || next == current - 1) {
                sequentialCount++;
                if (sequentialCount >= 6) { // 7 or more sequential digits
                    return true;
                }
            } else {
                sequentialCount = 0;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if the number contains too many repetitive digits
     */
    private static boolean isRepetitivePattern(String number) {
        // Check if more than 7 of the same digit appear
        String digits = number.substring(2); // Skip "09" prefix
        
        for (char digit = '0'; digit <= '9'; digit++) {
            int count = 0;
            for (char c : digits.toCharArray()) {
                if (c == digit) {
                    count++;
                }
            }
            if (count >= 7) { // 7 or more of the same digit
                return true;
            }
        }
        
        return false;
    }

    /**
     * Gets a user-friendly error message for invalid phone numbers
     *
     * @param phoneNumber The phone number that failed validation
     * @return Error message string
     */
    public static String getErrorMessage(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "Phone number is required";
        }

        String cleanNumber = phoneNumber.replaceAll("[\\s-]", "");

        if (cleanNumber.length() != 11) {
            return "Phone number must be exactly 11 digits";
        }

        if (!cleanNumber.startsWith("09")) {
            return "Phone number must start with 09";
        }
        
        // Check for sequential pattern
        if (isSequentialPattern(cleanNumber)) {
            return "Invalid number: Sequential patterns not allowed (e.g., 09123456789)";
        }
        
        // Check for repetitive pattern
        if (isRepetitivePattern(cleanNumber)) {
            return "Invalid number: Too many repeated digits (e.g., 09111111111)";
        }

        return "Invalid Philippine mobile number. Please check the network prefix.";
    }

    /**
     * Formats the phone number with dashes for better readability
     * Format: 09XX-XXX-XXXX
     *
     * @param phoneNumber The phone number to format
     * @return Formatted phone number or original if invalid
     */
    public static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }

        String cleanNumber = phoneNumber.replaceAll("[\\s-]", "");

        if (cleanNumber.length() == 11 && cleanNumber.startsWith("09")) {
            return cleanNumber.substring(0, 4) + "-" + 
                   cleanNumber.substring(4, 7) + "-" + 
                   cleanNumber.substring(7, 11);
        }

        return phoneNumber;
    }

    /**
     * Gets a list of supported network prefixes for display
     *
     * @return String describing supported networks
     */
    public static String getSupportedNetworks() {
        return "Supported networks: Globe, TM, Smart, TNT, Sun Cellular, DITO";
    }
}
