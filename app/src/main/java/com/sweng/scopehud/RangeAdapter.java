package com.sweng.scopehud;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RangeAdapter extends RecyclerView.Adapter<RangeAdapter.ViewHolder> {

    private final List<Range> ranges;

    /**
     * Constructor for RangeAdapter to initialize the list of ranges.
     *
     * @param ranges The list of Range objects to be displayed in the RecyclerView.
     */
    public RangeAdapter(List<Range> ranges) {
        this.ranges = ranges;
    }

    /**
     * This method is called when a new ViewHolder is created.
     * It inflates the layout for an individual range item in the RecyclerView.
     *
     * @param parent   The parent ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds the view for each range item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a range item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.range_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * This method binds the data for each range item to the ViewHolder.
     * It sets up the range information and click listeners for each item in the list.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the Range object at the given position
        Range range = ranges.get(position);

        // Display latitude and longitude in a user-friendly format
        String rangeInfo = "Range Location:\nLat: " + range.getLatitude() + ", Lng: " + range.getLongitude();
        holder.rangeTextView.setText(rangeInfo);

        // Set up an onClickListener to handle clicks on each range item
        holder.itemView.setOnClickListener(v -> {
            // Show a toast with the range information or perform some other action
            Toast.makeText(holder.itemView.getContext(), "Selected Range:\n" + rangeInfo, Toast.LENGTH_SHORT).show();
        });

        // Set up a long click listener to remove a range from the list
        holder.itemView.setOnLongClickListener(v -> {
            // Remove the clicked item from the list
            ranges.remove(position);
            notifyItemRemoved(position);  // Notify the adapter that an item was removed
            notifyItemRangeChanged(position, ranges.size());  // Notify that the range of items has changed
            Toast.makeText(holder.itemView.getContext(), "Range removed", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    /**
     * Returns the total number of range items in the list.
     *
     * @return The size of the ranges list.
     */
    @Override
    public int getItemCount() {
        return ranges.size();
    }

    /**
     * ViewHolder class that holds the view for each range item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView rangeTextView;

        /**
         * Constructor for ViewHolder.
         * It initializes the TextView for displaying the range information.
         *
         * @param itemView The view representing a single range item.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            // Initialize the TextView to display the range information
            rangeTextView = itemView.findViewById(R.id.rangeTextView);
        }
    }
}
