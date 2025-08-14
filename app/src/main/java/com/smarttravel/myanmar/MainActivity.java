// MainActivity.java
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
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private DestinationAdapter adapter;
    private List<Destination> destinations;
    private List<Destination> filteredDestinations;

    private EditText searchEditText;
    private Spinner divisionSpinner;
    private ChipGroup categoryChipGroup;

    private String selectedLocation = "";
    private String selectedCategory = "";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        initViews();
        setupRecyclerView();
        setupSearch();
        setupFilters();
        loadDestinations();
        setupBottomNavigation();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        divisionSpinner = findViewById(R.id.divisionSpinner);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        recyclerView = findViewById(R.id.recyclerView);

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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                filterDestinations();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        // Setup division spinner
        List<String> divisions = new ArrayList<>();
        divisions.add("All Divisions");
        db.collection("destinations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String location = document.getString("location");
                        if (location != null && !divisions.contains(location)) {
                            divisions.add(location);
                        }
                    }
                    ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, divisions);
                    divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    divisionSpinner.setAdapter(divisionAdapter);
                });

        divisionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLocation = position == 0 ? "" : divisions.get(position);
                filterDestinations();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup category chips (single selection)
        categoryChipGroup.setSingleSelection(true);
        String[] categories = {"Temple", "Pagoda", "Nature", "Cultural", "Adventure", "Food", "Shopping"};
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            categoryChipGroup.addView(chip);
        }
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedCategory = "";
            } else {
                Chip checkedChip = group.findViewById(checkedIds.get(0));
                if (checkedChip != null) {
                    selectedCategory = checkedChip.getText().toString();
                }
            }
            filterDestinations();
        });
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
                        Toast.makeText(this, "Error loading destinations", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterDestinations() {
        filteredDestinations.clear();

        for (Destination destination : destinations) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                    destination.getName().toLowerCase().contains(searchQuery) ||
                    destination.getDescription().toLowerCase().contains(searchQuery);

            boolean matchesLocation = selectedLocation.isEmpty() ||
                    destination.getLocation().equals(selectedLocation);

            boolean matchesCategory = selectedCategory.isEmpty() ||
                    destination.getCategory().equals(selectedCategory);

            if (matchesSearch && matchesLocation && matchesCategory) {
                filteredDestinations.add(destination);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Already on home, do nothing
                return true;
            } else if (id == R.id.nav_search) {
                bottomNav.setSelectedItemId(R.id.nav_home);
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (id == R.id.nav_trip_advice) {
                bottomNav.setSelectedItemId(R.id.nav_home);
                startActivity(new Intent(this, TripAdviceActivity.class));
                return true;
            } else if (id == R.id.nav_favorites) {
                bottomNav.setSelectedItemId(R.id.nav_home);
                startActivity(new Intent(this, FavouritesActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                bottomNav.setSelectedItemId(R.id.nav_home);
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
        bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
    }
}
