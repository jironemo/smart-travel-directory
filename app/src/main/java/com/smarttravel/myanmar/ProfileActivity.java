package com.smarttravel.myanmar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNavigation();
        // Get user info from User.currentUser
        User user = User.getCurrentUser();
        String email = user != null ? user.getEmail() : null;
        String username = user != null ? user.getUsername() : null;
        String profilePicture = user != null ? user.getProfile_picture() : null;
        String createdAt = user != null ? user.getCreatedAtString() : null;
        String userType = user != null ? user.getUserType() : "Guest";
        // Set user info to views
        TextView nameTextView = findViewById(R.id.profileNameTextView);
        TextView emailTextView = findViewById(R.id.profileEmailTextView);
        ImageView profileImageView = findViewById(R.id.profileImageView);
        TextView userTypeTextView = findViewById(R.id.profileUserTypeTextView);
        TextView createdAtTextView = findViewById(R.id.profileCreatedAtTextView);
        nameTextView.setText(username != null && !username.isEmpty() ? username : "Guest User");
        emailTextView.setText(email != null && !email.isEmpty() ? email : "guest@example.com");
        userTypeTextView.setText("User Type: " + (userType != null ? userType : "Guest"));
        if (createdAt != null && !createdAt.isEmpty()) {
            createdAtTextView.setText("Joined: " + createdAt);
        }
        if (profilePicture != null && !profilePicture.isEmpty()) {
            Glide.with(this)
                .load(profilePicture)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.ic_profile);
        }

        // Add Login button if user is not logged in
        if (user == null) {
            Button loginButton = new Button(this);
            loginButton.setText("Login");
            LinearLayout layout = (LinearLayout) findViewById(R.id.profileContainer);
            layout.addView(loginButton);
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        } else {
            Button logoutButton = new Button(this);
            logoutButton.setText("Logout");
            LinearLayout layout = (LinearLayout) findViewById(R.id.profileContainer);
            layout.addView(logoutButton);
            logoutButton.setOnClickListener(v -> {
                try {
                    User.setCurrentUser(null);
                    SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                    editor.remove("user_id");
                    editor.apply();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    android.widget.Toast.makeText(ProfileActivity.this, "Logout failed. Please try again.", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                // Clear activity stack so Home is not duplicated
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_search) {
                Intent intent = new Intent(ProfileActivity.this, SearchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_trip_advice) {
                Intent intent = new Intent(ProfileActivity.this, TripAdviceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_favorites) {
                Intent intent = new Intent(ProfileActivity.this, FavouritesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                // Already on profile, do nothing
                return true;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_profile);
    }
}
