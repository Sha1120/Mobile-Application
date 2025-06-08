package com.example.myuser; // ඔබේ package name එකට update කරන්න

import android.content.Context;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

public class FingerprintAuth {

    public interface AuthCallback {
        void onAuthenticationSuccess();
        void onAuthenticationFailed();
    }

    public static void showBiometricPrompt(Context context, AuthCallback callback) {
        BiometricManager biometricManager = BiometricManager.from(context);
        if (biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) {
            callback.onAuthenticationFailed();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(context);
        BiometricPrompt biometricPrompt = new BiometricPrompt((androidx.fragment.app.FragmentActivity) context,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        callback.onAuthenticationSuccess();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        callback.onAuthenticationFailed();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Login")
                .setSubtitle("Scan your fingerprint to continue")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
