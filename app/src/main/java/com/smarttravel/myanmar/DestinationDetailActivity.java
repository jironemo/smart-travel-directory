package com.smarttravel.myanmar;

import static com.smarttravel.myanmar.DestinationAdapter.disableSslVerification;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class DestinationDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_detail);
        db = FirebaseFirestore.getInstance();
        //get the destination ID from the intent;
        String destinationId = getIntent().getStringExtra("destination_id");

        Log.d("DESID", "Destination ID: " + destinationId);
        loadDestinationDetails(destinationId);
        // Now use this destinationId to fetch or load data on this page
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
       TextView categoryTextView = findViewById(R.id.detailCategoryTextView);
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
