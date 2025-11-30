package com.example.blottermanagementsystem.firebase;

import android.content.Context;
import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.blottermanagementsystem.websocket.RealtimeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * FirebaseRealtimeListener - Listen for real-time updates from Firebase
 * Syncs data from Firebase to local SQLite
 * Neon PostgreSQL remains primary database
 */
public class FirebaseRealtimeListener {
    private static final String TAG = "FirebaseRealtimeListener";
    
    private Context context;
    private DatabaseReference firebaseDb;
    private List<RealtimeListener> listeners = new ArrayList<>();
    
    public FirebaseRealtimeListener(Context context) {
        this.context = context;
        try {
            this.firebaseDb = FirebaseDatabase.getInstance().getReference();
            Log.d(TAG, "✅ Firebase Realtime Database initialized");
        } catch (Exception e) {
            Log.e(TAG, "❌ Firebase initialization error: " + e.getMessage());
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
    
    /**
     * Listen to hearing updates from Firebase
     */
    public void listenToHearings() {
        if (firebaseDb == null) {
            Log.w(TAG, "⚠️ Firebase not initialized");
            return;
        }
        
        firebaseDb.child("hearings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "📅 Hearing update from Firebase");
                        notifyListeners("firebase_hearing_update", dataSnapshot.getValue());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error processing hearing update: " + e.getMessage());
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "❌ Firebase error: " + databaseError.getMessage());
            }
        });
        
        Log.d(TAG, "👂 Listening to Firebase hearings");
    }
    
    /**
     * Listen to case updates from Firebase
     */
    public void listenToCases() {
        if (firebaseDb == null) {
            Log.w(TAG, "⚠️ Firebase not initialized");
            return;
        }
        
        firebaseDb.child("cases").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "📋 Case update from Firebase");
                        notifyListeners("firebase_case_update", dataSnapshot.getValue());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error processing case update: " + e.getMessage());
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "❌ Firebase error: " + databaseError.getMessage());
            }
        });
        
        Log.d(TAG, "👂 Listening to Firebase cases");
    }
    
    /**
     * Listen to person updates from Firebase
     */
    public void listenToPersons() {
        if (firebaseDb == null) {
            Log.w(TAG, "⚠️ Firebase not initialized");
            return;
        }
        
        firebaseDb.child("persons").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "👤 Person update from Firebase");
                        notifyListeners("firebase_person_update", dataSnapshot.getValue());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error processing person update: " + e.getMessage());
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "❌ Firebase error: " + databaseError.getMessage());
            }
        });
        
        Log.d(TAG, "👂 Listening to Firebase persons");
    }
    
    /**
     * Listen to all updates from Firebase
     */
    public void listenToAll() {
        Log.d(TAG, "👂 Starting to listen to all Firebase updates");
        listenToHearings();
        listenToCases();
        listenToPersons();
    }
    
    /**
     * Stop listening to Firebase updates
     */
    public void stopListening() {
        if (firebaseDb != null) {
            firebaseDb.removeValue();
            Log.d(TAG, "🔌 Stopped listening to Firebase");
        }
    }
}
