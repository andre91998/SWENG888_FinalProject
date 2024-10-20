package com.sweng.scopehud;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
/*
* There is still lots that need done here.
* The plus and minus buttons work for the yardage as they should.
* The enter button does nothing.
* The EditTexts need to be connected to the TextViews. So when the EditText value changes, the TextView value should change to the opposite of what it was.
* The Color circles need to be implemented properly, but that is something I will explain on a call.
* I still need to add the other two blocks that contain more customizable options.
* */
public class SightToolActivity extends NavigationActivity {
    private Button btnPlus, btnMinus, btnEnter; // Buttons for sight tool
    private EditText editTextUpdown, editTextLeftright, editTextYardage; // Input fields for sight tool
    private TextView upDown, leftRight; // TextViews for sight tool
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
        setupDrawer(toolbar, drawerLayout, navigationView); // Call the method from BaseActivity

        // Initialize views
        btnPlus = findViewById(R.id.plusButton);
        btnMinus = findViewById(R.id.minusButton);
        btnEnter = findViewById(R.id.enterRangeButton);
        editTextUpdown = findViewById(R.id.up_down_value);
        editTextLeftright = findViewById(R.id.left_right_value);
        editTextYardage = findViewById(R.id.yd_value);
        upDown = findViewById(R.id.up_down);
        leftRight = findViewById(R.id.left_right);

        btnMinus.setOnClickListener(v -> {
            // Decrement the up/down value
            int value = Integer.parseInt(editTextYardage.getText().toString());
            value--;
            editTextYardage.setText(String.valueOf(value));
        });
        btnPlus.setOnClickListener(v -> {
            // Increment the up/down value
            int value = Integer.parseInt(editTextYardage.getText().toString());
            value++;
            editTextYardage.setText(String.valueOf(value));
        });
    }
}

