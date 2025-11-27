package com.example.blottermanagementsystem.utils;

import android.app.Activity;
import android.widget.Toast;

public class RoleAccessControl {
    
    /**
     * Check if user has required role to access activity
     * @param activity Current activity
     * @param requiredRole Required role (Admin, Officer, User)
     * @param preferencesManager PreferencesManager instance
     * @return true if access granted, false if denied
     */
    public static boolean checkAccess(Activity activity, String requiredRole, PreferencesManager preferencesManager) {
        String userRole = preferencesManager.getRole();
        
        if (userRole == null || userRole.isEmpty()) {
            Toast.makeText(activity, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }
        
        // Admin has access to everything
        if (userRole.equals("Admin")) {
            return true;
        }
        
        // Check specific role
        if (userRole.equals(requiredRole)) {
            return true;
        }
        
        // Access denied
        Toast.makeText(activity, "Access denied. Insufficient privileges.", Toast.LENGTH_SHORT).show();
        activity.finish();
        return false;
    }
    
    /**
     * Check if user has any of the required roles
     */
    public static boolean checkAnyRole(Activity activity, String[] requiredRoles, PreferencesManager preferencesManager) {
        String userRole = preferencesManager.getRole();
        
        if (userRole == null || userRole.isEmpty()) {
            Toast.makeText(activity, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }
        
        // Admin always has access
        if (userRole.equals("Admin")) {
            return true;
        }
        
        // Check if user role matches any required role
        for (String role : requiredRoles) {
            if (userRole.equals(role)) {
                return true;
            }
        }
        
        // Access denied
        Toast.makeText(activity, "Access denied. Insufficient privileges.", Toast.LENGTH_SHORT).show();
        activity.finish();
        return false;
    }
}
