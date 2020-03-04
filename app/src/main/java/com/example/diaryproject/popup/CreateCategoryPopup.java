package com.example.diaryproject.popup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.diaryproject.DTO.CategoryDTO;
import com.example.diaryproject.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class CreateCategoryPopup extends AppCompatActivity {

    private static final int GALLERY_CODE = 10; // 갤러리를 접근하면

    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private ImageView category_image;
    private ImageView show_category_image;
    private TextInputEditText category_name;
    private Button create_btn;
    private Button cancel_btn;


    private String imagePath;

    // popup으로 바꾸자.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_create_category);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        category_image = findViewById(R.id.item_category_imageview);
        category_name = findViewById(R.id.category_name);
        create_btn = findViewById(R.id.create_category_activity_btn);
        cancel_btn = findViewById(R.id.cancel_button);
        show_category_image = findViewById(R.id.show_category_imageview);

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

        category_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK); //
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });
    }

    private void upload(String uri){
        StorageReference storageReference = storage.getReferenceFromUrl("gs://diaryproject-31c19.appspot.com");

        final Uri file = Uri.fromFile(new File(uri));
        StorageReference riversRef = storageReference.child("images/"+file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                while(!uri.isComplete());
                Uri url = uri.getResult();

                CategoryDTO categoryDTO = new CategoryDTO();
                categoryDTO.imageUrl = url.toString();
                categoryDTO.category_name = category_name.getText().toString();
                categoryDTO.uid = auth.getCurrentUser().getUid();
                categoryDTO.userId = auth.getCurrentUser().getEmail();
                categoryDTO.imageNmae = file.getLastPathSegment();

                databaseReference.child("category").push().setValue(categoryDTO);
                Toast.makeText(CreateCategoryPopup.this, "create category success", Toast.LENGTH_SHORT).show();
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateCategoryPopup.this, "create category fail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GALLERY_CODE) {
            imagePath = getPath(data.getData());
            File file = new File(imagePath);
            show_category_image.setVisibility(View.VISIBLE);
            category_image.setVisibility(View.GONE);
            Glide.with(this).load(file).into(show_category_image); // 이미지를 fragment_add_image의 이미지뷰에 가져오기 -> 이미지를 가져오는 부분에 있어서 속도가 다르다.
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
