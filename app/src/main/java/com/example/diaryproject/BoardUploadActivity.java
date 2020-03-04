package com.example.diaryproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.navigation.ui.AppBarConfiguration;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.diaryproject.DTO.ImageDTO;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class BoardUploadActivity extends AppCompatActivity {
    private long time = 0; // 취소 버튼을 누를때 사용하는 변수

    private static final int GALLERY_CODE = 10; // navigation에서 갤러리 이모티콘 불류

    FirebaseAuth mAuth;

    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private TextView textView_name; // navigation 에 띄워야할 현재 user의 이름
    private TextView textView_email; // navigation 에 띄워야할 현재 user의 email

    // 갤러리에 저장하는 변수들
    private String imagePath;
    private ImageView imageView_add_image;
    private EditText title;
    private EditText description;
    private Button upload_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_upload);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(); // 데이터베이스를 가져오는 부분
        databaseReference = database.getReference(); // 데이터베이스를 가져오는 부분
        storage = FirebaseStorage.getInstance(); // storage 가져오는 부분

        //upload를 위한 변수들 ---------------------------------------------------------------
        imageView_add_image = (ImageView) findViewById(R.id.imageView_add_image);

        imageView_add_image.setOnClickListener(new View.OnClickListener() { // 이미지뷰를 누루면 핸드폰 갤러리에 들어가지는 부분
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK); //
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });
        upload_btn = (Button) findViewById(R.id.upload_btn);
        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.description);
        upload_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                upload(imagePath);
            }
        });
        //------------------------------------------------------------------
    }
    // upload_btn 의 클릭 이벤트를 접근하지 못한다. -> HomeFragment에서 클릭 이벤트를 만들어보자
    private void upload(String uri) { // firebase storage 에 이미지와 제목, 내용을 올리는 함수
        StorageReference storageRef = storage.getReferenceFromUrl("gs://diaryproject-31c19.appspot.com"); // 내 firebase storage 경로를 넣어야한다. //(추가적인 작업)firebase storage의 규칙에서 승인을 해줘야한다.

        // Create a reference to a file from a Google Cloud Storage URI
        final Uri file = Uri.fromFile(new File(uri)); // 갤러리에서 가져온 이미지의 파일 경로를 여기에 넣는다.
        final StorageReference riversRef = storageRef.child("images/" + file.getLastPathSegment()); // final 원래는 붙힘
        UploadTask uploadTask = riversRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() { // 데이터베이스에 쓰는 부분
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl(); // 여기가 중요한 부분 이과정이 있어야 downloadurl을 가져올수 있다.
                while (!uri.isComplete()) ; // ★★★★★★★★★★★★★★★
                Uri url = uri.getResult(); // ★★★★★★★★★★★★★★★

                ImageDTO imageDTO = new ImageDTO();
                imageDTO.imageUrl = url.toString();
                imageDTO.title = title.getText().toString();
                imageDTO.description = description.getText().toString();
                imageDTO.uid = mAuth.getCurrentUser().getUid();
                imageDTO.userId = mAuth.getCurrentUser().getEmail();
                imageDTO.imageNmae = file.getLastPathSegment(); // 삭제할 이미지의 이름

                databaseReference.child("images").push().setValue(imageDTO); //  데이터베이스에 저장하는 부분 push()를 해야 array처럼 데이터베이스에 쌓이게 된다.

                Toast.makeText(getApplicationContext(), "upload success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GALLERY_CODE) {
            imagePath = getPath(data.getData());
            File file = new File(imagePath);
            Glide.with(this).load(file).into(imageView_add_image); // 이미지를 fragment_add_image의 이미지뷰에 가져오기 -> 이미지를 가져오는 부분에 있어서 속도가 다르다.
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
