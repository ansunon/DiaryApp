package com.example.diaryproject.popup;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.example.diaryproject.DTO.PostDTO;
import com.example.diaryproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class PostUploadPopup extends AppCompatActivity {
    FirebaseAuth mAuth;

    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    // 방명록에 저장하는 변수들
    private String imagePath; // 프로필 이미지 경로가 와야한다.
    private ImageView imageView_add_image;
    private EditText title;
    private EditText description;
    private Button upload_btn;
    private Button cancel_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_post_upload);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(); // 데이터베이스를 가져오는 부분
        databaseReference = database.getReference(); // 데이터베이스를 가져오는 부분
        storage = FirebaseStorage.getInstance(); // storage 가져오는 부분

        //upload를 위한 변수들 ---------------------------------------------------------------
        imageView_add_image = (ImageView) findViewById(R.id.postAuthor_image);
        upload_btn = (Button) findViewById(R.id.post_upload_activity_button);
        title = (EditText) findViewById(R.id.post_upload_title_textinput);
        description = (EditText) findViewById(R.id.post_upload_description_textinput);
        upload_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) { // 방명록을 올려야한다.
                upload();
            }
        });
        cancel_btn = findViewById(R.id.cancel_button);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //------------------------------------------------------------------
    }

    private void upload() { // firebase storage 에 이미지와 제목, 내용을 올리는 함수

                PostDTO postDTO = new PostDTO();
                postDTO.title = title.getText().toString();
                postDTO.description = description.getText().toString();
                postDTO.uid = mAuth.getCurrentUser().getUid();
                postDTO.userId = mAuth.getCurrentUser().getEmail();

                databaseReference.child("post").push().setValue(postDTO); //  데이터베이스에 저장하는 부분 push()를 해야 array처럼 데이터베이스에 쌓이게 된다.

                Toast.makeText(PostUploadPopup.this, "upload success", Toast.LENGTH_SHORT).show();
                finish();
    }


    public String getPath(Uri uri) { // 핸드폰 갤러리의 이미지 주소를 가져오는 부분
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(index);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // 프로필 이미지의 url을 가져와야하는 부분
//        if (requestCode == PROFILE_IMAGE_CODE) {
//            imagePath = getPath(data.getData());
//            File file = new File(imagePath);
//            //imageView_add_image.setImageURI(Uri.fromFile(file)); // 이미지를 fragment_add_image의 이미지뷰에 가져오기
//            Glide.with(this).load(file).into(imageView_add_image); //-> 이미지를 가져오는 부분에 있어서 속도가 다르다.
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
