package com.sweng.scopehud;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

/*
 * Updated SightToolActivity
 * - Plus and minus buttons work for the yardage as they should.
 * - The enter button now updates the TextViews to the opposite of the EditText values.
 * - EditTexts are now connected to the TextViews, so changes in EditText update the TextView.
 * - Color circles are to be implemented later.
 * - Additional blocks for more customizable options to be added later.
 */
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
        setupDrawer(toolbar, drawerLayout, navigationView); // Call the method from NavigationActivity

        // Initialize views
        btnPlus = findViewById(R.id.plusButton);
        btnMinus = findViewById(R.id.minusButton);
        btnEnter = findViewById(R.id.enterRangeButton);
        editTextUpdown = findViewById(R.id.up_down_value);
        editTextLeftright = findViewById(R.id.left_right_value);
        editTextYardage = findViewById(R.id.yd_value);
        upDown = findViewById(R.id.up_down);
        leftRight = findViewById(R.id.left_right);

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
}
