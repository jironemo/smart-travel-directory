package com.smarttravel.myanmar;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {

    private Context context;
    private List<Destination> destinations;

    public DestinationAdapter(Context context, List<Destination> destinations) {
        this.context = context;
        this.destinations = destinations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_destination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destination destination = destinations.get(position);
        holder.nameTextView.setText(destination.getName());
        holder.descriptionTextView.setText(destination.getDescription());
        holder.locationTextView.setText(destination.getLocationName());
        holder.categoryTextView.setText(destination.getCategory());
        holder.ratingTextView.setText(String.format("%.1f", destination.getRating()));
        holder.destinationId = destination.getId();
        holder.cardView.setOnClickListener(
                v-> {
                    Intent intent = new Intent(context, DestinationDetailActivity.class);
                    intent.putExtra("destination_id", destination.getId());
                    context.startActivity(intent);
                    Log.i("DEBUG","Clicked on destination: " + destination.getName());
                }
        );
        // Show popular badge
        holder.popularBadge.setVisibility(destination.isPopular() ? View.VISIBLE : View.GONE);
        // Display first image from the list (URL only)
        List<String> imageList = destination.getImageUrl();
        String firstImage = (imageList != null && !imageList.isEmpty()) ? imageList.get(0) : null;
        ImageView imageView = holder.imageView;
        if (firstImage != null && !firstImage.isEmpty()) {
            Glide.with(context)
                .load(firstImage)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    @Override
    public int getItemCount() {
        return destinations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imageView;
        TextView nameTextView;
        TextView descriptionTextView;
        TextView locationTextView;
        TextView categoryTextView;
        TextView ratingTextView;
        View popularBadge;
        String destinationId;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            popularBadge = itemView.findViewById(R.id.popularBadge);
        }
    }
}