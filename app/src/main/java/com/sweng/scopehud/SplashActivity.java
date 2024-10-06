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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view for the splash screen
        setContentView(R.layout.activity_splash);

        // Ensure that Firebase is initialized before proceeding
        initializeFirebaseAndProceed();
    }

    private void initializeFirebaseAndProceed() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase initialized successfully.");
            }

            // Delay before proceeding to next activity
            proceedToNextActivity();
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed: " + e.getMessage(), e);
        }
    }

    private void proceedToNextActivity() {
        new Handler().postDelayed(() -> {
            Intent intent;
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                intent = new Intent(SplashActivity.this, MainActivity.class); // User is logged in, go to MainActivity
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class); // User is not logged in, go to LoginActivity
            }
            startActivity(intent);
            finish();
        }, SPLASH_DISPLAY_LENGTH);
    }
}
