package com.sweng.scopehud.util;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scope_item_row,
                parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final Scope scope = mScopeList.get(position);
        holder.textView.setText(scope.getName());
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //click on scope behavior, for now, it's a toast
                Toast toast = new Toast(view.getContext());
                toast.setText(mScopeList.get(holder.getAdapterPosition()).toString());
                toast.show();
            }
        });
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

        private View view;
        private TextView textView;

        private MyViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.textView = (TextView) view.findViewById(R.id.text_view);
        }
    }
}
