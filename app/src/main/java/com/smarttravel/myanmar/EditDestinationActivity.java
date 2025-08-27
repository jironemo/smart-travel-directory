package com.smarttravel.myanmar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class EditDestinationActivity extends AppCompatActivity {
    private EditText nameEditText, locationEditText, categoryEditText, descriptionEditText;
    private ImageView imagePreview;
    private Button saveButton;
    private String destinationId;
    private FirebaseFirestore db;
    private String base64Image;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_destination);
        db = FirebaseFirestore.getInstance();
        destinationId = getIntent().getStringExtra("destination_id");
        nameEditText = findViewById(R.id.editDestinationNameEditText);
        locationEditText = findViewById(R.id.editDestinationLocationEditText);
        categoryEditText = findViewById(R.id.editDestinationCategoryEditText);
        descriptionEditText = findViewById(R.id.editDestinationDescriptionEditText);
        imagePreview = findViewById(R.id.editDestinationImagePreview);
        saveButton = findViewById(R.id.btnSaveDestination);
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
                        imagePreview.setImageBitmap(bitmap);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        byte[] imageBytes = baos.toByteArray();
                        base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        imagePreview.setOnClickListener(v -> selectImage());
    }

    private void loadDestination() {
        db.collection("destinations").document(destinationId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    nameEditText.setText(doc.getString("name"));
                    locationEditText.setText(doc.getString("location"));
                    categoryEditText.setText(doc.getString("category"));
                    descriptionEditText.setText(doc.getString("description"));
                    String base64 = doc.getString("imageUrl");
                    if (base64 != null && !base64.isEmpty()) {
                        try {
                            byte[] imageBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            imagePreview.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    } else {
                        imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
            });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void saveDestination() {
        String name = nameEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String category = categoryEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        if (name.isEmpty() || location.isEmpty() || category.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        String imageToSave = base64Image != null ? base64Image : null;
        db.collection("destinations").document(destinationId)
            .update("name", name,
                   "location", location,
                   "category", category,
                   "description", description,
                   "imageUrl", imageToSave)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Destination updated!", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(this, "Failed to update destination", Toast.LENGTH_SHORT).show());
    }
}
