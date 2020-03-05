package com.example.diaryproject;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.diaryproject.popup.BoardUploadPopup;
import com.example.diaryproject.popup.CreateCategoryPopup;
import com.example.diaryproject.popup.PostUploadPopup;
import com.example.diaryproject.ui.board.BoardFragment;
import com.facebook.login.LoginManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class    HomeActivity extends AppCompatActivity {
    public static String currentFragment = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_board, R.id.navigation_gallery) // 여기 board와 gallery를 추가해야 클릭시 해당 프레그먼트로 이동이 가능하다.
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration); // 이게 액션바 설정하는 부분
        NavigationUI.setupWithNavController(navView, navController);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentFragment.equals("HomeFrag"))
                {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else if(currentFragment.equals("DashboardFrag"))
                {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else if(currentFragment.equals("NotificationsFrag"))
                {
                    startActivity(new Intent(HomeActivity.this, PostUploadPopup.class)); // 게시글 작성 페이지로 이동한다. ※※※※※※※※※※※※※※※
//                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                }
                else if(currentFragment.equals("BoardFrag"))
                {
                    startActivity(new Intent(HomeActivity.this, BoardUploadPopup.class)); // 게시글 작성하는 액티비티로 이동
//                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                }
                else if(currentFragment.equals("CategoryFrag"))
                {
                    startActivity(new Intent(HomeActivity.this, CreateCategoryPopup.class));
//                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                }
            }
        });
    }
}
