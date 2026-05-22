package com.example.traveltracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.traveltracker.R;
import com.example.traveltracker.fragment.BudgetFragment;
import com.example.traveltracker.fragment.FeedFragment;
import com.example.traveltracker.fragment.HomeFragment;
import com.example.traveltracker.fragment.PlaceFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

// [진입점] 앱의 메인 화면 - BottomNavigation으로 4개 탭 전환
// 탭: 홈(HomeFragment), 장소(PlaceFragment), 예산(BudgetFragment), 커뮤니티(FeedFragment)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // PostSearchActivity에서 검색 결과와 함께 커뮤니티 탭으로 돌아올 때 처리
        int selectedNav = getIntent().getIntExtra("selected_nav", R.id.nav_home);
        String postSearchQuery = getIntent().getStringExtra(PostSearchActivity.EXTRA_SEARCH_QUERY);
        ArrayList<String> travelSettings = getIntent().getStringArrayListExtra("travel_settings");

        bottomNav.setSelectedItemId(selectedNav);
        loadFragment(fragmentForNavId(selectedNav, postSearchQuery, travelSettings));

        // 하단 탭 선택 시 해당 Fragment로 교체
        bottomNav.setOnItemSelectedListener(item -> {
            loadFragment(fragmentForNavId(item.getItemId(), null, null));
            return true;
        });
    }

    // 탭 ID에 따라 적절한 Fragment 반환 (커뮤니티 탭은 검색 조건 Bundle 전달)
    private Fragment fragmentForNavId(int id, String postSearchQuery, ArrayList<String> travelSettings) {
        if (id == R.id.nav_home) return new HomeFragment();
        if (id == R.id.nav_place) return new PlaceFragment();
        if (id == R.id.nav_budget) return new BudgetFragment();
        if (id == R.id.nav_community) {
            FeedFragment fragment = new FeedFragment();
            Bundle args = new Bundle();
            if (postSearchQuery != null) args.putString(PostSearchActivity.EXTRA_SEARCH_QUERY, postSearchQuery);
            if (travelSettings != null) args.putStringArrayList("travel_settings", travelSettings);
            fragment.setArguments(args);
            return fragment;
        }
        return new HomeFragment();
    }

    // 툴바 우상단에 main_menu.xml(로그아웃 항목)을 인플레이트
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // 툴바 메뉴 항목 선택 처리
    // action_logout: Firebase 세션 종료 후 LoginActivity로 이동 (백 스택 전체 초기화)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // fragmentContainer에 Fragment를 replace 방식으로 전환
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment).commit();
    }
}
