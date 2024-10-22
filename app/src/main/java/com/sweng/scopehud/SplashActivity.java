package com.sweng.scopehud;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DISPLAY_LENGTH = 2000; // Duration for splash screen in milliseconds

    /**
     * Called when the activity is created.
     * It sets up the splash screen layout and initializes Firebase before proceeding to the next activity.
     *
     * @param savedInstanceState If the activity is being re-initialized after being previously shut down, this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view for the splash screen
        setContentView(R.layout.activity_splash);

        // Ensure that Firebase is initialized before proceeding
        initializeFirebaseAndProceed();
    }

    /**
     * Initializes Firebase and logs the status.
     * If Firebase initialization succeeds, it proceeds to the next activity.
     * If Firebase is already initialized, it skips re-initialization.
     */
    private void initializeFirebaseAndProceed() {
        try {
            // Check if Firebase is initialized; if not, initialize it
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase initialized successfully.");
            }

            // Delay for the splash screen duration before proceeding
            proceedToNextActivity();
        } catch (Exception e) {
            // Log an error if Firebase initialization fails
            Log.e(TAG, "Firebase initialization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Proceeds to the next activity after a delay.
     * If the user is logged in, it redirects to MainActivity.
     * If the user is not logged in, it redirects to LoginActivity.
     */
    private void proceedToNextActivity() {
        new Handler().postDelayed(() -> {
            Intent intent;
            // Check if the user is logged in
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                // User is logged in, go to MainActivity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // User is not logged in, go to LoginActivity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            // Start the next activity and finish the splash screen
            startActivity(intent);
            finish();
        }, SPLASH_DISPLAY_LENGTH);  // Wait for the splash screen duration
    }
}
