package com.example.diaryproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.diaryproject.popup.BoardUploadPopup;
import com.example.diaryproject.popup.CreateCategoryPopup;
import com.example.diaryproject.popup.GalleryUploadPopup;
import com.example.diaryproject.popup.PostUploadPopup;
import com.facebook.login.LoginManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class HomeActivity extends AppCompatActivity {
    public static String currentFragment = "";

    private long pressedTime = 0; // 취소 버튼을 누를때 사용하는 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_board, R.id.navigation_category) // 여기 board와 gallery를 추가해야 클릭시 해당 프레그먼트로 이동이 가능하다.
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration); // 이게 액션바 설정하는 부분
        NavigationUI.setupWithNavController(navView, navController);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentFragment.equals("HomeFrag")) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else if (currentFragment.equals("DashboardFrag")) {
                    startActivity(new Intent(HomeActivity.this, GalleryUploadPopup.class)); // 임시로 갤러리 액티비티를 실행해본다.
                } else if (currentFragment.equals("NotificationsFrag")) {
                    startActivity(new Intent(HomeActivity.this, PostUploadPopup.class)); // 게시글 작성 페이지로 이동한다. ※※※※※※※※※※※※※※※
//                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                } else if (currentFragment.equals("BoardFrag")) {
                    startActivity(new Intent(HomeActivity.this, BoardUploadPopup.class)); // 게시글 작성하는 액티비티로 이동
//                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                } else if (currentFragment.equals("CategoryFrag")) {
                    startActivity(new Intent(HomeActivity.this, CreateCategoryPopup.class));
//                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                }
            }
        });
    }

    public interface OnBackPressedListener {
        public void onBack();
    }

    // 리스너 객체 생성
    private OnBackPressedListener mBackListener;

    // 리스너 설정 메소드
    public void setOnBackPressedListener(OnBackPressedListener listener) {
        mBackListener = listener;
    }

    // 뒤로가기 버튼을 눌렀을 때의 오버라이드 메소드
    @Override
    public void onBackPressed() {

        // 다른 Fragment 에서 리스너를 설정했을 때 처리됩니다.
        if (mBackListener != null) {
            mBackListener.onBack();
            // 리스너가 설정되지 않은 상태(예를들어 메인Fragment)라면
            // 뒤로가기 버튼을 연속적으로 두번 눌렀을 때 앱이 종료됩니다.
        } else {
            if (pressedTime == 0) {
                Toast.makeText(HomeActivity.this, " 한 번 더 누르면 로그아웃됩니다.", Toast.LENGTH_LONG).show();
                pressedTime = System.currentTimeMillis();
            } else {
                int seconds = (int) (System.currentTimeMillis() - pressedTime);

                if (seconds > 2000) {
                    Toast.makeText(HomeActivity.this, " 한 번 더 누르면 로그아웃됩니다.", Toast.LENGTH_LONG).show();
                    pressedTime = 0;
                } else {
                    super.onBackPressed();
                    logout();
                    //finish();
                    //android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        }
    }

    private void logout(){
        FirebaseAuth auth;
        auth = FirebaseAuth.getInstance();
        auth.signOut(); // 구글, 페이스북 로그아웃도 된다.
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
    }

}
