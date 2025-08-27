package com.smarttravel.myanmar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        db = FirebaseFirestore.getInstance();

        EditText emailEditText = findViewById(R.id.et_register_email);
        EditText passwordEditText = findViewById(R.id.et_register_password);
        EditText usernameEditText = findViewById(R.id.et_register_username);
        EditText profilePictureEditText = findViewById(R.id.et_register_profile_picture);
        Button registerButton = findViewById(R.id.btn_register_submit);
        Button backToLoginButton = findViewById(R.id.btn_back_to_login);

        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String profilePicture = profilePictureEditText.getText().toString().trim();
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
                        userData.put("profile_picture", profilePicture);
                        userData.put("created_at", now);
                        userData.put("user_type", "NORMAL");
                        db.collection("users").add(userData)
                            .addOnSuccessListener(docRef -> {
                                User user = new User();
                                user.setId(docRef.getId());
                                user.setEmail(email);
                                user.setPassword(password);
                                user.setUsername(username);
                                user.setProfile_picture(profilePicture);
                                user.setCreated_at(now);
                                user.setUserType("NORMAL"); // Set userType in User object
                                User.setCurrentUser(user);
                                SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                                editor.putString("user_id", user.getId());
                                editor.apply();
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.putExtra("from_login", true);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            });
                    }
                });
        });

        backToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
