package com.sweng.scopehud;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.sweng.scopehud.NavigationActivity;
import com.sweng.scopehud.database.DBHandler;
import com.sweng.scopehud.util.Scope;
import com.sweng.scopehud.util.ScopeRecyclerViewAdapter;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends NavigationActivity {

    private RecyclerView mScopeListView; // List of scopes
    private RecyclerView.Adapter mAdapter; // Adapter for scopes
    private DBHandler dbHandler; // Database handler
    private List<Scope> mScopeList; // Scope list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the navigation drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        setupDrawer(toolbar, drawerLayout, navigationView); // Call the method from BaseActivity

        // Initialize the database and recycler view
        initDB();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        mScopeListView = findViewById(R.id.scopeListView);
        mScopeList = dbHandler.queryAllScopes();
        mAdapter = new ScopeRecyclerViewAdapter(mScopeList);
        mScopeListView.setAdapter(mAdapter);
        mScopeListView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initDB() {
        getApplicationContext().deleteDatabase("scopeDB"); //to have fresh database every demo run
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
