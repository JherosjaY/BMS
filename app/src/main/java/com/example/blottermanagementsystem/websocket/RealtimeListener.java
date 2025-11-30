package com.example.blottermanagementsystem.websocket;

/**
 * RealtimeListener - Interface for real-time event updates
 * Implement this to receive WebSocket events
 */
public interface RealtimeListener {
    
    /**
     * Called when a real-time update is received
     * 
     * @param eventType Type of event (e.g., "hearing_update", "case_update", "notification")
     * @param data The event data (usually a JsonObject)
     */
    void onRealtimeUpdate(String eventType, Object data);
}
