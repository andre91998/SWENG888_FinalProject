package com.sweng.scopehud;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseFirestore firestore;
    private PlacesClient placesClient;
    private LocationCallback locationCallback;
    private Marker currentLocationMarker;

    private LatLng currentLocation;
    private EditText searchBar;
    private Button searchButton;
    private RecyclerView rangesRecyclerView;
    private RangeAdapter rangeAdapter;
    private List<Range> selectedRangesList = new ArrayList<>();

    /**
     * This method is called when the activity is created.
     * It initializes the UI elements, Firestore, location services, and Places API.
     * It also sets up the map and search functionality for the user.
     *
     * @param savedInstanceState if the activity is being re-created after being previously shut down, this contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize Firestore and Location services
        firestore = FirebaseFirestore.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Places API
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);

        // Initialize UI elements
        searchBar = findViewById(R.id.searchBar);
        searchButton = findViewById(R.id.searchButton);
        rangesRecyclerView = findViewById(R.id.rangesRecyclerView);
        rangeAdapter = new RangeAdapter(selectedRangesList);
        rangesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rangesRecyclerView.setAdapter(rangeAdapter);

        // Load the map fragment and set the callback for when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up the search button click listener
        searchButton.setOnClickListener(v -> {
            String location = searchBar.getText().toString();
            if (!location.isEmpty()) {
                searchLocation(location);
            } else {
                Toast.makeText(MapsActivity.this, "Please enter a city or zip code", Toast.LENGTH_SHORT).show();
            }
        });

        // Request location permissions and start tracking the user's location
        getLocationPermission();
    }

    /**
     * Called when the map is ready to be used.
     * It enables zoom controls and starts location updates.
     *
     * @param googleMap The GoogleMap object that is ready for use.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable zoom controls and gestures on the map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        // Start tracking the user's current location
        startLocationUpdates();
    }

    /**
     * Requests location permission from the user. If permission is granted, location updates are started.
     * If permission is not granted, it requests the permission from the user.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not already granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, start location updates
            startLocationUpdates();
        }
    }

    /**
     * Starts real-time location updates using the FusedLocationProviderClient.
     * It sets up a LocationRequest with high accuracy and sets intervals for receiving location updates.
     */
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);  // 10 seconds
        locationRequest.setFastestInterval(5000);  // 5 seconds

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                // Iterate through the list of locations and update the map
                for (Location location : locationResult.getLocations()) {
                    if (mMap != null) {
                        updateCurrentLocationMarker(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                }
            }
        };

        // Start location updates if the permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    /**
     * Updates the map by placing a marker at the current location and moving the camera to that location.
     * It also fetches nearby shooting ranges for the updated location.
     *
     * @param latLng The current latitude and longitude of the user.
     */
    private void updateCurrentLocationMarker(LatLng latLng) {
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();  // Remove the previous marker
        }

        // Add a new marker at the current location
        currentLocationMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        // Fetch nearby shooting ranges based on the current location
        fetchNearbyShootingRanges(latLng);
    }

    /**
     * Placeholder function to fetch nearby shooting ranges based on the user's location.
     * The actual implementation would use the Google Places API.
     *
     * @param location The user's current location (latitude and longitude).
     */
    private void fetchNearbyShootingRanges(LatLng location) {
        // TODO: Add implementation for fetching real shooting ranges using the Google Places API
    }

    /**
     * Handles the result of the location permission request.
     * If permission is granted, location updates are started. If denied, a message is displayed.
     *
     * @param requestCode The request code passed in requestPermissions.
     * @param permissions The requested permissions.
     * @param grantResults The results of the permission requests.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();  // Permission granted, start location updates
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Searches for a location by city name or zip code using the Geocoder API.
     * It fetches the latitude and longitude of the location and moves the camera on the map.
     *
     * @param locationName The name of the city or zip code entered by the user.
     */
    private void searchLocation(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng searchedLocation = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, 12));

                // Fetch nearby shooting ranges for the searched location
                fetchNearbyShootingRanges(searchedLocation);
            } else {
                Toast.makeText(MapsActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MapsActivity.this, "Error searching location", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Adds the selected shooting range (name and location) to the RecyclerView.
     * It updates the list and notifies the adapter to refresh the display.
     *
     * @param latLng The latitude and longitude of the shooting range.
     * @param rangeName The name of the shooting range.
     */
    private void addRangeToRecyclerView(LatLng latLng, String rangeName) {
        Range range = new Range(rangeName, latLng.latitude, latLng.longitude);
        if (!selectedRangesList.contains(range)) {
            selectedRangesList.add(range);
            rangeAdapter.notifyDataSetChanged();  // Update RecyclerView
        } else {
            Toast.makeText(this, "Range already added", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Stops location updates when the activity is destroyed to prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}
