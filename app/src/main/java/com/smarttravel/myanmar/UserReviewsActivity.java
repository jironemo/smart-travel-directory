package com.smarttravel.myanmar;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserReviewsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private ReviewsAdapter adapter;
    private List<ReviewDisplay> reviewDisplays;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reviews);
        recyclerView = findViewById(R.id.userReviewsRecyclerView);
        emptyTextView = findViewById(R.id.userReviewsEmptyTextView);
        reviewDisplays = new ArrayList<>();
        adapter = new ReviewsAdapter(reviewDisplays);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        String userId = getIntent().getStringExtra("user_id");
        loadReviews(userId);
    }

    private void loadReviews(String userId) {
        db.collection("reviews").whereEqualTo("userId", userId).get().addOnSuccessListener(query -> {
            reviewDisplays.clear();
            for (QueryDocumentSnapshot doc : query) {
                Review review = doc.toObject(Review.class);
                // Fetch username from user_id document reference
                com.google.firebase.firestore.DocumentReference userRef = (com.google.firebase.firestore.DocumentReference) doc.get("user_id");
                String reviewId = doc.getId();
                boolean show = doc.contains("show") ? doc.getBoolean("show") : true;
                if (show) {
                    if (userRef != null) {
                        userRef.get().addOnSuccessListener(userDoc -> {
                            String username = userDoc.getString("username");
                            ReviewDisplay display = new ReviewDisplay(review, username, "Unknown", true, reviewId);
                            reviewDisplays.add(display);
                            adapter.notifyDataSetChanged();
                            emptyTextView.setVisibility(reviewDisplays.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                        });
                    } else {
                        ReviewDisplay display = new ReviewDisplay(review, "Unknown", "Unknown", true, reviewId);
                        reviewDisplays.add(display);
                        adapter.notifyDataSetChanged();
                        emptyTextView.setVisibility(reviewDisplays.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                    }
                }
            }
            adapter.notifyDataSetChanged();
            emptyTextView.setVisibility(reviewDisplays.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        });
    }
}
