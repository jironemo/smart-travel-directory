package com.smarttravel.myanmar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.recyclerview.widget.RecyclerView;

public class TripAdviceManagementActivity extends AppCompatActivity {
    private Spinner locationSpinner;
    private LinearLayout categorySectionsContainer;
    private Button saveTripAdviceButton;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private List<String> locations = new ArrayList<>();
    private Map<String, List<Destination>> categoryDestinations = new HashMap<>();
    private Map<String, List<Destination>> selectedDestinations = new HashMap<>();
    private String selectedLocation = "";
    private String[] categories = {"Temple", "Pagoda", "Nature", "Cultural", "Adventure", "Food", "Shopping", "Local Best Bites"};
    private TextView estimatedCostEditText;
    private RecyclerView tripAdviceRecyclerView;
    private TripAdviceAdapter tripAdviceAdapter;
    private List<TripAdvice> tripAdviceList = new ArrayList<>();
    private LinearLayout editTripAdviceContainer;
    private TripAdvice selectedTripAdvice = null;
    private boolean ignoreSpinnerCallback = false;
    private Button backToListButton;
    private ImageButton addTripAdviceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_advice_management);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        tripAdviceRecyclerView = findViewById(R.id.tripAdviceRecyclerView);
        tripAdviceRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        tripAdviceAdapter = new TripAdviceAdapter(tripAdviceList, tripAdvice -> {
            showEditTripAdvice(tripAdvice);
        });
        tripAdviceRecyclerView.setAdapter(tripAdviceAdapter);
        editTripAdviceContainer = findViewById(R.id.editTripAdviceContainer);
        locationSpinner = findViewById(R.id.locationSpinner);
        categorySectionsContainer = findViewById(R.id.categorySectionsContainer);
        saveTripAdviceButton = findViewById(R.id.saveTripAdviceButton);
        estimatedCostEditText = findViewById(R.id.estimatedCostEditText);
        backToListButton = findViewById(R.id.backToListButton);
        addTripAdviceButton = findViewById(R.id.addTripAdviceButton);
        setupSidebarButton(toolbar);
        fetchTripAdvices();
        saveTripAdviceButton.setOnClickListener(v -> saveTripAdvice());
        backToListButton.setOnClickListener(v -> {
            editTripAdviceContainer.setVisibility(View.GONE);
            tripAdviceRecyclerView.setVisibility(View.VISIBLE);
        });
        addTripAdviceButton.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AddTripAdviceActivity.class);
            startActivity(intent);
        });
        navigationView.setNavigationItemSelectedListener(item -> {
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
            } else if (id == R.id.nav_add_destination) {
                // Fix: Use correct resource id for admin dashboard navigation
                startActivity(new android.content.Intent(this, AdminDashboardActivity.class));
                return true;
            } else if (id == R.id.nav_logout) {
                // Logout logic: clear session and go to login
                android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            drawerLayout.closeDrawers();
            return false;
        });
    }

    private void setupSidebarButton(Toolbar toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(navigationView));
    }

    private void fetchTripAdvices() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("trip_advice").get()
            .addOnSuccessListener(snaps -> {
                tripAdviceList.clear();
                for (QueryDocumentSnapshot doc : snaps) {
                    TripAdvice ta = doc.toObject(TripAdvice.class);
                    ta.setId(doc.getId());
                    tripAdviceList.add(ta);
                }
                tripAdviceAdapter.notifyDataSetChanged();
            });
    }

    private void showEditTripAdvice(TripAdvice tripAdvice) {
        selectedTripAdvice = tripAdvice;
        tripAdviceRecyclerView.setVisibility(View.GONE);
        editTripAdviceContainer.setVisibility(View.VISIBLE);
        locationSpinner.setEnabled(false); // Make location selector immutable
        // Fetch latest trip advice data from Firestore using its id
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("trip_advice").document(tripAdvice.getId()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String location = documentSnapshot.getString("location");
                    String estimatedCost = "";
                    Object costObj = documentSnapshot.get("estimated_cost");
                    if (costObj != null) estimatedCost = costObj.toString();
                    List<DocumentReference> selectedRefs = (List<DocumentReference>) documentSnapshot.get("destinations");
                    selectedLocation = location;
                    estimatedCostEditText.setText(estimatedCost);
                    fetchLocations(); // will set spinner selection
                    fetchDestinationsByCategory(selectedLocation, selectedRefs);
                }
            });
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
                // Set spinner selection programmatically and ignore callback
                ignoreSpinnerCallback = true;
                int selectedIndex = locations.indexOf(selectedLocation);
                if (selectedIndex >= 0) {
                    locationSpinner.setSelection(selectedIndex);
                }
                locationSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        if (ignoreSpinnerCallback) {
                            ignoreSpinnerCallback = false;
                            return;
                        }
                        selectedLocation = locations.get(position);
                        fetchDestinationsByCategory(selectedLocation, null);
                    }
                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
            })
            .addOnFailureListener(e -> {
                android.widget.Toast.makeText(this, "Failed to load locations.", android.widget.Toast.LENGTH_LONG).show();
                android.util.Log.e("TripAdviceMgmt", "Error fetching locations", e);
            });
    }

    private void fetchDestinationsByCategory(String location, List<DocumentReference> selectedRefs) {
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
                renderCategorySections(selectedRefs);
            })
            .addOnFailureListener(e -> {
                android.widget.Toast.makeText(this, "Failed to load destinations.", android.widget.Toast.LENGTH_LONG).show();
                android.util.Log.e("TripAdviceMgmt", "Error fetching destinations", e);
            });
    }

    private void renderCategorySections(List<DocumentReference> selectedRefs) {
        categorySectionsContainer.removeAllViews();
        selectedDestinations.clear();
        List<String> selectedIds = new ArrayList<>();
        if (selectedRefs != null) {
            for (DocumentReference ref : selectedRefs) {
                selectedIds.add(ref.getId());
            }
        }
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
                cb.setChecked(selectedIds.contains(d.getId()));
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
        if (selectedTripAdvice != null && selectedTripAdvice.getId() != null && !selectedTripAdvice.getId().isEmpty()) {
            // Update existing trip advice
            db.collection("trip_advice").document(selectedTripAdvice.getId())
                .set(tripAdvice)
                .addOnSuccessListener(ref -> {
                    android.widget.Toast.makeText(this, "Trip advice updated!", android.widget.Toast.LENGTH_SHORT).show();
                    editTripAdviceContainer.setVisibility(View.GONE);
                    tripAdviceRecyclerView.setVisibility(View.VISIBLE);
                    fetchTripAdvices();
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(this, "Failed to update trip advice.", android.widget.Toast.LENGTH_LONG).show();
                    android.util.Log.e("TripAdviceMgmt", "Error updating trip advice", e);
                });
        } else {
            // Add new trip advice
            db.collection("trip_advice").add(tripAdvice)
                .addOnSuccessListener(ref -> {
                    android.widget.Toast.makeText(this, "Trip advice saved!", android.widget.Toast.LENGTH_SHORT).show();
                    editTripAdviceContainer.setVisibility(View.GONE);
                    tripAdviceRecyclerView.setVisibility(View.VISIBLE);
                    fetchTripAdvices();
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(this, "Failed to save trip advice.", android.widget.Toast.LENGTH_LONG).show();
                    android.util.Log.e("TripAdviceMgmt", "Error saving trip advice", e);
                });
        }
    }
}
