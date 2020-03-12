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
import com.example.diaryproject.HomeActivity;
import com.example.diaryproject.R;
import com.example.diaryproject.popup.GalleryUploadPopup;
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

        HomeActivity.currentFragment = "DashboardFrag"; // 구분하기위해서

        return root;
    }

}