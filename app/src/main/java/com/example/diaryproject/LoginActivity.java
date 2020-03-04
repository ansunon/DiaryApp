package com.example.diaryproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/// 페이스북 연동시 앱 연결한 자바 클래스를 LoginActivity로 했다.
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String GOOLGE_TAG = "GoogleActivity"; // 로그를 찍기위해서 어떤 부분에서 에러가 났는지를 판단하기위해 문자열 지정
    private static final String FACEBOOK_TAG = "FacebookActivity";
    private static final int RC_SIGN_IN = 1;

    private long time = 0; // 취소 버튼을 누를때 사용하는 변수

    private CallbackManager mCallbackManager; // 콜백 리스너

    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseAuth mAuth; // 파이어베이스 변수
    private FirebaseAuth.AuthStateListener mAuthListener; // 이메일 로그인을 위한 리스너

    private EditText emailTextview; // 로그인에서 아이디
    private EditText passwordTextview; // 비밀번호

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Button listeners
        findViewById(R.id.signInButton).setOnClickListener(this);
        findViewById(R.id.email_login_button).setOnClickListener(this); // email로 로그인 하는 버튼
        findViewById(R.id.register_textview).setOnClickListener(this); // 회원가입하는 부분

        emailTextview = findViewById(R.id.email_edittext);
        passwordTextview = findViewById(R.id.password_edittext);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(FACEBOOK_TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(FACEBOOK_TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(FACEBOOK_TAG, "facebook: onError", error);
            }
        });// ...

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else { // 로그인이 안되어있는 부분

                }
            }
        };
    }

    // facebook 로그인 시에 사용되는 함수
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(FACEBOOK_TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(FACEBOOK_TAG, "signInWithCredential:success");

                    Toast.makeText(LoginActivity.this, "current user: " + mAuth.getCurrentUser().getDisplayName(), Toast.LENGTH_LONG).show();
                    //FirebaseUser user = mAuth.getCurrentUser(); // 리스너에 현재 접속한 계정을 넣어준다.
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(LoginActivity.this, "fail", Toast.LENGTH_LONG).show();
                    Log.w(FACEBOOK_TAG, "signInWithCredential:failure", task.getException());
                }
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthListener); // 리스너에 연결해주는 부분 귀를 붙혀주는 부분
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener); // 귀를 때는 부분
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data); // facebook으로 로그인 하는 부분

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try { // 로그인을 성공하는 부분
                Log.w(GOOLGE_TAG, "Google sign in success");
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(GOOLGE_TAG, "Google sign in failed", e);
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(GOOLGE_TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.w(GOOLGE_TAG, "Google sign in success");
                            //FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            Log.w(GOOLGE_TAG, "Google sign in failed");
                        }
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.signInButton) {
            signIn();
        } else if (i == R.id.email_login_button) {
            if (!emailTextview.getText().toString().equals("") && !passwordTextview.getText().toString().equals("") ) {
                loginUser(emailTextview.getText().toString(), passwordTextview.getText().toString()); // 여기서 인텐트를 시작하면 안돼
            } else {
                Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_LONG).show();
            }
        } else if (i == R.id.register_textview) { //회원가입 부분을 클릭할 때
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void loginUser(final String email, final String password) { // email 로 로그인하는 부분
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "email login success.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "email login failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}


