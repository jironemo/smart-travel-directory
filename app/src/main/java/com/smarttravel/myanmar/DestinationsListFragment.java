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

public class DestinationsListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_destinations_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.destinationsListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // TODO: Set adapter and load destinations from Firestore
        return view;
    }
}

