package com.sweng.scopehud;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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

public abstract class NavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Additional initialization logic can be added here if needed in subclasses
    }

    /**
     * Sets up the navigation drawer with the toolbar, drawer layout, and navigation view.
     */
    protected void setupDrawer(Toolbar toolbar, DrawerLayout drawerLayout, NavigationView navigationView) {
        this.drawerLayout = drawerLayout;

        // Setup ActionBarDrawerToggle for opening and closing the drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set the navigation view listener
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Handles navigation item selections from the drawer.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Handle each item based on the ID using if-else structure
        if (id == R.id.nav_logout) {
            showLogoutConfirmation(); // Handle logout action
        } else if (id == R.id.nav_sight_tool) {
            // Navigate to SightToolActivity
            startActivity(new Intent(this, SightToolActivity.class));
        } else if (id == R.id.nav_scopes) {
            // Navigate to MainActivity
            startActivity(new Intent(this, MainActivity.class));
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
     * Displays a confirmation dialog for logging out the user.
     * You can implement more complex logic like redirecting to a login activity.
     */
    private void showLogoutConfirmation() {
        // Logic for logout confirmation (e.g., Firebase sign out) should go here
        Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT).show();
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
}
