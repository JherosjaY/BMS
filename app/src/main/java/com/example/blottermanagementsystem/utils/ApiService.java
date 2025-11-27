package com.example.blottermanagementsystem.utils;

import com.example.blottermanagementsystem.data.entity.BlotterReport;

import java.util.List;

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
    
    // ============ REPORTS ============
    
    /**
     * Create a new report
     * POST /reports
     */
    @POST("reports")
    Call<BlotterReport> createReport(@Body BlotterReport report);
    
    /**
     * Get all reports
     * GET /reports
     */
    @GET("reports")
    Call<List<BlotterReport>> getAllReports();
    
    /**
     * Get report by ID
     * GET /reports/{id}
     */
    @GET("reports/{id}")
    Call<BlotterReport> getReportById(@Path("id") int reportId);
    
    /**
     * Update report
     * PUT /reports/{id}
     */
    @PUT("reports/{id}")
    Call<BlotterReport> updateReport(@Path("id") int reportId, @Body BlotterReport report);
    
    /**
     * Delete report
     * DELETE /reports/{id}
     */
    @DELETE("reports/{id}")
    Call<String> deleteReport(@Path("id") int reportId);
    
    // ============ WITNESSES ============
    
    /**
     * Get witnesses by report ID
     * GET /witnesses/report/{reportId}
     */
    @GET("witnesses/report/{reportId}")
    Call<List<Object>> getWitnessesByReportId(@Path("reportId") int reportId);
    
    /**
     * Create witness
     * POST /witnesses
     */
    @POST("witnesses")
    Call<Object> createWitness(@Body Object witness);
    
    /**
     * Delete witness
     * DELETE /witnesses/{id}
     */
    @DELETE("witnesses/{id}")
    Call<String> deleteWitness(@Path("id") int witnessId);
    
    // ============ SUSPECTS ============
    
    /**
     * Get suspects by report ID
     * GET /suspects/report/{reportId}
     */
    @GET("suspects/report/{reportId}")
    Call<List<Object>> getSuspectsByReportId(@Path("reportId") int reportId);
    
    /**
     * Create suspect
     * POST /suspects
     */
    @POST("suspects")
    Call<Object> createSuspect(@Body Object suspect);
    
    /**
     * Delete suspect
     * DELETE /suspects/{id}
     */
    @DELETE("suspects/{id}")
    Call<String> deleteSuspect(@Path("id") int suspectId);
    
    // ============ EVIDENCE ============
    
    /**
     * Get evidence by report ID
     * GET /evidence/report/{reportId}
     */
    @GET("evidence/report/{reportId}")
    Call<List<Object>> getEvidenceByReportId(@Path("reportId") int reportId);
    
    /**
     * Create evidence
     * POST /evidence
     */
    @POST("evidence")
    Call<Object> createEvidence(@Body Object evidence);
    
    /**
     * Delete evidence
     * DELETE /evidence/{id}
     */
    @DELETE("evidence/{id}")
    Call<String> deleteEvidence(@Path("id") int evidenceId);
    
    // ============ HEARINGS ============
    
    /**
     * Get hearings by report ID
     * GET /hearings/report/{reportId}
     */
    @GET("hearings/report/{reportId}")
    Call<List<Object>> getHearingsByReportId(@Path("reportId") int reportId);
    
    /**
     * Create hearing
     * POST /hearings
     */
    @POST("hearings")
    Call<Object> createHearing(@Body Object hearing);
    
    /**
     * Delete hearing
     * DELETE /hearings/{id}
     */
    @DELETE("hearings/{id}")
    Call<String> deleteHearing(@Path("id") int hearingId);
    
    // ============ RESOLUTIONS ============
    
    /**
     * Get resolutions by report ID
     * GET /resolutions/report/{reportId}
     */
    @GET("resolutions/report/{reportId}")
    Call<List<Object>> getResolutionsByReportId(@Path("reportId") int reportId);
    
    /**
     * Create resolution
     * POST /resolutions
     */
    @POST("resolutions")
    Call<Object> createResolution(@Body Object resolution);
    
    /**
     * Delete resolution
     * DELETE /resolutions/{id}
     */
    @DELETE("resolutions/{id}")
    Call<String> deleteResolution(@Path("id") int resolutionId);
    
    // ============ KP FORMS ============
    
    /**
     * Get KP forms by report ID
     * GET /kpforms/report/{reportId}
     */
    @GET("kpforms/report/{reportId}")
    Call<List<Object>> getKPFormsByReportId(@Path("reportId") int reportId);
    
    /**
     * Create KP form
     * POST /kpforms
     */
    @POST("kpforms")
    Call<Object> createKPForm(@Body Object kpForm);
    
    /**
     * Delete KP form
     * DELETE /kpforms/{id}
     */
    @DELETE("kpforms/{id}")
    Call<String> deleteKPForm(@Path("id") int kpFormId);
    
    // ============ SUMMONS ============
    
    /**
     * Get summons by report ID
     * GET /summons/report/{reportId}
     */
    @GET("summons/report/{reportId}")
    Call<List<Object>> getSummonsByReportId(@Path("reportId") int reportId);
    
    /**
     * Create summons
     * POST /summons
     */
    @POST("summons")
    Call<Object> createSummons(@Body Object summons);
    
    /**
     * Delete summons
     * DELETE /summons/{id}
     */
    @DELETE("summons/{id}")
    Call<String> deleteSummons(@Path("id") int summonsId);
}
