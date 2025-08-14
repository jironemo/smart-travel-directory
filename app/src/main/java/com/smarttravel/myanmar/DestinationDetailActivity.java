package com.smarttravel.myanmar;

import static com.smarttravel.myanmar.DestinationAdapter.disableSslVerification;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class DestinationDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
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
        //get the destination ID from the intent;
        String destinationId = getIntent().getStringExtra("destination_id");

        Log.d("DESID", "Destination ID: " + destinationId);
        loadDestinationDetails(destinationId);
        // Now use this destinationId to fetch or load data on this page
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
              }
          })
          .addOnFailureListener(e -> Log.e("DESID", "Error fetching destination", e));
    }


   private void updateUI(Destination destination) {
            TextView nameTextView = findViewById(R.id.detailNameTextView);

       TextView descriptionTextView = findViewById(R.id.detailDescriptionTextView);
       Chip  categoryTextView = findViewById(R.id.detailCategoryChip);
         TextView locationTextView = findViewById(R.id.detailLocationTextView);
         ImageView imageView = findViewById(R.id.detailImageView);
        TextView ratingTextView = findViewById(R.id.detailRatingTextView);


       disableSslVerification();
       // Load image with Glide
       Glide.with(this.getApplicationContext())
               .load(destination.getImageUrl())
               .placeholder(R.drawable.placeholder_image)
               .error(R.drawable.placeholder_image)
               .into(imageView);

            nameTextView.setText(destination.getName());
            descriptionTextView.setText(destination.getDescription());
            categoryTextView.setText(destination.getCategory());
            locationTextView.setText(destination.getLocation());
            ratingTextView.setText(String.format("%.1f", destination.getRating()));
    }
}
