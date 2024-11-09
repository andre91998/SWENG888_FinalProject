package com.sweng.scopehud;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RangeAdapter adapter;
    private FirebaseFirestore firestore;

    /**
     * This method is called when the activity is created.
     * It initializes the RecyclerView and Firestore, and starts fetching data for the markers.
     *
     * @param savedInstanceState If the activity is being re-initialized after being previously shut down, this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        // Initialize the RecyclerView and set its layout manager
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Fetch the shooting range markers from Firestore
        fetchMarkers();
    }

    /**
     * Fetches the markers (shooting range locations) from the Firestore database.
     * It retrieves the data from the "shooting_ranges" collection and updates the RecyclerView with the results.
     */
    private void fetchMarkers() {
        // Query the "shooting_ranges" collection in Firestore
        firestore.collection("shooting_ranges").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Create a list to hold the fetched ranges
                        List<Range> ranges = new ArrayList<>();
                        // Iterate through the query results and create Range objects
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            double latitude = document.getDouble("latitude");
                            double longitude = document.getDouble("longitude");
                            ranges.add(new Range("Shooting Range", latitude, longitude));
                        }
                        // Set the adapter for the RecyclerView with the fetched data
                        adapter = new RangeAdapter(ranges);
                        recyclerView.setAdapter(adapter);
                    } else {
                        // Show a Toast message if the data fetch failed
                        Toast.makeText(RecyclerViewActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}