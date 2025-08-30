package com.smarttravel.myanmar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddCategoryFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_category, container, false);
        EditText nameEditText = view.findViewById(R.id.categoryNameEditText);
        EditText descriptionEditText = view.findViewById(R.id.categoryDescriptionEditText);
        Button submitButton = view.findViewById(R.id.btnSubmitCategory);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        submitButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();
            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            Category category = new Category(name, description);
            db.collection("category").add(category)
                .addOnSuccessListener(docRef -> Toast.makeText(getContext(), "Category added!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add category", Toast.LENGTH_SHORT).show());
        });
        return view;
    }
}

