package com.smarttravel.myanmar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {
    private final List<ReviewDisplay> reviews;

    public ReviewsAdapter(List<ReviewDisplay> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewDisplay reviewDisplay = reviews.get(position);
        Review review = reviewDisplay.getReview();
        holder.usernameTextView.setText(reviewDisplay.getUsername());
        holder.commentTextView.setText(review.getComment());
        holder.dateTextView.setText(formatTimestamp(review.getCreated_at()));
        setStars(holder, review.getRating());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    private void setStars(ReviewViewHolder holder, double rating) {
        int fullStars = (int) rating;
        for (int i = 0; i < 5; i++) {
            if (i < fullStars) {
                holder.stars[i].setImageResource(R.drawable.ic_star);
            } else {
                holder.stars[i].setImageResource(R.drawable.ic_star_border);
            }
        }
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, commentTextView, dateTextView;
        ImageView[] stars = new ImageView[5];

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.reviewUsernameTextView);
            commentTextView = itemView.findViewById(R.id.reviewCommentTextView);
            dateTextView = itemView.findViewById(R.id.reviewDateTextView);
            stars[0] = itemView.findViewById(R.id.reviewStar1);
            stars[1] = itemView.findViewById(R.id.reviewStar2);
            stars[2] = itemView.findViewById(R.id.reviewStar3);
            stars[3] = itemView.findViewById(R.id.reviewStar4);
            stars[4] = itemView.findViewById(R.id.reviewStar5);
        }
    }
}

