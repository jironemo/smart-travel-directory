// MainActivity.java
package com.smarttravel.myanmar;
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

    private String selectedDivision = "";
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
        String[] divisions = {"All Divisions", "Yangon", "Mandalay", "Bagan", "Inle Lake", "Kalaw", "Hsipaw", "Mrauk U"};
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, divisions);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        divisionSpinner.setAdapter(divisionAdapter);

        divisionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDivision = position == 0 ? "" : divisions[position];
                filterDestinations();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup category chips
        String[] categories = {"Temple", "Pagoda", "Nature", "Cultural", "Adventure", "Food", "Shopping"};
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategory = category;
                    // Uncheck other chips
                    for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                        Chip otherChip = (Chip) categoryChipGroup.getChildAt(i);
                        if (otherChip != chip) {
                            otherChip.setChecked(false);
                        }
                    }
                } else {
                    selectedCategory = "";
                }
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

            boolean matchesDivision = selectedDivision.isEmpty() ||
                    destination.getDivision().equals(selectedDivision);

            boolean matchesCategory = selectedCategory.isEmpty() ||
                    destination.getCategory().equals(selectedCategory);

            if (matchesSearch && matchesDivision && matchesCategory) {
                filteredDestinations.add(destination);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
