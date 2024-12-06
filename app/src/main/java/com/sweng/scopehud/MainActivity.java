package com.sweng.scopehud;

import android.location.Location;
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
import com.sweng.scopehud.util.ScopeZero;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends NavigationActivity {

    private RecyclerView mScopeListView;
    private DBHandler dbHandler;
    private List<Scope> mScopeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the drawer layout and navigation view
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        setupDrawer(toolbar, drawer, navigationView, currentUser);

        // Initialize the database handler
        dbHandler = new DBHandler(this);
        /*
        * initialize RecyclerView
        * Add sample data to the list(this can be removed later of course
        * set the adapter
        */
        mScopeListView = findViewById(R.id.scopeListView);
        mScopeListView.setLayoutManager(new LinearLayoutManager(this));

        List<Scope> scopeList = new ArrayList<>();
        scopeList.add(new Scope(1, "RAZOR HD GEN III", "Vortex Optics", 18.0f, true,
                new ScopeZero(100, 0.1f, 0.2f, new Date(), new Location(""))));
        scopeList.add(new Scope(2, "STRIKE EAGLE", "Vortex Optics", 24.0f, true,
                new ScopeZero(200, 0.0f, 0.1f, new Date(), new Location(""))));

        ScopeRecyclerViewAdapter adapter = new ScopeRecyclerViewAdapter(scopeList);
        mScopeListView.setAdapter(adapter);

        // Set up the RecyclerView
        //setupRecyclerView(); uncomment this once db stuff is working
    }

    /**
     * Sets up the RecyclerView to display a list of scopes from the database.
     */
    private void setupRecyclerView() {
        // Query the database for all scopes and populate the list
        mScopeList = dbHandler.queryAllScopes();

        // Check if the list is empty and display a message if needed
        if (mScopeList == null || mScopeList.isEmpty()) {
            // Handle empty state, e.g., show a placeholder view or a message
            // This can be a TextView or other UI element to indicate "No data available"
        } else {
            // Set up the adapter and layout manager for the RecyclerView
            ScopeRecyclerViewAdapter mAdapter = new ScopeRecyclerViewAdapter(mScopeList);
            mScopeListView.setAdapter(mAdapter);
            mScopeListView.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            // Close the drawer if it is open
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // Proceed with the normal back button behavior
            super.onBackPressed();
        }
    }
}
