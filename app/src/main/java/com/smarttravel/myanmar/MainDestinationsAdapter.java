package com.smarttravel.myanmar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainDestinationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_POPULAR = 1;
    private static final int VIEW_TYPE_ALL_DESTINATIONS_HEADER = 2;
    private static final int VIEW_TYPE_DESTINATION = 3;
    private Context context;
    private List<Destination> popularDestinations;
    private List<Destination> allDestinations;

    public MainDestinationsAdapter(Context context, List<Destination> popularDestinations, List<Destination> allDestinations) {
        this.context = context;
        this.popularDestinations = popularDestinations;
        this.allDestinations = allDestinations;
    }

    public void setPopularDestinations(List<Destination> popular) {
        this.popularDestinations = popular;
        notifyDataSetChanged();
    }
    public void setAllDestinations(List<Destination> all) {
        this.allDestinations = all;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return VIEW_TYPE_HEADER;
        if (position == 1) return VIEW_TYPE_POPULAR;
        if (position == 2) return VIEW_TYPE_ALL_DESTINATIONS_HEADER;
        return VIEW_TYPE_DESTINATION;
    }

    @Override
    public int getItemCount() {
        return 3 + allDestinations.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_HEADER) {
            View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_POPULAR) {
            View view = inflater.inflate(R.layout.item_popular_destinations, parent, false);
            return new PopularViewHolder(view);
        } else if (viewType == VIEW_TYPE_ALL_DESTINATIONS_HEADER) {
            View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new AllDestinationsHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_destination, parent, false);
            return new DestinationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).title.setText("Popular Destinations");
            ((HeaderViewHolder) holder).title.setTextSize(20);
            ((HeaderViewHolder) holder).title.setTextColor(holder.itemView.getResources().getColor(R.color.primary));
            ((HeaderViewHolder) holder).title.setPadding(32, 32, 0, 16);
        } else if (holder instanceof PopularViewHolder) {
            ((PopularViewHolder) holder).bind(popularDestinations);
        } else if (holder instanceof AllDestinationsHeaderViewHolder) {
            ((AllDestinationsHeaderViewHolder) holder).title.setText("All Destinations");
            ((AllDestinationsHeaderViewHolder) holder).title.setTextSize(20);
            ((AllDestinationsHeaderViewHolder) holder).title.setTextColor(holder.itemView.getResources().getColor(R.color.primary));
            ((AllDestinationsHeaderViewHolder) holder).title.setPadding(32, 32, 0, 16);
        } else if (holder instanceof DestinationViewHolder) {
            int destIndex = position - 3;
            if (destIndex >= 0 && destIndex < allDestinations.size()) {
                ((DestinationViewHolder) holder).bind(allDestinations.get(destIndex));
            }
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        HeaderViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
        }
    }
    static class PopularViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        PopularViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.popularRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        }
        void bind(List<Destination> popularDestinations) {
            PopularDestinationAdapter adapter = new PopularDestinationAdapter(itemView.getContext(), popularDestinations);
            recyclerView.setAdapter(adapter);
        }
    }
    static class AllDestinationsHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        AllDestinationsHeaderViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
        }
    }
    static class DestinationViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        TextView locationTextView;
        TextView categoryTextView;
        TextView ratingTextView;
        View popularBadge;
        DestinationViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            popularBadge = itemView.findViewById(R.id.popularBadge);
        }
        void bind(Destination destination) {
            nameTextView.setText(destination.getName());
            descriptionTextView.setText(destination.getDescription());
            locationTextView.setText(destination.getLocationName());
            categoryTextView.setText(destination.getCategory());
            ratingTextView.setText(String.format("%.1f", destination.getRating()));
            popularBadge.setVisibility(destination.isPopular() ? View.VISIBLE : View.GONE);
            // Display first image from the list (URL only)
            List<String> imageList = destination.getImageUrl();
            String firstImage = (imageList != null && !imageList.isEmpty()) ? imageList.get(0) : null;
            ImageView imageView = itemView.findViewById(R.id.imageView);
            if (firstImage != null && !firstImage.isEmpty()) {
                com.bumptech.glide.Glide.with(itemView.getContext())
                    .load(firstImage)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder_image);
            }
            // Handle click to open details
            itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(itemView.getContext(), DestinationDetailActivity.class);
                intent.putExtra("destination_id", destination.getId());
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
