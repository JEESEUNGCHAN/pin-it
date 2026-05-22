package com.example.pinit.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pinit.R;

// [화면] 주변 장소 Activity (미완성 스텁)
// 현재 PlaceFragment의 "주변찾기" 탭(panelNearby)이 같은 역할을 담당하고 있음
// 이 Activity는 레이아웃 재사용(activity_place_search.xml)만 하고 실제 검색 기능은 없음
public class NearbyPlacesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("주변 장소");
        }
    }
    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
