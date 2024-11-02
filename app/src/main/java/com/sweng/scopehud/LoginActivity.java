package com.sweng.scopehud;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword; // Input fields for email and password
    private Button buttonLogin; // Button to log in
    private TextView textViewSignup; // TextView to navigate to signup
    private CheckBox checkBoxRememberMe; // Checkbox for remembering user credentials
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER_ME = "remember_me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Set the layout for this activity

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignup = findViewById(R.id.textViewSignup);
        checkBoxRememberMe = findViewById(R.id.checkBoxRememberMe);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load saved credentials
        loadSavedCredentials();

        // Set click listener for login button
        buttonLogin.setOnClickListener(v -> loginUser());

        // Set click listener for signup text
        textViewSignup.setOnClickListener(v -> openSignupActivity());
    }

    /**
     * Load saved email and password from SharedPreferences if Remember Me is checked.
     */
    private void loadSavedCredentials() {
        // Load saved email and password from SharedPreferences
        String email = sharedPreferences.getString(KEY_EMAIL, "");
        String password = sharedPreferences.getString(KEY_PASSWORD, "");
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);

        if (rememberMe) {
            editTextEmail.setText(email);
            editTextPassword.setText(password);
            checkBoxRememberMe.setChecked(true);
        }
    }

    /**
     * Perform the login using Firebase Authentication.
     */
    private void loginUser() {
        // Retrieve email and password from input fields
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required.");
            editTextEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required.");
            editTextPassword.requestFocus();
            return;
        }

        // Perform login using Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        // Save credentials if Remember Me is checked
                        if (checkBoxRememberMe.isChecked()) {
                            saveCredentials(email, password);
                        } else {
                            clearSavedCredentials();
                        }

                        redirectToMainActivity();
                    } else {
                        // Login failed, display error message
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed.";
                        Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Save email and password to SharedPreferences when Remember Me is checked.
     */
    private void saveCredentials(String email, String password) {
        // Save email and password to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putBoolean(KEY_REMEMBER_ME, true);
        editor.apply();
    }

    /**
     * Clear saved email and password from SharedPreferences when Remember Me is unchecked.
     */
    private void clearSavedCredentials() {
        // Clear saved email and password from SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASSWORD);
        editor.putBoolean(KEY_REMEMBER_ME, false);
        editor.apply();
    }

    /**
     * Open the SignupActivity when the signup TextView is clicked.
     */
    private void openSignupActivity() {
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
    }

    /**
     * Redirect the user to the MainActivity after a successful login.
     */
    private void redirectToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
