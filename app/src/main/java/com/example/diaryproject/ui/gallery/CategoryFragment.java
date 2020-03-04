package com.example.diaryproject.ui.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diaryproject.popup.CreateCategoryPopup;
import com.example.diaryproject.DTO.CategoryDTO;
import com.example.diaryproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    private List<CategoryDTO> categoryDTOList = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();

    private Button add_category_btn;

    private CategoryViewModel galleryViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(CategoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_category, container, false);


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // grid 레이아웃으로 띄울꺼니까

        final CategoryRecyclerviewAdapter categoryRecyclerviewAdapter = new CategoryRecyclerviewAdapter();
        recyclerView.setAdapter(categoryRecyclerviewAdapter);


        database.getReference().child("category").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryDTOList.clear();
                uidList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    CategoryDTO categoryDTO = snapshot.getValue(CategoryDTO.class);
                    categoryDTOList.add(categoryDTO);
                    String uidKey = snapshot.getKey();
                    uidList.add(uidKey);
                }
                categoryRecyclerviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        add_category_btn = root.findViewById(R.id.create_category_btn); // category 생성하는 버튼
        add_category_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 새로운 카테고리 생성하는 액티비티로 이동
                startActivity(new Intent(getContext(), CreateCategoryPopup.class));
            }
        });
        return root;
    }

    class CategoryRecyclerviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            return new CategoryRecyclerviewAdapter.CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((CustomViewHolder)holder).textView.setText(categoryDTOList.get(position).category_name);
            Glide.with(((CustomViewHolder)holder).itemView.getContext()).load(categoryDTOList.get(position).imageUrl).into(((CustomViewHolder)holder).imageView);
        }

        @Override
        public int getItemCount() {
            return categoryDTOList.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder{
            private ImageView imageView;
            private TextView textView;

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.item_category_imageview);
                textView = itemView.findViewById(R.id.item_category_textview);
            }
        }
    }
}