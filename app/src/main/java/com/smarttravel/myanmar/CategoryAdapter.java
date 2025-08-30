package com.smarttravel.myanmar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    public interface OnEditClickListener {
        void onEdit(Category category);
    }
    public interface OnDeleteClickListener {
        void onDelete(Category category, int position);
    }
    private List<Category> categoryList;
    private OnEditClickListener editClickListener;
    private OnDeleteClickListener deleteClickListener;

    public CategoryAdapter(List<Category> categoryList, OnEditClickListener editClickListener, OnDeleteClickListener deleteClickListener) {
        this.categoryList = categoryList;
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvName.setText(category.getName());
        holder.tvDesc.setText(category.getDescription());
        holder.btnEdit.setOnClickListener(v -> {
            if (editClickListener != null) editClickListener.onEdit(category);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) deleteClickListener.onDelete(category, position);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc;
        ImageButton btnEdit, btnDelete;
        CategoryViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvDesc = itemView.findViewById(R.id.tvCategoryDescription);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
