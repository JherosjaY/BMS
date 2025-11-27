package com.example.blottermanagementsystem.utils;

/**
 * Philippine Phone Number Validator
 * Validates phone numbers against real Philippine telecom providers
 * Supported: Globe, TM, Smart, TNT, Sun, DITO, Converge, etc.
 */
public class PhilippinePhoneValidator {
    
    /**
     * Valid Philippine mobile prefixes (first 4 digits after country code)
     * Format: 63 + 9 + XX + XXXXXXX
     */
    private static final String[] VALID_PREFIXES = {
        // Globe Telecom
        "0917", "0918", "0919", "0920", "0921", "0930", "0931", "0932", "0933", "0934", "0935", "0936", "0937", "0938", "0939", "0940", "0941", "0942", "0943", "0944", "0945",
        
        // Smart Communications
        "0910", "0911", "0912", "0913", "0914", "0915", "0916", "0917", "0918", "0919", "0920", "0921", "0922", "0923", "0924", "0925", "0926", "0927", "0928", "0929",
        
        // Sun Cellular
        "0922", "0923", "0924", "0925", "0926", "0927", "0928", "0929",
        
        // Talk 'N Text (TNT)
        "0908", "0909", "0910", "0911", "0912", "0913", "0914", "0915", "0916", "0917", "0918", "0919",
        
        // TM (Touch Mobile)
        "0919", "0920", "0921", "0922", "0923", "0924", "0925", "0926", "0927", "0928", "0929",
        
        // DITO Telecommunity
        "0991", "0992", "0993", "0994", "0995", "0996", "0997", "0998",
        
        // Converge ICT
        "0998", "0999",
    };
    
    /**
     * Validate if phone number is from a real Philippine telecom provider
     * Accepts formats: 09xxxxxxxxx or +639xxxxxxxxx
     */
    public static boolean isValidPhilippineNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        
        // Clean the number
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        
        // Convert +63 format to 0 format
        if (cleaned.startsWith("+63")) {
            cleaned = "0" + cleaned.substring(3);
        }
        
        // Must be 11 digits starting with 09
        if (!cleaned.startsWith("09") || cleaned.length() != 11) {
            return false;
        }
        
        // Check if first 4 digits match a valid telecom prefix
        String prefix = cleaned.substring(0, 4);
        for (String validPrefix : VALID_PREFIXES) {
            if (prefix.equals(validPrefix)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get the telecom provider name from phone number
     */
    public static String getTelecomProvider(String phoneNumber) {
        if (!isValidPhilippineNumber(phoneNumber)) {
            return "Unknown";
        }
        
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        if (cleaned.startsWith("+63")) {
            cleaned = "0" + cleaned.substring(3);
        }
        
        String prefix = cleaned.substring(0, 4);
        
        // Globe Telecom
        if (prefix.matches("0917|0918|0919|0920|0921|0930|0931|0932|0933|0934|0935|0936|0937|0938|0939|0940|0941|0942|0943|0944|0945")) {
            return "Globe Telecom";
        }
        
        // Smart Communications
        if (prefix.matches("0910|0911|0912|0913|0914|0915|0916|0922|0923|0924|0925|0926|0927|0928|0929")) {
            return "Smart Communications";
        }
        
        // DITO Telecommunity
        if (prefix.matches("0991|0992|0993|0994|0995|0996|0997|0998")) {
            return "DITO Telecommunity";
        }
        
        // Sun Cellular
        if (prefix.matches("0922|0923|0924|0925|0926|0927|0928|0929")) {
            return "Sun Cellular";
        }
        
        // Talk 'N Text (TNT)
        if (prefix.matches("0908|0909|0910|0911|0912|0913|0914|0915|0916|0917|0918|0919")) {
            return "Talk 'N Text (TNT)";
        }
        
        // TM (Touch Mobile)
        if (prefix.matches("0919|0920|0921|0922|0923|0924|0925|0926|0927|0928|0929")) {
            return "TM (Touch Mobile)";
        }
        
        // Converge ICT
        if (prefix.matches("0998|0999")) {
            return "Converge ICT";
        }
        
        return "Unknown";
    }
    
    /**
     * Format phone number to standard 09xxxxxxxxx format
     */
    public static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }
        
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        
        if (cleaned.startsWith("+63")) {
            return "0" + cleaned.substring(3);
        }
        
        return cleaned;
    }
}
