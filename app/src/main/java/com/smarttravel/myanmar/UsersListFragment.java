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
import java.util.ArrayList;
import java.util.List;

public class UsersListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.usersListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        List<User> users = new ArrayList<>();
        AdminUserAdapter adapter = new AdminUserAdapter(getContext(), users);
        recyclerView.setAdapter(adapter);
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("users")
            .orderBy("username")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                users.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    User user = document.toObject(User.class);
                    user.setId(document.getId());
                    users.add(user);
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                android.widget.Toast.makeText(getContext(), "Failed to load users.", android.widget.Toast.LENGTH_LONG).show();
                android.util.Log.e("UsersListFragment", "Error fetching users", e);
            });
        return view;
    }
}
