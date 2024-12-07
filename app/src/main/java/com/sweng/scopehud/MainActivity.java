package com.sweng.scopehud;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Paint;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.sweng.scopehud.database.DBHandler;
import com.sweng.scopehud.util.Scope;
import com.sweng.scopehud.util.ScopeRecyclerViewAdapter;
import com.sweng.scopehud.util.ScopeZero;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends NavigationActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private RecyclerView mScopeListView;
    private DBHandler dbHandler;
    private List<Scope> mScopeList;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;
    private String town;
    private String state;
    private ScopeRecyclerViewAdapter adapter;


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
        FloatingActionButton fab = findViewById(R.id.add_scope);
        // Initialize the database handler
        dbHandler = new DBHandler(this);
        /*
        * initialize RecyclerView
        * Add sample data to the list(this can be removed later of course
        * set the adapter
        */
        mScopeListView = findViewById(R.id.scopeListView);
        mScopeListView.setLayoutManager(new LinearLayoutManager(this));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestQueue = Volley.newRequestQueue(this);
        mScopeList = new ArrayList<>();
        mScopeList.add(new Scope(1, "RAZOR HD GEN III", "Vortex Optics", 18.0f, true,
                new ScopeZero(100, 0.1f, 0.2f, new Date(), new Location("")),"State College","PA"));
        mScopeList.add(new Scope(2, "STRIKE EAGLE", "Vortex Optics", 24.0f, true,
                new ScopeZero(200, 0.0f, 0.1f, new Date(), new Location("")),"Davidsville","PA"));

        //ScopeRecyclerViewAdapter adapter = new ScopeRecyclerViewAdapter(scopeList)
        adapter = new ScopeRecyclerViewAdapter(mScopeList); // Initialize adapter with mScopeList
        mScopeListView.setAdapter(adapter);

        // Add swipe-to-delete functionality
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // Do nothing for drag-and-drop
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Get the position of the swiped item
                int position = viewHolder.getAdapterPosition();

                // Remove the item from the list
                Scope removedScope = mScopeList.get(position); // Save the removed item in case you want to undo
                mScopeList.remove(position);
                adapter.notifyItemRemoved(position);

                // Optionally show a Snackbar to undo the delete
                Snackbar.make(mScopeListView, "Scope deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            // Add the item back if undo is clicked
                            mScopeList.add(position, removedScope);
                            adapter.notifyItemInserted(position);
                        }).show();

                // delete from the database
                // dbHandler.deleteScope(removedScope.getId());
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Translate the item only during swipe
                    viewHolder.itemView.setTranslationX(dX);
                } else {
                    // Reset the translation when no swipe action is active
                    c.drawColor(Color.TRANSPARENT);
                    viewHolder.itemView.setTranslationX(0);

                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                Context context = recyclerView.getContext();
                // Draw the red background
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                c.drawRect(
                        (float) viewHolder.itemView.getLeft(),
                        (float) viewHolder.itemView.getTop(),
                        dX,
                        (float) viewHolder.itemView.getBottom(),
                        paint
                );

                // Draw the delete icon
                Drawable deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
                if (deleteIcon != null) {
                    // Set custom size for the icon
                    int iconWidth = 80; // Desired width in pixels
                    int iconHeight = 80; // Desired height in pixels

                    // Calculate icon position
                    int itemTop = viewHolder.itemView.getTop();
                    int itemBottom = viewHolder.itemView.getBottom();
                    int iconLeft = viewHolder.itemView.getLeft() + 50; // Add margin from the left
                    int iconTop = itemTop + (itemBottom - itemTop - iconHeight) / 2; // Center vertically
                    int iconRight = iconLeft + iconWidth;
                    int iconBottom = iconTop + iconHeight;

                    // Set bounds and draw the icon
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }
            }
            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                // Reset the view to its original position
                viewHolder.itemView.setTranslationX(0);
            }
        };

        // Attach the ItemTouchHelper to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mScopeListView);
        fab.setOnClickListener(v -> {
            //get location
            getCurrentLocation();
            // Inflate the custom layout for the dialog
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_scope, null);

            // Create the AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            dialog.show();

            // Get references to the dialog's input fields
            EditText etName = dialogView.findViewById(R.id.et_name);
            EditText etBrand = dialogView.findViewById(R.id.et_brand);
            EditText etMaxMag = dialogView.findViewById(R.id.et_max_magnification);
            Spinner variableMagnificationSpinner = dialogView.findViewById(R.id.spinner_variable_magnification);
            EditText etZeroDistance = dialogView.findViewById(R.id.et_zero_distance);
            EditText etZeroWindage = dialogView.findViewById(R.id.et_zero_windage);
            EditText etZeroElevation = dialogView.findViewById(R.id.et_zero_elevation);
            EditText etZeroDate = dialogView.findViewById(R.id.et_zero_date);

            // Populate the EditText fields with existing data
            etZeroDistance.setText(etName.getText().toString());
            String windDirection = etZeroWindage.getText().toString(); // Get the text from windDir
            String strippedValue = windDirection.replace("DIR:", "").trim(); // Remove "DIR:" and trim spaces
            etZeroWindage.setText(strippedValue  + " degrees");
            etZeroElevation.setText(etZeroElevation.getText().toString()+" degrees");
            etZeroDate.setText(LocalDate.now().toString());
            // Handle Save and Cancel buttons
            Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
            Button btnSave = dialogView.findViewById(R.id.btn_save);

            // Create the options and their corresponding values
            List<String> options = Arrays.asList("Yes", "No");
            ArrayAdapter<String> varibaleMagAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, options);
            varibaleMagAdapter.setDropDownViewResource(R.layout.spinner_item);

            // Set the adapter to the Spinner
            variableMagnificationSpinner.setAdapter(varibaleMagAdapter);

            // Handle selection
            variableMagnificationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedOption = options.get(position);
                    int value = selectedOption.equals("Yes") ? 1 : 0; // Convert "Yes" to 1 and "No" to 0
                    // Do something with the value
                    Log.d("SpinnerValue", "Selected: " + selectedOption + ", Value: " + value);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Handle case when nothing is selected
                }
            });
            btnCancel.setOnClickListener(view -> dialog.dismiss());

            btnSave.setOnClickListener(view -> {
                // Retrieve input values from Dialog
                String name = etName.getText().toString();
                String brand = etBrand.getText().toString();
                String maxMag = etMaxMag.getText().toString();
                String varMag = variableMagnificationSpinner.getSelectedItem().toString();
                String zeroDistance = etZeroDistance.getText().toString();
                String zeroWindage = etZeroWindage.getText().toString();
                String zeroElevation = etZeroElevation.getText().toString();
                String zeroDate = etZeroDate.getText().toString();
                String scopeTown = town;
                String scopeState = state;
                Snackbar.make(view, scopeTown + ","+scopeState, Snackbar.LENGTH_SHORT).show();
                /*
                 * You also need to get the values from the Sight tool and save them to the database.
                 */

                // Save the values to the database or perform desired action
                //saveScopeToDatabase(name, brand, maxMag, varMag, zeroDistance, zeroWindage, zeroElevation, zeroDate);

                // Dismiss the dialog
                dialog.dismiss();
            });
        });
        Spinner locationSpinner = findViewById(R.id.location_spinner);
        List<String> uniqueLocations = getUniqueLocations(mScopeList);

        setupLocationSpinner(locationSpinner, uniqueLocations);

        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLocation = (String) parent.getItemAtPosition(position);

                if ("All Locations".equals(selectedLocation)) {
                    // Show all scopes when "All Locations" is selected
                    updateRecyclerView(mScopeList);
                } else {
                    // Filter scopes by selected location
                    List<Scope> filteredScopes = filterScopesByLocation(selectedLocation);
                    updateRecyclerView(filteredScopes);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Show all scopes if nothing is selected
                updateRecyclerView(mScopeList);
            }
        });
        // Set up the RecyclerView
        setupRecyclerView();
    }

    private List<String> getUniqueLocations(List<Scope> scopeList) {
        Set<String> uniqueLocations = new HashSet<>();
        for (Scope scope : scopeList) {
            String location = scope.getTown() + ", " + scope.getState();
            uniqueLocations.add(location);
        }
        return new ArrayList<>(uniqueLocations);
    }
    private void setupLocationSpinner(Spinner locationSpinner, List<String> locations) {
        locations.add(0, "All Locations");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Default dropdown style
        locationSpinner.setAdapter(adapter);
    }
    private List<Scope> filterScopesByLocation(String selectedLocation) {
        List<Scope> filteredScopes = new ArrayList<>();
        for (Scope scope : mScopeList) {
            String location = scope.getTown() + ", " + scope.getState();
            if (location.equals(selectedLocation)) {
                filteredScopes.add(scope);
            }
        }
        return filteredScopes;
    }
    private void updateRecyclerView(List<Scope> filteredScopes) {
        adapter.updateData(filteredScopes); // Create an `updateData` method in your adapter to refresh the list.
    }

    @Override
    public void onResume(){
        super.onResume();
        setupRecyclerView();
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
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Get the last known location
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    //convert to state and town
                    getTownAndState(latitude,longitude);
                } else {
                    Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void getTownAndState(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                town = address.getLocality(); // Town or city
                state = address.getAdminArea(); // State

                // Do something with the town and state
                Toast.makeText(this, "Town: " + town + ", State: " + state, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Unable to get address for the location", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error fetching address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
