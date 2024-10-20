package com.sweng.scopehud;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
        }

        // Close the drawer after an item is clicked
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
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

