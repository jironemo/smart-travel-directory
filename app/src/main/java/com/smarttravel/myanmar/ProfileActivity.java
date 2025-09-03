package com.smarttravel.myanmar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentReference;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImageView;
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNavigation();
        user = User.getCurrentUser();
        // Get user info from User.currentUser
        String email = user != null ? user.getEmail() : null;
        String username = user != null ? user.getUsername() : null;
        String profilePicture = user != null ? user.getProfile_picture() : null;
        String createdAt = user != null ? user.getCreatedAtString() : null;
        // Set user info to views
        TextView nameTextView = findViewById(R.id.profileNameTextView);
        TextView emailTextView = findViewById(R.id.profileEmailTextView);
        profileImageView = findViewById(R.id.profileImageView);
        TextView createdAtTextView = findViewById(R.id.profileCreatedAtTextView);
        nameTextView.setText(username != null && !username.isEmpty() ? username : "Guest User");
        nameTextView.setPadding(0, 16, 0, 16);
        emailTextView.setText(email != null && !email.isEmpty() ? email : "guest@example.com");
        emailTextView.setPadding(0, 8, 0, 16);
        // Format joined date as "Member since m yyyy" using Timestamp directly
        if (user != null && user.getCreated_at() != null) {
            java.util.Date date = user.getCreated_at().toDate();
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault());
            String formattedDate = outputFormat.format(date);
            createdAtTextView.setText("Member since " + formattedDate);
        } else {
            createdAtTextView.setText("Member since");
        }
        createdAtTextView.setPadding(0, 8, 0, 16);
        // Load profile image from base64 or URL
        if (profilePicture != null && !profilePicture.isEmpty()) {
            if (profilePicture.startsWith("/9j") || profilePicture.startsWith("iVBOR")) { // base64 JPEG/PNG
                try {
                    byte[] imageBytes = android.util.Base64.decode(profilePicture, android.util.Base64.DEFAULT);
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    profileImageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    profileImageView.setImageResource(R.drawable.ic_profile);
                }
            } else {
                Glide.with(this)
                        .load(profilePicture)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(profileImageView);
            }
        } else {
            profileImageView.setImageResource(R.drawable.ic_profile);
        }

        // Remove old count TextViews and add cards for counts
        LinearLayout profileContainer = findViewById(R.id.profileContainer);
        profileContainer.removeView(findViewById(R.id.profileReviewsCountTextView));
        profileContainer.removeView(findViewById(R.id.profileFavouritesCountTextView));
        // Add cards for counts in a single row, center-aligned
        LinearLayout countsRow = new LinearLayout(this);
        countsRow.setOrientation(LinearLayout.HORIZONTAL);
        countsRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        countsRow.setGravity(android.view.Gravity.CENTER);
        countsRow.setPadding(0, 0, 0, 0);
        // Review Card
        LinearLayout reviewCard = new LinearLayout(this);
        reviewCard.setOrientation(LinearLayout.VERTICAL);
        reviewCard.setBackgroundResource(R.drawable.card_profile_border);
        reviewCard.setPadding(32, 32, 32, 32);
        LinearLayout.LayoutParams reviewParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        reviewParams.setMarginEnd(24); // Add space between cards
        reviewCard.setLayoutParams(reviewParams);
        reviewCard.setGravity(android.view.Gravity.CENTER);
        reviewCard.setElevation(8f);
        TextView reviewCountText = new TextView(this);
        reviewCountText.setId(R.id.profileReviewsCountTextView);
        reviewCountText.setTextSize(32);
        reviewCountText.setTypeface(null, android.graphics.Typeface.BOLD);
        reviewCountText.setTextColor(getResources().getColor(R.color.orange, getTheme()));
        reviewCountText.setText("0");
        reviewCountText.setGravity(android.view.Gravity.CENTER);
        ImageView reviewIcon = new ImageView(this);
        reviewIcon.setImageResource(R.drawable.ic_material_book);
        reviewIcon.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
        reviewIcon.setPadding(0, 16, 0, 0);
        reviewIcon.setColorFilter(getResources().getColor(R.color.orange, getTheme()));
        reviewIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        reviewCard.addView(reviewCountText);
        reviewCard.addView(reviewIcon);
        // Favourites Card
        LinearLayout favCard = new LinearLayout(this);
        favCard.setOrientation(LinearLayout.VERTICAL);
        favCard.setBackgroundResource(R.drawable.card_profile_border);
        favCard.setPadding(32, 32, 32, 32);
        LinearLayout.LayoutParams favParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        favParams.setMarginStart(24); // Add space between cards
        favCard.setLayoutParams(favParams);
        favCard.setGravity(android.view.Gravity.CENTER);
        favCard.setElevation(8f);
        TextView favCountText = new TextView(this);
        favCountText.setId(R.id.profileFavouritesCountTextView);
        favCountText.setTextSize(32);
        favCountText.setTypeface(null, android.graphics.Typeface.BOLD);
        favCountText.setTextColor(getResources().getColor(R.color.orange, getTheme()));
        favCountText.setText("0");
        favCountText.setGravity(android.view.Gravity.CENTER);
        ImageView favIcon = new ImageView(this);
        favIcon.setImageResource(R.drawable.ic_material_star);
        favIcon.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
        favIcon.setPadding(0, 16, 0, 0);
        favIcon.setColorFilter(getResources().getColor(R.color.orange, getTheme()));
        favIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        favCard.addView(favCountText);
        favCard.addView(favIcon);
        // Add cards to row
        countsRow.addView(reviewCard);
        countsRow.addView(favCard);
        profileContainer.addView(countsRow); // Add at the end to avoid index out of bounds

        // Add a single button at the end of the profile page
        Button actionButton = new Button(this);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.setMargins(0, 32, 0, 32);
        actionButton.setLayoutParams(btnParams);
        actionButton.setTextSize(18);
        actionButton.setTextColor(getResources().getColor(R.color.white, getTheme()));
        actionButton.setBackgroundResource(R.drawable.button_logout_modern);
        profileContainer.addView(actionButton);

        if (user == null) {
            actionButton.setText("Login");
            actionButton.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        } else {
            actionButton.setText("Logout");
            actionButton.setOnClickListener(v -> {
                try {
                    User.setCurrentUser(null);
                    SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                    editor.remove("user_id");
                    editor.apply();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    android.widget.Toast.makeText(ProfileActivity.this, "Logout failed. Please try again.", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Get review and favourite counts from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (user != null) {
            String userId = user.getId();
            DocumentReference userRef = db.collection("users").document(userId);
            // Get review count
            db.collection("review")
                    .whereEqualTo("user_id", userRef)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        reviewCountText.setText("" + queryDocumentSnapshots.size());
                    });
            // Get favourite count
            db.collection("favourites")
                    .whereEqualTo("user_id", userRef)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        favCountText.setText("" + queryDocumentSnapshots.size());
                    });
        }

        // Recent Reviews Section
        if (user != null) {
            TextView recentReviewsHeader = new TextView(this);
            recentReviewsHeader.setText("Recent Reviews");
            recentReviewsHeader.setTextSize(20);
            recentReviewsHeader.setTypeface(null, android.graphics.Typeface.BOLD);
            recentReviewsHeader.setPadding(0, 48, 0, 16);
            profileContainer.addView(recentReviewsHeader);

            LinearLayout recentReviewsList = new LinearLayout(this);
            recentReviewsList.setOrientation(LinearLayout.VERTICAL);
            profileContainer.addView(recentReviewsList);

            String userId = user.getId();
            DocumentReference userRef = db.collection("users").document(userId);
            db.collection("review")
                    .whereEqualTo("user_id", userRef)
                    .orderBy("created_at", Query.Direction.DESCENDING)
                    .limit(2)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        android.util.Log.d("ProfileRecentReviews", "Recent reviews query result count: " + queryDocumentSnapshots.size());
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            android.util.Log.d("ProfileRecentReviews", "Review doc: " + doc.getData().toString());
                        }
                        if (queryDocumentSnapshots.isEmpty()) {
                            TextView noReviewsText = new TextView(this);
                            noReviewsText.setText("No recent reviews found.");
                            noReviewsText.setTextSize(15);
                            noReviewsText.setPadding(0, 16, 0, 16);
                            noReviewsText.setGravity(android.view.Gravity.CENTER);
                            recentReviewsList.addView(noReviewsText);
                        }
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Review review = doc.toObject(Review.class);
                            boolean show = doc.contains("show") && doc.get("show") != null ? Boolean.TRUE.equals(doc.getBoolean("show")) : true;
                            String destinationName = "Unknown";
                            review.getDestination_id().get().addOnSuccessListener(destDoc -> {
                                String destName = destDoc.exists() ? destDoc.getString("name") : destinationName;
                                // Card for review
                                LinearLayout reviewCardLayout = new LinearLayout(this);
                                reviewCardLayout.setOrientation(LinearLayout.VERTICAL);
                                reviewCardLayout.setBackgroundResource(R.drawable.card_profile_border);
                                reviewCardLayout.setPadding(32, 32, 32, 32);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT);
                                params.setMargins(0, 0, 0, 32);
                                reviewCardLayout.setLayoutParams(params);
                                reviewCardLayout.setElevation(6f);

                                // Header row (just the header text)
                                LinearLayout headerRow = new LinearLayout(this);
                                headerRow.setOrientation(LinearLayout.HORIZONTAL);
                                headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
                                TextView headerText = new TextView(this);
                                headerText.setText("Your review of " + destName);
                                headerText.setTextSize(16);
                                headerText.setTypeface(null, android.graphics.Typeface.BOLD);
                                headerText.setPadding(0, 0, 0, 0);
                                headerRow.addView(headerText);
                                reviewCardLayout.addView(headerRow);

                                // Status chip row (chip below header)
                                LinearLayout chipRow = new LinearLayout(this);
                                chipRow.setOrientation(LinearLayout.HORIZONTAL);
                                chipRow.setGravity(android.view.Gravity.START);
                                com.google.android.material.chip.Chip statusChip = new com.google.android.material.chip.Chip(this);
                                statusChip.setText(show ? "Your review is available." : "Your review was hidden for guideline reasons.");
                                statusChip.setTextColor(getResources().getColor(R.color.white, getTheme()));
                                // Use built-in Android colors if custom colors are missing
                                int chipColor = show ? android.graphics.Color.parseColor("#4CAF50") : android.graphics.Color.parseColor("#F44336");
                                statusChip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(chipColor));
                                statusChip.setTextSize(14);
                                statusChip.setChipMinHeight(44);
                                // Use setShapeAppearanceModel for rounded corners (recommended)
                                com.google.android.material.shape.ShapeAppearanceModel shapeModel = statusChip.getShapeAppearanceModel().withCornerSize(18f);
                                statusChip.setShapeAppearanceModel(shapeModel);
                                statusChip.setPadding(24, 0, 24, 0);
                                statusChip.setClickable(false);
                                statusChip.setCheckable(false);
                                chipRow.addView(statusChip);
                                reviewCardLayout.addView(chipRow);

                                // Clickable to show details
                                reviewCardLayout.setOnClickListener(v -> {
                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                                    builder.setTitle("Your review of " + destName);
                                    builder.setMessage("Review: " + review.getComment() +
                                            "\n\nRating: " + String.format("%.1f", review.getRating()) +
                                            "\n\nStatus: " + (show ? "available" : "hidden by admin"));
                                    builder.setPositiveButton("OK", null);
                                    builder.show();
                                });
                                recentReviewsList.addView(reviewCardLayout);
                            });
                        }
                    });
        }

        // Show Go To Admin View button for admin users
        com.google.android.material.button.MaterialButton toggleAdminBtn = findViewById(R.id.btnToggleAdminView);
        if (user != null && "admin".equalsIgnoreCase(user.getUser_type())) {
            toggleAdminBtn.setVisibility(android.view.View.VISIBLE);
            toggleAdminBtn.setText("Go To Admin View");
            toggleAdminBtn.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
                finish();
            });
        } else {
            toggleAdminBtn.setVisibility(android.view.View.GONE);
        }

        ImageButton btnEditProfileImage = findViewById(R.id.btnEditProfileImage);
        if (user == null || (user.getUsername() == null || user.getUsername().isEmpty() || "Guest User".equals(user.getUsername()))) {
            btnEditProfileImage.setVisibility(View.GONE);
        } else {
            btnEditProfileImage.setVisibility(View.VISIBLE);
            btnEditProfileImage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), 101);
            });
        }
        // Make image oval
        profileImageView.setClipToOutline(true);
        // Show admin button if user is admin
        MaterialButton btnToggleAdminView = findViewById(R.id.btnToggleAdminView);
        if (user != null && user.getUser_type() != null && user.getUser_type().equalsIgnoreCase("admin")) {
            btnToggleAdminView.setVisibility(View.VISIBLE);
            btnToggleAdminView.setText("Go To Admin View");
            btnToggleAdminView.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
                finish();
            });
        } else {
            btnToggleAdminView.setVisibility(View.GONE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == PICK_IMAGE_REQUEST || requestCode == 101) && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                android.graphics.Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);
                // Encode bitmap to base64
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                // Save to Firestore
                if (user != null && user.getId() != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference userRef = db.collection("users").document(user.getId());
                    userRef.update("profile_picture", base64Image)
                        .addOnSuccessListener(aVoid -> {
                            user.setProfile_picture(base64Image);
                            android.widget.Toast.makeText(ProfileActivity.this, "Profile image updated!", android.widget.Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            android.widget.Toast.makeText(ProfileActivity.this, "Failed to update profile image.", android.widget.Toast.LENGTH_SHORT).show();
                        });
                }
            } catch (Exception e) {
                e.printStackTrace();
                android.widget.Toast.makeText(ProfileActivity.this, "Error processing image.", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Helper methods for toggling admin view


    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                // Clear activity stack so Home is not duplicated
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_search) {
                Intent intent = new Intent(ProfileActivity.this, SearchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_favorites) {
                Intent intent = new Intent(ProfileActivity.this, FavouritesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                // Already on profile, do nothing
                return true;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_profile);
    }
}
