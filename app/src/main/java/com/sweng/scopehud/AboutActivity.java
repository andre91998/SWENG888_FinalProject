package com.sweng.scopehud;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

public class AboutActivity extends NavigationActivity {

    // UI components
    private Toolbar toolbar;

    /**
     * This method is called when the activity is first created.
     * It sets the content view and initializes the TextViews to display the app's details.
     *
     * @param savedInstanceState If the activity is being re-initialized after being previously shut down,
     *                           this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Initialize TextView for the app version
        TextView versionTextView = findViewById(R.id.versionTextView);
        // Initialize TextView for the developer names
        TextView developerTextView = findViewById(R.id.developerTextView);
        // Initialize TextView for the license information
        TextView licenseTextView = findViewById(R.id.licenseTextView);
        // Initialize TextView for the app description
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);

        // Set the version of the app
        versionTextView.setText("Version: 1.0.0");
        // Set the developers of the app
        developerTextView.setText("Developers: Roberto Luna, André Medeiros & Andrew Young");
        // Set the licenses for the app
        licenseTextView.setText("Licenses: MIT License");

        // Set the description text that explains the purpose and functionality of the app
        descriptionTextView.setText("Description: The Dialed In Zeroing App is designed to help users easily track and save their progress while zeroing a scope on any rifle or gun. " +
                "The app allows users to make precise adjustments to their scope’s windage (left/right) and elevation (up/down) controls to ensure the crosshairs align with the point of impact. " +
                "These adjustments are typically measured in MOA (Minute of Angle) or MRAD (Milliradian), but for this app, we will be using milliradians (MRAD). Once the user inputs their adjustments " +
                "and the scope is fully zeroed, the app can save this data to the user’s profile for future reference, ensuring they always have access to their zeroing details for different scopes.");

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set toolbar as ActionBar

        // Initialize DrawerLayout and NavigationView
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        setupDrawer(toolbar, drawer, navigationView, currentUser);
    }
}
