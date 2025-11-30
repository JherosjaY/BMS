package com.example.blottermanagementsystem.services;

import android.content.Context;
import android.util.Log;
import com.example.blottermanagementsystem.data.entity.CaseInvolvement;
import com.example.blottermanagementsystem.data.entity.CriminalRecord;
import com.example.blottermanagementsystem.data.entity.PersonProfile;
import com.example.blottermanagementsystem.utils.ApiClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonHistoryService {
    private static final String TAG = "PersonHistoryService";
    private Context context;
    // ❌ REMOVED: private SyncManager syncManager; (Pure online mode)

    public PersonHistoryService(Context context) {
        this.context = context;
        // ❌ REMOVED: this.syncManager = new SyncManager(context); (Pure online mode)
    }

    // SEARCH PERSON HISTORY - HYBRID APPROACH
    public void searchPersonHistory(String searchTerm, PersonHistoryCallback callback) {
        Log.d(TAG, "🔍 Searching for: " + searchTerm);
        
        if (!syncManager.isOnline()) {
            callback.onError("Device is offline. Please go online to search.");
            return;
        }

        Map<String, Object> searchData = new HashMap<>();
        searchData.put("search_term", searchTerm);

        ApiClient.getApiService().searchPersonHistory(searchData)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Search successful");
                        callback.onSuccess(response.body());
                    } else {
                        Log.e(TAG, "❌ Search failed: " + response.code());
                        callback.onError("No results found");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "❌ Search error", t);
                    callback.onError("Search failed: " + t.getMessage());
                }
            });
    }

    // GET COMPLETE PERSON HISTORY
    public void getPersonCompleteHistory(String personId, CompleteHistoryCallback callback) {
        Log.d(TAG, "📋 Loading complete history for: " + personId);
        
        if (!syncManager.isOnline()) {
            callback.onError("Device is offline. Please go online to view history.");
            return;
        }

        ApiClient.getApiService().getPersonCompleteHistory(personId)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ History loaded successfully");
                        callback.onSuccess(response.body());
                    } else {
                        Log.e(TAG, "❌ History load failed: " + response.code());
                        callback.onError("No history found for this person");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "❌ History load error", t);
                    callback.onError("Failed to load history: " + t.getMessage());
                }
            });
    }

    // ADD NEW CRIMINAL RECORD - DUAL WRITE
    public void addCriminalRecord(String personId, String crimeType, String crimeDescription, 
                                  String dateCommitted, String dateArrested, String officerId, 
                                  DataOperationCallback callback) {
        Log.d(TAG, "➕ Adding criminal record for: " + personId);
        
        Map<String, Object> recordData = new HashMap<>();
        recordData.put("person_id", personId);
        recordData.put("crime_type", crimeType);
        recordData.put("crime_description", crimeDescription);
        recordData.put("date_committed", dateCommitted);
        recordData.put("date_arrested", dateArrested);
        recordData.put("officer_id", officerId);

        if (!syncManager.isOnline()) {
            // OFFLINE - QUEUE FOR SYNC
            syncManager.queueForSync("criminal_records", recordData);
            callback.onSuccess("Record added (offline - will sync when online)");
            return;
        }

        // ONLINE - SYNC IMMEDIATELY
        ApiClient.getApiService().addCriminalRecord(recordData)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Criminal record added successfully");
                        callback.onSuccess("Criminal record added successfully");
                    } else {
                        Log.e(TAG, "❌ Failed to add record: " + response.code());
                        syncManager.queueForSync("criminal_records", recordData);
                        callback.onSuccess("Record added (will sync when online)");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "❌ Add record error", t);
                    syncManager.queueForSync("criminal_records", recordData);
                    callback.onSuccess("Record added (will sync when online)");
                }
            });
    }

    // CREATE NEW PERSON PROFILE
    public void createPersonProfile(String firstName, String lastName, String alias, 
                                   String dateOfBirth, String gender, String riskLevel, 
                                   String officerId, DataOperationCallback callback) {
        Log.d(TAG, "👤 Creating new person profile: " + firstName + " " + lastName);
        
        String personId = "PERSON-" + System.currentTimeMillis();
        
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("person_id", personId);
        profileData.put("first_name", firstName);
        profileData.put("last_name", lastName);
        profileData.put("alias", alias);
        profileData.put("date_of_birth", dateOfBirth);
        profileData.put("gender", gender);
        profileData.put("risk_level", riskLevel);
        profileData.put("created_by", officerId);

        if (!syncManager.isOnline()) {
            syncManager.queueForSync("person_profiles", profileData);
            callback.onSuccess("Profile created (offline - will sync when online)");
            return;
        }

        ApiClient.getApiService().createPersonProfile(profileData)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Person profile created successfully");
                        callback.onSuccess("Person profile created successfully");
                    } else {
                        Log.e(TAG, "❌ Failed to create profile: " + response.code());
                        syncManager.queueForSync("person_profiles", profileData);
                        callback.onSuccess("Profile created (will sync when online)");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "❌ Create profile error", t);
                    syncManager.queueForSync("person_profiles", profileData);
                    callback.onSuccess("Profile created (will sync when online)");
                }
            });
    }

    // ADD CASE INVOLVEMENT
    public void addCaseInvolvement(String personId, String caseId, String involvementType, 
                                   String involvementDetails, String officerId, 
                                   DataOperationCallback callback) {
        Log.d(TAG, "🔗 Adding case involvement for: " + personId);
        
        Map<String, Object> involvementData = new HashMap<>();
        involvementData.put("person_id", personId);
        involvementData.put("case_id", caseId);
        involvementData.put("involvement_type", involvementType);
        involvementData.put("involvement_details", involvementDetails);
        involvementData.put("created_by", officerId);

        if (!syncManager.isOnline()) {
            syncManager.queueForSync("case_involvements", involvementData);
            callback.onSuccess("Case involvement added (offline - will sync when online)");
            return;
        }

        ApiClient.getApiService().addCaseInvolvement(involvementData)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Case involvement added successfully");
                        callback.onSuccess("Case involvement added successfully");
                    } else {
                        Log.e(TAG, "❌ Failed to add involvement: " + response.code());
                        syncManager.queueForSync("case_involvements", involvementData);
                        callback.onSuccess("Case involvement added (will sync when online)");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "❌ Add involvement error", t);
                    syncManager.queueForSync("case_involvements", involvementData);
                    callback.onSuccess("Case involvement added (will sync when online)");
                }
            });
    }

    // CALLBACK INTERFACES
    public interface PersonHistoryCallback {
        void onSuccess(Map<String, Object> results);
        void onError(String error);
    }

    public interface CompleteHistoryCallback {
        void onSuccess(Map<String, Object> history);
        void onError(String error);
    }

    public interface DataOperationCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}
