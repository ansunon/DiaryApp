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

    private List<GalleryDTO> galleryDTOS = new ArrayList<>();
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
                galleryDTOS.clear(); // 처음에 초기화를 해야한다. -> 미리 저장되어있던 데이터를 클리어
                uidList.clear(); // 처음에 초기화를 해야한다.
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // 하나씩 데이터를 읽어오는 부분
                    GalleryDTO galleryDTO = snapshot.getValue(GalleryDTO.class); // 데이터베이스에 이상하게 저장되어있으면 읽어올때 문제가 생긴다.
                    galleryDTOS.add(galleryDTO);
                    String uidKey = snapshot.getKey(); // 데이터베이스에있는 하나의 게시글의 key값을 가져온다.
                    uidList.add(uidKey);
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
            //ImageView zoom_grid_image;
            CustomViewHolder(View view){
                super(view);
                grid_image = view.findViewById(R.id.mypage_img);
                //zoom_grid_image = view.findViewById(R.id.mypage_bigger_img);
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

            Glide.with(holder.itemView).load(galleryDTOS.get(position).imageUrl).into(myViewHolder.grid_image);

//            myViewHolder.grid_image.setOnClickListener(new View.OnClickListener(){ // 해당이미지 클릭시 확대 intent를 하지말고 animation을 이용할 것
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(getContext(), Zoomimage.class);
//                    intent.putExtra("imageno",imageArrayList.get(position).mypage_image);
//                    startActivity(intent);
//
//                    //-> 여기서 이미지를 클릭하면 확대하는 부분
//                    ImageView zoomimageview = getActivity().findViewById(R.id.mypage_bigger_img); // 확대할 이미지
//                    ImageView imageView = getActivity().findViewById(R.id.mypage_img); // 잠시 안보여야할 이미지
//
//                    Toolbar toolbar = getActivity().findViewById(R.id.my_toolbar);
//                    Toolbar columntoobar = getActivity().findViewById(R.id.column_Fragment);
//                    toolbar.setVisibility(View.GONE);
//                    toolbar.setVisibility(View.GONE); // 툴바 위와 오른쪽에 있는거 숨긴다.
//
//                    // 현재 프레그먼트를 가져와서 그걸 이미지 확대를 해야한다.
//                    // 오른쪽 프레그먼트도 GONE으로 해야한다.
//
//                }
//            });
        }
        @Override
        public int getItemCount() { return galleryDTOS.size(); }
    }
}
