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
    }

    // Setup Drawer for Navigation
    protected void setupDrawer(Toolbar toolbar, DrawerLayout drawerLayout, NavigationView navigationView) {
        this.drawerLayout = drawerLayout;

        // Set up ActionBarDrawerToggle for opening and closing the drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set the navigation view listener
        navigationView.setNavigationItemSelectedListener(this);
    }

    // Handle Navigation Drawer Item Clicks
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            showLogoutConfirmation();
        } else if (id == R.id.nav_scopes) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.nav_sight_tool) {
            startActivity(new Intent(this, SightToolActivity.class));
        } else if (id == R.id.nav_weather_tool) {
            Toast.makeText(this, "Weather Tool selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_find_range) {
            // Call method to find shooting ranges
            findShootingRangeNearby();
        }

        // Close the drawer after an item is clicked
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void findShootingRangeNearby() {
        // Check location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Assume you have a method to get the current location
        Location currentLocation = getCurrentLocation();

        if (currentLocation != null) {
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

    // Placeholder method to get the current location
    private Location getCurrentLocation() {
        // You will need to implement location services to get the current location
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findShootingRangeNearby();
            } else {
                Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLogoutConfirmation() {
        // Implement logout confirmation logic here
        Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer if open
        } else {
            super.onBackPressed();
        }
    }
}

