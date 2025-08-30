package com.smarttravel.myanmar;

import static com.smarttravel.myanmar.DestinationAdapter.disableSslVerification;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DestinationDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String destinationId;
    private boolean isFavourite = false;
    private String favouriteDocId = null;
    private MaterialButton favouriteButton;

    private RecyclerView reviewsRecyclerView;
    private ReviewsAdapter reviewsAdapter;
    private final List<ReviewDisplay> reviewDisplayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_detail);
        // Set up the toolbar as the ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        db = FirebaseFirestore.getInstance();
        destinationId = getIntent().getStringExtra("destination_id");



        Log.d("DESID", "Destination ID: " + destinationId);
        loadDestinationDetails(destinationId);
        // Now use this destinationId to fetch or load data on this page

        // Favourite button setup
        favouriteButton = findViewById(R.id.btn_favourite);
        checkFavouriteState();

        favouriteButton.setOnClickListener(v -> {
            if (isFavourite) {
                removeFromFavourites();
            } else {
                addToFavourites();
            }
        });

        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        reviewsAdapter = new ReviewsAdapter(reviewDisplayList);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewsAdapter);
        loadReviews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadDestinationDetails(String destinationId) {
        // Fetch the destination details from Firestore using the destinationId
        // and update the UI accordingly
        Log.d("DESID", "Destination ID: " + destinationId);
        db.collection("destinations").document(destinationId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Destination d = documentSnapshot.toObject(Destination.class);
                    if (d != null) {
                        updateUI(d);
                    } else {
                        android.widget.Toast.makeText(this, "Destination not found.", android.widget.Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DESID", "Error fetching destination", e);
                    android.widget.Toast.makeText(this, "Failed to load destination.", android.widget.Toast.LENGTH_LONG).show();
                    finish();
                });
    }


    private void updateUI(Destination destination) {
        // Find views
        TextView nameTextView = findViewById(R.id.detailNameTextView);
        TextView descriptionTextView = findViewById(R.id.detailDescriptionTextView);
        Chip categoryTextView = findViewById(R.id.detailCategoryChip);
        TextView locationTextView = findViewById(R.id.detailLocationTextView);
        TextView ratingTextView = findViewById(R.id.detailRatingTextView);
        RecyclerView imagesRecyclerView = findViewById(R.id.detailImagesRecyclerView);
        LinearLayout reviewInputContainer = findViewById(R.id.reviewInputContainer);
        final Button submitReviewButton;
        final EditText reviewCommentEditText;
        final android.widget.RatingBar reviewRatingBar;
        TextView addressTextView = findViewById(R.id.detailAddressTextView);
        TextView contactTextView = findViewById(R.id.detailContactTextView);
        TextView additionalInfoTextView = findViewById(R.id.detailAdditionalInfoTextView);
        if (User.getCurrentUser() != null) {
            submitReviewButton = findViewById(R.id.submitReviewButton);
            reviewCommentEditText = findViewById(R.id.reviewCommentEditText);
            reviewRatingBar = findViewById(R.id.reviewRatingBar);
        } else {
            submitReviewButton = null;
            reviewCommentEditText = null;
            reviewRatingBar = null;
        }

        // Set destination info
        nameTextView.setText(destination.getName());
        descriptionTextView.setText(destination.getDescription());
        categoryTextView.setText(destination.getCategory());
        locationTextView.setText(destination.getLocationName());
        ratingTextView.setText(String.format(Locale.getDefault(), "%.1f", destination.getRating()));
        descriptionTextView.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);
        descriptionTextView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        addressTextView.setText(destination.getAddress() != null ? destination.getAddress() : "No address provided");
        contactTextView.setText(destination.getContact() != null ? destination.getContact() : "No contact provided");
        additionalInfoTextView.setText(destination.getAdditionalInformation() != null ? destination.getAdditionalInformation() : "No additional info");

        // Handle images (base64 or placeholder)
        List<Bitmap> imageBitmaps = new ArrayList<>();
        List<String> base64Images = destination.getImageUrl();
        if (base64Images != null) {
            for (String base64 : base64Images) {
                if (base64 != null && !base64.isEmpty()) {
                    try {
                        if (base64.startsWith("data:image")) {
                            int commaIndex = base64.indexOf(",");
                            if (commaIndex != -1) base64 = base64.substring(commaIndex + 1);
                        }
                        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imageBitmaps.add(bitmap);
                    } catch (Exception e) {
                        Log.e("ImageDecode", "Failed to decode base64 image", e);
                    }
                }
            }
        }
        if (imageBitmaps.isEmpty()) {
            imageBitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_image));
        }
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(new ImagesCarouselAdapter(imageBitmaps));
        // Add PagerSnapHelper for snapping
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(imagesRecyclerView);

        // Hide/show UI elements based on destination data
        locationTextView.setVisibility((destination.getLocationName() == null || destination.getLocationName().isEmpty()) ? View.GONE : View.VISIBLE);
        categoryTextView.setVisibility((destination.getCategory() == null || destination.getCategory().isEmpty()) ? View.GONE : View.VISIBLE);
        ratingTextView.setVisibility((destination.getRating() <= 0) ? View.GONE : View.VISIBLE);
        if (destination.getCategory() != null && !destination.getCategory().isEmpty()) {
            categoryTextView.setText(destination.getCategory());
        }
        if (destination.getRating() > 0) {
            ratingTextView.setText(String.format(Locale.getDefault(), "%.1f", destination.getRating()));
        }

        // Favourite button visibility
        favouriteButton.setVisibility(User.getCurrentUser() == null ? View.GONE : View.VISIBLE);

        // Review input visibility and logic
        if (User.getCurrentUser() != null) {
            reviewInputContainer.setVisibility(View.VISIBLE);
            if (submitReviewButton != null && reviewCommentEditText != null && reviewRatingBar != null) {
                submitReviewButton.setOnClickListener(v -> {
                    final String comment = reviewCommentEditText.getText() != null ? reviewCommentEditText.getText().toString().trim() : "";
                    final float rating = reviewRatingBar.getRating();
                    if (comment.isEmpty() || rating == 0) {
                        android.widget.Toast.makeText(this, "Please enter a comment and rating.", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    HashMap<String, Object> reviewData = new HashMap<>();
                    reviewData.put("comment", comment);
                    reviewData.put("created_at", com.google.firebase.Timestamp.now());
                    reviewData.put("destination_id", db.collection("destinations").document(destinationId));
                    reviewData.put("rating", rating);
                    reviewData.put("updated_at", com.google.firebase.Timestamp.now());
                    String userId = User.getCurrentUser().getId();
                    if (userId == null || userId.isEmpty()) {
                        userId = User.getCurrentUser().getEmail();
                        if (userId == null || userId.isEmpty()) {
                            android.widget.Toast.makeText(this, "User ID not found.", android.widget.Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    reviewData.put("user_id", db.collection("users").document(userId));
                    db.collection("review").add(reviewData)
                        .addOnSuccessListener(docRef -> {
                            reviewCommentEditText.setText("");
                            reviewRatingBar.setRating(0);
                            loadReviews();
                            android.widget.Toast.makeText(this, "Review submitted.", android.widget.Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            android.widget.Toast.makeText(this, "Failed to submit review.", android.widget.Toast.LENGTH_LONG).show();
                            Log.e("ReviewSubmit", "Error submitting review", e);
                        });
                });
            }
        } else {
            reviewInputContainer.setVisibility(View.GONE);
        }

        // Debug logging
        if (base64Images != null) {
            Log.d("DestinationDetailActivity", "imageUrl length: " + base64Images.size());
            if (!base64Images.isEmpty()) {
                Log.d("DestinationDetailActivity", "First image string: " + base64Images.get(0));
            }
        }
    }

    private void checkFavouriteState() {
        DocumentReference userRef = null;
        if (User.getCurrentUser() != null && User.getCurrentUser().getId() != null) {
            userRef = db.collection("users").document(User.getCurrentUser().getId());
        }
        DocumentReference destinationRef = db.collection("destinations").document(destinationId);
        db.collection("favourites")
                .whereEqualTo("destination_id", destinationRef)
                .whereEqualTo("user_id", userRef)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        isFavourite = true;
                        favouriteDocId = query.getDocuments().get(0).getId();
                        updateFavouriteButton();
                    } else {
                        isFavourite = false;
                        favouriteDocId = null;
                        updateFavouriteButton();
                    }
                });
    }

    private void updateFavouriteButton() {
        if (isFavourite) {
            favouriteButton.setText("");
            favouriteButton.setIconResource(R.drawable.ic_favorite_nav);
        } else {
            favouriteButton.setText("");
            favouriteButton.setIconResource(R.drawable.ic_favorite_border);
        }
    }

    private void addToFavourites() {
        DocumentReference userRef = null;
        if (User.getCurrentUser() != null && User.getCurrentUser().getId() != null) {
            userRef = db.collection("users").document(User.getCurrentUser().getId());
        }
        DocumentReference destinationRef = db.collection("destinations").document(destinationId);
        HashMap<String, Object> fav = new HashMap<>();
        fav.put("destination_id", destinationRef);
        fav.put("user_id", userRef);
        db.collection("favourites").add(fav)
                .addOnSuccessListener(docRef -> {
                    isFavourite = true;
                    favouriteDocId = docRef.getId();
                    updateFavouriteButton();
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(this, "Failed to add to favourites.", android.widget.Toast.LENGTH_LONG).show();
                    Log.e("Favourites", "Error adding favourite", e);
                });
    }

    private void removeFromFavourites() {
        if (favouriteDocId != null) {
            db.collection("favourites").document(favouriteDocId).delete()
                    .addOnSuccessListener(aVoid -> {
                        isFavourite = false;
                        favouriteDocId = null;
                        updateFavouriteButton();
                    })
                    .addOnFailureListener(e -> {
                        android.widget.Toast.makeText(this, "Failed to remove from favourites.", android.widget.Toast.LENGTH_LONG).show();
                        Log.e("Favourites", "Error removing favourite", e);
                    });
        }
    }

    private void loadReviews() {
        Log.d("REVIEWS", "Loading reviews for destination ID: " + destinationId);
        DocumentReference destinationRef = db.collection("destinations").document(destinationId);

        db.collection("review")
                .whereEqualTo("destination_id", destinationRef)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                             if (task.isSuccessful()) {
                                 QuerySnapshot querySnapshot = task.getResult();
                                 System.out.println("Fetched reviews count: " + querySnapshot.size());
                                 reviewDisplayList.clear();
                                 List<ReviewDisplay> tempList = new ArrayList<>();
                                 int totalReviews = querySnapshot.size();
                                 if (totalReviews == 0) {
                                     reviewsAdapter.notifyDataSetChanged();
                                     return;
                                 }
                                 final int[] loadedCount = {0};
                                 for (QueryDocumentSnapshot doc : querySnapshot) {
                                     Review review = doc.toObject(Review.class);
                                     boolean show = doc.contains("show") ? doc.getBoolean("show") : true;
                                     if (show) { // Only show reviews with show==true or missing
                                         String userId = review.getUser_id().getId();
                                         String destinationName = "Unknown";
                                         String reviewId = doc.getId();
                                         db.collection("users").document(userId).get()
                                             .addOnSuccessListener(userDoc -> {
                                                 String username = userDoc.exists() ? userDoc.getString("username") : "Unknown";
                                                 // Fetch destination name
                                                 review.getDestination_id().get().addOnSuccessListener(destDoc -> {
                                                     String destName = destDoc.exists() ? destDoc.getString("name") : destinationName;
                                                     tempList.add(new ReviewDisplay(review, username, destName, show, reviewId));
                                                     loadedCount[0]++;
                                                     if (loadedCount[0] == totalReviews) {
                                                         reviewDisplayList.clear();
                                                         reviewDisplayList.addAll(tempList);
                                                         reviewsAdapter.notifyDataSetChanged();
                                                     }
                                                 });
                                             });
                                     } else {
                                         loadedCount[0]++;
                                         if (loadedCount[0] == totalReviews) {
                                             reviewDisplayList.clear();
                                             reviewDisplayList.addAll(tempList);
                                             reviewsAdapter.notifyDataSetChanged();
                                         }
                                     }
                                 }
                             }
                         });
    }

}
