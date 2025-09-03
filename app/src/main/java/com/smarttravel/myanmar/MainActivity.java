// MainActivity.java
package com.smarttravel.myanmar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private RecyclerView mainRecyclerView;
    private MainDestinationsAdapter mainAdapter;
    private List<Destination> allDestinations = new ArrayList<>();
    private List<Destination> popularDestinations = new ArrayList<>();

    private TextView noResultsTextView;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;

    private static final int PAGE_SIZE = 3;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private QueryDocumentSnapshot lastVisible = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Keep current launcher/login flow
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        initViews();
        setupRecyclerView();
        loadPopularDestinations();
        loadDestinations();
        setupBottomNavigation();
    }

    private void initViews() {
        mainRecyclerView = findViewById(R.id.mainRecyclerView);
        noResultsTextView = findViewById(R.id.noResultsTextView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadDestinations();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupRecyclerView() {
        mainAdapter = new MainDestinationsAdapter(this, popularDestinations, allDestinations);
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainRecyclerView.setAdapter(mainAdapter);
        mainRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && !isLastPage && lastVisibleItem >= totalItemCount - 2) {
                    loadDestinationsPaginated();
                }
            }
        });
    }

    private void loadDestinationsPaginated() {
        isLoading = true;
        com.google.firebase.firestore.Query query = db.collection("destinations").orderBy("name").limit(PAGE_SIZE);
        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Destination> newDestinations = new ArrayList<>();
                List<QueryDocumentSnapshot> docs = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Destination destination = document.toObject(Destination.class);
                    destination.setId(document.getId());
                    newDestinations.add(destination);
                    docs.add(document);
                }
                if (!docs.isEmpty()) {
                    lastVisible = docs.get(docs.size() - 1);
                }
                if (newDestinations.size() < PAGE_SIZE) {
                    isLastPage = true;
                }
                allDestinations.addAll(newDestinations);
                mainAdapter.setAllDestinations(allDestinations);
            } else {
                Toast.makeText(this, "Error loading destinations", Toast.LENGTH_SHORT).show();
            }
            isLoading = false;
        });
    }

    private void loadDestinations() {
        // Reset for refresh
        allDestinations.clear();
        lastVisible = null;
        isLastPage = false;
        loadDestinationsPaginated();
    }

    private void loadPopularDestinations() {
        db.collection("destinations")
            .orderBy("rating", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                popularDestinations.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Destination d = doc.toObject(Destination.class);
                    d.setId(doc.getId());
                    d.setPopular(true);
                    // Log imageUrl for debugging
                    if (d.getImageUrl() != null && !d.getImageUrl().isEmpty()) {
                        Log.d("MainActivity", "Popular destination: " + d.getName() + ", imageUrl[0]: " + d.getImageUrl().get(0));
                    } else {
                        Log.w("MainActivity", "Popular destination: " + d.getName() + " has no imageUrl!");
                    }
                    popularDestinations.add(d);
                }
                mainAdapter.setPopularDestinations(popularDestinations);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load popular destinations.", Toast.LENGTH_SHORT).show();
            });
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

    @Override
    protected void onStart() {
        super.onStart();
        boolean fromAdmin = getIntent().getBooleanExtra("fromAdmin", false);
        if (!fromAdmin && User.getCurrentUser() != null && User.getCurrentUser().getUser_type() != null && User.getCurrentUser().getUser_type().equalsIgnoreCase("admin")) {
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean fromAdmin = getIntent().getBooleanExtra("fromAdmin", false);
        if (!fromAdmin && User.getCurrentUser() != null && User.getCurrentUser().getUser_type() != null && User.getCurrentUser().getUser_type().equalsIgnoreCase("admin")) {
            Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
