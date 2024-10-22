package com.sweng.scopehud;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.sweng.scopehud.database.DBHandler;
import com.sweng.scopehud.util.Scope;
import com.sweng.scopehud.util.ScopeRecyclerViewAdapter;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends NavigationActivity {

    // UI components
    private Toolbar toolbar;
    private RecyclerView mScopeListView;

    // Database components
    private DBHandler dbHandler;
    private RecyclerView.Adapter mAdapter;
    private List<Scope> mScopeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout for MainActivity

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set toolbar as ActionBar

        // Initialize DrawerLayout and NavigationView
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        setupDrawer(toolbar, drawer, navigationView, currentUser);

        // Setup RecyclerView and Database
        initDB();
        setupRecyclerView();
    }


    /**
     * Handle the back press to close the drawer if open, or exit the app.
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer if it's open
        } else {
            super.onBackPressed(); // Perform the normal back press action
        }
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