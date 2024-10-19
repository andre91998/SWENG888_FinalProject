package com.sweng.scopehud;

import android.app.AlertDialog; // For creating dialog alerts
import android.content.Intent; // For starting new activities
import android.os.Bundle; // For passing data between activities
import android.view.MenuItem; // For handling menu item clicks
import android.view.View; // For view-related operations
import android.widget.TextView; // For displaying text
import android.widget.Toast; // For displaying short messages
import androidx.annotation.NonNull; // For non-null annotations
import androidx.appcompat.app.ActionBarDrawerToggle; // For handling the navigation drawer toggle
import androidx.appcompat.app.AppCompatActivity; // Base class for activities
import androidx.appcompat.widget.Toolbar; // For using Toolbar as an ActionBar
import androidx.core.view.GravityCompat; // For managing navigation drawer states
import androidx.drawerlayout.widget.DrawerLayout; // For creating the navigation drawer
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView; // For navigation drawer
import com.google.firebase.auth.FirebaseAuth; // For Firebase authentication
import com.google.firebase.auth.FirebaseUser; // For Firebase user details
import com.sweng.scopehud.database.DBHandler;
import com.sweng.scopehud.util.Scope;
import com.sweng.scopehud.util.ScopeRecyclerViewAdapter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer; // Navigation drawer layout
    private TextView textViewUserName, textViewUserEmail; // TextViews for user information
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private Toolbar toolbar; // Toolbar for the action bar
    private List<Scope> mScopeList; //list of scopes populated from database
    private RecyclerView mScopeListView; //list of scopes
    private RecyclerView.Adapter mAdapter; //used to populate scope list
    private DBHandler dbHandler; //for database initialization

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout for this activity

        // Get Firebase Auth instance (no initialization needed, just retrieval)
        mAuth = FirebaseAuth.getInstance();

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar); // Find the toolbar view
        if (toolbar != null) {
            setSupportActionBar(toolbar); // Set the toolbar as the action bar
        }

        // Initialize DrawerLayout and NavigationView
        drawer = findViewById(R.id.drawer_layout); // Find the drawer layout
        NavigationView navigationView = findViewById(R.id.nav_view); // Find the navigation view
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this); // Set the navigation item listener

            // Update header with user information
            updateNavHeader(navigationView); // Set user info in the navigation drawer header
        }

        // Setup ActionBarDrawerToggle for opening and closing the drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle); // Add the toggle as a listener to the drawer
        }
        toggle.syncState(); // Sync the toggle state

        // Set the initial title for the activity
        updateTitle(); // Set the initial title

        //configure database and insert our product entries
        initDB();
        //Setup Recycler List View of Scopes
        mScopeListView = (RecyclerView) findViewById(R.id.scopeListView);
        mScopeList = dbHandler.queryAllScopes();
        mAdapter = new ScopeRecyclerViewAdapter(mScopeList);
        mScopeListView.setAdapter(mAdapter);
        mScopeListView.setLayoutManager(new LinearLayoutManager(this));
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            showLogoutConfirmation();
        } else if (id == R.id.nav_exit) {
            showExitConfirmation();
        } else if (id == R.id.nav_home) {
            // Handle home action
            Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_gallery) {
            // Handle gallery action
            Toast.makeText(this, "Gallery selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_slideshow) {
            // Handle slideshow action
            Toast.makeText(this, "Slideshow selected", Toast.LENGTH_SHORT).show();
        }

        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START); // Close the drawer after an item is clicked
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START); // Close the drawer if open when back is pressed
        } else {
            super.onBackPressed();
        }
    }

    private void updateTitle() {
        // Update the activity and toolbar title
        String title = getString(R.string.title_activity_main); // Get the title from strings.xml
        setTitle(title); // Set the activity title
        if (toolbar != null) {
            toolbar.setTitle(title); // Set the toolbar title
        }
    }

    private void redirectToLogin() {
        // Redirect to the LoginActivity
        Intent intent = new Intent(this, LoginActivity.class); // Create an intent to start LoginActivity
        startActivity(intent); // Start LoginActivity
        finish(); // Close MainActivity
    }

    private void updateNavHeader(NavigationView navigationView) {
        if (navigationView == null) return;

        // Update the navigation drawer header with user information
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get the currently signed-in user
        if (currentUser != null) {
            // Get user name and email
            String userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail();
            String userEmail = currentUser.getEmail();

            View headerView = navigationView.getHeaderView(0); // Get the header view of the navigation drawer
            if (headerView != null) {
                textViewUserName = headerView.findViewById(R.id.textViewUserName); // Find user name TextView
                textViewUserEmail = headerView.findViewById(R.id.textViewUserEmail); // Find user email TextView

                // Set user name and email in the header
                if (textViewUserName != null) textViewUserName.setText(userName);
                if (textViewUserEmail != null) textViewUserEmail.setText(userEmail);
            }
        }
    }

    private void showLogoutConfirmation() {
        // Show a confirmation dialog for logout
        new AlertDialog.Builder(this)
                .setTitle("Logout") // Set dialog title
                .setMessage("Are you sure you want to logout?") // Set dialog message
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    mAuth.signOut(); // Sign out the user
                    Toast.makeText(MainActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show(); // Show logout message
                    redirectToLogin(); // Redirect to login activity
                })
                .setNegativeButton(android.R.string.no, null) // Cancel action
                .setIcon(android.R.drawable.ic_dialog_alert) // Set alert icon
                .show(); // Show the dialog
    }

    private void showExitConfirmation() {
        // Show a confirmation dialog for exiting the app
        new AlertDialog.Builder(this)
                .setTitle("Exit App") // Set dialog title
                .setMessage("Are you sure you want to exit the application?") // Set dialog message
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Close the app
                    finishAffinity(); // This will close the app and all activities
                })
                .setNegativeButton(android.R.string.no, null) // Cancel action
                .setIcon(android.R.drawable.ic_dialog_alert) // Set alert icon
                .show(); // Show the dialog
    }

    private void initDB() {
        getApplicationContext().deleteDatabase("practice3DB"); //to have fresh database every demo run
        dbHandler = new DBHandler(getApplicationContext());
        dbHandler.addNewScope("RAZOR HD GEN III", "Vortex",
                36f, true, 100,
                2f, 1f, Calendar.getInstance().getTime());
        dbHandler.addNewScope("GOLDEN EAGLE HD", "Vortex",
                60f, true, 100,
                1.3f, 0.5f, Calendar.getInstance().getTime());
        dbHandler.addNewScope("HWS EXPS2", "EOTECH",
                1f, false, 50,
                0.5f, 2f, Calendar.getInstance().getTime());
    }
}
