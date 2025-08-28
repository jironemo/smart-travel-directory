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
import java.io.InputStream;
import java.util.HashMap;

public class AddDestinationFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 101;
    private Uri imageUri = null;
    private ImageView imagePreview;
    private androidx.activity.result.ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_destination, container, false);
        EditText nameEditText = view.findViewById(R.id.destinationNameEditText);
        EditText locationEditText = view.findViewById(R.id.destinationLocationEditText);
        Spinner categorySpinner = view.findViewById(R.id.destinationCategorySpinner);
        EditText descriptionEditText = view.findViewById(R.id.destinationDescriptionEditText);
        Button submitButton = view.findViewById(R.id.btnSubmitDestination);
        Button selectImageButton = view.findViewById(R.id.btnSelectImage);
        imagePreview = view.findViewById(R.id.destinationImagePreview);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        imagePickerLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    imagePreview.setImageURI(imageUri);
                }
            }
        );
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        });
        submitButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String location = locationEditText.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            String description = descriptionEditText.getText().toString().trim();
            if (name.isEmpty() || location.isEmpty() || category.isEmpty() || description.isEmpty() || imageUri == null) {
                Toast.makeText(getContext(), "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
                return;
            }
            String base64Image = "";
            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
                inputStream.close();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to encode image.", Toast.LENGTH_LONG).show();
                return;
            }
            HashMap<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("location", location);
            data.put("category", category);
            data.put("description", description);
            data.put("imageUrl", base64Image); // Store base64 string in imageUrl
            data.put("rating", 0.0);
            data.put("isPopular", false);
            db.collection("destinations").add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Destination added!", Toast.LENGTH_SHORT).show();
                    nameEditText.setText("");
                    locationEditText.setText("");
                    categorySpinner.setSelection(0); // Reset to first category
                    descriptionEditText.setText("");
                    imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
                    imageUri = null;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add destination.", Toast.LENGTH_LONG).show();
                });
        });
        return view;
    }
}
