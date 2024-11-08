package com.sweng.scopehud;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Marker currentLocationMarker;
    private RecyclerView rangesRecyclerView;
    private RangeAdapter rangeAdapter;
    private List<Range> selectedRangesList = new ArrayList<>();
    private MapView mapView;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize Places API
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        // Set up the AutocompleteSupportFragment for the search bar
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocompleteFragment);
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setHint("Search for a place");
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    if (place.getLatLng() != null) {
                        LatLng selectedLocation = place.getLatLng();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
                        mMap.addMarker(new MarkerOptions()
                                .position(selectedLocation)
                                .title(place.getName())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); // Optional customization

                        // Call the method correctly
                        addRangeToRecyclerView(place.getId(), place.getLatLng(), place.getName());
                    }
                }

                @Override
                public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                    Toast.makeText(MapsActivity.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Initialize MapView
        mapView = findViewById(R.id.mapContainer);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        // Initialize RecyclerView
        rangesRecyclerView = findViewById(R.id.rangesRecyclerView);
        rangeAdapter = new RangeAdapter(selectedRangesList);
        rangesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rangesRecyclerView.setAdapter(rangeAdapter);

        // Request location permissions
        getLocationPermission();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);  // Shows navigation toolbar for directions
        startLocationUpdates();

        // Allow user to add markers by long-pressing on the map
        mMap.setOnMapLongClickListener(latLng -> {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Custom Marker")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            if (marker != null) {
                double distanceToNearest = calculateDistanceToNearestLocation(latLng);
                addRangeToRecyclerView(marker.getId(), latLng, "Custom Marker", distanceToNearest);
                saveMarkerToDatabase(latLng);
                Toast.makeText(MapsActivity.this, "Marker added and saved to database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveMarkerToDatabase(LatLng latLng) {
        Map<String, Object> markerData = new HashMap<>();
        markerData.put("latitude", latLng.latitude);
        markerData.put("longitude", latLng.longitude);
        markerData.put("title", "Custom Marker");

        firestore.collection("markers").add(markerData)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(MapsActivity.this, "Marker saved successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(MapsActivity.this, "Failed to save marker: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addRangeToRecyclerView(String id, LatLng latLng, String name, double distance) {
        // Create a Range object using the existing constructor
        Range newRange = new Range(id, latLng.latitude, latLng.longitude);

        // Optionally, add logic to handle `name` and `distance` if needed
        selectedRangesList.add(newRange);
        rangeAdapter.notifyDataSetChanged();
    }

    // Overloaded method for convenience
    private void addRangeToRecyclerView(String id, LatLng latLng, String name) {
        double distance = calculateDistanceToNearestLocation(latLng);
        addRangeToRecyclerView(id, latLng, name, distance);
    }

    private double calculateDistanceToNearestLocation(LatLng latLng) {
        float[] result = new float[1];
        double minDistance = Double.MAX_VALUE;

        for (Range range : selectedRangesList) {
            // Replace `getLatLng()` with `getLatitude()` and `getLongitude()`
            Location.distanceBetween(latLng.latitude, latLng.longitude, range.getLatitude(), range.getLongitude(), result);
            if (result[0] < minDistance) {
                minDistance = result[0];
            }
        }
        return minDistance != Double.MAX_VALUE ? minDistance : 0;
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (mMap != null) {
                        updateCurrentLocationMarker(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateCurrentLocationMarker(LatLng latLng) {
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }
        currentLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Your Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }
}
