package com.smarttravel.myanmar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EditDestinationActivity extends AppCompatActivity {
    private EditText nameEditText, descriptionEditText, addressEditText, contactEditText, additionalInfoEditText;
    private Button saveButton;
    private String destinationId;
    private FirebaseFirestore db;
    private List<String> base64Images = new ArrayList<>();
    private RecyclerView imagesRecyclerView;
    private ImagesPreviewAdapter imagesPreviewAdapter;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Spinner categorySpinner, divisionSpinner, locationSpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_destination);
        db = FirebaseFirestore.getInstance();
        destinationId = getIntent().getStringExtra("destination_id");
        nameEditText = findViewById(R.id.editDestinationNameEditText);
        categorySpinner = findViewById(R.id.editDestinationCategorySpinner);
        divisionSpinner = findViewById(R.id.editDestinationDivisionSpinner);
        locationSpinner = findViewById(R.id.editDestinationLocationSpinner);
        descriptionEditText = findViewById(R.id.editDestinationDescriptionEditText);
        addressEditText = findViewById(R.id.editDestinationAddressEditText);
        contactEditText = findViewById(R.id.editDestinationContactEditText);
        additionalInfoEditText = findViewById(R.id.editDestinationAdditionalInfoEditText);
        saveButton = findViewById(R.id.btnSaveDestination);
        imagesRecyclerView = findViewById(R.id.editDestinationImagesRecyclerView);
        imagesPreviewAdapter = new ImagesPreviewAdapter(base64Images, this::selectImage);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(imagesPreviewAdapter);
        List<Category> categoryList = new ArrayList<>();
        List<String> categoryNames = new ArrayList<>();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        db.collection("category").get().addOnSuccessListener(queryDocumentSnapshots -> {
            categoryList.clear();
            categoryNames.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Category cat = doc.toObject(Category.class);
                cat.setId(doc.getId());
                categoryList.add(cat);
                categoryNames.add(cat.getName());
            }
            categoryAdapter.notifyDataSetChanged();
        });
        List<String> divisionNames = new ArrayList<>();
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, divisionNames);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        divisionSpinner.setAdapter(divisionAdapter);
        db.collection("divisions").get().addOnSuccessListener(queryDocumentSnapshots -> {
            divisionNames.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String divisionName = doc.getString("name");
                if (divisionName != null) divisionNames.add(divisionName);
            }
            divisionAdapter.notifyDataSetChanged();
        });
        List<String> locationNames = new ArrayList<>();
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locationNames);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
        db.collection("locations").get().addOnSuccessListener(queryDocumentSnapshots -> {
            locationNames.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String locationName = doc.getString("name");
                if (locationName != null) locationNames.add(locationName);
            }
            locationAdapter.notifyDataSetChanged();
        });
        loadDestination();
        saveButton.setOnClickListener(v -> saveDestination());
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        byte[] imageBytes = baos.toByteArray();
                        String base64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                        if (base64Images.size() < 3) {
                            base64Images.add(base64);
                            imagesPreviewAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "Maximum 3 images allowed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        divisionSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedDivision = divisionSpinner.getSelectedItem() != null ? divisionSpinner.getSelectedItem().toString() : "";
                // Query locations for the selected division
                db.collection("locations").whereEqualTo("division", selectedDivision).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> filteredLocationNames = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String locationName = doc.getString("name");
                        if (locationName != null) filteredLocationNames.add(locationName);
                    }
                    ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(EditDestinationActivity.this, android.R.layout.simple_spinner_item, filteredLocationNames);
                    locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    locationSpinner.setAdapter(locationAdapter);
                });
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void loadDestination() {
        db.collection("destinations").document(destinationId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    nameEditText.setText(doc.getString("name"));
                    categorySpinner.setSelection(getCategoryIndex(doc.getString("category")));
                    divisionSpinner.setSelection(getIndex(divisionSpinner, doc.getString("division")));
                    locationSpinner.setSelection(getIndex(locationSpinner, doc.getString("locationName")));
                    descriptionEditText.setText(doc.getString("description"));
                    addressEditText.setText(doc.getString("address"));
                    contactEditText.setText(doc.getString("contact"));
                    additionalInfoEditText.setText(doc.getString("additionalInformation"));
                    List<String> images = (List<String>) doc.get("imageUrl");
                    base64Images.clear();
                    if (images != null) base64Images.addAll(images);
                    imagesPreviewAdapter.notifyDataSetChanged();
                }
            });
    }

    private void selectImage(int position) {
        if (base64Images.size() < 3) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        } else {
            Toast.makeText(this, "Maximum 3 images allowed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDestination() {
        String name = nameEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem() != null ? categorySpinner.getSelectedItem().toString() : "";
        String division = divisionSpinner.getSelectedItem() != null ? divisionSpinner.getSelectedItem().toString() : "";
        String locationName = locationSpinner.getSelectedItem() != null ? locationSpinner.getSelectedItem().toString() : "";
        String description = descriptionEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String contact = contactEditText.getText().toString().trim();
        String additionalInfo = additionalInfoEditText.getText().toString().trim();
        if (name.isEmpty() || locationName.isEmpty() || category.isEmpty() || description.isEmpty() || address.isEmpty() || contact.isEmpty() || additionalInfo.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("destinations").document(destinationId)
            .update("name", name,

                    "category", category,
                    "division", division,
                    "locationName", locationName,
                    "description", description,
                    "address", address,
                    "contact", contact,
                    "additionalInformation", additionalInfo,
                    "imageUrl", base64Images)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Destination updated!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Failed to update destination", Toast.LENGTH_SHORT).show());
    }

    private int getCategoryIndex(String category) {
        String[] categories = getResources().getStringArray(R.array.destination_categories);
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(category)) return i;
        }
        return 0;
    }

    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }
}
