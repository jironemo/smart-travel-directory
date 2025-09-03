package com.smarttravel.myanmar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PopularDestinationAdapter extends RecyclerView.Adapter<PopularDestinationAdapter.ViewHolder> {
    private Context context;
    private List<Destination> destinations;

    public PopularDestinationAdapter(Context context, List<Destination> destinations) {
        this.context = context;
        this.destinations = destinations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_popular_destination_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destination destination = destinations.get(position);
        holder.destinationName.setText(destination.getName());
        if (destination.getImageUrl() != null && !destination.getImageUrl().isEmpty()) {
            String image = destination.getImageUrl().get(0);
            // Only use URLs now
            Glide.with(context)
                .load(image)
                .centerCrop()
                .into(holder.imageView);
        } else {
            holder.imageView.setImageDrawable(null);
        }
        // Make card clickable to navigate to detail
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, DestinationDetailActivity.class);
            intent.putExtra("destination_id", destination.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return destinations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView destinationName;
        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            destinationName = itemView.findViewById(R.id.destinationName);
        }
    }
}
