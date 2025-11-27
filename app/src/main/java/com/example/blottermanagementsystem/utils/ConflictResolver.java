package com.example.blottermanagementsystem.utils;

import android.util.Log;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.data.entity.User;

/**
 * ✅ CONFLICT RESOLVER
 * Resolves data conflicts between local and Neon database
 */
public class ConflictResolver {
    private static final String TAG = "ConflictResolver";
    
    // ✅ RESOLVE REPORT CONFLICTS
    public BlotterReport resolveReportConflict(BlotterReport localReport, BlotterReport neonReport) {
        Log.d(TAG, "🔄 Resolving conflict for: " + localReport.getCaseNumber());
        
        BlotterReport resolved = new BlotterReport();
        
        // Copy critical fields from Neon (server wins)
        resolved.setId(neonReport.getId());
        resolved.setCaseNumber(neonReport.getCaseNumber());
        resolved.setIncidentType(neonReport.getIncidentType());
        resolved.setStatus(neonReport.getStatus());
        resolved.setAssignedOfficerId(neonReport.getAssignedOfficerId());
        resolved.setAssignedOfficerIds(neonReport.getAssignedOfficerIds());
        
        // Choose latest for editable fields
        resolved.setNarrative(chooseLatestString(
            localReport.getNarrative(), 
            neonReport.getNarrative()
        ));
        
        resolved.setIncidentLocation(chooseLatestString(
            localReport.getIncidentLocation(), 
            neonReport.getIncidentLocation()
        ));
        
        // Preserve complainant/respondent info
        resolved.setComplainantName(neonReport.getComplainantName());
        resolved.setRespondentName(neonReport.getRespondentName());
        resolved.setIncidentDate(neonReport.getIncidentDate());
        resolved.setDateFiled(Math.max(localReport.getDateFiled(), neonReport.getDateFiled()));
        resolved.setUserId(neonReport.getUserId());
        
        Log.d(TAG, "✅ Conflict resolved for: " + resolved.getCaseNumber());
        return resolved;
    }
    
    // ✅ RESOLVE USER CONFLICTS
    public User resolveUserConflict(User localUser, User neonUser) {
        Log.d(TAG, "🔄 Resolving user conflict for: " + neonUser.getEmail());
        
        User resolved = new User();
        
        // Server wins for critical fields
        resolved.setId(neonUser.getId());
        resolved.setEmail(neonUser.getEmail());
        resolved.setRole(neonUser.getRole());
        resolved.setUsername(neonUser.getUsername());
        
        // Choose latest for display fields
        resolved.setFirstName(chooseLatestString(
            localUser.getFirstName(), 
            neonUser.getFirstName()
        ));
        
        resolved.setLastName(chooseLatestString(
            localUser.getLastName(), 
            neonUser.getLastName()
        ));
        
        resolved.setPhoneNumber(chooseLatestString(
            localUser.getPhoneNumber(), 
            neonUser.getPhoneNumber()
        ));
        
        resolved.setProfilePhotoUri(chooseLatestString(
            localUser.getProfilePhotoUri(), 
            neonUser.getProfilePhotoUri()
        ));
        
        Log.d(TAG, "✅ User conflict resolved for: " + resolved.getEmail());
        return resolved;
    }
    
    // ✅ DETECT IF CONFLICT EXISTS
    public boolean hasConflict(BlotterReport local, BlotterReport remote) {
        if (local == null || remote == null) return false;
        
        return !equals(local.getStatus(), remote.getStatus()) ||
               !equals(local.getAssignedOfficerId(), remote.getAssignedOfficerId()) ||
               !equals(local.getNarrative(), remote.getNarrative()) ||
               !equals(local.getIncidentLocation(), remote.getIncidentLocation());
    }
    
    // ✅ DETECT USER CONFLICT
    public boolean hasUserConflict(User local, User remote) {
        if (local == null || remote == null) return false;
        
        return !equals(local.getFirstName(), remote.getFirstName()) ||
               !equals(local.getLastName(), remote.getLastName()) ||
               !equals(local.getPhoneNumber(), remote.getPhoneNumber()) ||
               !equals(local.getProfilePhotoUri(), remote.getProfilePhotoUri());
    }
    
    // ✅ CHOOSE LATEST STRING VALUE
    private String chooseLatestString(String local, String remote) {
        // If remote is empty/null but local has value, use local
        if ((remote == null || remote.trim().isEmpty()) && 
            (local != null && !local.trim().isEmpty())) {
            return local;
        }
        
        // Default to remote (server wins)
        return remote != null ? remote : local;
    }
    
    // ✅ SAFE EQUALS CHECK
    private boolean equals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
