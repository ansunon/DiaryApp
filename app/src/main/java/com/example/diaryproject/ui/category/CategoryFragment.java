package com.example.diaryproject.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.diaryproject.HomeActivity;
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

    private Button move_category_btn;

    private CategoryViewModel galleryViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(CategoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_category, container, false);

        HomeActivity.currentFragment = "CategoryFrag"; // 구분하기위해서

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

        move_category_btn = root.findViewById(R.id.move_category_btn); // category 생성하는 버튼
        move_category_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "해당 카테고리 이동",Toast.LENGTH_LONG).show();
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

            ((CustomViewHolder)holder).imageView.setOnClickListener(new View.OnClickListener(){ // 카테고리 이미지 클릭시 해당 카테고리로 이동하는 부분
                @Override
                public void onClick(View v) { // 카테고리 이미지 클릭시 해당 그리드 이미지로 이동
//                    Fragment gallery_frag;
//                    gallery_frag = new Gallery_frag(); // fragment 객체를 여기서 미리 생성
//                    FragmentManager fm = getActivity().getSupportFragmentManager();
//                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
//
//                    fragmentTransaction.replace(R.id.main_content, gallery_frag);
//                    fragmentTransaction.addToBackStack(null); // commit()을 호출하기 전에 먼저 addToBackStack()를 호출해야 해야 트랜잭션을 프래그먼트 트랜잭션의 백 스택에 추가할 수 있다
//                    fragmentTransaction.commit();
                }
            });
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