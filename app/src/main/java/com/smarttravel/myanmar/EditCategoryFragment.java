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

public class EditCategoryFragment extends Fragment {
    private String categoryId;
    private String initialName;
    private String initialDescription;

    public EditCategoryFragment() {}
    public EditCategoryFragment(String categoryId, String name, String description) {
        this.categoryId = categoryId;
        this.initialName = name;
        this.initialDescription = description;
    }

    public static EditCategoryFragment newInstance(Category category) {
        EditCategoryFragment fragment = new EditCategoryFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", category.getId());
        args.putString("categoryName", category.getName());
        args.putString("categoryDescription", category.getDescription());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            initialName = getArguments().getString("categoryName");
            initialDescription = getArguments().getString("categoryDescription");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_category, container, false);
        EditText nameEditText = view.findViewById(R.id.categoryNameEditText);
        EditText descriptionEditText = view.findViewById(R.id.categoryDescriptionEditText);
        Button saveButton = view.findViewById(R.id.btnSaveCategory);
        nameEditText.setText(initialName);
        descriptionEditText.setText(initialDescription);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();
            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("category").document(categoryId)
                .update("name", name, "description", description)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Category updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update category", Toast.LENGTH_SHORT).show());
        });
        return view;
    }
}
