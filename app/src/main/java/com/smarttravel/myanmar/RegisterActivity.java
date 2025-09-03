package com.smarttravel.myanmar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private Uri profileImageUri = null;
    private String profileImageBase64 = null;
    private ImageView profileImageView;
    private androidx.activity.result.ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = FirebaseFirestore.getInstance();
        EditText emailEditText = findViewById(R.id.et_register_email);
        EditText passwordEditText = findViewById(R.id.et_register_password);
        EditText usernameEditText = findViewById(R.id.et_register_username);
        profileImageView = findViewById(R.id.iv_register_profile_image);
        Button selectImageButton = findViewById(R.id.btn_select_profile_image);
        Button registerButton = findViewById(R.id.btn_register_submit);
        Button backToLoginButton = findViewById(R.id.btn_back_to_login);
        imagePickerLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    profileImageUri = result.getData().getData();
                    profileImageView.setImageURI(profileImageUri);
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(profileImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        byte[] imageBytes = baos.toByteArray();
                        profileImageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        selectImageButton.setOnClickListener(v -> selectProfileImage());
        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(RegisterActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (username.length() < 3) {
                Toast.makeText(RegisterActivity.this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Toast.makeText(RegisterActivity.this, "Email already registered", Toast.LENGTH_SHORT).show();
                    } else {
                        com.google.firebase.Timestamp now = com.google.firebase.Timestamp.now();
                        java.util.HashMap<String, Object> userData = new java.util.HashMap<>();
                        userData.put("email", email);
                        userData.put("password", password);
                        userData.put("username", username);
                        userData.put("profile_picture", profileImageBase64);
                        userData.put("created_at", now);
                        userData.put("user_type", "NORMAL");
                        db.collection("users").add(userData)
                            .addOnSuccessListener(docRef -> {
                                User user = new User();
                                user.setId(docRef.getId());
                                user.setEmail(email);
                                user.setPassword(password);
                                user.setUsername(username);
                                user.setProfile_picture(profileImageBase64);
                                user.setCreated_at(now);
                                user.setUser_type("NORMAL"); // Set userType in User object
                                User.setCurrentUser(user);
                                SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                                editor.putString("user_id", user.getId());
                                editor.apply();
                                // Show custom success dialog with animation, no buttons, auto-dismiss after 1 second
                                android.app.AlertDialog successDialog = new android.app.AlertDialog.Builder(RegisterActivity.this)
                                    .setView(getLayoutInflater().inflate(R.layout.dialog_success, null))
                                    .setCancelable(false)
                                    .create();
                                successDialog.show();
                                new android.os.Handler().postDelayed(() -> {
                                    successDialog.dismiss();
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    intent.putExtra("from_login", true);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }, 1000);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            });
                    }
                });
        });

        backToLoginButton.setOnClickListener(v -> finish());
    }
    private void selectProfileImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Profile Image"));
    }
}
