package com.example.diaryproject;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.diaryproject.DTO.ImageDTO;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.loader.content.CursorLoader;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

//원래 되던 어플이 안되면  권한을 꼭 확인하자..........
public class HomeActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private long time = 0; // 취소 버튼을 누를때 사용하는 변수
    private AppBarConfiguration mAppBarConfiguration;

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

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(); // 데이터베이스를 가져오는 부분
        databaseReference = database.getReference(); // 데이터베이스를 가져오는 부분
        storage = FirebaseStorage.getInstance(); // storage 가져오는 부분


        setContentView(R.layout.activity_home2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //navigationView.setNavigationItemSelectedListener(HomeActivity2.this); // navigation tiem 선택하는 부분

        //-------------------------------------------------------------------------------
        View view = navigationView.getHeaderView(0); // navigation에 있는 textview를 가져오는 부분이므로
        textView_name = (TextView) view.findViewById(R.id.textview_name);
        textView_email = (TextView) view.findViewById(R.id.textView_email);
        textView_name.setText(mAuth.getCurrentUser().getDisplayName());
        textView_email.setText(mAuth.getCurrentUser().getEmail());
        //-------------------------------------------------------------------------------

        //upload를 위한 변수들 ---------------------------------------------------------------
        imageView_add_image = (ImageView) findViewById(R.id.imageView_add_image);
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

        //아래 주석된 소스코드로 해도 결과는 똑같다 다만 두개의 차이점에 대해서 잘 모르겠다. -> Key 값을 안주는 것이 차이점이긴 한데 밑에서 push을 안해서 인지 그것을 확인해봐야한다.
//        riversRef.putFile(file).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        Uri dluri = uri;
//                        ImageDTO imageDTO = new ImageDTO();
//                        imageDTO.imageUrl = dluri.toString();
//                        imageDTO.title = title.getText().toString();
//                        imageDTO.description = description.getText().toString();
//                        imageDTO.uid = mAuth.getCurrentUser().getUid();
//                        imageDTO.userId = mAuth.getCurrentUser().getEmail();
//                        imageDTO.imageNmae = file.getLastPathSegment(); // 삭제할 이미지의 이름
//
//                        databaseReference.child("images").setValue(imageDTO); //  데이터베이스에 저장하는 부분
//
//                        Toast.makeText(HomeActivity2.this, "upload success", Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(HomeActivity2.this, "upload fail", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });


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

                //Toast.makeText(HomeActivity2.this, "upload success", Toast.LENGTH_SHORT).show();
            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GALLERY_CODE) {
            imagePath = getPath(data.getData());
            File file = new File(imagePath);
            //imageView_add_image.setImageURI(Uri.fromFile(file)); // 이미지를 fragment_add_image의 이미지뷰에 가져오기
            Glide.with(this).load(file).into(imageView_add_image); //-> 이미지를 가져오는 부분에 있어서 속도가 다르다.
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void logout() {
        mAuth.signOut(); // 구글, 페이스북 로그아웃도 된다.
        LoginManager.getInstance().logOut();
        finish();
    }

    @Override
    public void onBackPressed() { // 취소를 누르면 로그아웃을 해야한다.
        if (System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "한번 더 누르면시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - time < 2000) {
            logout();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) { // navigation menu 를 터치할때
        int id = item.getItemId();
        if (id == R.id.nav_board) { // 데이터베이스에 있는것을 가져오는 부분
            startActivity(new Intent(this, BoardActivity.class));

        } else if (id == R.id.nav_gallery) { // 앨범을 불러오는 코드
            Intent intent = new Intent(Intent.ACTION_PICK); //
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, GALLERY_CODE);
        } else if (id == R.id.nav_logout) { // 로그아웃 하는 부분
            Toast.makeText(this, "로그아웃", Toast.LENGTH_LONG).show();
            logout();
            startActivity(new Intent(this, LoginActivity.class)); // 괜히 변수를 많들지 말고 바로 startActivity안에서 new를 해버리자
            finish();
        }
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
