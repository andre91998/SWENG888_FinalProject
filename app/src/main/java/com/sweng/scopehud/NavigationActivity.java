package com.sweng.scopehud;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class NavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected TextView textViewUserName, textViewUserEmail;
    protected FirebaseUser currentUser;
    protected NavigationView navigationView;

    // Firebase Authentication
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Additional initialization logic can be added here if needed in subclasses
        // Initialize Firebase Auth instance
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get the currently signed-in user
        if (currentUser == null) {
            redirectToLogin(); // Redirect to login if no user is signed in
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get the currently signed-in user
        if (currentUser == null) {
            redirectToLogin(); // Redirect to login if no user is signed in
        } else {
            updateNavHeader(currentUser); // Update the navigation header with user info
        }
    }

    /**
     * Sets up the navigation drawer with the toolbar, drawer layout, and navigation view.
     */
    protected void setupDrawer(Toolbar toolbar, DrawerLayout drawerLayout,
                               NavigationView navigationView, FirebaseUser currentUser) {
        this.drawerLayout = drawerLayout;
        this.currentUser = currentUser;
        this.navigationView = navigationView;

        // Setup ActionBarDrawerToggle for opening and closing the drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set the navigation view listener
        navigationView.setNavigationItemSelectedListener(this);

        // Update navigation header with user information
        updateNavHeader(currentUser);
    }

    /**
     * Handles navigation item selections from the drawer.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        // Handle each item based on the ID using if-else structure
        if (id == R.id.nav_logout) {
            showLogoutConfirmation(); // Handle logout action
        } else if (id == R.id.nav_sight_tool) {
            // Navigate to SightToolActivity
            intent = new Intent(this, SightToolActivity.class);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        } else if (id == R.id.nav_scopes) {
            // Navigate to MainActivity
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("currentUser", currentUser);
            startActivity(intent);
        } else if (id == R.id.nav_weather_tool) {
            Toast.makeText(this, "Weather Tool selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_find_range) {
            // Find nearby shooting ranges
            findShootingRangeNearby();
        } else {
            Toast.makeText(this, "Unknown navigation option selected", Toast.LENGTH_SHORT).show();
        }

        // Close the drawer after an item is clicked
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    /**
     * Finds shooting ranges nearby using the current location and opens Google Maps.
     */
    private void findShootingRangeNearby() {
        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Get the current location (you should implement actual location retrieval here)
        Location currentLocation = getCurrentLocation();

        if (currentLocation != null) {
            // Open Google Maps to search for nearby shooting ranges
            String uri = String.format("geo:%f,%f?q=shooting+range", currentLocation.getLatitude(), currentLocation.getLongitude());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Google Maps is not installed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Placeholder method to get the current location.
     * You need to implement the actual logic to retrieve the user's current location.
     */
    private Location getCurrentLocation() {
        // Return null for now, implement location retrieval here
        return null;
    }

    /**
     * Handles the result of the permission request for location access.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, find nearby shooting range
                findShootingRangeNearby();
            } else {
                Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Show a confirmation dialog for logging out the user.
     */
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    mAuth.signOut(); // Sign out the user from Firebase
                    Toast.makeText(NavigationActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
                    redirectToLogin(); // Redirect to login after sign out
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show(); // Show the dialog
    }

    /**
     * Redirect to the LoginActivity when the user is not authenticated.
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class); // Start LoginActivity
        startActivity(intent);
        finish(); // Close the current activity
    }

    /**
     * Handles the back press to close the drawer if open, or exit the app.
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer if open
        } else {
            super.onBackPressed(); // Perform the normal back press action
        }
    }

    /**
     * Update the navigation header with the current user's name and email.
     */
    public void updateNavHeader(FirebaseUser currentUser) {
        if (currentUser != null) {
            // Get user details
            String userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail();
            String userEmail = currentUser.getEmail();

            // Find header view and set user details
            View headerView = navigationView.getHeaderView(0);
            textViewUserName = headerView.findViewById(R.id.textViewUserName);
            textViewUserEmail = headerView.findViewById(R.id.textViewUserEmail);

            textViewUserName.setText(userName); // Set user name in navigation header
            textViewUserEmail.setText(userEmail); // Set user email in navigation header
        }
    }
}
