package com.sweng.scopehud;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImageView;
    private EditText usernameEditText, addressEditText, cityEditText, stateEditText, countryEditText;
    private Button updateProfileButton, deleteAccountButton, uploadProfileButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private Uri profileImageUri;

    /**
     * Called when the activity is created.
     * Initializes Firebase services, UI elements, and loads the current user profile details.
     *
     * @param savedInstanceState If the activity is being re-initialized after being previously shut down, this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Firebase services
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize UI elements
        profileImageView = findViewById(R.id.profileImageView);
        usernameEditText = findViewById(R.id.usernameEditText);
        addressEditText = findViewById(R.id.addressEditText);
        cityEditText = findViewById(R.id.cityEditText);
        stateEditText = findViewById(R.id.stateEditText);
        countryEditText = findViewById(R.id.countryEditText);
        updateProfileButton = findViewById(R.id.updateProfileButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        uploadProfileButton = findViewById(R.id.uploadProfileButton);

        // Load current user profile details
        loadCurrentUserProfile();

        // Set up the button click listeners
        updateProfileButton.setOnClickListener(v -> updateUserProfile());
        deleteAccountButton.setOnClickListener(v -> deleteAccount());
        uploadProfileButton.setOnClickListener(v -> openFileChooser());
    }

    /**
     * Opens the file chooser to allow the user to select a profile picture from their device.
     */
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result of the file chooser.
     * Displays the selected image in the ImageView.
     *
     * @param requestCode The request code passed when starting the activity.
     * @param resultCode  The result code returned by the activity.
     * @param data        The intent data containing the image URI.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            profileImageView.setImageURI(profileImageUri);
        }
    }

    /**
     * Loads the current user profile data from FirebaseAuth and Firestore.
     * Populates the UI with the user's existing information, including their address and profile picture.
     */
    private void loadCurrentUserProfile() {
        if (currentUser != null) {
            // Set the username from FirebaseAuth
            usernameEditText.setText(currentUser.getDisplayName());

            // Fetch user data from Firestore
            firestore.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Populate address fields
                            addressEditText.setText(documentSnapshot.getString("address"));
                            cityEditText.setText(documentSnapshot.getString("city"));
                            stateEditText.setText(documentSnapshot.getString("state"));
                            countryEditText.setText(documentSnapshot.getString("country"));
                            loadProfileImage();  // Load the profile picture
                        } else {
                            Toast.makeText(SettingsActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Loads the user's profile picture from Firebase Storage and displays it in the ImageView.
     */
    private void loadProfileImage() {
        StorageReference profileRef = storage.getReference().child("profile_images/" + currentUser.getUid() + ".jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            profileImageView.setImageURI(Uri.parse(uri.toString()));
        }).addOnFailureListener(e -> {
            Toast.makeText(SettingsActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Updates the user's profile information, including their username, address, and profile picture.
     * The username is updated in FirebaseAuth, and other details are updated in Firestore.
     */
    private void updateUserProfile() {
        String newUsername = usernameEditText.getText().toString().trim();
        String newAddress = addressEditText.getText().toString().trim();
        String newCity = cityEditText.getText().toString().trim();
        String newState = stateEditText.getText().toString().trim();
        String newCountry = countryEditText.getText().toString().trim();

        if (!newUsername.isEmpty()) {
            // Update the username in FirebaseAuth
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    .build();

            currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SettingsActivity.this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Failed to update username", Toast.LENGTH_SHORT).show();
                }
            });

            // Update the address, city, state, and country in Firestore
            Map<String, Object> userUpdates = new HashMap<>();
            userUpdates.put("address", newAddress);
            userUpdates.put("city", newCity);
            userUpdates.put("state", newState);
            userUpdates.put("country", newCountry);

            firestore.collection("users").document(currentUser.getUid())
                    .update(userUpdates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SettingsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        uploadProfileImage();  // Upload profile picture if changed
                    })
                    .addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Uploads the selected profile image to Firebase Storage.
     */
    private void uploadProfileImage() {
        if (profileImageUri != null) {
            StorageReference profileRef = storage.getReference().child("profile_images/" + currentUser.getUid() + ".jpg");
            profileRef.putFile(profileImageUri)
                    .addOnSuccessListener(taskSnapshot -> Toast.makeText(SettingsActivity.this, "Profile image uploaded", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, "Failed to upload profile image", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Deletes the user's account from FirebaseAuth and Firestore.
     * Prompts the user with an alert dialog for confirmation.
     */
    private void deleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    if (currentUser != null) {
                        // Delete the FirebaseAuth account
                        currentUser.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Delete user data from Firestore
                                firestore.collection("users").document(currentUser.getUid()).delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(SettingsActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                            redirectToLogin();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, "Failed to delete Firestore data", Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(SettingsActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Redirects the user to the login screen after account deletion.
     */
    private void redirectToLogin() {
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
