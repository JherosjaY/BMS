package com.example.blottermanagementsystem.websocket;

import android.content.Context;
import android.util.Log;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * WebSocketManager - Real-time synchronization with Render backend
 * Handles WebSocket connections for instant data updates
 * Neon PostgreSQL remains primary database
 */
public class WebSocketManager extends WebSocketListener {
    private static final String TAG = "WebSocketManager";
    private static final String WS_URL = "wss://bms-1op6.onrender.com/ws/realtime";
    
    private WebSocket webSocket;
    private OkHttpClient okHttpClient;
    private Context context;
    private BlotterDatabase database;
    private Gson gson;
    
    private String userId;
    private String userRole;
    private boolean isConnected = false;
    
    private List<RealtimeListener> listeners = new ArrayList<>();
    
    public WebSocketManager(Context context) {
        this.context = context;
        this.database = BlotterDatabase.getDatabase(context);
        this.gson = new Gson();
        
        // Create OkHttpClient with WebSocket support
        this.okHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Connect to WebSocket server
     */
    public void connect(String userId, String userRole) {
        if (isConnected) {
            Log.d(TAG, "⚠️ Already connected");
            return;
        }
        
        this.userId = userId;
        this.userRole = userRole;
        
        Log.d(TAG, "🔌 Connecting to WebSocket: " + WS_URL);
        
        Request request = new Request.Builder()
                .url(WS_URL)
                .build();
        
        webSocket = okHttpClient.newWebSocket(request, this);
    }
    
    /**
     * Disconnect from WebSocket server
     */
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnecting");
            isConnected = false;
            Log.d(TAG, "🔌 Disconnected from WebSocket");
        }
    }
    
    /**
     * Subscribe to a channel
     */
    public void subscribe(String channel) {
        if (!isConnected) {
            Log.w(TAG, "⚠️ Not connected, cannot subscribe to: " + channel);
            return;
        }
        
        JsonObject message = new JsonObject();
        message.addProperty("type", "subscribe");
        message.addProperty("channel", channel);
        
        sendMessage(message);
        Log.d(TAG, "📡 Subscribed to channel: " + channel);
    }
    
    /**
     * Unsubscribe from a channel
     */
    public void unsubscribe(String channel) {
        if (!isConnected) return;
        
        JsonObject message = new JsonObject();
        message.addProperty("type", "unsubscribe");
        message.addProperty("channel", channel);
        
        sendMessage(message);
        Log.d(TAG, "📡 Unsubscribed from channel: " + channel);
    }
    
    /**
     * Send message to server
     */
    private void sendMessage(JsonObject message) {
        if (webSocket != null && isConnected) {
            webSocket.send(message.toString());
        }
    }
    
    /**
     * Add listener for real-time events
     */
    public void addListener(RealtimeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            Log.d(TAG, "👂 Listener added");
        }
    }
    
    /**
     * Remove listener
     */
    public void removeListener(RealtimeListener listener) {
        listeners.remove(listener);
        Log.d(TAG, "👂 Listener removed");
    }
    
    /**
     * Notify all listeners of an event
     */
    private void notifyListeners(String eventType, Object data) {
        for (RealtimeListener listener : listeners) {
            listener.onRealtimeUpdate(eventType, data);
        }
    }
    
    // ============================================================================
    // WebSocketListener Callbacks
    // ============================================================================
    
    @Override
    public void onOpen(WebSocket webSocket, okhttp3.Response response) {
        Log.d(TAG, "✅ WebSocket connected!");
        isConnected = true;
        
        // Authenticate
        JsonObject authMessage = new JsonObject();
        authMessage.addProperty("type", "auth");
        authMessage.addProperty("userId", userId);
        authMessage.addProperty("role", userRole);
        
        sendMessage(authMessage);
        
        // Subscribe to channels
        subscribe("hearings");
        subscribe("cases");
        subscribe("persons");
        subscribe("notifications");
        
        notifyListeners("connected", null);
    }
    
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        try {
            JsonObject message = gson.fromJson(text, JsonObject.class);
            String type = message.get("type").getAsString();
            
            Log.d(TAG, "📨 Message received: " + type);
            
            switch (type) {
                case "authenticated":
                    handleAuthenticated(message);
                    break;
                    
                case "hearing_update":
                    handleHearingUpdate(message);
                    break;
                    
                case "case_update":
                    handleCaseUpdate(message);
                    break;
                    
                case "person_update":
                    handlePersonUpdate(message);
                    break;
                    
                case "notification":
                    handleNotification(message);
                    break;
                    
                case "pong":
                    Log.d(TAG, "💓 Pong received (connection alive)");
                    break;
                    
                case "error":
                    handleError(message);
                    break;
                    
                default:
                    Log.w(TAG, "⚠️ Unknown message type: " + type);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing message: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        Log.d(TAG, "📨 Binary message received");
    }
    
    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "🔌 WebSocket closing: " + reason);
        isConnected = false;
    }
    
    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "🔌 WebSocket closed: " + reason);
        isConnected = false;
        notifyListeners("disconnected", null);
    }
    
    @Override
    public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
        Log.e(TAG, "❌ WebSocket error: " + t.getMessage(), t);
        isConnected = false;
        notifyListeners("error", t.getMessage());
        
        // Attempt reconnect after 5 seconds
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                () -> connect(userId, userRole),
                5000
        );
    }
    
    // ============================================================================
    // Message Handlers
    // ============================================================================
    
    private void handleAuthenticated(JsonObject message) {
        Log.d(TAG, "✅ Authenticated with server");
        notifyListeners("authenticated", message);
    }
    
    private void handleHearingUpdate(JsonObject message) {
        try {
            String eventType = message.get("eventType").getAsString();
            JsonObject hearingData = message.getAsJsonObject("data");
            
            Log.d(TAG, "📅 Hearing update: " + eventType);
            
            // Notify listeners
            notifyListeners("hearing_update", hearingData);
            
            // Sync to local database
            syncHearingToLocal(hearingData);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling hearing update: " + e.getMessage(), e);
        }
    }
    
    private void handleCaseUpdate(JsonObject message) {
        try {
            String eventType = message.get("eventType").getAsString();
            JsonObject caseData = message.getAsJsonObject("data");
            
            Log.d(TAG, "📋 Case update: " + eventType);
            
            // Notify listeners
            notifyListeners("case_update", caseData);
            
            // Sync to local database
            syncCaseToLocal(caseData);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling case update: " + e.getMessage(), e);
        }
    }
    
    private void handlePersonUpdate(JsonObject message) {
        try {
            String eventType = message.get("eventType").getAsString();
            JsonObject personData = message.getAsJsonObject("data");
            
            Log.d(TAG, "👤 Person update: " + eventType);
            
            // Notify listeners
            notifyListeners("person_update", personData);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling person update: " + e.getMessage(), e);
        }
    }
    
    private void handleNotification(JsonObject message) {
        try {
            JsonObject notification = message.getAsJsonObject("data");
            
            Log.d(TAG, "🔔 Notification received");
            
            // Notify listeners
            notifyListeners("notification", notification);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling notification: " + e.getMessage(), e);
        }
    }
    
    private void handleError(JsonObject message) {
        String errorMsg = message.get("message").getAsString();
        Log.e(TAG, "❌ Server error: " + errorMsg);
        notifyListeners("error", errorMsg);
    }
    
    // ============================================================================
    // Local Database Sync
    // ============================================================================
    
    private void syncHearingToLocal(JsonObject hearingData) {
        // Implementation depends on your local database schema
        Log.d(TAG, "💾 Syncing hearing to local database");
    }
    
    private void syncCaseToLocal(JsonObject caseData) {
        // Implementation depends on your local database schema
        Log.d(TAG, "💾 Syncing case to local database");
    }
    
    // ============================================================================
    // Getters
    // ============================================================================
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getUserRole() {
        return userRole;
    }
}
