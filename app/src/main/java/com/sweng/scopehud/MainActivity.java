package com.sweng.scopehud;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.sweng.scopehud.database.DBHandler;
import com.sweng.scopehud.util.Scope;
import com.sweng.scopehud.util.ScopeRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends NavigationActivity {

    private static final String TAG = "ScopeListActivity";
    private static final double EARTH_RADIUS = 6371; // Earth's radius in kilometers
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private RecyclerView mScopeListView;
    private DBHandler dbHandler;
    private List<Scope> mScopeList;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;
    private String town;
    private String state;
    private ScopeRecyclerViewAdapter adapter;
    private double latitude, longitude;

    private int distanceFilter = Integer.MAX_VALUE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();
        setContentView(R.layout.activity_main);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the drawer layout and navigation view
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        setupDrawer(toolbar, drawer, navigationView, currentUser);
        FloatingActionButton fabAddScope = findViewById(R.id.add_scope);
        FloatingActionButton fabMapPin = findViewById(R.id.fab_map);
        // Reference the Toggle Button and Slider
        SwitchCompat toggleSlider = findViewById(R.id.toggle_slider);
        Slider distanceSlider = findViewById(R.id.distance_slider);
        // Initialize the database handler
        dbHandler = new DBHandler(this);
        /*
        * initialize RecyclerView
        * Add sample data to the list(this can be removed later of course
        * set the adapter
        */
        mScopeListView = findViewById(R.id.scopeListView);
        mScopeListView.setLayoutManager(new LinearLayoutManager(this));
        requestQueue = Volley.newRequestQueue(this);
        mScopeList = new ArrayList<>();

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

                // delete from the database
                dbHandler.deleteScope(removedScope.getId());
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

        // Set a listener for the toggle button
        toggleSlider.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Enable or disable the slider based on the toggle button state
            distanceSlider.setEnabled(isChecked);
            distanceSlider.setActivated(isChecked);
            if (!isChecked) {
                distanceFilter = Integer.MAX_VALUE;
                updateRecyclerView(new ArrayList<>(mScopeList));
            } else {
                distanceFilter = (int) distanceSlider.getValue();
                Toast.makeText(this, "Distance: " + distanceFilter + " kilometers", Toast.LENGTH_SHORT).show();
                updateRecyclerView(filterScopesByLocation(distanceFilter));
            }
        });

        // Set a listener for the slider value changes
        distanceSlider.addOnChangeListener((slider, value, fromUser) -> {
            // Handle slider value changes but only if slider is enabled
            if (distanceSlider.isActivated()) {
                distanceFilter = (int) value; // Cast to int since the stepSize is 1
                Toast.makeText(this, "Distance: " + distanceFilter + " kilometers", Toast.LENGTH_SHORT).show();
                updateRecyclerView(filterScopesByLocation(distanceFilter));
            } else {
                distanceFilter = Integer.MAX_VALUE;
                updateRecyclerView(new ArrayList<>(mScopeList));
            }
        });
        // Attach the ItemTouchHelper to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mScopeListView);
        // FAB for Adding a scope
        fabAddScope.setOnClickListener(v -> {
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
            etZeroWindage.setText(strippedValue);
            etZeroElevation.setText(etZeroElevation.getText().toString());
            etZeroDate.setText((new Date()).toString());
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
                try {
                    saveScopeToDatabase(name, brand, Float.parseFloat(maxMag), varMag.equals("Yes"),
                            Integer.parseInt(zeroDistance), Float.parseFloat(zeroWindage),
                            Float.parseFloat(zeroElevation), Date.parse(zeroDate), latitude, longitude);
                    Toast.makeText(MainActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Failed to Save Scope Data: "
                            + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // Dismiss the dialog
                dialog.dismiss();
                //update the list view
                setupRecyclerView();
            });
        });

        // FAB fpr google maps
        fabMapPin.setOnClickListener(v ->{
            // Launch Google Maps intent with "shooting ranges near me" query
            Intent mapIntent = new Intent(getApplicationContext(), MapWithMarkersActivity.class);
            mapIntent.putExtra("scopes", filterScopesByLocation(distanceFilter));
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(mapIntent, 0);
            } else {
                Toast.makeText(this, "Google Maps is not installed", Toast.LENGTH_SHORT).show();
            }
            // Close MapsActivity after attempting to launch the intent
            finish();
        });

        // Set up the RecyclerView
        setupRecyclerView();
    }

    /**
     * Method for filtering the scope list based on distance from current location
     * @param radius within which we want to see the existing scopes
     * @return filtered list of scopes
     */
    private ArrayList<Scope> filterScopesByLocation(float radius) {
        return mScopeList.parallelStream().filter(l -> calculateDistance(l.getLatitude(), l.getLongitude(),
                latitude, longitude) <= radius).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Method to update the Scope List view UI dynamically as the filter changes
     * @param filteredScopes
     */
    private void updateRecyclerView(ArrayList<Scope> filteredScopes) {
        ScopeRecyclerViewAdapter mAdapter = new ScopeRecyclerViewAdapter(filteredScopes);
        mScopeListView.setAdapter(mAdapter);
        mScopeListView.setLayoutManager(new LinearLayoutManager(this));
        //adapter.updateData(filteredScopes); // Create an `updateData` method in your adapter to refresh the list.
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

    /**
     * Method to get and set current device coordinates
     */
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Get the last known location
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.d(TAG, "coord: " + latitude + ", " +longitude);
                } else {
                    Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
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

    /**
     * Method for saving a new scope to the scope table
     */
    private void saveScopeToDatabase(String name, String brand, float maxMag, boolean varMag,
                                     int zeroDistance, float zeroWindage, float zeroElevation,
                                     long zeroDate, double latitude, double longitude) {
        dbHandler.addNewScope(name, brand, maxMag, varMag, zeroDistance, zeroWindage, zeroElevation,
                new Date(zeroDate), latitude, longitude, "", "");
    }

    /**
     * Haversine Formula method
     * @param val
     * @return formula output
     */
    double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    /**
     * Method for calculating the distance between to global coordinates
     * @param startLat
     * @param startLong
     * @param endLat
     * @param endLong
     * @return distance between the provided coordinates
     */
    double calculateDistance(double startLat, double startLong, double endLat, double endLong) {
        double dLat = Math.toRadians(endLat - startLat);
        double dLong = Math.toRadians(endLong - startLong);
        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }
}
