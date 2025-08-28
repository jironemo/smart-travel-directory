package com.smarttravel.myanmar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TripDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // Get trip_advice id from intent
        String tripAdviceId = getIntent().getStringExtra("trip_advice_id");
        if (tripAdviceId == null) {
            android.widget.Toast.makeText(this, "Trip details not found.", android.widget.Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set up toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Bind trip info to card after fetching from Firestore
        TextView locationText = findViewById(R.id.tripDetailsLocationText);
        TextView costText = findViewById(R.id.tripDetailsCostText);
        TextView foodText = findViewById(R.id.tripDetailsFoodText);
        TextView accommodationText = findViewById(R.id.tripDetailsAccommodationText);
        RecyclerView destinationsRecycler = findViewById(R.id.tripDetailsDestinationsRecycler);
        destinationsRecycler.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView bestBitesRecycler = findViewById(R.id.tripDetailsBestBitesRecycler);
        bestBitesRecycler.setLayoutManager(new LinearLayoutManager(this));

        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("trip_advice").document(tripAdviceId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String location = documentSnapshot.getString("location");
                    String estimatedCost = "";
                    Object costObj = documentSnapshot.get("estimated_cost");
                    if (costObj != null) estimatedCost = costObj.toString();
                    locationText.setText(location != null ? location : "");
                    costText.setText(estimatedCost);
                    String food = documentSnapshot.getString("food");
                    if (food != null && !food.isEmpty()) {
                        foodText.setText("Food: " + food);
                        foodText.setVisibility(View.VISIBLE);
                    } else {
                        foodText.setVisibility(View.GONE);
                    }
                    accommodationText.setVisibility(View.GONE); // Hide if not used
                    List<com.google.firebase.firestore.DocumentReference> destinationRefs = (List<com.google.firebase.firestore.DocumentReference>) documentSnapshot.get("destinations");
                    // Collect destination IDs from trip advice
                    List<String> tripAdviceDestinationIds = new ArrayList<>();
                    if (destinationRefs != null) {
                        for (com.google.firebase.firestore.DocumentReference ref : destinationRefs) {
                            tripAdviceDestinationIds.add(ref.getId());
                        }
                    }
                    // Fetch all destinations and split by category
                    db.collection("destinations").get()
                        .addOnSuccessListener(querySnapshot -> {
                            List<Destination> allDestinations = new ArrayList<>();
                            List<Destination> bestBitesList = new ArrayList<>();
                            List<Destination> otherDestList = new ArrayList<>();
                            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                                Destination d = doc.toObject(Destination.class);
                                d.setId(doc.getId());
                                // Only add if part of trip advice
                                if (tripAdviceDestinationIds.contains(d.getId())) {
                                    d.setSelected(true);
                                    String category = d.getCategory();
                                    if ("Local Best Bites".equalsIgnoreCase(category)) {
                                        bestBitesList.add(d);
                                    } else {
                                        otherDestList.add(d);
                                    }
                                }
                            }
                            bestBitesRecycler.setAdapter(new TripDestinationsAdapter(bestBitesList));
                            destinationsRecycler.setAdapter(new TripDestinationsAdapter(otherDestList));
                        })
                        .addOnFailureListener(e -> {
                            android.util.Log.e("TripDetails", "Error loading all destinations", e);
                        });
                } else {
                    android.widget.Toast.makeText(this, "Trip details not found.", android.widget.Toast.LENGTH_LONG).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                android.widget.Toast.makeText(this, "Failed to load trip details.", android.widget.Toast.LENGTH_LONG).show();
                android.util.Log.e("TripDetails", "Error fetching trip advice", e);
                finish();
            });
        // Bottom navigation
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new android.content.Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new android.content.Intent(this, SearchActivity.class));
                return true;
            } else if (id == R.id.nav_favorites) {
                startActivity(new android.content.Intent(this, FavouritesActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new android.content.Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
