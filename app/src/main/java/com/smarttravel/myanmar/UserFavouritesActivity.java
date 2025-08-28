package com.smarttravel.myanmar;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserFavouritesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private DestinationAdapter adapter;
    private List<Destination> favourites;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_favourites);
        recyclerView = findViewById(R.id.userFavouritesRecyclerView);
        emptyTextView = findViewById(R.id.userFavouritesEmptyTextView);
        favourites = new ArrayList<>();
        adapter = new DestinationAdapter(this, favourites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        String userId = getIntent().getStringExtra("user_id");
        loadFavourites(userId);
    }

    private void loadFavourites(String userId) {
        db.collection("favourites").whereEqualTo("userId", userId).get().addOnSuccessListener(query -> {
            favourites.clear();
            for (QueryDocumentSnapshot doc : query) {
                String destName = doc.getString("destinationName");
                String destId = doc.getString("destinationId");
                Destination d = new Destination();
                d.setId(destId);
                d.setName(destName);
                favourites.add(d);
            }
            adapter.notifyDataSetChanged();
            emptyTextView.setVisibility(favourites.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        });
    }
}

