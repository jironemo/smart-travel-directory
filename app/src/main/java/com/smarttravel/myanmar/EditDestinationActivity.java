package com.smarttravel.myanmar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
    private static final int MAX_IMAGES = 3;
    private static final String IMGBB_API_KEY = "f6fe35548b59cf2a2fbef9d576f1c5d0";
    private EditText nameEditText, descriptionEditText, addressEditText, contactEditText, additionalInfoEditText;
    private Button saveButton;
    private String destinationId;
    private FirebaseFirestore db;
    private RecyclerView imagesRecyclerView;
    private ImagesPreviewAdapter imagesPreviewAdapter;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Spinner categorySpinner, divisionSpinner, locationSpinner;
    // Holds both URLs (String) and new images (Uri)
    private List<Object> previewImages = new ArrayList<>();
    private ProgressBar progressBarSaving;

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
        progressBarSaving = findViewById(R.id.progressBarSaving);
        progressBarSaving.setVisibility(View.GONE);
        // Convert previewImages to List<String> or List<Uri> for the adapter
        imagesPreviewAdapter = new ImagesPreviewAdapter(previewImages, position -> {
            if (position == previewImages.size() && previewImages.size() < MAX_IMAGES) {
                selectImages();
            } else if (position < previewImages.size()) {
                previewImages.remove(position);
                imagesPreviewAdapter.notifyDataSetChanged();
            }
        });
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(imagesPreviewAdapter);
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    List<Uri> uris = new ArrayList<>();
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count && previewImages.size() + uris.size() < MAX_IMAGES; i++) {
                            uris.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        if (previewImages.size() < MAX_IMAGES) {
                            uris.add(result.getData().getData());
                        }
                    }
                    previewImages.addAll(uris);
                    imagesPreviewAdapter.notifyDataSetChanged();
                }
            }
        );
        setupSpinners();
        loadDestination();
        saveButton.setOnClickListener(v -> {
            progressBarSaving.setVisibility(View.VISIBLE);
            saveButton.setEnabled(false);
            saveDestination();
        });
    }

    private void setupSpinners() {
        List<String> categoryNames = new ArrayList<>();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        db.collection("category").get().addOnSuccessListener(queryDocumentSnapshots -> {
            categoryNames.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("name");
                if (name != null) categoryNames.add(name);
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
        divisionSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedDivision = divisionSpinner.getSelectedItem() != null ? divisionSpinner.getSelectedItem().toString() : "";
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
                    previewImages.clear();
                    if (images != null) {
                        previewImages.addAll(images); // Add URLs for preview
                    }
                    imagesPreviewAdapter.notifyDataSetChanged();
                }
            });
    }

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select up to 3 images"));
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
        if (previewImages.isEmpty()) {
            Toast.makeText(this, "Please select and upload at least one image", Toast.LENGTH_SHORT).show();
            return;
        }
        saveButton.setEnabled(false);
        List<String> uploadedUrls = new ArrayList<>();
        final int[] uploadCount = {0};
        List<Uri> urisToUpload = new ArrayList<>();
        List<String> urlsToKeep = new ArrayList<>();
        for (Object obj : previewImages) {
            if (obj instanceof Uri) {
                urisToUpload.add((Uri) obj);
            } else if (obj instanceof String) {
                urlsToKeep.add((String) obj);
            }
        }
        if (urisToUpload.isEmpty()) {
            saveToFirestore(urlsToKeep);
            return;
        }
        for (Uri uri : urisToUpload) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream == null) throw new Exception("Cannot open image stream");
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap == null) throw new Exception("Cannot decode image");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Always compress as JPEG
                byte[] imageBytes = baos.toByteArray();
                String base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                ImgbbUploader.uploadImage(base64, IMGBB_API_KEY, new ImgbbUploader.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        uploadedUrls.add(imageUrl);
                        uploadCount[0]++;
                        if (uploadCount[0] == urisToUpload.size()) {
                            List<String> allUrls = new ArrayList<>(urlsToKeep);
                            allUrls.addAll(uploadedUrls);
                            saveToFirestore(allUrls);
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("ImageUpload", e.getMessage(), e);
                        Toast.makeText(EditDestinationActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        uploadCount[0]++;
                        if (uploadCount[0] == urisToUpload.size()) {
                            List<String> allUrls = new ArrayList<>(urlsToKeep);
                            allUrls.addAll(uploadedUrls);
                            saveToFirestore(allUrls);
                        }
                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, "Failed to process image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                uploadCount[0]++;
                if (uploadCount[0] == urisToUpload.size()) {
                    List<String> allUrls = new ArrayList<>(urlsToKeep);
                    allUrls.addAll(uploadedUrls);
                    saveToFirestore(allUrls);
                }
            }
        }
    }

    private void saveToFirestore(List<String> imageUrls) {
        String name = nameEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem() != null ? categorySpinner.getSelectedItem().toString() : "";
        String division = divisionSpinner.getSelectedItem() != null ? divisionSpinner.getSelectedItem().toString() : "";
        String locationName = locationSpinner.getSelectedItem() != null ? locationSpinner.getSelectedItem().toString() : "";
        String description = descriptionEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String contact = contactEditText.getText().toString().trim();
        String additionalInfo = additionalInfoEditText.getText().toString().trim();
        db.collection("destinations").document(destinationId)
            .update("name", name,
                    "category", category,
                    "division", division,
                    "locationName", locationName,
                    "description", description,
                    "address", address,
                    "contact", contact,
                    "additionalInformation", additionalInfo,
                    "imageUrl", imageUrls)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Destination updated successfully!", Toast.LENGTH_SHORT).show();
                progressBarSaving.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update destination", Toast.LENGTH_SHORT).show();
                progressBarSaving.setVisibility(View.GONE);
                saveButton.setEnabled(true);
            });
        saveButton.setEnabled(true);
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
