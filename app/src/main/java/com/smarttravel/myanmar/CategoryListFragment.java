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

public class CategoryListFragment extends Fragment {
    private List<Category> categoryList = new ArrayList<>();
    private CategoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.categoryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CategoryAdapter(
            categoryList,
            cat -> {
                // Edit logic
                EditCategoryFragment editFragment = EditCategoryFragment.newInstance(cat);
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.admin_content_frame, editFragment)
                    .addToBackStack(null)
                    .commit();
            },
            (cat, position) -> {
                // Delete logic
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("category").document(cat.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        categoryList.remove(position);
                        adapter.notifyItemRemoved(position);
                    });
            }
        );
        recyclerView.setAdapter(adapter);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("category").get().addOnSuccessListener(queryDocumentSnapshots -> {
            categoryList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Category cat = doc.toObject(Category.class);
                cat.setId(doc.getId());
                categoryList.add(cat);
            }
            adapter.notifyDataSetChanged();
        });
        return view;
    }
}
