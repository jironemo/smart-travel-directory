package com.smarttravel.myanmar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    public interface OnEditClickListener {
        void onEdit(Location location);
    }
    public interface OnDeleteClickListener {
        void onDelete(Location location, int position);
    }
    private List<Location> locationList;
    private OnEditClickListener editClickListener;
    private OnDeleteClickListener deleteClickListener;

    public LocationAdapter(List<Location> locationList, OnEditClickListener editClickListener, OnDeleteClickListener deleteClickListener) {
        this.locationList = locationList;
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location location = locationList.get(position);
        holder.tvName.setText(location.getName());
        holder.tvDivision.setText(location.getDivision());
        holder.tvDesc.setText(location.getDescription());
        holder.btnEdit.setOnClickListener(v -> {
            if (editClickListener != null) editClickListener.onEdit(location);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) deleteClickListener.onDelete(location, position);
        });
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDivision, tvDesc;
        ImageButton btnEdit, btnDelete;
        LocationViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvLocationName);
            tvDivision = itemView.findViewById(R.id.tvLocationDivision);
            tvDesc = itemView.findViewById(R.id.tvLocationDescription);
            btnEdit = itemView.findViewById(R.id.btnEditLocation);
            btnDelete = itemView.findViewById(R.id.btnDeleteLocation);
        }
    }
}
