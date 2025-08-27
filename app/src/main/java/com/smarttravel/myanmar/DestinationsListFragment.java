package com.smarttravel.myanmar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DestinationsListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_destinations_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.destinationsListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Destination> destinations = new ArrayList<>();
        AdminDestinationAdapter adapter = new AdminDestinationAdapter(getContext(), destinations);
        recyclerView.setAdapter(adapter);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("destinations")
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    destinations.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Destination destination = document.toObject(Destination.class);
                        destination.setId(document.getId());
                        destinations.add(destination);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(getContext(), "Failed to load destinations.", android.widget.Toast.LENGTH_LONG).show();
                    android.util.Log.e("DestinationsListFragment", "Error fetching destinations", e);
                });
        androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.adminSwipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            db.collection("destinations")
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    destinations.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Destination destination = document.toObject(Destination.class);
                        destination.setId(document.getId());
                        destinations.add(destination);
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(getContext(), "Failed to refresh destinations.", android.widget.Toast.LENGTH_LONG).show();
                    android.util.Log.e("DestinationsListFragment", "Error refreshing destinations", e);
                    swipeRefreshLayout.setRefreshing(false);
                });
        });
        EditText searchEditText = view.findViewById(R.id.searchDestinationEditText);
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                List<Destination> filtered = new ArrayList<>();
                for (Destination d : destinations) {
                    if (d.getName().toLowerCase().contains(query) ||
                        (d.getLocation() != null && d.getLocation().toLowerCase().contains(query)) ||
                        (d.getCategory() != null && d.getCategory().toLowerCase().contains(query))) {
                        filtered.add(d);
                    }
                }
                adapter.updateList(filtered);
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        return view;
    }
}
