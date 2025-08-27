package com.smarttravel.myanmar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTripAdviceActivity extends AppCompatActivity {
    private Spinner locationSpinner;
    private LinearLayout categorySectionsContainer;
    private Button saveTripAdviceButton;
    private TextView estimatedCostEditText;
    private List<String> locations = new ArrayList<>();
    private Map<String, List<Destination>> categoryDestinations = new HashMap<>();
    private Map<String, List<Destination>> selectedDestinations = new HashMap<>();
    private String selectedLocation = "";
    private String[] categories = {"Temple", "Pagoda", "Nature", "Cultural", "Adventure", "Food", "Shopping", "Local Best Bites"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip_advice);
        locationSpinner = findViewById(R.id.addLocationSpinner);
        categorySectionsContainer = findViewById(R.id.addCategorySectionsContainer);
        saveTripAdviceButton = findViewById(R.id.addSaveTripAdviceButton);
        estimatedCostEditText = findViewById(R.id.addEstimatedCostEditText);
        fetchLocations();
        saveTripAdviceButton.setOnClickListener(v -> saveTripAdvice());
    }

    private void fetchLocations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("destinations").get()
            .addOnSuccessListener(snaps -> {
                locations.clear();
                for (QueryDocumentSnapshot doc : snaps) {
                    String loc = doc.getString("location");
                    if (loc != null && !locations.contains(loc)) {
                        locations.add(loc);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locations);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                locationSpinner.setAdapter(adapter);
                locationSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        selectedLocation = locations.get(position);
                        fetchDestinationsByCategory(selectedLocation);
                    }
                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
            })
            .addOnFailureListener(e -> {
                android.widget.Toast.makeText(this, "Failed to load locations.", android.widget.Toast.LENGTH_LONG).show();
            });
    }

    private void fetchDestinationsByCategory(String location) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("destinations").whereEqualTo("location", location).get()
            .addOnSuccessListener(snaps -> {
                categoryDestinations.clear();
                for (String cat : categories) categoryDestinations.put(cat, new ArrayList<>());
                for (QueryDocumentSnapshot doc : snaps) {
                    String cat = doc.getString("category");
                    if (cat != null && categoryDestinations.containsKey(cat)) {
                        Destination d = doc.toObject(Destination.class);
                        d.setId(doc.getId());
                        categoryDestinations.get(cat).add(d);
                    }
                }
                renderCategorySections();
            })
            .addOnFailureListener(e -> {
                android.widget.Toast.makeText(this, "Failed to load destinations.", android.widget.Toast.LENGTH_LONG).show();
            });
    }

    private void renderCategorySections() {
        categorySectionsContainer.removeAllViews();
        selectedDestinations.clear();
        for (String cat : categories) {
            List<Destination> dests = categoryDestinations.get(cat);
            if (dests == null || dests.isEmpty()) continue;
            TextView header = new TextView(this);
            header.setText(cat);
            header.setTextSize(16f);
            header.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.primary));
            header.setPadding(0, 16, 0, 8);
            categorySectionsContainer.addView(header);
            LinearLayout section = new LinearLayout(this);
            section.setOrientation(LinearLayout.VERTICAL);
            for (Destination d : dests) {
                CheckBox cb = new CheckBox(this);
                cb.setText(d.getName());
                cb.setTag(d);
                cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (!selectedDestinations.containsKey(cat)) selectedDestinations.put(cat, new ArrayList<>());
                        if (selectedDestinations.get(cat) != null) selectedDestinations.get(cat).add(d);
                    } else {
                        if (selectedDestinations.get(cat) != null) selectedDestinations.get(cat).remove(d);
                    }
                });
                section.addView(cb);
            }
            categorySectionsContainer.addView(section);
        }
    }

    private void saveTripAdvice() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<DocumentReference> allSelectedRefs = new ArrayList<>();
        for (List<Destination> dests : selectedDestinations.values()) {
            for (Destination d : dests) {
                allSelectedRefs.add(db.collection("destinations").document(d.getId()));
            }
        }
        Map<String, Object> tripAdvice = new HashMap<>();
        tripAdvice.put("location", selectedLocation);
        tripAdvice.put("destinations", allSelectedRefs);
        String estimatedCost = estimatedCostEditText.getText().toString().trim();
        if (estimatedCost.isEmpty()) estimatedCost = "0";
        tripAdvice.put("estimated_cost", estimatedCost);
        tripAdvice.put("rating", 0.0); // Placeholder
        db.collection("trip_advice").add(tripAdvice)
            .addOnSuccessListener(ref -> {
                android.widget.Toast.makeText(this, "Trip advice added!", android.widget.Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                android.widget.Toast.makeText(this, "Failed to add trip advice.", android.widget.Toast.LENGTH_LONG).show();
            });
    }
}

