package com.smarttravel.myanmar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TripDestinationsAdapter extends RecyclerView.Adapter<TripDestinationsAdapter.DestinationViewHolder> {
    private List<Destination> destinations;
    public TripDestinationsAdapter(List<Destination> destinations) {
        this.destinations = destinations;
    }
    @NonNull
    @Override
    public DestinationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_destination, parent, false);
        return new DestinationViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull DestinationViewHolder holder, int position) {
        Destination destination = destinations.get(position);
        holder.destinationText.setText(destination.getName() != null ? destination.getName() : "Unknown");
        // Highlight selected destinations
        if (destination.isSelected()) {
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#FFEB3B")); // Yellow highlight
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.WHITE);
        }
        holder.itemView.setOnClickListener(v -> {
            android.content.Context context = holder.itemView.getContext();
            String destinationId = destination.getId();
            android.content.Intent intent = new android.content.Intent(context, DestinationDetailActivity.class);
            intent.putExtra("destination_id", destinationId);
            context.startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return destinations.size();
    }
    static class DestinationViewHolder extends RecyclerView.ViewHolder {
        TextView destinationText;
        DestinationViewHolder(View itemView) {
            super(itemView);
            destinationText = itemView.findViewById(R.id.tripDestinationText);
        }
    }
}
