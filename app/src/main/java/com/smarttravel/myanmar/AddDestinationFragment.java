package com.smarttravel.myanmar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ArrayAdapter;

public class AddDestinationFragment extends Fragment {
    private static final int MAX_IMAGES = 3;
    private static final String IMGBB_API_KEY = "f6fe35548b59cf2a2fbef9d576f1c5d0"; // Replace with your actual API key
    private List<String> imageUrls = new ArrayList<>();
    private ImagesPreviewAdapter imagesPreviewAdapter;
    private RecyclerView imagesRecyclerView;
    private androidx.activity.result.ActivityResultLauncher<Intent> imagePickerLauncher;
    private Spinner divisionSpinner, locationSpinner;
    private int pendingUploads = 0;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private List<Uri> previewImages = new ArrayList<>();
    private EditText nameEditText, descriptionEditText, addressEditText, contactEditText, additionalInfoEditText;
    private Spinner categorySpinner;
    private Button submitButton;
    private FirebaseFirestore db;
    private ProgressBar progressBarSaving;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_destination, container, false);
        nameEditText = view.findViewById(R.id.destinationNameEditText);
        categorySpinner = view.findViewById(R.id.destinationCategorySpinner);
        descriptionEditText = view.findViewById(R.id.destinationDescriptionEditText);
        addressEditText = view.findViewById(R.id.destinationAddressEditText);
        contactEditText = view.findViewById(R.id.destinationContactEditText);
        additionalInfoEditText = view.findViewById(R.id.destinationAdditionalInfoEditText);
        submitButton = view.findViewById(R.id.btnSubmitDestination);
        imagesRecyclerView = view.findViewById(R.id.imagesRecyclerView);
        divisionSpinner = view.findViewById(R.id.destinationDivisionSpinner);
        locationSpinner = view.findViewById(R.id.destinationLocationSpinner);
        progressBarSaving = view.findViewById(R.id.progressBarSaving);
        db = FirebaseFirestore.getInstance();
        // Remove Java Stream usage for compatibility
        List<String> selectedImageUrisStrings = new ArrayList<>();
        for (Uri uri : selectedImageUris) {
            selectedImageUrisStrings.add(uri.toString());
        }
        imagesPreviewAdapter = new ImagesPreviewAdapter((List<Object>) (List<?>) previewImages, position -> {
            if (position == previewImages.size() && previewImages.size() < MAX_IMAGES) {
                selectImages();
            } else if (position < previewImages.size()) {
                previewImages.remove(position);
                imagesPreviewAdapter.notifyDataSetChanged();
            }
        });
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(imagesPreviewAdapter);
        imagePickerLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
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
        setupSubmitButton();
        return view;
    }

    private void setupSpinners() {
        List<String> categoryNames = new ArrayList<>();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoryNames);
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
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, divisionNames);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        divisionSpinner.setAdapter(divisionAdapter);
        db.collection("divisions").get().addOnSuccessListener(queryDocumentSnapshots -> {
            divisionNames.clear();
            divisionNames.add("All Divisions");
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String divisionName = doc.getString("name");
                if (divisionName != null) divisionNames.add(divisionName);
            }
            divisionAdapter.notifyDataSetChanged();
        });

        List<String> locationNames = new ArrayList<>();
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, locationNames);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
        divisionSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedDivision = divisionSpinner.getSelectedItem().toString();
                if (selectedDivision.equals("All Divisions")) {
                    db.collection("locations").get().addOnSuccessListener(queryDocumentSnapshots -> {
                        locationNames.clear();
                        locationNames.add("All Locations");
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String locationName = doc.getString("name");
                            if (locationName != null) locationNames.add(locationName);
                        }
                        locationAdapter.notifyDataSetChanged();
                    });
                } else {
                    db.collection("locations").whereEqualTo("division", selectedDivision).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        locationNames.clear();
                        locationNames.add("All Locations");
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String locationName = doc.getString("name");
                            if (locationName != null) locationNames.add(locationName);
                        }
                        locationAdapter.notifyDataSetChanged();
                    });
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            String division = divisionSpinner.getSelectedItem().toString();
            String locationName = locationSpinner.getSelectedItem().toString();
            String description = descriptionEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            String contact = contactEditText.getText().toString().trim();
            String additionalInfo = additionalInfoEditText.getText().toString().trim();
            if (name.isEmpty() || category.isEmpty() || division.isEmpty() || locationName.isEmpty() || description.isEmpty() || address.isEmpty() || contact.isEmpty() || additionalInfo.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (previewImages.isEmpty()) {
                Toast.makeText(getContext(), "Please select and upload at least one image", Toast.LENGTH_SHORT).show();
                return;
            }
            progressBarSaving.setVisibility(View.VISIBLE);
            submitButton.setEnabled(false);
            List<String> uploadedUrls = new ArrayList<>();
            final int[] uploadCount = {0};
            for (Uri uri : previewImages) {
                try {
                    InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] imageBytes = baos.toByteArray();
                    String base64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP);
                    ImgbbUploader.uploadImage(base64, IMGBB_API_KEY, new ImgbbUploader.UploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            uploadedUrls.add(imageUrl);
                            uploadCount[0]++;
                            if (uploadCount[0] == previewImages.size()) {
                                saveToFirestore(uploadedUrls);
                            }
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            uploadCount[0]++;
                            if (uploadCount[0] == previewImages.size()) {
                                saveToFirestore(uploadedUrls);
                            }
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Failed to load image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    uploadCount[0]++;
                    if (uploadCount[0] == previewImages.size()) {
                        saveToFirestore(uploadedUrls);
                    }
                }
            }
        });
    }

    private void saveToFirestore(List<String> imageUrls) {
        String name = nameEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String division = divisionSpinner.getSelectedItem().toString();
        String locationName = locationSpinner.getSelectedItem().toString();
        String description = descriptionEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String contact = contactEditText.getText().toString().trim();
        String additionalInfo = additionalInfoEditText.getText().toString().trim();
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("category", category);
        data.put("division", division);
        data.put("locationName", locationName);
        data.put("description", description);
        data.put("address", address);
        data.put("contact", contact);
        data.put("additionalInformation", additionalInfo);
        data.put("imageUrl", imageUrls);
        db.collection("destinations").add(data)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(getContext(), "Destination added successfully!", Toast.LENGTH_SHORT).show();
                progressBarSaving.setVisibility(View.GONE);
                submitButton.setEnabled(true);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to add destination", Toast.LENGTH_SHORT).show();
                progressBarSaving.setVisibility(View.GONE);
                submitButton.setEnabled(true);
            });
    }

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select up to 3 images"));
    }
}
