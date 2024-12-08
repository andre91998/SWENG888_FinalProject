package com.sweng.scopehud.util;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sweng.scopehud.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
        // Inflate the custom layout for the dialog
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_scope, null);

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        // Get references to the dialog's input fields
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etBrand = dialogView.findViewById(R.id.et_brand);
        EditText etMaxMag = dialogView.findViewById(R.id.et_max_magnification);
        Spinner spinnerVarMag = dialogView.findViewById(R.id.spinner_variable_magnification);
        EditText etZeroDistance = dialogView.findViewById(R.id.et_zero_distance);
        EditText etZeroWindage = dialogView.findViewById(R.id.et_zero_windage);
        EditText etZeroElevation = dialogView.findViewById(R.id.et_zero_elevation);
        EditText etZeroDate = dialogView.findViewById(R.id.et_zero_date);

        // Populate fields with existing scope data
        etName.setText(scope.getName());
        etBrand.setText(scope.getBrand());
        etMaxMag.setText(String.valueOf(scope.getMaxMagnification()));
        etZeroDistance.setText(String.valueOf(scope.getScopeZero().getDistance()));
        etZeroWindage.setText(String.valueOf(scope.getScopeZero().getWindage()));
        etZeroElevation.setText(String.valueOf(scope.getScopeZero().getElevation()));
        etZeroDate.setText(scope.getScopeZero().getDate().toString());

        // Setup Spinner for variable magnification
        List<String> options = Arrays.asList("Yes", "No");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVarMag.setAdapter(adapter);
        spinnerVarMag.setSelection(scope.isVariableMagnification() ? 0 : 1);

        // Handle Save and Cancel buttons
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        AlertDialog dialog = builder.create();
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            ScopeZero currentScopeZero = scope.getScopeZero();
            // Update the scope object with new values
            scope.setName(etName.getText().toString());
            scope.setBrand(etBrand.getText().toString());
            scope.setMaxMagnification(Float.parseFloat(etMaxMag.getText().toString()));
            scope.setVariableMagnification(spinnerVarMag.getSelectedItem().toString().equals("Yes"));
            currentScopeZero.setDistance((int) Float.parseFloat(etZeroDistance.getText().toString()));
            currentScopeZero.setWindage(Float.parseFloat(etZeroWindage.getText().toString()));
            currentScopeZero.setElevation(Float.parseFloat(etZeroElevation.getText().toString()));

            // Handle the date parsing (add validation as needed)
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                currentScopeZero.setDate(dateFormat.parse(etZeroDate.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(context, "Invalid date format. Use yyyy-MM-dd.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save the updated scope to the database or perform your action here
            // updateScopeInDatabase(scope);

            Toast.makeText(context, "Scope updated successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
    @Override
    public int getItemCount() {
        if(mScopeList == null || mScopeList.isEmpty()) {
            return 0;
        } else {
            return mScopeList.size();
        }
    }
    public void updateData(ArrayList<Scope> newScopes) {
        mScopeList = newScopes;
        notifyDataSetChanged();
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
