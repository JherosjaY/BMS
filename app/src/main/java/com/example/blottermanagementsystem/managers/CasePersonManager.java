package com.example.blottermanagementsystem.managers;

import android.content.Context;
import android.util.Log;
import com.example.blottermanagementsystem.utils.ApiClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ✅ CASE PERSON MANAGER
 * Manages witnesses and suspects for blotter cases
 */
public class CasePersonManager {
    private static final String TAG = "CasePersonManager";
    private Context context;
    
    public CasePersonManager(Context context) {
        this.context = context;
    }
    
    // ✅ WITNESS MANAGEMENT
    
    /**
     * Add witness to case
     */
    public void addWitness(int caseId, String name, String contactInfo, 
                          String statement, String address, ApiCallback<Map<String, Object>> callback) {
        Log.d(TAG, "👥 Adding witness to case: " + caseId);
        
        Map<String, Object> witnessData = createWitnessData(name, contactInfo, statement, address);
        
        ApiClient.addWitnessToCase(caseId, witnessData, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ Witness added successfully");
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", result);
                callback.onSuccess(response);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to add witness: " + error);
                callback.onError("Failed to add witness: " + error);
            }
        });
    }
    
    /**
     * Get all witnesses for a case
     */
    public void getCaseWitnesses(int caseId, ApiCallback<List<Map<String, Object>>> callback) {
        Log.d(TAG, "🔍 Getting witnesses for case: " + caseId);
        
        ApiClient.getCaseWitnesses(caseId, new ApiClient.ApiCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> witnesses) {
                Log.d(TAG, "✅ Retrieved " + witnesses.size() + " witnesses");
                callback.onSuccess(witnesses);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to get witnesses: " + error);
                callback.onError("Failed to get witnesses: " + error);
            }
        });
    }
    
    /**
     * Update witness information
     */
    public void updateWitness(int caseId, int witnessId, Map<String, Object> witnessData, 
                             ApiCallback<Map<String, Object>> callback) {
        Log.d(TAG, "✏️ Updating witness: " + witnessId);
        
        ApiClient.updateWitness(caseId, witnessId, witnessData, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ Witness updated successfully");
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", result);
                callback.onSuccess(response);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to update witness: " + error);
                callback.onError("Failed to update witness: " + error);
            }
        });
    }
    
    /**
     * Delete witness
     */
    public void deleteWitness(int caseId, int witnessId, ApiCallback<String> callback) {
        Log.d(TAG, "🗑️ Deleting witness: " + witnessId);
        
        ApiClient.deleteWitness(caseId, witnessId, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ Witness deleted successfully");
                callback.onSuccess(result);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to delete witness: " + error);
                callback.onError("Failed to delete witness: " + error);
            }
        });
    }
    
    // ✅ SUSPECT MANAGEMENT
    
    /**
     * Add suspect to case
     */
    public void addSuspect(int caseId, String name, String alias, 
                          String address, String description, ApiCallback<Map<String, Object>> callback) {
        Log.d(TAG, "👤 Adding suspect to case: " + caseId);
        
        Map<String, Object> suspectData = createSuspectData(name, alias, address, description);
        
        ApiClient.addSuspectToCase(caseId, suspectData, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ Suspect added successfully");
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", result);
                callback.onSuccess(response);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to add suspect: " + error);
                callback.onError("Failed to add suspect: " + error);
            }
        });
    }
    
    /**
     * Get all suspects for a case
     */
    public void getCaseSuspects(int caseId, ApiCallback<List<Map<String, Object>>> callback) {
        Log.d(TAG, "🔍 Getting suspects for case: " + caseId);
        
        ApiClient.getCaseSuspects(caseId, new ApiClient.ApiCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> suspects) {
                Log.d(TAG, "✅ Retrieved " + suspects.size() + " suspects");
                callback.onSuccess(suspects);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to get suspects: " + error);
                callback.onError("Failed to get suspects: " + error);
            }
        });
    }
    
    /**
     * Update suspect information
     */
    public void updateSuspect(int caseId, int suspectId, Map<String, Object> suspectData, 
                             ApiCallback<Map<String, Object>> callback) {
        Log.d(TAG, "✏️ Updating suspect: " + suspectId);
        
        ApiClient.updateSuspect(caseId, suspectId, suspectData, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ Suspect updated successfully");
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", result);
                callback.onSuccess(response);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to update suspect: " + error);
                callback.onError("Failed to update suspect: " + error);
            }
        });
    }
    
    /**
     * Delete suspect
     */
    public void deleteSuspect(int caseId, int suspectId, ApiCallback<String> callback) {
        Log.d(TAG, "🗑️ Deleting suspect: " + suspectId);
        
        ApiClient.deleteSuspect(caseId, suspectId, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ Suspect deleted successfully");
                callback.onSuccess(result);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to delete suspect: " + error);
                callback.onError("Failed to delete suspect: " + error);
            }
        });
    }
    
    // ✅ HELPER METHODS
    
    /**
     * Create witness data map
     */
    public Map<String, Object> createWitnessData(String name, String contactInfo, 
                                                 String statement, String address) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("contact_info", contactInfo);
        data.put("statement", statement);
        data.put("address", address);
        return data;
    }
    
    /**
     * Create suspect data map
     */
    public Map<String, Object> createSuspectData(String name, String alias, 
                                                 String address, String description) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("alias", alias);
        data.put("address", address);
        data.put("description", description);
        data.put("status", "under_investigation");
        return data;
    }
    
    /**
     * Callback interface
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
