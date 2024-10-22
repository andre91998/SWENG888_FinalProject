package com.sweng.scopehud;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {

    // UI elements for user input
    private EditText editTextDisplayName, editTextEmail, editTextPassword;
    private Button buttonSignup, buttonCancel;
    private FirebaseAuth mAuth;
    private static final String TAG = "SignupActivity";

    /**
     * This method is called when the activity is first created.
     * It initializes Firebase, the authentication instance, and UI components.
     *
     * @param savedInstanceState If the activity is being re-initialized after being previously shut down, this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase initialized successfully.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed: " + e.getMessage(), e);
            Toast.makeText(SignupActivity.this, "Firebase initialization failed. Please try again later.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Auth instance
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements for user input
        editTextDisplayName = findViewById(R.id.editTextDisplayName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignup = findViewById(R.id.buttonSignup);
        buttonCancel = findViewById(R.id.buttonCancel);

        // Set click listener for the sign-up button
        buttonSignup.setOnClickListener(v -> signupUser());

        // Set click listener for the cancel button to close the activity
        buttonCancel.setOnClickListener(v -> finish());
    }

    /**
     * This method is responsible for signing up the user.
     * It validates the user input, shows a loading dialog, and creates the user with Firebase Authentication.
     */
    private void signupUser() {
        // Retrieve user input
        String displayName = editTextDisplayName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate user input
        if (!validateInput(displayName, email, password)) {
            return;
        }

        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Create user with email and password in Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();  // Dismiss the loading dialog

                        if (task.isSuccessful()) {
                            // Sign-up success
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Set display name for the user
                                setDisplayName(user, displayName);
                            }
                        } else {
                            // If sign-up fails, display an error message to the user
                            String errorMessage = getErrorMessage(task);
                            Log.e(TAG, "Signup failed: " + errorMessage);
                            Toast.makeText(SignupActivity.this, "Signup failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Validates the user input fields for sign-up.
     *
     * @param displayName The display name entered by the user.
     * @param email The email entered by the user.
     * @param password The password entered by the user.
     * @return true if all input fields are valid, false otherwise.
     */
    private boolean validateInput(String displayName, String email, String password) {
        if (TextUtils.isEmpty(displayName)) {
            editTextDisplayName.setError("Display name is required.");
            editTextDisplayName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required.");
            editTextEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email address.");
            editTextEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required.");
            editTextPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters.");
            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Sets the display name for the newly created user.
     *
     * @param user The FirebaseUser object representing the current user.
     * @param displayName The display name to be set for the user.
     */
    private void setDisplayName(FirebaseUser user, String displayName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> profileTask) {
                        if (profileTask.isSuccessful()) {
                            // Notify the user that the sign-up was successful
                            Toast.makeText(SignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                            // Redirect to the login activity
                            redirectToLoginActivity(user.getEmail(), displayName);
                        } else {
                            // Handle error in setting the display name
                            String error = profileTask.getException() != null ? profileTask.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Failed to set display name: " + error);
                            Toast.makeText(SignupActivity.this, "Failed to set display name. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Redirects the user to the login activity after successful sign-up.
     *
     * @param email The user's email to be passed to the login activity.
     * @param displayName The user's display name to be passed to the login activity.
     */
    private void redirectToLoginActivity(String email, String displayName) {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        intent.putExtra("USER_NAME", displayName);
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
        finish();
    }

    /**
     * Retrieves the error message from a Firebase task if available.
     *
     * @param task The task result from Firebase.
     * @return The error message or "Unknown error occurred" if no specific error message is available.
     */
    private String getErrorMessage(@NonNull Task<AuthResult> task) {
        return task.getException() != null ? task.getException().getMessage() : "Unknown error occurred.";
    }
}
