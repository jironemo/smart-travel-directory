package com.smarttravel.myanmar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TripAdviceActivity extends AppCompatActivity {
    private RecyclerView adviceRecyclerView;
    private TripAdviceAdapter adviceAdapter;
    private List<TripAdvice> adviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_advice);
        adviceRecyclerView = findViewById(R.id.adviceRecyclerView);
        adviceList = new ArrayList<>();
        adviceAdapter = new TripAdviceAdapter(adviceList, tripAdvice -> {
            // Open TripDetailsActivity for selected advice
            android.content.Intent intent = new android.content.Intent(TripAdviceActivity.this, TripDetailsActivity.class);
            intent.putExtra("trip_advice_id", tripAdvice.getId());
            startActivity(intent);
        });
        adviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adviceRecyclerView.setAdapter(adviceAdapter);
        fetchTripAdvices();
    }

    private void fetchTripAdvices() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("trip_advice").get()
            .addOnSuccessListener(querySnapshot -> {
                adviceList.clear();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    String location = doc.getString("location");
                    List<DocumentReference> destinationRefs = (List<DocumentReference>) doc.get("destinations");
                    Object estimatedCostObj = doc.get("estimated_cost");
                    String estimatedCost = estimatedCostObj != null ? estimatedCostObj.toString() : "";
                    Double rating = doc.getDouble("rating");
                    String id = doc.getId();
                    adviceList.add(new TripAdvice(id, location, destinationRefs, estimatedCost, rating));
                }
                adviceAdapter.notifyDataSetChanged();
            });
    }

    private void setupBottomNavigation() {
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
    protected void onResume() {
        super.onResume();
        setupBottomNavigation();
    }
}
