package com.example.pinit.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinit.R;
import com.example.pinit.activity.AddTripActivity;
import com.example.pinit.activity.TripDetailActivity;
import com.example.pinit.adapter.TripAdapter;
import com.example.pinit.database.DatabaseHelper;
import com.example.pinit.model.Trip;

import java.util.List;

// [프래그먼트] BottomNavigation 홈 탭 (1번째 탭)
// 여행 목록을 TripAdapter로 RecyclerView에 표시
// 여행 없을 때: layoutEmpty(빈 화면 안내) 표시
// 새 여행: btnNewTrip / btnMakePlan 버튼 → AddTripActivity
// 카드 클릭: TripDetailActivity, 롱클릭: 삭제 확인 다이얼로그
public class HomeFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TripAdapter adapter;
    private RecyclerView recyclerView;
    private View layoutEmpty; // 여행이 없을 때 표시하는 안내 레이아웃

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerView = view.findViewById(R.id.recyclerView);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TripAdapter(requireContext(), new java.util.ArrayList<>(),
                trip -> {
                    // 카드 클릭 → 여행 상세 화면
                    Intent intent = new Intent(requireContext(), TripDetailActivity.class);
                    intent.putExtra("trip_id", trip.getId());
                    startActivity(intent);
                },
                trip -> {
                    // 롱클릭 → 삭제 확인 다이얼로그
                    new AlertDialog.Builder(requireContext())
                            .setTitle("여행 삭제")
                            .setMessage("'" + trip.getTitle() + "' 여행을 삭제하시겠습니까?")
                            .setPositiveButton("삭제", (d, w) -> {
                                dbHelper.deleteTrip(trip.getId()); // 연관 일정/예산/기록 함께 삭제
                                loadData();
                            })
                            .setNegativeButton("취소", null).show();
                });
        recyclerView.setAdapter(adapter);

        // "새 여행 추가" / "여행 계획 만들기" 버튼 모두 AddTripActivity로 이동
        view.findViewById(R.id.btnNewTrip).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddTripActivity.class)));

        view.findViewById(R.id.btnMakePlan).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddTripActivity.class)));

        loadData();
        return view;
    }

    // 탭 복귀 시 새로 추가된 여행을 반영하기 위해 onResume에서 목록 갱신
    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    // DB에서 전체 여행 목록 조회 → 어댑터 갱신 + 빈 화면/목록 표시 전환
    private void loadData() {
        List<Trip> trips = dbHelper.getAllTrips();
        adapter.updateList(trips);
        boolean isEmpty = trips.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
