package com.sweng.scopehud;
import android.content.Context;
import android.graphics.Paint;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.sweng.scopehud.database.DBHandler;
import com.sweng.scopehud.util.Scope;
import com.sweng.scopehud.util.ScopeRecyclerViewAdapter;
import com.sweng.scopehud.util.ScopeZero;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends NavigationActivity {

    private RecyclerView mScopeListView;
    private DBHandler dbHandler;
    private List<Scope> mScopeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the drawer layout and navigation view
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        setupDrawer(toolbar, drawer, navigationView, currentUser);

        // Initialize the database handler
        dbHandler = new DBHandler(this);
        /*
        * initialize RecyclerView
        * Add sample data to the list(this can be removed later of course
        * set the adapter
        */
        mScopeListView = findViewById(R.id.scopeListView);
        mScopeListView.setLayoutManager(new LinearLayoutManager(this));

        List<Scope> scopeList = new ArrayList<>();
        scopeList.add(new Scope(1, "RAZOR HD GEN III", "Vortex Optics", 18.0f, true,
                new ScopeZero(100, 0.1f, 0.2f, new Date(), new Location(""))));
        scopeList.add(new Scope(2, "STRIKE EAGLE", "Vortex Optics", 24.0f, true,
                new ScopeZero(200, 0.0f, 0.1f, new Date(), new Location(""))));

        ScopeRecyclerViewAdapter adapter = new ScopeRecyclerViewAdapter(scopeList);
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
                Scope removedScope = scopeList.get(position); // Save the removed item in case you want to undo
                scopeList.remove(position);
                adapter.notifyItemRemoved(position);

                // Optionally show a Snackbar to undo the delete
                Snackbar.make(mScopeListView, "Scope deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            // Add the item back if undo is clicked
                            scopeList.add(position, removedScope);
                            adapter.notifyItemInserted(position);
                        }).show();

                // delete from the database
                // dbHandler.deleteScope(removedScope.getId());
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

        // Attach the ItemTouchHelper to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mScopeListView);
        // Set up the RecyclerView
        //setupRecyclerView(); uncomment this once db stuff is working
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
}
