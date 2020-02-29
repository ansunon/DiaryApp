package com.example.diaryproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// 회원가입하는 부분
public class RegisterActivity extends AppCompatActivity {
    private long time = 0; // 취소 버튼을 누를때 사용하는 변수
    private FirebaseAuth mAuth;

    private EditText editTextEmail;
    private EditText editTextPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.edittext_email);
        editTextPassword = findViewById(R.id.edittext_password);

        Button button1 = (Button) findViewById(R.id.registerButton); // 이거 다시 변경해야한다.
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser(editTextEmail.getText().toString(), editTextPassword.getText().toString()); // 아이다와 비밀번호를 가져오는 부분
            }
        });
    }

    private void createUser(final String email, final String password) { // 회원가입이 끝난다.
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) { // 회원가입 성공하는 부분
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(RegisterActivity.this, "createUserWithEmail:success", Toast.LENGTH_LONG).show();
                } else { // 회원가입 실패하는 부분
                    Toast.makeText(RegisterActivity.this, "createUserWithEmail:failure", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() { // 메인에서는 취소를 두번 누르면 로그인 화면으로 이동
        if (System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "뒤로 버튼을 한번 더 누르면 로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - time < 2000) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

