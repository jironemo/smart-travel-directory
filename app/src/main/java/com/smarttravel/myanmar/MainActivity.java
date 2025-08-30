// MainActivity.java
package com.smarttravel.myanmar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private DestinationAdapter adapter;
    private List<Destination> destinations;
    private List<Destination> filteredDestinations;

    private TextView noResultsTextView;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Keep current launcher/login flow
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        initViews();
        setupRecyclerView();
        loadDestinations();
        setupBottomNavigation();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        noResultsTextView = findViewById(R.id.noResultsTextView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadDestinations();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupRecyclerView() {
        destinations = new ArrayList<>();
        filteredDestinations = new ArrayList<>();
        adapter = new DestinationAdapter(this, filteredDestinations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadDestinations() {
        db.collection("destinations")
                .orderBy("name")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        destinations.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Destination destination = document.toObject(Destination.class);
                            destination.setId(document.getId());
                            // Log imageUrl size (imageUrl is an array)
                            if (destination.getImageUrl() != null) {
                                Log.d("MainActivity", "imageUrl size: " + destination.getImageUrl().size());
                            }
                            destinations.add(destination);
                        }
                        filterDestinations();
                    } else {
                        Toast.makeText(this, "Error loading destinations", Toast.LENGTH_SHORT).show();
                        if (task.getException() != null) task.getException().printStackTrace();
                    }
                });
    }

    private void filterDestinations() {
        // No search/filters on Home: show all
        filteredDestinations.clear();
        filteredDestinations.addAll(destinations);
        adapter.notifyDataSetChanged();
        noResultsTextView.setVisibility(filteredDestinations.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (id == R.id.nav_favorites) {
                startActivity(new Intent(this, FavouritesActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
        bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
    }
}
