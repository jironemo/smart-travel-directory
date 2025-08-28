package com.smarttravel.myanmar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReviewsListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.reviewsListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<ReviewDisplay> reviews = new ArrayList<>();
        AdminReviewAdapter adapter = new AdminReviewAdapter(reviews);
        recyclerView.setAdapter(adapter);
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("review")
            .orderBy("created_at", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                reviews.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Review review = document.toObject(Review.class);
                    boolean show = document.contains("show") ? document.getBoolean("show") : true;
                    String reviewId = document.getId();
                    String userId = review.getUser_id().getId();
                    String destinationName = "Unknown";
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener(userDoc -> {
                            String username = userDoc.exists() ? userDoc.getString("username") : "Unknown";
                            // Fetch destination name
                            review.getDestination_id().get().addOnSuccessListener(destDoc -> {
                                if (destDoc.exists()) {
                                    String destName = destDoc.getString("name");
                                    // Add all reviews for admin, regardless of show flag
                                    reviews.add(new ReviewDisplay(review, username, destName, show, reviewId));
                                } else {
                                    reviews.add(new ReviewDisplay(review, username, destinationName, show, reviewId));
                                }
                                // Notify adapter after each review is added
                                adapter.notifyDataSetChanged();
                            });
                        });
                }
            })
            .addOnFailureListener(e -> {
                android.widget.Toast.makeText(getContext(), "Failed to load reviews.", android.widget.Toast.LENGTH_LONG).show();
                android.util.Log.e("ReviewsListFragment", "Error fetching reviews", e);
            });
        return view;
    }
}
