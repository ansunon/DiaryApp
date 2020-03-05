package com.example.diaryproject.ui.home;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.content.CursorLoader;
import androidx.navigation.ui.AppBarConfiguration;

import com.bumptech.glide.Glide;
import com.example.diaryproject.DTO.UserDTO;
import com.example.diaryproject.HomeActivity;
import com.example.diaryproject.LoginActivity;
import com.example.diaryproject.R;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
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
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private long time = 0; // 취소 버튼을 누를때 사용하는 변수

    private static final int GALLERY_CODE = 10; // 갤러리 선택 번호

    FirebaseAuth auth;

    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private TextView textView_name; // navigation 에 띄워야할 현재 user의 이름
    private TextView textView_email; // navigation 에 띄워야할 현재 user의 email


    private List<UserDTO> userDTOS = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();


    private String imagePath;
    private ImageView profile_image;

    private Button logout_btn;
    private Button fix_profile;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(); // 데이터베이스를 가져오는 부분
        databaseReference = database.getReference(); // 데이터베이스를 가져오는 부분
        storage = FirebaseStorage.getInstance(); // storage 가져오는 부분

        textView_email = root.findViewById(R.id.home_profile_useremail);
        textView_email.setText(auth.getCurrentUser().getDisplayName()); // 이것도 문제이다. 원래 여기에 닉네임을 띄울 생각이였는데....

        logout_btn = root.findViewById(R.id.mypage_logout_btn);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut(); // 구글, 페이스북 로그아웃도 된다.
                LoginManager.getInstance().logOut();
                // 현재 프래그먼트를 종료시켜야하는데...
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        profile_image = root.findViewById(R.id.mypage_profile_image);
        Glide.with(this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into(profile_image); // 현재 프로필 이미지 저장 한걸 가져올수 있다.
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK); //
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });

        fix_profile = root.findViewById(R.id.fix_profile_btn);
        fix_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 프로필 고치는 액티비티로 이동해야한다.
                Toast.makeText(getContext(), "fix profile button click",Toast.LENGTH_LONG).show();
            }
        });

        database.getReference().child("category").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userDTOS.clear();
                uidList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    UserDTO userDTO = snapshot.getValue(UserDTO.class);
                    userDTOS.add(userDTO);
                    String uidKey = snapshot.getKey();
                    uidList.add(uidKey);
                }
                //Glide.with().load().into(profile_image); -> 프로필 사진을 등록해야해
                //categoryRecyclerviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return root;
    }

    // --------------------------------------------------------------------------------------------------------------
// 현재 유저의 프로필 접근
    public void getUserProfile() {
        // [START get_user_profile]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();
        }
        // [END get_user_profile]
    }

    public void getProviderData() {
        // [START get_provider_data]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                // Id of the provider (ex: google.com)
                String providerId = profile.getProviderId();

                // UID specific to the provider
                String uid = profile.getUid();

                // Name, email address, and profile photo Url
                String name = profile.getDisplayName();
                String email = profile.getEmail();
                Uri photoUrl = profile.getPhotoUrl();
            }
        }
        // [END get_provider_data]
    }

    public void updateProfile() {
        // [START update_profile]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                //.setDisplayName("Jane Q. User") // 입력받은 텍스트가 들어가면 된다. Displayname nickname이 된다.
                .setPhotoUri(Uri.parse(imagePath)) // 여기서 storage 경로가 들어오면 된다.
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "User profile updated.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END update_profile]
    }

    // --------------------------------------------------------------------------------------------------------------

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

                UserDTO userDTO = new UserDTO();
                userDTO.imageUrl = url.toString();
                userDTO.uid = auth.getCurrentUser().getUid();
                userDTO.userId = auth.getCurrentUser().getEmail();
                userDTO.imageNmae = file.getLastPathSegment(); // 삭제할 이미지의 이름

                databaseReference.child("user").push().setValue(userDTO); //  데이터베이스에 저장하는 부분 push()를 해야 array처럼 데이터베이스에 쌓이게 된다.

                Toast.makeText(getContext(), "upload success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GALLERY_CODE) {
            imagePath = getPath(data.getData());
            File file = new File(imagePath);
            Glide.with(this).load(file).into(profile_image); // 이미지를 fragment_add_image의 이미지뷰에 가져오기 -> 이미지를 가져오는 부분에 있어서 속도가 다르다.
            updateProfile(); // upload가 이뤄지면 된다. 프로필 이미지 변경
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getPath(Uri uri) { // 핸드폰 갤러리의 이미지 주소를 가져오는 부분
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(getContext(), uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(index);
    }
}