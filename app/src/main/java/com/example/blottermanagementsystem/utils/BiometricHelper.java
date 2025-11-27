package com.example.blottermanagementsystem.utils;

import android.content.Context;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class BiometricHelper {
    
    public static boolean isBiometricAvailable(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG | 
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;
    }
    
    public static void authenticate(FragmentActivity activity, BiometricCallback callback) {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Authenticate to access the app")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | 
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build();
        
        BiometricPrompt biometricPrompt = new BiometricPrompt(activity,
            ContextCompat.getMainExecutor(activity),
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    callback.onSuccess();
                }
                
                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    callback.onError("Authentication failed");
                }
                
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    callback.onError(errString.toString());
                }
            });
        
        biometricPrompt.authenticate(promptInfo);
    }
    
    public interface BiometricCallback {
        void onSuccess();
        void onError(String error);
    }
}
