package com.smarttravel.myanmar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminDestinationAdapter extends RecyclerView.Adapter<AdminDestinationAdapter.ViewHolder> {
    public interface OnDeleteClickListener {
        void onDelete(Destination destination, int position);
    }
    public interface OnEditClickListener {
        void onEdit(Destination destination);
    }
    private Context context;
    private List<Destination> destinations;
    private OnDeleteClickListener deleteClickListener;
    private OnEditClickListener editClickListener;

    public AdminDestinationAdapter(Context context, List<Destination> destinations, OnDeleteClickListener deleteClickListener, OnEditClickListener editClickListener) {
        this.context = context;
        this.destinations = destinations;
        this.deleteClickListener = deleteClickListener;
        this.editClickListener = editClickListener;
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
        holder.tvName.setText(destination.getName());
        holder.tvCategory.setText(destination.getCategory());
        holder.tvDivision.setText(destination.getDivision());
        holder.tvDesc.setText(destination.getDescription());
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) deleteClickListener.onDelete(destination, position);
        });
        holder.btnEdit.setOnClickListener(v -> {
            if (editClickListener != null) editClickListener.onEdit(destination);
        });
    }

    @Override
    public int getItemCount() {
        return destinations.size();
    }

    public void updateList(List<Destination> newList) {
        destinations = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvDivision, tvDesc;
        ImageButton btnDelete, btnEdit;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDestinationName);
            tvCategory = itemView.findViewById(R.id.tvDestinationCategory);
            tvDivision = itemView.findViewById(R.id.tvDestinationDivision);
            tvDesc = itemView.findViewById(R.id.tvDestinationDescription);
            btnDelete = itemView.findViewById(R.id.btnDeleteDestination);
            btnEdit = itemView.findViewById(R.id.btnEditDestination);
        }
    }
}
