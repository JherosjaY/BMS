package com.example.blottermanagementsystem.services;

import android.content.Context;
import android.util.Log;
import com.example.blottermanagementsystem.utils.ApiClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HearingsService {
    private static final String TAG = "HearingsService";
    private Context context;
    private SyncManager syncManager;

    public HearingsService(Context context) {
        this.context = context;
        this.syncManager = new SyncManager(context);
    }

    // SCHEDULE HEARING - HYBRID SYNC
    public void scheduleHearing(String caseId, String hearingDate, String hearingTime, 
                               String location, String presidingOfficer, String createdBy,
                               DataOperationCallback callback) {
        Log.d(TAG, "📅 Scheduling hearing for case: " + caseId);
        
        Map<String, Object> hearingData = new HashMap<>();
        hearingData.put("case_id", caseId);
        hearingData.put("hearing_date", hearingDate);
        hearingData.put("hearing_time", hearingTime);
        hearingData.put("location", location);
        hearingData.put("presiding_officer", presidingOfficer);
        hearingData.put("created_by", createdBy);

        if (!syncManager.isOnline()) {
            syncManager.queueForSync("hearings", hearingData);
            callback.onSuccess("Hearing scheduled (offline - will sync when online)");
            return;
        }

        ApiClient.getApiService().scheduleHearing(hearingData)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Hearing scheduled successfully");
                        callback.onSuccess("Hearing scheduled successfully");
                    } else {
                        Log.e(TAG, "❌ Failed to schedule hearing: " + response.code());
                        syncManager.queueForSync("hearings", hearingData);
                        callback.onSuccess("Hearing scheduled (will sync when online)");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "❌ Schedule hearing error", t);
                    syncManager.queueForSync("hearings", hearingData);
                    callback.onSuccess("Hearing scheduled (will sync when online)");
                }
            });
    }

    // GET HEARINGS FOR CALENDAR
    public void getHearingsForCalendar(String startDate, String endDate, HearingsCallback callback) {
        Log.d(TAG, "📅 Getting hearings from " + startDate + " to " + endDate);
        
        if (!syncManager.isOnline()) {
            callback.onError("Device is offline. Please go online to view hearings.");
            return;
        }

        Map<String, Object> dateRange = new HashMap<>();
        dateRange.put("start_date", startDate);
        dateRange.put("end_date", endDate);

        ApiClient.getApiService().getHearingsByDateRange(dateRange)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Hearings loaded successfully");
                        callback.onSuccess(response.body());
                    } else {
                        Log.e(TAG, "❌ Failed to load hearings: " + response.code());
                        callback.onError("No hearings found");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "❌ Get hearings error", t);
                    callback.onError("Failed to load hearings: " + t.getMessage());
                }
            });
    }

    // GET USER'S HEARINGS
    public void getUserHearings(String userId, HearingsCallback callback) {
        Log.d(TAG, "👤 Getting hearings for user: " + userId);
        
        if (!syncManager.isOnline()) {
            callback.onError("Device is offline. Please go online to view your hearings.");
            return;
        }

        ApiClient.getApiService().getUserHearings(userId)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ User hearings loaded successfully");
                        callback.onSuccess(response.body());
                    } else {
                        Log.e(TAG, "❌ Failed to load user hearings: " + response.code());
                        callback.onError("No hearings found for user");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "❌ Get user hearings error", t);
                    callback.onError("Failed to load user hearings: " + t.getMessage());
                }
            });
    }

    // ADD HEARING MINUTES
    public void addHearingMinutes(String hearingId, String minuteType, String content, 
                                 String addedBy, DataOperationCallback callback) {
        Log.d(TAG, "📝 Adding hearing minutes for: " + hearingId);
        
        Map<String, Object> minuteData = new HashMap<>();
        minuteData.put("hearing_id", hearingId);
        minuteData.put("minute_type", minuteType);
        minuteData.put("content", content);
        minuteData.put("added_by", addedBy);

        if (!syncManager.isOnline()) {
            syncManager.queueForSync("hearing_minutes", minuteData);
            callback.onSuccess("Minutes added (offline - will sync when online)");
            return;
        }

        ApiClient.getApiService().addHearingMinutes(minuteData)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Hearing minutes added successfully");
                        callback.onSuccess("Hearing minutes added successfully");
                    } else {
                        Log.e(TAG, "❌ Failed to add minutes: " + response.code());
                        syncManager.queueForSync("hearing_minutes", minuteData);
                        callback.onSuccess("Minutes added (will sync when online)");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "❌ Add minutes error", t);
                    syncManager.queueForSync("hearing_minutes", minuteData);
                    callback.onSuccess("Minutes added (will sync when online)");
                }
            });
    }

    // UPDATE HEARING STATUS
    public void updateHearingStatus(String hearingId, String newStatus, DataOperationCallback callback) {
        Log.d(TAG, "🔄 Updating hearing status to: " + newStatus);
        
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("hearing_id", hearingId);
        statusData.put("new_status", newStatus);

        if (!syncManager.isOnline()) {
            syncManager.queueForSync("hearing_status", statusData);
            callback.onSuccess("Status updated (offline - will sync when online)");
            return;
        }

        ApiClient.getApiService().updateHearingStatus(hearingId, statusData)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Hearing status updated successfully");
                        callback.onSuccess("Hearing status updated successfully");
                    } else {
                        Log.e(TAG, "❌ Failed to update status: " + response.code());
                        syncManager.queueForSync("hearing_status", statusData);
                        callback.onSuccess("Status updated (will sync when online)");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "❌ Update status error", t);
                    syncManager.queueForSync("hearing_status", statusData);
                    callback.onSuccess("Status updated (will sync when online)");
                }
            });
    }

    // CALLBACK INTERFACES
    public interface HearingsCallback {
        void onSuccess(Map<String, Object> hearings);
        void onError(String error);
    }

    public interface DataOperationCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}
