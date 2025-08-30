package com.smarttravel.myanmar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class LocationsListFragment extends Fragment {
    private List<Location> locationList = new ArrayList<>();
    private LocationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locations_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.locationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LocationAdapter(
            locationList,
            loc -> {
                EditLocationFragment editFragment = EditLocationFragment.newInstance(loc);
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.admin_content_frame, editFragment)
                    .addToBackStack(null)
                    .commit();
            },
            (loc, position) -> {
                // Delete logic
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("locations").document(loc.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        locationList.remove(position);
                        adapter.notifyItemRemoved(position);
                    });
            }
        );
        recyclerView.setAdapter(adapter);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locations").get().addOnSuccessListener(queryDocumentSnapshots -> {
            locationList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Location loc = doc.toObject(Location.class);
                loc.setId(doc.getId());
                locationList.add(loc);
            }
            adapter.notifyDataSetChanged();
        });
        return view;
    }
}
