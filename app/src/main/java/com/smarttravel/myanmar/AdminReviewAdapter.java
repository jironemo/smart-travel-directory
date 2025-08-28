package com.smarttravel.myanmar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.ViewHolder> {
    private final List<ReviewDisplay> reviews;

    public AdminReviewAdapter(List<ReviewDisplay> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_admin_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewDisplay reviewDisplay = reviews.get(position);
        String header = reviewDisplay.getUsername() + "'s review of " + reviewDisplay.getDestinationName();
        holder.headerTextView.setText(header);
        holder.commentTextView.setText(reviewDisplay.getReview().getComment());
        holder.ratingTextView.setText("Rating: " + String.format("%.1f", reviewDisplay.getReview().getRating()));
        holder.toggleShowButton.setText(reviewDisplay.isShow() ? "Hide" : "Show");
        holder.toggleShowButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            holder.toggleShowButton.getContext().getResources().getColor(
                reviewDisplay.isShow() ? R.color.orange : R.color.chip_background, null)));
        holder.toggleShowButton.setOnClickListener(v -> {
            boolean newShow = !reviewDisplay.isShow();
            reviewDisplay.setShow(newShow);
            holder.toggleShowButton.setText(newShow ? "Hide" : "Show");
            holder.toggleShowButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                holder.toggleShowButton.getContext().getResources().getColor(
                    newShow ? R.color.orange : R.color.chip_background, null)));
            // Update Firestore
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            String reviewDocId = reviewDisplay.getReviewDocId();
            if (reviewDocId != null) {
                db.collection("review").document(reviewDocId).update("show", newShow);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(holder.itemView.getContext());
            builder.setTitle(header);
            String statusText = reviewDisplay.isShow() ? "Visible (Show)" : "Hidden (Hide)";
            builder.setMessage("Review: " + reviewDisplay.getReview().getComment() +
                    "\n\nRating: " + String.format("%.1f", reviewDisplay.getReview().getRating()) +
                    "\n\nStatus: " + statusText);
            builder.setPositiveButton("OK", null);
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView headerTextView, commentTextView, ratingTextView;
        android.widget.Button toggleShowButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.reviewHeaderTextView);
            commentTextView = itemView.findViewById(R.id.reviewCommentTextView);
            ratingTextView = itemView.findViewById(R.id.reviewRatingTextView);
            toggleShowButton = itemView.findViewById(R.id.toggleShowButton);
        }
    }
}
