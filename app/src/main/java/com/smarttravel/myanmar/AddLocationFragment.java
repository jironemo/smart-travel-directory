package com.smarttravel.myanmar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;
import android.widget.EditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddLocationFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_location, container, false);
        Spinner divisionSpinner = view.findViewById(R.id.locationDivisionSpinner);
        EditText nameEditText = view.findViewById(R.id.locationNameEditText);
        EditText descriptionEditText = view.findViewById(R.id.locationDescriptionEditText);
        List<String> divisionNames = new ArrayList<>();
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, divisionNames);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        divisionSpinner.setAdapter(divisionAdapter);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("divisions").get().addOnSuccessListener(queryDocumentSnapshots -> {
            divisionNames.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String divisionName = doc.getString("name");
                if (divisionName != null) divisionNames.add(divisionName);
            }
            divisionAdapter.notifyDataSetChanged();
        });
        Button submitButton = view.findViewById(R.id.btnSubmitLocation);
        submitButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String division = divisionSpinner.getSelectedItem() != null ? divisionSpinner.getSelectedItem().toString() : "";
            String description = descriptionEditText.getText().toString().trim();
            if (name.isEmpty() || division.isEmpty() || description.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            // Save to Firestore
            java.util.HashMap<String, Object> data = new java.util.HashMap<>();
            data.put("name", name);
            data.put("division", division);
            data.put("description", description);
            db.collection("locations").add(data)
                .addOnSuccessListener(docRef -> Toast.makeText(getContext(), "Location added!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add location", Toast.LENGTH_SHORT).show());
        });
        return view;
    }
}
