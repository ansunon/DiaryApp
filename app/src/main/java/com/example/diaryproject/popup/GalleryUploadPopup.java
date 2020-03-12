package com.example.diaryproject.popup;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.example.diaryproject.DTO.CategoryDTO;
import com.example.diaryproject.DTO.FirebasePost;
import com.example.diaryproject.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryUploadPopup extends AppCompatActivity {

    private static final int GALLERY_CODE = 10; // 갤러리를 접근하면

    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private ImageView gallery_add_img;
    private Button gallery_category_chose;
    private Button create_btn;
    private Button cancel_btn;

    private List<CategoryDTO> categoryDTOS = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();

    private String imagePath;
    private static int imagePathNum = 0;

    // popup으로 바꾸자.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_gallery_upload);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        gallery_add_img = findViewById(R.id.gallery_add_image);
        create_btn = findViewById(R.id.create_btn);
        cancel_btn = findViewById(R.id.cancel_btn);

        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 새로운 카테고리를 생성하는 버튼
                upload(imagePath);
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        gallery_add_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK); //
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });

        database.getReference().child("category").addValueEventListener(new ValueEventListener() { // 옵저버 패턴
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { // 이게 무조건 있어야 한다.

            }
        });
    }

    private void upload(String uri) {
        StorageReference storageReference = storage.getReferenceFromUrl("gs://diaryproject-31c19.appspot.com");

        final Uri file = Uri.fromFile(new File(uri));
        StorageReference riversRef = storageReference.child("images/" + file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                while (!uri.isComplete()) ;
                Uri url = uri.getResult();
                // category 가 데이터베이스 이름
                // categoryName 이 해당 카테고리 이름
                // 해당 카테고리의 갤러리
                // 갤러리 안에 있는 이미지의 수
                databaseReference.child("category").child("12234").child("gallery").child((imagePathNum++)+"").setValue(url.toString()); // push()를 하면 자동으로 프라이머리 키가 생성된다.
                Toast.makeText(GalleryUploadPopup.this, "create category success", Toast.LENGTH_SHORT).show();
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GalleryUploadPopup.this, "create category fail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GALLERY_CODE) {
            imagePath = getPath(data.getData());
            File file = new File(imagePath);
            Glide.with(this).load(file).into(gallery_add_img);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getPath(Uri uri) { // 핸드폰 갤러리의 이미지 주소를 가져오는 부분
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(index);
    }
}

