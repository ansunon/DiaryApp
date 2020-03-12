package com.example.diaryproject.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.example.diaryproject.HomeActivity;
import com.example.diaryproject.LoginActivity;
import com.example.diaryproject.ProfileModifyActivity;
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

import static com.facebook.FacebookSdk.getApplicationContext;

public class HomeFragment extends Fragment implements HomeActivity.OnBackPressedListener{

    private HomeViewModel homeViewModel;

    private static final int GALLERY_CODE = 10; // 갤러리 선택 번호

    FirebaseAuth auth;

    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private TextView textView_name; // navigation 에 띄워야할 현재 user의 이름
    private TextView textView_email; // navigation 에 띄워야할 현재 user의 email

    private String imagePath;
    private ImageView profile_image;

    private Button logout_btn;
    private Button fix_profile;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        HomeActivity.currentFragment = "HomeFrag"; // 구분하기위해서

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(); // 데이터베이스를 가져오는 부분
        databaseReference = database.getReference(); // 데이터베이스를 가져오는 부분
        storage = FirebaseStorage.getInstance(); // storage 가져오는 부분

        textView_email = root.findViewById(R.id.home_profile_useremail);

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
        getUserProfile();
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
                startActivity(new Intent(getActivity(), ProfileModifyActivity.class));
            }
        });

        return root;
    }

    @Override
    public void onResume() { // 재시작시 프로필 이미지를 변경해야한다.
        super.onResume();
        getUserProfile();
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
            Glide.with(this).load(photoUrl).into(profile_image); // 현재 프로필 이미지 저장 한걸 가져올수 있다.
            textView_email.setText(name);
        }
        // [END get_user_profile]
    }

    public void updateProfile() {
        // [START update_profile]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
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

    @Override
    public void onBack() { // 해당 소스 https://jinunthing.tistory.com/22
        // 리스너를 설정하기 위해 Activity 를 받아옵니다.
        HomeActivity activity = (HomeActivity)getActivity();
        // 한번 뒤로가기 버튼을 눌렀다면 Listener 를 null 로 해제해줍니다.
        activity.setOnBackPressedListener(null);
    }
    @Override // fragment 호출시 반드시 호출되는 오버라이드 메소이다.
    public void onAttach(Activity context) {
        super.onAttach(context);
        ((HomeActivity)context).setOnBackPressedListener(this);
    }
}