package com.sweng.scopehud;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sweng.scopehud.databinding.ActivityMapWithMarkersBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.sweng.scopehud.util.Scope;

import java.util.ArrayList;

public class MapWithMarkersActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MarkerMap";
    private GoogleMap mMap;
    private ActivityMapWithMarkersBinding binding;
    private Toolbar toolbar;
    private FusedLocationProviderClient fusedLocationClient;
    
    private ArrayList<Scope> scopeArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapWithMarkersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location runtime permission
            ActivityCompat.requestPermissions(MapWithMarkersActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }

        // Add a marker at current location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        scopeArrayList = getIntent().getParcelableArrayListExtra("scopes");
                        setLocationMarkers(location);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, do your work....
                    // Add a marker at current location
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        scopeArrayList = getIntent().getParcelableArrayListExtra("scopes");
                                        setLocationMarkers(location);
                                    }
                                });
                    }
                } else {
                    // permission denied, go back
                    finish();
                }
                return;
            }
        }
    }

    /**
     * Provided the current location, set the current location marker as well as markers for all the
     * scopes in the scope list view
     * @param location
     */
    private void setLocationMarkers(Location location) {
        // Got last known location. In some rare situations this can be null.
        if (location != null) {
            // Logic to handle location object

            Log.d(TAG, "number of markers: " + scopeArrayList.size());
            for (int i = 0; i < scopeArrayList.size(); i++) {
                Log.d(TAG, "index: " + i + " | coordinates: " + scopeArrayList.get(i).getLatitude()
                        + "/" + scopeArrayList.get(i).getLongitude());
                mMap.addMarker((new MarkerOptions().position(new LatLng(scopeArrayList.get(i).getLatitude(),
                        scopeArrayList.get(i).getLongitude())).title(scopeArrayList.get(i).getName()).visible(true)));
            }
            LatLng currentLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());
            Marker marker = mMap.addMarker((new MarkerOptions().position(currentLocation)
                    .title("You are Here!")).contentDescription("Your current Location").visible(true));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 11.0f));
            if (marker != null && marker.isVisible()) {
                marker.showInfoWindow();
            }
        }
    }
}