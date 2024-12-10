package com.sweng.scopehud;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.sweng.scopehud.database.DBHandler;

public class SettingsActivity extends NavigationActivity {

    private static String TAG = "SettingsActivity";
    private ImageView profileImageView;
    private EditText usernameEditText, addressEditText, cityEditText, stateEditText, countryEditText;
    private Button updateProfileButton, deleteAccountButton, uploadProfileButton;
    private DBHandler dbHandler;
    private byte[] profileImage;
    private Uri profileImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHandler = new DBHandler(this);

        // Initialize UI components
        profileImageView = findViewById(R.id.profileImageView);
        usernameEditText = findViewById(R.id.usernameEditText);
        addressEditText = findViewById(R.id.addressEditText);
        cityEditText = findViewById(R.id.cityEditText);
        stateEditText = findViewById(R.id.stateEditText);
        countryEditText = findViewById(R.id.countryEditText);
        updateProfileButton = findViewById(R.id.updateProfileButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        uploadProfileButton = findViewById(R.id.uploadProfileButton);

        // Load existing user profile data
        loadUserProfile();

        // Set click listeners
        updateProfileButton.setOnClickListener(v -> updateUserProfile());
        deleteAccountButton.setOnClickListener(v -> deleteAccount());
        uploadProfileButton.setOnClickListener(v -> openFileChooser());

        // Set up toolbar and drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        setupDrawer(toolbar, drawer, navigationView, null);
    }

    /**
     * Triggers the file chooser to open
     */
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            profileImageUri = data.getData();
            if (profileImageUri != null) {
                try {
                    AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver()
                            .openAssetFileDescriptor(profileImageUri , "r");
                    long fileSize = fileDescriptor.getLength();
                    profileImage = new byte[(int)fileSize];
                    getContentResolver().openInputStream(data.getData()).read(profileImage);
                    profileImageView.setImageBitmap(BitmapFactory.decodeByteArray(profileImage,
                            0, profileImage.length));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to Load Profile Image");
                }
            }


        }
    }

    /**
     * Loads saved User profile data
     */
    private void loadUserProfile() {
        Cursor cursor = dbHandler.getUserSettings(1);
        if (cursor != null && cursor.moveToFirst()) {
            usernameEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            addressEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow("address")));
            cityEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow("city")));
            stateEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow("state")));
            countryEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow("country")));

            profileImage = cursor.getBlob(cursor.getColumnIndexOrThrow("profile_image"));
            profileImageView.setImageBitmap(BitmapFactory.decodeByteArray(profileImage,
                    0, profileImage.length));

            cursor.close();
        } else {
            Toast.makeText(this, "No profile data found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves Updated User Profile values to DB
     */
    private void updateUserProfile() {
        String username = usernameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String state = stateEditText.getText().toString().trim();
        String country = countryEditText.getText().toString().trim();
        byte[] profileImageArray = profileImage != null ? profileImage : null;

        if (!username.isEmpty()) {
            dbHandler.upsertUserSettings(1, username, address, city, state, country, profileImageArray);
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes the user's account
     */
    private void deleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    dbHandler.deleteUserSettings(1);
                    Toast.makeText(SettingsActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Redirects the user to the login screen (for use after account deletion)
     */
    private void redirectToLogin() {
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
