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
    private List<String> base64Images = new ArrayList<>();
    private ImagesPreviewAdapter imagesPreviewAdapter;
    private RecyclerView imagesRecyclerView;
    private androidx.activity.result.ActivityResultLauncher<Intent> imagePickerLauncher;
    private Spinner divisionSpinner, locationSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_destination, container, false);
        EditText nameEditText = view.findViewById(R.id.destinationNameEditText);
        Spinner categorySpinner = view.findViewById(R.id.destinationCategorySpinner);
        EditText descriptionEditText = view.findViewById(R.id.destinationDescriptionEditText);
        EditText addressEditText = view.findViewById(R.id.destinationAddressEditText);
        EditText contactEditText = view.findViewById(R.id.destinationContactEditText);
        EditText additionalInfoEditText = view.findViewById(R.id.destinationAdditionalInfoEditText);
        Button submitButton = view.findViewById(R.id.btnSubmitDestination);
        imagesRecyclerView = view.findViewById(R.id.imagesRecyclerView);
        imagesPreviewAdapter = new ImagesPreviewAdapter(base64Images, position -> {
            if (position == base64Images.size() && base64Images.size() < MAX_IMAGES) {
                selectImages();
            } else {
                // Optionally: show image preview or remove
            }
        });
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(imagesPreviewAdapter);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        imagePickerLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    List<Uri> uris = new ArrayList<>();
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count && base64Images.size() + uris.size() < MAX_IMAGES; i++) {
                            uris.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        if (base64Images.size() < MAX_IMAGES) {
                            uris.add(result.getData().getData());
                        }
                    }
                    for (Uri uri : uris) {
                        try {
                            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos);
                            byte[] imageBytes = baos.toByteArray();
                            String base64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                            base64Images.add(base64);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                    imagesPreviewAdapter.notifyDataSetChanged();
                }
            }
        );
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
            HashMap<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("category", category);
            data.put("division", division);
            data.put("locationName", locationName);
            data.put("description", description);
            data.put("address", address);
            data.put("contact", contact);
            data.put("additionalInformation", additionalInfo);
            data.put("imageUrl", base64Images);
            android.util.Log.d("AddDestinationFragment", "Sending to Firestore: " + data.toString());
            db.collection("destinations").add(data)
                .addOnSuccessListener(docRef -> Toast.makeText(getContext(), "Destination added!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add destination", Toast.LENGTH_SHORT).show());
        });
        List<Category> categoryList = new ArrayList<>();
        List<String> categoryNames = new ArrayList<>();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoryNames);
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
        divisionSpinner = view.findViewById(R.id.destinationDivisionSpinner);
        locationSpinner = view.findViewById(R.id.destinationLocationSpinner);
        List<String> divisionNames = new ArrayList<>();
        ArrayAdapter<String> divisionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, divisionNames);
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
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, locationNames);
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
        return view;
    }
    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select up to 3 images"));
    }
}
