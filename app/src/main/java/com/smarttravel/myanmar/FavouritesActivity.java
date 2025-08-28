package com.smarttravel.myanmar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView favRecyclerView;
    private DestinationAdapter favAdapter;
    private List<Destination> favouriteDestinations;
    private TextView emptyText;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout loginPromptContainer;
    private EditText favSearchBar;
    private List<Destination> filteredDestinations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        db = FirebaseFirestore.getInstance();
        initViews();
        setupBottomNavigation();
        setupRecycler();
        bindLoginPrompt();
        loadFavourites();
    }

    private void initViews() {
        favRecyclerView = findViewById(R.id.favRecyclerView);
        emptyText = findViewById(R.id.emptyFavouritesText);
        swipeRefreshLayout = findViewById(R.id.favSwipeRefresh);
        loginPromptContainer = findViewById(R.id.loginPromptContainer);
        favSearchBar = findViewById(R.id.favSearchBar);
        filteredDestinations = new ArrayList<>();
        favSearchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFavourites(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadFavourites();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupRecycler() {
        favouriteDestinations = new ArrayList<>();
        filteredDestinations = new ArrayList<>();
        favAdapter = new DestinationAdapter(this, filteredDestinations);
        favRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favRecyclerView.setAdapter(favAdapter);
    }

    private void bindLoginPrompt() {
        Button btnLogin = findViewById(R.id.btnLoginFromFav);
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                Intent intent = new Intent(FavouritesActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        }
    }

    private void filterFavourites(String query) {
        filteredDestinations.clear();
        if (query.isEmpty()) {
            filteredDestinations.addAll(favouriteDestinations);
        } else {
            String lower = query.toLowerCase();
            for (Destination d : favouriteDestinations) {
                if (d.getName().toLowerCase().contains(lower) ||
                    (d.getLocation() != null && d.getLocation().toLowerCase().contains(lower)) ||
                    (d.getCategory() != null && d.getCategory().toLowerCase().contains(lower))) {
                    filteredDestinations.add(d);
                }
            }
        }
        favAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void loadFavourites() {
        User current = User.getCurrentUser();
        if (current == null || current.getId() == null || current.getId().isEmpty()) {
            // Show login prompt
            loginPromptContainer.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
            return;
        }
        // Logged in
        loginPromptContainer.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        favouriteDestinations.clear();
        filteredDestinations.clear();
        favAdapter.notifyDataSetChanged();

        // Query favourites collection by user reference
        CollectionReference favouritesCol = db.collection("favourites");
        DocumentReference userRef = db.collection("users").document(current.getId());
        favouritesCol
                .whereEqualTo("user_id", userRef)
                .get()
                .addOnSuccessListener(snaps -> {
                    if (snaps.isEmpty()) {
                        updateEmptyState();
                        return;
                    }
                    final int[] remaining = {snaps.size()};
                    for (QueryDocumentSnapshot favDoc : snaps) {
                        Object destField = favDoc.get("destination_id");
                        if (destField instanceof DocumentReference) {
                            ((DocumentReference) destField).get().addOnSuccessListener(destSnap -> {
                                if (destSnap.exists()) {
                                    Destination d = destSnap.toObject(Destination.class);
                                    if (d != null) {
                                        d.setId(destSnap.getId());
                                        favouriteDestinations.add(d);
                                    }
                                }
                                if (--remaining[0] == 0) {
                                    filterFavourites(favSearchBar.getText().toString());
                                    updateEmptyState();
                                }
                            }).addOnFailureListener(e -> {
                                if (--remaining[0] == 0) {
                                    filterFavourites(favSearchBar.getText().toString());
                                    updateEmptyState();
                                }
                            });
                        } else if (destField instanceof String) {
                            // Fallback if stored as plain id string
                            String destId = (String) destField;
                            db.collection("destinations").document(destId)
                                    .get().addOnSuccessListener(destSnap -> {
                                        if (destSnap.exists()) {
                                            Destination d = destSnap.toObject(Destination.class);
                                            if (d != null) {
                                                d.setId(destSnap.getId());
                                                favouriteDestinations.add(d);
                                            }
                                        }
                                        if (--remaining[0] == 0) {
                                            filterFavourites(favSearchBar.getText().toString());
                                            updateEmptyState();
                                        }
                                    }).addOnFailureListener(e -> {
                                        if (--remaining[0] == 0) {
                                            filterFavourites(favSearchBar.getText().toString());
                                            updateEmptyState();
                                        }
                                    });
                        } else {
                            // Unknown format; just decrement
                            if (--remaining[0] == 0) {
                                filterFavourites(favSearchBar.getText().toString());
                                updateEmptyState();
                            }
                        }
                    }
                });
    }

    private void updateEmptyState() {
        if (favouriteDestinations.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            favRecyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            favRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_search) {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_favorites) {
                // Already on favourites
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            }
            return false;
        });
        bottomNav.getMenu().findItem(R.id.nav_favorites).setChecked(true);
    }
}
