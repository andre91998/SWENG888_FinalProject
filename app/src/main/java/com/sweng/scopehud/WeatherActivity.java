package com.sweng.scopehud;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class WeatherActivity extends NavigationActivity {

    private static final String TAG = "WeatherActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // UI elements
    private TextView temperatureTextView, windSpeedTextView, windDirectionTextView, humidityTextView, conditionsTextView, locationTextView;
    private EditText cityZipEditText;
    private Button searchButton, locationButton;

    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;

    /**
     * Called when the activity is created.
     * Initializes UI elements and sets up listeners for buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after being previously shut down, this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the drawer layout and navigation view
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        setupDrawer(toolbar, drawer, navigationView, currentUser);

        // Initialize UI elements
        temperatureTextView = findViewById(R.id.temperatureTextView);
        windSpeedTextView = findViewById(R.id.windSpeedTextView);
        windDirectionTextView = findViewById(R.id.windDirectionTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        conditionsTextView = findViewById(R.id.conditionsTextView);
        locationTextView = findViewById(R.id.locationTextView);
        cityZipEditText = findViewById(R.id.cityZipEditText);
        searchButton = findViewById(R.id.searchButton);
        locationButton = findViewById(R.id.locationButton);

        // Initialize location client and request queue for API calls
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestQueue = Volley.newRequestQueue(this);

        // Set up listeners for search and location buttons
        searchButton.setOnClickListener(v -> {
            String query = cityZipEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                getWeatherData(query); // Fetch weather data based on input query
            } else {
                Toast.makeText(WeatherActivity.this, "Please enter a city name or zip code", Toast.LENGTH_SHORT).show();
            }
        });

        locationButton.setOnClickListener(v -> getCurrentLocationWeather()); // Fetch weather data based on current location
    }

    /**
     * Fetches weather data based on a city name or zip code and updates the UI.
     * Uses the OpenWeather API to retrieve the data.
     *
     * @param query The city name or zip code entered by the user.
     */
    private void getWeatherData(String query) {
        String apiKey = getString(R.string.open_weather_key);

        // Adjust query for Puerto Rico zip codes
        if (query.matches("\\d{5}")) {
            if (Integer.parseInt(query) >= 601 && Integer.parseInt(query) <= 988) {
                query += ",pr"; // Puerto Rico
            } else {
                query += ",us"; // Mainland USA
            }
        }

        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + query + "&appid=" + apiKey + "&units=metric";

        // Create a request to the OpenWeather API
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse the weather data from the response
                        JSONObject main = response.getJSONObject("main");
                        double tempCelsius = main.getDouble("temp");
                        double tempFahrenheit = (tempCelsius * 9 / 5) + 32; // Convert Celsius to Fahrenheit
                        int humidity = main.getInt("humidity");

                        JSONObject wind = response.getJSONObject("wind");
                        double windSpeed = wind.getDouble("speed");
                        int windDeg = wind.getInt("deg");

                        String conditions = response.getJSONArray("weather").getJSONObject(0).getString("description");

                        // Update the UI with weather data
                        temperatureTextView.setText(String.format("%.2f°F", tempFahrenheit));
                        windSpeedTextView.setText(String.format("%.2f m/s", windSpeed));
                        windDirectionTextView.setText(String.format("%d degrees", windDeg));
                        humidityTextView.setText(String.format("%d%%", humidity));
                        conditionsTextView.setText(conditions);

                        // Get the location name using latitude and longitude from the response
                        double latitude = response.getJSONObject("coord").getDouble("lat");
                        double longitude = response.getJSONObject("coord").getDouble("lon");
                        getLocationName(latitude, longitude);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(WeatherActivity.this, "Error parsing weather data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching weather data: " + error.getMessage());
                    Toast.makeText(WeatherActivity.this, "Error fetching weather data", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the Volley request queue
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Fetches the weather data based on the user's current location.
     * Checks for location permissions and retrieves the location coordinates.
     */
    private void getCurrentLocationWeather() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Get the last known location
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    getLocationName(latitude, longitude);  // Fetch location name
                    getWeatherDataByLocation(latitude, longitude);  // Fetch weather based on location
                } else {
                    Toast.makeText(WeatherActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Fetches the weather data based on the given latitude and longitude.
     *
     * @param latitude  The latitude of the current location.
     * @param longitude The longitude of the current location.
     */
    private void getWeatherDataByLocation(double latitude, double longitude) {
        String apiKey = getString(R.string.open_weather_key);
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey + "&units=metric";

        // Create a request to the OpenWeather API
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse the weather data from the response
                        JSONObject main = response.getJSONObject("main");
                        double tempCelsius = main.getDouble("temp");
                        double tempFahrenheit = (tempCelsius * 9 / 5) + 32;
                        int humidity = main.getInt("humidity");

                        JSONObject wind = response.getJSONObject("wind");
                        double windSpeed = wind.getDouble("speed");
                        int windDeg = wind.getInt("deg");

                        String conditions = response.getJSONArray("weather").getJSONObject(0).getString("description");

                        // Update the UI with weather data
                        temperatureTextView.setText(String.format("%.2f°F", tempFahrenheit));
                        windSpeedTextView.setText(String.format("%.2f m/s", windSpeed));
                        windDirectionTextView.setText(String.format("%d degrees", windDeg));
                        humidityTextView.setText(String.format("%d%%", humidity));
                        conditionsTextView.setText(conditions);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(WeatherActivity.this, "Error parsing weather data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching weather data: " + error.getMessage());
                    Toast.makeText(WeatherActivity.this, "Error fetching weather data", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the Volley request queue
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Uses Geocoder to get the location name from the given latitude and longitude.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     */
    private void getLocationName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                locationTextView.setText("Location: " + address.getLocality());
            } else {
                locationTextView.setText("Location: Unknown");
            }
        } catch (IOException e) {
            e.printStackTrace();
            locationTextView.setText("Error fetching location name");
        }
    }

    /**
     * Handles the result of the location permission request.
     * If permission is granted, it fetches the weather for the current location.
     *
     * @param requestCode  The request code for permission request.
     * @param permissions  The requested permissions.
     * @param grantResults The result of the permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationWeather();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
