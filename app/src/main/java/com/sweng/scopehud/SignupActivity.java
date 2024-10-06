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

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views for user input
        editTextDisplayName = findViewById(R.id.editTextDisplayName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignup = findViewById(R.id.buttonSignup);
        buttonCancel = findViewById(R.id.buttonCancel);

        // Set click listener for the sign-up button
        buttonSignup.setOnClickListener(v -> signupUser());

        // Set click listener for the cancel button
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void signupUser() {
        String displayName = editTextDisplayName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

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
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Sign-up success
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                setDisplayName(user, displayName);
                            }
                        } else {
                            // If sign-up fails, display an error message to the user.
                            String errorMessage = getErrorMessage(task);
                            Log.e(TAG, "Signup failed: " + errorMessage);
                            Toast.makeText(SignupActivity.this, "Signup failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

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

    private void setDisplayName(FirebaseUser user, String displayName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> profileTask) {
                        if (profileTask.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                            redirectToLoginActivity(user.getEmail(), displayName);
                        } else {
                            String error = profileTask.getException() != null ? profileTask.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Failed to set display name: " + error);
                            Toast.makeText(SignupActivity.this, "Failed to set display name. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void redirectToLoginActivity(String email, String displayName) {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        intent.putExtra("USER_NAME", displayName);
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
        finish();
    }

    private String getErrorMessage(@NonNull Task<AuthResult> task) {
        return task.getException() != null ? task.getException().getMessage() : "Unknown error occurred.";
    }
}
