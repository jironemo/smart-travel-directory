package com.smarttravel.myanmar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String savedUserId = prefs.getString("user_id", null);
        if (savedUserId != null) {
            db.collection("users").document(savedUserId).get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        user.setId(doc.getId());
                        User.setCurrentUser(user);
                        String userType = doc.getString("user_type");
                        Intent intent;
                        if ("ADMIN".equalsIgnoreCase(userType)) {
                            intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                        }
                        intent.putExtra("from_login", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        return;
                    }
                });
        }

        Button guestButton = findViewById(R.id.btn_login_guest);
        guestButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("from_login", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        Button emailLoginButton = findViewById(R.id.btn_login_email);
        EditText emailEditText = findViewById(R.id.et_email);
        EditText passwordEditText = findViewById(R.id.et_password);
        emailLoginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            db.collection("users")
                    .whereEqualTo("email", email)
                    .whereEqualTo("password", password)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            User user;
                            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : task.getResult()) {
                                user = doc.toObject(User.class);
                                String userType = doc.getString("user_type");
                                user.setId(doc.getId());
                                user.setUserType(userType); // Set userType from firebase property
                                User.setCurrentUser(user);
                                // Save user id for persistent login
                                SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                                editor.putString("user_id", user.getId());
                                editor.apply();
                                // Show custom success dialog with animation, no buttons, auto-dismiss after 1 second
                                android.app.AlertDialog successDialog = new android.app.AlertDialog.Builder(LoginActivity.this)
                                    .setView(getLayoutInflater().inflate(R.layout.dialog_success, null))
                                    .setCancelable(false)
                                    .create();
                                successDialog.show();
                                new android.os.Handler().postDelayed(() -> {
                                    successDialog.dismiss();
                                    Intent intent;
                                    if ("ADMIN".equalsIgnoreCase(userType)) {
                                        intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                    } else {
                                        intent = new Intent(LoginActivity.this, MainActivity.class);
                                    }
                                    intent.putExtra("from_login", true);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }, 1000);
                                return;
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        Button goRegisterButton = findViewById(R.id.btn_go_register);
        goRegisterButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
