package com.sweng.scopehud;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sweng.scopehud.database.DBHandler;
import com.sweng.scopehud.util.Scope;
import com.sweng.scopehud.util.ScopeRecyclerViewAdapter;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // UI components
    private DrawerLayout drawer;
    private TextView textViewUserName, textViewUserEmail;
    private Toolbar toolbar;
    private RecyclerView mScopeListView;

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Database components
    private DBHandler dbHandler;
    private RecyclerView.Adapter mAdapter;
    private List<Scope> mScopeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout for MainActivity

        // Initialize Firebase Auth instance
        mAuth = FirebaseAuth.getInstance();

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set toolbar as ActionBar

        // Initialize DrawerLayout and NavigationView
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this); // Set the navigation item listener

        // Update navigation header with user information
        updateNavHeader(navigationView);

        // Setup ActionBarDrawerToggle for DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle); // Add the drawer toggle listener
        toggle.syncState(); // Sync toggle state

        // Setup RecyclerView and Database
        initDB();
        setupRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get the currently signed-in user
        if (currentUser == null) {
            redirectToLogin(); // Redirect to login if no user is signed in
        } else {
            updateNavHeader(findViewById(R.id.nav_view)); // Update the navigation header with user info
        }
    }

    /**
     * Handles navigation item selections in the drawer.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId(); // Get the ID of the clicked menu item

        if (id == R.id.nav_logout) {
            showLogoutConfirmation(); // Handle logout action
        }

        drawer.closeDrawer(GravityCompat.START); // Close the navigation drawer after item click
        return true; // Indicate the event has been handled
    }

    /**
     * Handle the back press to close the drawer if open, or exit the app.
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START); // Close the drawer if it's open
        } else {
            super.onBackPressed(); // Perform the normal back press action
        }
    }

    /**
     * Update the navigation header with the current user's name and email.
     */
    private void updateNavHeader(NavigationView navigationView) {
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get current Firebase user
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

    /**
     * Redirect to the LoginActivity when the user is not authenticated.
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class); // Start LoginActivity
        startActivity(intent);
        finish(); // Close the current activity
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
                    Toast.makeText(MainActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
                    redirectToLogin(); // Redirect to login after sign out
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show(); // Show the dialog
    }

    /**
     * Show a confirmation dialog for exiting the application.
     */
    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit the application?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> finishAffinity()) // Close all app activities
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show(); // Show the dialog
    }

    /**
     * Initialize the RecyclerView to display a list of scopes.
     */
    private void setupRecyclerView() {
        mScopeListView = findViewById(R.id.scopeListView); // Find RecyclerView
        mScopeList = dbHandler.queryAllScopes(); // Query all scopes from the database
        mAdapter = new ScopeRecyclerViewAdapter(mScopeList); // Create an adapter with the scope list
        mScopeListView.setAdapter(mAdapter); // Set the adapter to the RecyclerView
        mScopeListView.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager
    }

    /**
     * Initialize the database with scope data for demo purposes.
     */
    private void initDB() {
        // Delete existing database for fresh data each run (optional for demo purposes)
        getApplicationContext().deleteDatabase("scopeDB");

        // Initialize the database handler
        dbHandler = new DBHandler(getApplicationContext());

        // Add sample scopes to the database
        dbHandler.addNewScope("RAZOR HD GEN III", "Vortex", 36f, true, 100, 2f, 1f, Calendar.getInstance().getTime());
        dbHandler.addNewScope("GOLDEN EAGLE HD", "Vortex", 60f, true, 100, 1.3f, 0.5f, Calendar.getInstance().getTime());
        dbHandler.addNewScope("HWS EXPS2", "EOTECH", 1f, false, 50, 0.5f, 2f, Calendar.getInstance().getTime());
    }
}
