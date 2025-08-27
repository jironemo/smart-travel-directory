package com.smarttravel.myanmar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private DestinationAdapter adapter;
    private List<Destination> destinations;
    private List<Destination> filteredDestinations;

    private EditText searchEditText;
    private Spinner divisionSpinner;
    private ChipGroup categoryChipGroup;
    private TextView noResultsTextView;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;

    private String selectedLocation = "";
    private String selectedCategory = "";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        db = FirebaseFirestore.getInstance();
        initViews();
        setupRecyclerView();
        setupSearch();
        setupFilters();
        loadDestinations();
        setupBottomNavigation();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        divisionSpinner = findViewById(R.id.divisionSpinner);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
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

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                filterDestinations();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        List<String> divisions = new ArrayList<>();
        divisions.add("All Divisions");
        db.collection("destinations").get()
            .addOnSuccessListener(snaps -> {
                for (QueryDocumentSnapshot document : snaps) {
                    String location = document.getString("location");
                    if (location != null && !divisions.contains(location)) {
                        divisions.add(location);
                    }
                }
                ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, divisions);
                divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                divisionSpinner.setAdapter(divisionAdapter);
            })
            .addOnFailureListener(e -> {
                android.widget.Toast.makeText(this, "Failed to load divisions.", android.widget.Toast.LENGTH_LONG).show();
                android.util.Log.e("SearchActivity", "Error fetching divisions", e);
            });

        divisionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLocation = position == 0 ? "" : (String) parent.getItemAtPosition(position);
                filterDestinations();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        categoryChipGroup.setSingleSelection(true);
        String[] categories = {"Temple", "Pagoda", "Nature", "Cultural", "Adventure", "Food", "Shopping"};
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                Chip clicked = (Chip) v;
                selectedCategory = clicked.isChecked() ? clicked.getText().toString() : "";
                filterDestinations();
            });
            categoryChipGroup.addView(chip);
        }
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
                            destinations.add(destination);
                        }
                        filterDestinations();
                    } else {
                        android.widget.Toast.makeText(this, "Failed to load destinations.", android.widget.Toast.LENGTH_LONG).show();
                        android.util.Log.e("SearchActivity", "Error fetching destinations", task.getException());
                    }
                });
    }

    private void filterDestinations() {
        filteredDestinations.clear();
        for (Destination d : destinations) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                    d.getName().toLowerCase().contains(searchQuery) ||
                    d.getDescription().toLowerCase().contains(searchQuery);
            boolean matchesLocation = selectedLocation.isEmpty() || d.getLocation().equals(selectedLocation);
            boolean matchesCategory = selectedCategory.isEmpty() || d.getCategory().equals(selectedCategory);
            if (matchesSearch && matchesLocation && matchesCategory) {
                filteredDestinations.add(d);
            }
        }
        if (filteredDestinations.isEmpty()) {
            noResultsTextView.setText("No results found. Try adjusting your search or filters.");
            noResultsTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            noResultsTextView.setVisibility(View.VISIBLE);
        } else {
            noResultsTextView.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
        // Remove focus from search box if no results
        if (filteredDestinations.isEmpty()) {
            if (searchEditText != null) {
                searchEditText.clearFocus();
            }
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
                return true;
            } else if (id == R.id.nav_trip_advice) {
                Intent intent = new Intent(this, TripAdviceActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_favorites) {
                Intent intent = new Intent(this, FavouritesActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
        bottomNav.getMenu().findItem(R.id.nav_search).setChecked(true);
    }
}
