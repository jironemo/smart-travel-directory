package com.smarttravel.myanmar;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.navigation.NavigationView;

public class AdminDashboardActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin_dashboard);
            drawerLayout = findViewById(R.id.drawer_layout);
            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(item -> {
                Fragment fragment = null;
                int id = item.getItemId();
                if (id == R.id.nav_destinations) {
                    fragment = new DestinationsListFragment();
                } else if (id == R.id.nav_users) {
                    fragment = new UsersListFragment();
                } else if (id == R.id.nav_reviews) {
                    fragment = new ReviewsListFragment();
                } else if (id == R.id.nav_add_destination) {
                    fragment = new AddDestinationFragment();
                }
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.admin_content_frame, fragment)
                            .commit();
                    drawerLayout.closeDrawers();
                    return true;
                }
                return false;
            });
            // Show destinations by default
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_content_frame, new DestinationsListFragment())
                    .commit();

            // Add logout button to navigation drawer header
            android.view.View headerView = navigationView.getHeaderView(0);
            android.widget.Button logoutButton = headerView.findViewById(R.id.adminLogoutButton);
            logoutButton.setOnClickListener(v -> {
                try {
                    User.setCurrentUser(null);
                    android.content.SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                    editor.remove("user_id");
                    editor.apply();
                    android.content.Intent intent = new android.content.Intent(AdminDashboardActivity.this, LoginActivity.class);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    android.widget.Toast.makeText(AdminDashboardActivity.this, "Logout failed. Please try again.", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            android.util.Log.e("AdminDashboard", "Error initializing dashboard", e);
            android.widget.Toast.makeText(this, "Error loading dashboard.", android.widget.Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
