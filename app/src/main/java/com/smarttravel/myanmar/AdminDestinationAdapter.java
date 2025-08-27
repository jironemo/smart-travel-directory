package com.smarttravel.myanmar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminDestinationAdapter extends RecyclerView.Adapter<AdminDestinationAdapter.ViewHolder> {
    private Context context;
    private List<Destination> destinations;

    public AdminDestinationAdapter(Context context, List<Destination> destinations) {
        this.context = context;
        this.destinations = destinations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_destination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destination destination = destinations.get(position);
        holder.nameTextView.setText(destination.getName());
        holder.locationTextView.setText(destination.getLocation());
        holder.categoryTextView.setText(destination.getCategory());
        holder.descriptionTextView.setText(destination.getDescription());
        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditDestinationActivity.class);
            intent.putExtra("destination_id", destination.getId());
            context.startActivity(intent);
        });
        // Store base64 image string in imageUrl for compatibility
        String base64 = destination.getImageUrl();
        if (base64 != null && !base64.isEmpty()) {
            try {
                byte[] imageBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.thumbnailImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.thumbnailImageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.thumbnailImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return destinations.size();
    }

    public void updateList(List<Destination> newDestinations) {
        this.destinations = newDestinations;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, locationTextView, categoryTextView, descriptionTextView;
        Button editButton;
        ImageView thumbnailImageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.adminDestinationNameTextView);
            locationTextView = itemView.findViewById(R.id.adminDestinationLocationTextView);
            categoryTextView = itemView.findViewById(R.id.adminDestinationCategoryTextView);
            descriptionTextView = itemView.findViewById(R.id.adminDestinationDescriptionTextView);
            editButton = itemView.findViewById(R.id.adminEditDestinationButton);
            thumbnailImageView = itemView.findViewById(R.id.adminDestinationThumbnailImageView);
        }
    }
}
