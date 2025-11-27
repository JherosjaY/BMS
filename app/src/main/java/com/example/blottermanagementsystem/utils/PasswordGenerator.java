package com.example.blottermanagementsystem.utils;

import java.security.SecureRandom;

public class PasswordGenerator {
    
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL = "!@#$%&*";
    
    /**
     * Generate a secure temporary password for officer accounts
     * Format: [BadgeNumber]-[Random4Chars]
     * Example: PNP-2025-47739-Tz4*
     * Uses the full badge number that already includes PNP-YYYY prefix
     */
    public static String generateOfficerPassword(String rank, String badgeNumber) {
        SecureRandom random = new SecureRandom();
        
        // Generate 4 random characters (1 uppercase, 1 lowercase, 1 number, 1 special)
        char upper = UPPERCASE.charAt(random.nextInt(UPPERCASE.length()));
        char lower = LOWERCASE.charAt(random.nextInt(LOWERCASE.length()));
        char number = NUMBERS.charAt(random.nextInt(NUMBERS.length()));
        char special = SPECIAL.charAt(random.nextInt(SPECIAL.length()));
        
        // Badge number already includes PNP-YYYY prefix, just add random chars
        return badgeNumber + "-" + upper + lower + number + special;
    }
    
    /**
     * Generate a fully random secure password
     * Minimum 8 characters with uppercase, lowercase, number, and special char
     */
    public static String generateSecurePassword(int length) {
        if (length < 8) length = 8;
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one of each type
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        
        // Fill the rest randomly
        String allChars = UPPERCASE + LOWERCASE + NUMBERS + SPECIAL;
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        return shuffleString(password.toString(), random);
    }
    
    /**
     * Generate username from first name and last name
     * Format: Off.firstnamelastname (lowercase, no spaces)
     * Example: Off.kriszzlejoy
     */
    public static String generateUsername(String firstName, String lastName) {
        String cleanFirstName = firstName
            .toLowerCase()
            .replaceAll("\\s+", "")
            .replaceAll("[^a-z]", "");
        String cleanLastName = lastName
            .toLowerCase()
            .replaceAll("\\s+", "")
            .replaceAll("[^a-z]", "");
        return "Off." + cleanFirstName + cleanLastName;
    }
    
    /**
     * Validate password strength
     * Returns true if password meets security requirements
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasNumber = true;
            else if (SPECIAL.indexOf(c) >= 0) hasSpecial = true;
        }
        
        return hasUpper && hasLower && hasNumber && hasSpecial;
    }
    
    private static String shuffleString(String input, SecureRandom random) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }
}
