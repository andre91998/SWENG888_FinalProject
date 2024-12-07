package com.sweng.scopehud;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.sweng.scopehud.database.DBHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.time.Instant;
import java.util.Date;

/*
 * Updated SightToolActivity
 * - Plus and minus buttons work for the yardage as they should.
 * - The enter button now updates the TextViews to the opposite of the EditText values.
 * - EditTexts are now connected to the TextViews, so changes in EditText update the TextView.
 * - Color circles are to be implemented later.
 * - Additional blocks for more customizable options to be added later.
 */
public class SightToolActivity extends NavigationActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Button btnEnter; // Buttons for sight tool
    private EditText editTextUpdown, editTextLeftright, editTextYardage,angle_value; // Input fields for sight tool
    private TextView upDown, leftRight, windDir; // TextViews for sight tool
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;
    private DBHandler dbHandler;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sight_tool); // Set the layout

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the navigation drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        setupDrawer(toolbar, drawerLayout, navigationView, currentUser); // Call the method from NavigationActivity

        // Initialize views
        Button btnPlus = findViewById(R.id.plusButton);
        Button btnMinus = findViewById(R.id.minusButton);
        FloatingActionButton fab = findViewById(R.id.add_scope);
        btnEnter = findViewById(R.id.enterRangeButton);
        editTextUpdown = findViewById(R.id.up_down_value);
        editTextLeftright = findViewById(R.id.left_right_value);
        editTextYardage = findViewById(R.id.yd_value);
        upDown = findViewById(R.id.up_down);
        leftRight = findViewById(R.id.left_right);
        windDir = findViewById(R.id.windDir);
        angle_value = findViewById(R.id.angle_value);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestQueue = Volley.newRequestQueue(this);

        //Intialize DBHandler
        dbHandler = new DBHandler(this);

        getCurrentLocationWeather(); // Fetch weather data based on current location
        // Plus button increments the yardage value
        btnPlus.setOnClickListener(v -> {
            int value = Integer.parseInt(editTextYardage.getText().toString());
            value++;
            editTextYardage.setText(String.valueOf(value));
        });

        // Minus button decrements the yardage value
        btnMinus.setOnClickListener(v -> {
            int value = Integer.parseInt(editTextYardage.getText().toString());
            value--;
            editTextYardage.setText(String.valueOf(value));
        });

        // Enter button updates the TextViews with the opposite of the EditText values
        btnEnter.setOnClickListener(v -> {
            // Get the current values from EditTexts
            String upDownValue = editTextUpdown.getText().toString();
            String leftRightValue = editTextLeftright.getText().toString();

            // Update TextViews with opposite values
            upDown.setText(invertValue(upDownValue));
            leftRight.setText(invertValue(leftRightValue));
        });

        fab.setOnClickListener(v -> {
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
            etZeroDistance.setText(editTextYardage.getText());
            EditText etZeroWindage = dialogView.findViewById(R.id.et_zero_windage);
            etZeroWindage.setText(editTextLeftright.getText());
            EditText etZeroElevation = dialogView.findViewById(R.id.et_zero_elevation);
            etZeroElevation.setText(editTextUpdown.getText());
            EditText etZeroDate = dialogView.findViewById(R.id.et_zero_date);
            etZeroDate.setText((new Date()).toString());

            // Populate the EditText fields with existing data
            etZeroDistance.setText(editTextYardage.getText().toString());
            String windDirection = windDir.getText().toString(); // Get the text from windDir
            String strippedValue = windDirection.replace("DIR:", "").trim(); // Remove "DIR:" and trim spaces
            etZeroWindage.setText(strippedValue);
            etZeroElevation.setText(angle_value.getText().toString());
            etZeroDate.setText((new Date()).toString());
            // Handle Save and Cancel buttons
            Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
            Button btnSave = dialogView.findViewById(R.id.btn_save);

            // Create the options and their corresponding values
            List<String> options = Arrays.asList("Yes", "No");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, options);
            adapter.setDropDownViewResource(R.layout.spinner_item);

            // Set the adapter to the Spinner
            variableMagnificationSpinner.setAdapter(adapter);

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
                Snackbar.make(view, "Scope saved", Snackbar.LENGTH_SHORT).show();
                /*
                * You also need to get the values from the Sight tool and save them to the database.
                */

                // Save the values to the database or perform desired action
                saveScopeToDatabase(name, brand, Float.parseFloat(maxMag), Boolean.parseBoolean(varMag),
                        Integer.parseInt(zeroDistance), Float.parseFloat(zeroWindage),
                        Float.parseFloat(zeroElevation), Date.parse(zeroDate));

                // Dismiss the dialog
                dialog.dismiss();
            });
        });
        // Add TextWatcher to update the TextViews when the EditText values change
        editTextUpdown.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Update upDown TextView when EditText changes
                upDown.setText(invertValue(charSequence.toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        editTextLeftright.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Update leftRight TextView when EditText changes
                leftRight.setText(invertValue(charSequence.toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }
    public void  onDirectionClick(View v) {
            if (v.getId() == R.id.up_down) {
                if (upDown.getText().toString().equals("D")) {
                    upDown.setText("U");
                } else {
                    upDown.setText("D");
                }
            } else if (v.getId() == R.id.left_right) {
                if(leftRight.getText().toString().equals("R")) {
                    leftRight.setText("L");
                } else {
                    leftRight.setText("R");
                }
            }
        }
    public void onAdjustmentClick(View v) {
        // Get references to both TextViews
        TextView adjL = findViewById(R.id.adjL);
        TextView adjR = findViewById(R.id.adjR);

        // Swap styles based on the clicked circle
        if (v.getId() == R.id.adjR) {
            // Swap styles between Left and Right
            // Save current styles of the Right circle
            Drawable rightBackground = adjR.getBackground();
            int rightTextColor = adjR.getCurrentTextColor();

            // Apply Right's style to Left
            adjR.setBackground(getResources().getDrawable(R.drawable.dark_circle));
            adjR.setTextColor(getResources().getColor(R.color.orange_hue));

            // Apply Left's style to Right
            adjL.setBackground(getResources().getDrawable(R.drawable.beige_background));
            adjL.setTextColor(getResources().getColor(R.color.black));
        } else if (v.getId() == R.id.adjL) {
            // If Left is clicked, swap styles in reverse
            Drawable leftBackground = adjL.getBackground();
            int leftTextColor = adjL.getCurrentTextColor();

            // Apply Left's style to Right
            adjL.setBackground(getResources().getDrawable(R.drawable.dark_circle));
            adjL.setTextColor(getResources().getColor(R.color.orange_hue));

            // Apply Right's style to Left
            adjR.setBackground(getResources().getDrawable(R.drawable.beige_background));
            adjR.setTextColor(getResources().getColor(android.R.color.black));
        }
        else{
            // If neither is clicked, reset both styles
            adjL.setBackground(getResources().getDrawable(R.drawable.beige_background));
            adjL.setTextColor(getResources().getColor(android.R.color.black));
            adjR.setBackground(getResources().getDrawable(R.drawable.beige_background));
            adjR.setTextColor(getResources().getColor(R.color.black));
        }
    }

    /**
     * Fetches the weather data based on the user's current location.
     * Checks for location permissions and retrieves the location coordinates.
     */
    private void getCurrentLocationWeather() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Get the last known location
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    getWeatherDataByLocation(latitude, longitude);  // Fetch weather based on location
                } else {
                    Toast.makeText(SightToolActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void getWeatherDataByLocation(double latitude, double longitude) {
        String apiKey = getString(R.string.open_weather_key);
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey + "&units=metric";

        // Create a request to the OpenWeather API
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse the weather data from the response
                        JSONObject wind = response.getJSONObject("wind");
                        int windDeg = wind.getInt("deg");
                        double windSpd = wind.getDouble("speed");
                        double windSpds = windSpd * 2.237; // Convert m/s to mph
                        TextView windSpeed = findViewById(R.id.windSpeed);
                        windSpeed.setText(String.format("SPD: %.1f MPH", windSpds));

                        windDir.setText(String.format("DIR: %d", windDeg));


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(SightToolActivity.this, "Error parsing weather data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching weather data: " + error.getMessage());
                    Toast.makeText(SightToolActivity.this, "Error fetching weather data", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the Volley request queue
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Method to invert a numeric string value (e.g., turn "1" into "-1").
     */
    private String invertValue(String value) {
        try {
            int intValue = Integer.parseInt(value);
            return String.valueOf(-intValue);
        } catch (NumberFormatException e) {
            return "0"; // Default to 0 if there's an invalid input
        }
    }

    /**
     *
     */
    private void saveScopeToDatabase(String name, String brand, float maxMag, boolean varMag,
                                     int zeroDistance, float zeroWindage, float zeroElevation,
                                     long zeroDate) {
        dbHandler.addNewScope(name, brand, maxMag, varMag, zeroDistance, zeroWindage, zeroElevation,
                new Date(zeroDate), new Location(""), "", "");
    }
}
