package com.example.diaryproject.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.diaryproject.DTO.CategoryDTO;
import com.example.diaryproject.GalleryActivity;
import com.example.diaryproject.R;
import com.example.diaryproject.ui.board.BoardViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private static final int GALLERY_CODE = 10; // navigation에서 갤러리 이모티콘 불류
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private List<CategoryDTO> categoryDTOS = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();
    private Spinner spinner;

    private ArrayAdapter<String> adapter;
    ArrayList<String> list;



    private BoardViewModel dashboardViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(BoardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
//        startActivity(new Intent(getContext(), GalleryActivity.class)); // 임시로 갤러리 액티비티를 실행해본다.

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        spinner = root.findViewById(R.id.gallery_category_spinner);
        list = new ArrayList<String>();
        //list = new ArrayList<CharSequence>();
        list.add("category1");
        list.add("category2");

        //adapter = new ArrayAdapter<String>(this, R.id.gallery_category_spinner, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);



        database.getReference().child("images").addValueEventListener(new ValueEventListener() { // 옵저버 패턴
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) { // 실시간으로 데이터변환이 일어나는 부분
                categoryDTOS.clear(); // 처음에 초기화를 해야한다. -> 미리 저장되어있던 데이터를 클리어
                uidList.clear(); // 처음에 초기화를 해야한다.
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // 하나씩 데이터를 읽어오는 부분
                    CategoryDTO categoryDTO = snapshot.getValue(CategoryDTO.class); // 데이터베이스에 이상하게 저장되어있으면 읽어올때 문제가 생긴다.
                    categoryDTOS.add(categoryDTO);
                    String uidKey = snapshot.getKey(); // 데이터베이스에있는 하나의 게시글의 key값을 가져온다.
                    uidList.add(uidKey);
                }
                //root.notifyDataSetChanged(); // 계속 갱신 해줘야한다.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { // 이게 무조건 있어야 한다.

            }
        });
        return root;
    }

}