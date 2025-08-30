package com.smarttravel.myanmar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImagesPreviewAdapter extends RecyclerView.Adapter<ImagesPreviewAdapter.ImageViewHolder> {
    private final List<String> base64Images;
    private final OnImageClickListener onImageClickListener;

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    public ImagesPreviewAdapter(List<String> base64Images, OnImageClickListener listener) {
        this.base64Images = base64Images;
        this.onImageClickListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preview_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        if (position < base64Images.size()) {
            String base64 = base64Images.get(position);
            try {
                if (base64.startsWith("data:image")) {
                    int commaIndex = base64.indexOf(",");
                    if (commaIndex != -1) base64 = base64.substring(commaIndex + 1);
                }
                byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            holder.imageView.setOnClickListener(v -> {
                if (onImageClickListener != null) {
                    onImageClickListener.onImageClick(position);
                }
            });
            holder.imageView.setOnLongClickListener(v -> {
                // Remove image on long press
                base64Images.remove(position);
                notifyDataSetChanged();
                Toast.makeText(holder.imageView.getContext(), "Image removed", Toast.LENGTH_SHORT).show();
                return true;
            });
        } else {
            // Show + icon
            holder.imageView.setImageResource(android.R.drawable.ic_input_add);
            holder.imageView.setOnClickListener(v -> {
                if (onImageClickListener != null) {
                    onImageClickListener.onImageClick(position);
                }
            });
            holder.imageView.setOnLongClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        // Show + icon if less than 3 images
        return base64Images.size() < 3 ? base64Images.size() + 1 : 3;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.previewImageView);
            if (imageView == null) {
                imageView = itemView.findViewById(R.id.addImageIcon);
            }
        }
    }
}
