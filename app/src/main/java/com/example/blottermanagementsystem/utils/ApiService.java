package com.example.blottermanagementsystem.utils;

import com.example.blottermanagementsystem.data.entity.BlotterReport;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * ApiService - Retrofit interface for Elysia backend API
 * Defines all HTTP endpoints for the Blotter Management System
 */
public interface ApiService {
    
    // ============ AUTHENTICATION ============
    
    /**
     * Check if email exists
     * POST /api/auth/check-email
     */
    @POST("api/auth/check-email")
    Call<Map<String, Object>> checkEmailExists(@Body Map<String, Object> emailData);
    
    /**
     * User Registration
     * POST /api/auth/register
     */
    @POST("api/auth/register")
    Call<Map<String, Object>> register(@Body Map<String, Object> registrationData);
    
    /**
     * User Login
     * POST /api/auth/login
     */
    @POST("api/auth/login")
    Call<Map<String, Object>> login(@Body Map<String, Object> loginData);
    
    /**
     * Google Sign-In
     * POST /api/auth/google
     */
    @POST("api/auth/google")
    Call<Map<String, Object>> googleSignIn(@Body Map<String, Object> googleAuthData);
    
    /**
     * Update Profile Picture (Save to Neon)
     * POST /api/auth/profile/picture/{userId}
     */
    @POST("api/auth/profile/picture/{userId}")
    Call<Map<String, Object>> updateProfilePicture(
        @Path("userId") int userId,
        @Body Map<String, Object> pictureData
    );
    
    /**
     * Delete User Account (Admin only)
     * DELETE /api/auth/users/{userId}
     */
    @DELETE("api/auth/users/{userId}")
    Call<Map<String, Object>> deleteUser(@Path("userId") String userId);
    
    // ============ REPORTS ============
    
    /**
     * Create a new report
     * POST /api/reports
     */
    @POST("api/reports")
    Call<BlotterReport> createReport(@Body BlotterReport report);
    
    /**
     * Get all reports
     * GET /api/reports
     */
    @GET("api/reports")
    Call<List<BlotterReport>> getAllReports();
    
    /**
     * Get report by ID
     * GET /api/reports/{id}
     */
    @GET("api/reports/{id}")
    Call<BlotterReport> getReportById(@Path("id") int reportId);
    
    /**
     * Update report
     * PUT /api/reports/{id}
     */
    @PUT("api/reports/{id}")
    Call<BlotterReport> updateReport(@Path("id") int reportId, @Body BlotterReport report);
    
    /**
     * Delete report
     * DELETE /api/reports/{id}
     */
    @DELETE("api/reports/{id}")
    Call<String> deleteReport(@Path("id") int reportId);
    
    // ============ WITNESSES ============
    
    /**
     * Get witnesses by report ID
     * GET /api/witnesses/report/{reportId}
     */
    @GET("api/witnesses/report/{reportId}")
    Call<List<Object>> getWitnessesByReportId(@Path("reportId") int reportId);
    
    /**
     * Create witness
     * POST /api/witnesses
     */
    @POST("api/witnesses")
    Call<Object> createWitness(@Body Object witness);
    
    /**
     * Delete witness
     * DELETE /api/witnesses/{id}
     */
    @DELETE("api/witnesses/{id}")
    Call<String> deleteWitness(@Path("id") int witnessId);
    
    // ============ SUSPECTS ============
    
    /**
     * Get suspects by report ID
     * GET /api/suspects/report/{reportId}
     */
    @GET("api/suspects/report/{reportId}")
    Call<List<Object>> getSuspectsByReportId(@Path("reportId") int reportId);
    
    /**
     * Create suspect
     * POST /api/suspects
     */
    @POST("api/suspects")
    Call<Object> createSuspect(@Body Object suspect);
    
    /**
     * Delete suspect
     * DELETE /api/suspects/{id}
     */
    @DELETE("api/suspects/{id}")
    Call<String> deleteSuspect(@Path("id") int suspectId);
    
    // ============ EVIDENCE ============
    
    /**
     * Get evidence by report ID
     * GET /api/evidence/report/{reportId}
     */
    @GET("api/evidence/report/{reportId}")
    Call<List<Object>> getEvidenceByReportId(@Path("reportId") int reportId);
    
    /**
     * Create evidence
     * POST /api/evidence
     */
    @POST("api/evidence")
    Call<Object> createEvidence(@Body Object evidence);
    
    /**
     * Delete evidence
     * DELETE /api/evidence/{id}
     */
    @DELETE("api/evidence/{id}")
    Call<String> deleteEvidence(@Path("id") int evidenceId);
    
    // ============ HEARINGS ============
    
    /**
     * Get hearings by report ID
     * GET /api/hearings/report/{reportId}
     */
    @GET("api/hearings/report/{reportId}")
    Call<List<Object>> getHearingsByReportId(@Path("reportId") int reportId);
    
    /**
     * Create hearing
     * POST /api/hearings
     */
    @POST("api/hearings")
    Call<Object> createHearing(@Body Object hearing);
    
    /**
     * Delete hearing
     * DELETE /api/hearings/{id}
     */
    @DELETE("api/hearings/{id}")
    Call<String> deleteHearing(@Path("id") int hearingId);
    
    // ============ RESOLUTIONS ============
    
    /**
     * Get resolutions by report ID
     * GET /api/resolutions/report/{reportId}
     */
    @GET("api/resolutions/report/{reportId}")
    Call<List<Object>> getResolutionsByReportId(@Path("reportId") int reportId);
    
    /**
     * Create resolution
     * POST /api/resolutions
     */
    @POST("api/resolutions")
    Call<Object> createResolution(@Body Object resolution);
    
    /**
     * Delete resolution
     * DELETE /api/resolutions/{id}
     */
    @DELETE("api/resolutions/{id}")
    Call<String> deleteResolution(@Path("id") int resolutionId);
    
    // ============ KP FORMS ============
    
    /**
     * Get KP forms by report ID
     * GET /api/kpforms/report/{reportId}
     */
    @GET("api/kpforms/report/{reportId}")
    Call<List<Object>> getKPFormsByReportId(@Path("reportId") int reportId);
    
    /**
     * Create KP form
     * POST /api/kpforms
     */
    @POST("api/kpforms")
    Call<Object> createKPForm(@Body Object kpForm);
    
    /**
     * Delete KP form
     * DELETE /api/kpforms/{id}
     */
    @DELETE("api/kpforms/{id}")
    Call<String> deleteKPForm(@Path("id") int kpFormId);
    
    // ============ SUMMONS ============
    
    /**
     * Get summons by report ID
     * GET /api/summons/report/{reportId}
     */
    @GET("api/summons/report/{reportId}")
    Call<List<Object>> getSummonsByReportId(@Path("reportId") int reportId);
    
    /**
     * Create summons
     * POST /api/summons
     */
    @POST("api/summons")
    Call<Object> createSummons(@Body Object summons);
    
    /**
     * Delete summons
     * DELETE /api/summons/{id}
     */
    @DELETE("api/summons/{id}")
    Call<String> deleteSummons(@Path("id") int summonsId);
    
    // ============ PERSON HISTORY ============
    
    /**
     * Search person history
     * POST /api/person-history/search
     */
    @POST("api/person-history/search")
    Call<Map<String, Object>> searchPersonHistory(@Body Map<String, Object> searchData);
    
    /**
     * Get complete person history
     * GET /api/person-history/{personId}
     */
    @GET("api/person-history/{personId}")
    Call<Map<String, Object>> getPersonCompleteHistory(@Path("personId") String personId);
    
    /**
     * Create person profile
     * POST /api/person-history/profile
     */
    @POST("api/person-history/profile")
    Call<Map<String, Object>> createPersonProfile(@Body Map<String, Object> profileData);
    
    /**
     * Add criminal record
     * POST /api/person-history/criminal-record
     */
    @POST("api/person-history/criminal-record")
    Call<Map<String, Object>> addCriminalRecord(@Body Map<String, Object> recordData);
    
    /**
     * Add case involvement
     * POST /api/person-history/case-involvement
     */
    @POST("api/person-history/case-involvement")
    Call<Map<String, Object>> addCaseInvolvement(@Body Map<String, Object> involvementData);
    
    /**
     * Update person risk level
     * PUT /api/person-history/{personId}/risk-level
     */
    @PUT("api/person-history/{personId}/risk-level")
    Call<Map<String, Object>> updatePersonRiskLevel(@Path("personId") String personId, @Body Map<String, Object> riskData);
    
    // ============ HEARINGS ============
    
    /**
     * Schedule hearing
     * POST /api/hearings/schedule
     */
    @POST("api/hearings/schedule")
    Call<Map<String, Object>> scheduleHearing(@Body Map<String, Object> hearingData);
    
    /**
     * Get hearings by date range
     * POST /api/hearings/by-date-range
     */
    @POST("api/hearings/by-date-range")
    Call<Map<String, Object>> getHearingsByDateRange(@Body Map<String, Object> dateRange);
    
    /**
     * Get user hearings
     * GET /api/hearings/user/{userId}
     */
    @GET("api/hearings/user/{userId}")
    Call<Map<String, Object>> getUserHearings(@Path("userId") String userId);
    
    /**
     * Add hearing minutes
     * POST /api/hearings/minutes
     */
    @POST("api/hearings/minutes")
    Call<Map<String, Object>> addHearingMinutes(@Body Map<String, Object> minuteData);
    
    /**
     * Update hearing status
     * PUT /api/hearings/{hearingId}/status
     */
    @PUT("api/hearings/{hearingId}/status")
    Call<Map<String, Object>> updateHearingStatus(@Path("hearingId") String hearingId, @Body Map<String, Object> statusData);
}
