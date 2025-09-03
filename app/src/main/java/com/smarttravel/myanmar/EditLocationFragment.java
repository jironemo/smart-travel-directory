package com.smarttravel.myanmar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EditLocationFragment extends Fragment {
    private static final String ARG_LOCATION = "location";
    private Location location;

    public static EditLocationFragment newInstance(Location location) {
        EditLocationFragment fragment = new EditLocationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LOCATION, location);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            location = (Location) getArguments().getSerializable(ARG_LOCATION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_location, container, false);
        Spinner divisionSpinner = view.findViewById(R.id.editLocationDivisionSpinner);
        EditText nameEditText = view.findViewById(R.id.editLocationNameEditText);
        EditText descriptionEditText = view.findViewById(R.id.editLocationDescriptionEditText);
        Button saveButton = view.findViewById(R.id.btnSaveLocation);
        List<String> divisionNames = new ArrayList<>();
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, divisionNames);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        divisionSpinner.setAdapter(divisionAdapter);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("divisions").get().addOnSuccessListener(queryDocumentSnapshots -> {
            divisionNames.clear();
            int selectedIndex = 0;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String divisionName = doc.getString("name");
                if (divisionName != null) {
                    divisionNames.add(divisionName);
                    if (location != null && divisionName.equals(location.getDivision())) {
                        selectedIndex = divisionNames.size() - 1;
                    }
                }
            }
            divisionAdapter.notifyDataSetChanged();
            divisionSpinner.setSelection(selectedIndex);
        });
        if (location != null) {
            nameEditText.setText(location.getName());
            descriptionEditText.setText(location.getDescription());
        }
        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String division = divisionSpinner.getSelectedItem() != null ? divisionSpinner.getSelectedItem().toString() : "";
            String description = descriptionEditText.getText().toString().trim();
            if (name.isEmpty() || division.isEmpty() || description.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            // Update Firestore
            java.util.HashMap<String, Object> data = new java.util.HashMap<>();
            data.put("name", name);
            data.put("division", division);
            data.put("description", description);
            db.collection("locations").document(location.getId())
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Location updated successfully!", Toast.LENGTH_SHORT).show();
                    // Navigate back to Locations List
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().beginTransaction()
                            .replace(R.id.admin_content_frame, new LocationsListFragment())
                            .commit();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update location", Toast.LENGTH_SHORT).show());
        });
        return view;
    }
}
