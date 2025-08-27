package com.smarttravel.myanmar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TripAdviceAdapter extends RecyclerView.Adapter<TripAdviceAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(TripAdvice tripAdvice);
    }
    private final List<TripAdvice> tripAdvices;
    private final OnItemClickListener listener;

    public TripAdviceAdapter(List<TripAdvice> tripAdvices, OnItemClickListener listener) {
        this.tripAdvices = tripAdvices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TripAdvice tripAdvice = tripAdvices.get(position);
        holder.title.setText(tripAdvice.getLocation());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(tripAdvice));
    }

    @Override
    public int getItemCount() {
        return tripAdvices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
        }
    }
}

