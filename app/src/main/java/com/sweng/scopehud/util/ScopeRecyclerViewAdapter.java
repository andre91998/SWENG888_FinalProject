package com.sweng.scopehud.util;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sweng.scopehud.R;

import java.util.List;

public class ScopeRecyclerViewAdapter extends RecyclerView.Adapter<ScopeRecyclerViewAdapter.MyViewHolder> {

    private List<Scope> mScopeList;

    public ScopeRecyclerViewAdapter(List<Scope> scopeList) {
        mScopeList = scopeList;
    }

    @Override
    @NonNull
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scope_item_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        //final Scope scope = mScopeList.get(position);
        Scope scope  = mScopeList.get(position); // `items` is your data list

        // Set data for the views
        holder.titleTextView.setText(scope.getName());
        holder.companyTextView.setText(scope.getBrand());
        // Set a click listener for the info icon
        holder.scopeInfo.setOnClickListener(v -> {
            // Show a dialog with additional scope stats
            showScopeInfoDialog(v.getContext(), scope);
        });
    }
    private void showScopeInfoDialog(Context context, Scope scope) {
        // Create a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set dialog title
        builder.setTitle(scope.getName());

        // Set dialog message (additional stats)
        String message = "Brand: " + scope.getBrand() +
                "\nMax Magnification: " + scope.getMaxMagnification() +
                "\nVariable Magnification: " + (scope.isVariableMagnification() ? "Yes" : "No") +
                "\nZero Distance: " + scope.getScopeZero().getDistance() +
                "\nWindage: " + scope.getScopeZero().getWindage() +
                "\nElevation: " + scope.getScopeZero().getElevation() +
                "\nZeroed On: " + scope.getScopeZero().getDate().toString();
        builder.setMessage(message);

        // Add an "OK" button to close the dialog
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        // Create and show the dialog
        builder.create().show();
    }
    @Override
    public int getItemCount() {
        if(mScopeList == null || mScopeList.isEmpty()) {
            return 0;
        } else {
            return mScopeList.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView titleTextView;
        private TextView companyTextView;
        private ImageView scopeInfo;

        public MyViewHolder(View itemView) {
            super(itemView);
            // Bind the views from the layout
            titleTextView = itemView.findViewById(R.id.title_text_view);
            companyTextView = itemView.findViewById(R.id.company_text_view);
            scopeInfo = itemView.findViewById(R.id.scope_info);
        }
    }
}
