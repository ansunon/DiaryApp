package com.example.diaryproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.diaryproject.DTO.CategoryDTO;
import com.example.diaryproject.DTO.GalleryDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;

    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage storage;

    private List<CategoryDTO> categoryDTOS = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery);
        firebaseDatabase = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        recyclerView = findViewById(R.id.gallery_recyclerview);
        recyclerView.setHasFixedSize(true);
        gridLayoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(gridLayoutManager);
        final GridFragmentRecyclerViewAdapter gridFragmentRecyclerViewAdapter = new GridFragmentRecyclerViewAdapter();
        recyclerView.setAdapter(gridFragmentRecyclerViewAdapter);

        // 새로운 데이터베이스가 필요하고 "gallery" category id랑 이미지 url만 있다.
        firebaseDatabase.getReference().child("gallery").addValueEventListener(new ValueEventListener() { // 옵저버 패턴
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) { // 실시간으로 데이터변환이 일어나는 부분
                categoryDTOS.clear(); // 처음에 초기화를 해야한다. -> 미리 저장되어있던 데이터를 클리어
                uidList.clear(); // 처음에 초기화를 해야한다.
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // 하나씩 데이터를 읽어오는 부분
                    CategoryDTO categoryDTO = snapshot.getValue(CategoryDTO.class); // 데이터베이스에 이상하게 저장되어있으면 읽어올때 문제가 생긴다.
                    categoryDTOS.add(categoryDTO);
                    String uidKey = snapshot.getKey(); // 데이터베이스에있는 하나의 게시글의 key값을 가져온다.
                    uidList.add(uidKey);
                    // 갤러리 이미지들을 가져와야한다.
                }
                gridFragmentRecyclerViewAdapter.notifyDataSetChanged(); // 계속 갱신 해줘야한다.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { // 이게 무조건 있어야 한다.

            }
        });
    }

    // 내부 클래스로 어댑터 구현
    public class GridFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        // 1. MyViewHolder 만들기
        public class CustomViewHolder extends RecyclerView.ViewHolder {
            ImageView grid_image;
            CustomViewHolder(View view){
                super(view);
                grid_image = view.findViewById(R.id.mypage_img);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
            return new CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            final CustomViewHolder myViewHolder = (CustomViewHolder)holder;
            ViewGroup.LayoutParams params = (ViewGroup.LayoutParams)holder.itemView.getLayoutParams(); // 이미지뷰의 params 가져오는 부분

            View main_content = findViewById(R.id.nav_host_fragment); // nav_host_fragment의 크기를 가져오는 부분
            params.width = main_content.getWidth() / 3;
            params.height = main_content.getWidth() / 3;

            holder.itemView.setLayoutParams(params); // 해당 이미지뷰 크기를 조정 해주자

            Glide.with(holder.itemView).load(categoryDTOS.get(position).imageUrl).into(myViewHolder.grid_image);
        }
        @Override
        public int getItemCount() { return categoryDTOS.size(); }
    }
}
