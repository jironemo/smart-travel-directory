package com.smarttravel.myanmar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImagesPreviewAdapter extends RecyclerView.Adapter<ImagesPreviewAdapter.ImageViewHolder> {
    private final List<Object> images; // Can be String (URL/base64) or Uri
    private final OnImageClickListener onImageClickListener;

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    public ImagesPreviewAdapter(List<Object> images, OnImageClickListener listener) {
        this.images = images;
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
        if (position < images.size()) {
            Object imgObj = images.get(position);
            if (imgObj instanceof Uri) {
                Glide.with(holder.imageView.getContext())
                        .load((Uri) imgObj)
                        .centerCrop()
                        .into(holder.imageView);
            } else if (imgObj instanceof String) {
                String imgStr = (String) imgObj;
                if (imgStr.startsWith("http")) {
                    Glide.with(holder.imageView.getContext())
                            .load(imgStr)
                            .centerCrop()
                            .into(holder.imageView);
                } else {
                    try {
                        if (imgStr.startsWith("data:image")) {
                            int commaIndex = imgStr.indexOf(",");
                            if (commaIndex != -1) imgStr = imgStr.substring(commaIndex + 1);
                        }
                        byte[] imageBytes = Base64.decode(imgStr, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        holder.imageView.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
            }
            holder.imageView.setOnClickListener(v -> {
                if (onImageClickListener != null) {
                    onImageClickListener.onImageClick(position);
                }
            });
            holder.imageView.setOnLongClickListener(v -> {
                images.remove(position);
                notifyDataSetChanged();
                Toast.makeText(holder.imageView.getContext(), "Image removed", Toast.LENGTH_SHORT).show();
                return true;
            });
        } else {
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
        return images.size() < 3 ? images.size() + 1 : 3;
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
