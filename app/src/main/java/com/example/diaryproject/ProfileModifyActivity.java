package com.example.diaryproject;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.preference.PreferenceFragmentCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.File;

public class ProfileModifyActivity extends AppCompatActivity {
    private static final int GALLERY_CODE = 10;

    private ImageView profile_imageview;
    private TextView profile_textview;
    private ImageView cancel_imageview;
    private ImageView upload_imageview;
    private TextInputEditText name;
    private TextInputEditText userName;
    private TextView useremail;
    private TextView userphonenumber;

    private FirebaseAuth auth;
    private FirebaseUser user;

    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        profile_imageview = findViewById(R.id.profile_modify_image);

        profile_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK); //
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });
        profile_textview = findViewById(R.id.profile_modify_textview);
        profile_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK); //
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });
        cancel_imageview = findViewById(R.id.cancel_imageview);
        cancel_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        upload_imageview = findViewById(R.id.upload_imageview);
        upload_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });
        name = findViewById(R.id.profile_modify_name);
        userName = findViewById(R.id.profile_modify_username);
        useremail = findViewById(R.id.profile_modify_useremail);
        userphonenumber = findViewById(R.id.profile_modify_phonenumber);
        getUserProfile();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
    // 현재 유저의 프로필 접근
    public void getUserProfile() {
        // [START get_user_profile]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();
            String phoneNumber = user.getPhoneNumber();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();
            Glide.with(this).load(photoUrl).into(profile_imageview);
            userName.setText(name);
            userName.setText(email);
            userName.setText(phoneNumber);
        }
        // [END get_user_profile]
    }
    public void updateProfile() {
        // [START update_profile]

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (imagePath != null && !userName.getText().toString().equals("")) { // 2개다 변경 했을 때
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userName.getText().toString()) // 입력받은 텍스트가 들어가면 된다. Displayname nickname이 된다.
                    .setPhotoUri(Uri.parse(imagePath)) // 여기서 storage 경로가 들어오면 된다.
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Glide.with(ProfileModifyActivity.this).load(imagePath).into(profile_imageview);
                                Toast.makeText(ProfileModifyActivity.this, "User profile updated. 1", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            // [END update_profile]

        }else if (imagePath != null && userName.getText().toString().equals("")) { // 이미지 만 변경했을 때
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(imagePath)) // 여기서 storage 경로가 들어오면 된다.
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Glide.with(ProfileModifyActivity.this).load(imagePath).into(profile_imageview);
                                    Toast.makeText(ProfileModifyActivity.this, "User profile updated. 2", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                // [END update_profile]
         } else if (imagePath == null && !userName.getText().toString().equals("") && userName != null) { // 이름만 변경했을 떄
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userName.getText().toString()) // 입력받은 텍스트가 들어가면 된다. Displayname nickname이 된다.
                    .build();
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ProfileModifyActivity.this, "User profile updated. 3", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            // [END update_profile]
        } else {
            Toast.makeText(ProfileModifyActivity.this, "변경할 사항이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE) {
            imagePath = getPath(data.getData());
            File file = new File(imagePath);
            Glide.with(this).load(file).into(profile_imageview);
        }
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