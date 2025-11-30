package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

/**
 * 🌐 NETWORK CONNECTIVITY MANAGER
 * 
 * Checks internet connectivity for Pure Neon Online-Only Mode
 * - If online: App works normally
 * - If offline: Show "No internet" message
 */
public class NetworkConnectivityManager {
    private static final String TAG = "NetworkConnectivity";
    private Context context;
    private ConnectivityManager connectivityManager;
    
    public NetworkConnectivityManager(Context context) {
        this.context = context;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    
    /**
     * 🌐 CHECK IF DEVICE HAS INTERNET CONNECTION
     * Returns true if device is connected to internet
     */
    public boolean isConnectedToInternet() {
        try {
            if (connectivityManager == null) {
                Log.w(TAG, "⚠️ ConnectivityManager is null");
                return false;
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0 and above
                android.net.Network network = connectivityManager.getActiveNetwork();
                if (network == null) {
                    Log.d(TAG, "❌ No active network - OFFLINE");
                    return false;
                }
                
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities == null) {
                    Log.d(TAG, "❌ No network capabilities - OFFLINE");
                    return false;
                }
                
                boolean hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                boolean hasValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                
                if (hasInternet && hasValidated) {
                    Log.d(TAG, "✅ Connected to internet - ONLINE");
                    return true;
                } else {
                    Log.d(TAG, "❌ No valid internet connection - OFFLINE");
                    return false;
                }
            } else {
                // Android 5.1 and below
                android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Log.d(TAG, "✅ Connected to internet - ONLINE");
                    return true;
                } else {
                    Log.d(TAG, "❌ No internet connection - OFFLINE");
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error checking connectivity: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 🌐 CHECK IF WIFI CONNECTED
     */
    public boolean isWifiConnected() {
        try {
            if (connectivityManager == null) return false;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.net.Network network = connectivityManager.getActiveNetwork();
                if (network == null) return false;
                
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            } else {
                android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error checking WiFi: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 🌐 CHECK IF MOBILE DATA CONNECTED
     */
    public boolean isMobileConnected() {
        try {
            if (connectivityManager == null) return false;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.net.Network network = connectivityManager.getActiveNetwork();
                if (network == null) return false;
                
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            } else {
                android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error checking mobile: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 🌐 GET CONNECTION TYPE
     */
    public String getConnectionType() {
        if (!isConnectedToInternet()) {
            return "OFFLINE";
        } else if (isWifiConnected()) {
            return "WIFI";
        } else if (isMobileConnected()) {
            return "MOBILE";
        } else {
            return "UNKNOWN";
        }
    }
}
