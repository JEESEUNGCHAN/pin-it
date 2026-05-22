package com.example.traveltracker.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveltracker.R;
import com.example.traveltracker.activity.AddBudgetActivity;
import com.example.traveltracker.activity.ReceiptScanActivity;
import com.example.traveltracker.adapter.BudgetAdapter;
import com.example.traveltracker.database.DatabaseHelper;
import com.example.traveltracker.model.Budget;
import com.example.traveltracker.model.Trip;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// [프래그먼트] BottomNavigation 예산 탭 (3번째 탭)
// 상단: 여행 선택 Spinner + 예산 요약 (총예산/사용/잔액 + 카테고리별 분류)
// 하단: 지출/수입 항목 RecyclerView (BudgetAdapter)
// 직접 입력 버튼 → AddBudgetActivity
// 영수증 스캔 버튼 → ReceiptScanActivity → OCR 금액 → AddBudgetActivity (자동 입력)
public class BudgetFragment extends Fragment {

    private static final int REQUEST_SCAN = 100;  // 영수증 스캔 요청 코드
    private static final int REQUEST_ADD = 200;   // 지출 추가 요청 코드
    private static final int REQUEST_EDIT = 300;  // 지출 수정 요청 코드

    private DatabaseHelper dbHelper;
    private BudgetAdapter adapter;
    private RecyclerView recyclerView;
    private Spinner spinnerTrip;           // 여행 선택 스피너
    private TextView tvBudgetTotal, tvBudgetUsed, tvBudgetRemain, tvBudgetCount;
    private TextView tvCatFood, tvCatTransport, tvCatLodge, tvCatShopping, tvCatActivity, tvCatEtc;
    private List<Trip> tripList = new ArrayList<>();
    private int selectedTripId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        spinnerTrip = view.findViewById(R.id.spinnerTrip);
        tvBudgetTotal = view.findViewById(R.id.tvBudgetTotal);
        tvBudgetUsed = view.findViewById(R.id.tvBudgetUsed);
        tvBudgetRemain = view.findViewById(R.id.tvBudgetRemain);
        tvBudgetCount = view.findViewById(R.id.tvBudgetCount);
        tvCatFood = view.findViewById(R.id.tvCatFood);
        tvCatTransport = view.findViewById(R.id.tvCatTransport);
        tvCatLodge = view.findViewById(R.id.tvCatLodge);
        tvCatShopping = view.findViewById(R.id.tvCatShopping);
        tvCatActivity = view.findViewById(R.id.tvCatActivity);
        tvCatEtc = view.findViewById(R.id.tvCatEtc);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BudgetAdapter(requireContext(), new ArrayList<>(),
                budget -> {
                    // 항목 클릭 → 수정 화면
                    Intent intent = new Intent(requireContext(), AddBudgetActivity.class);
                    intent.putExtra("trip_id", selectedTripId);
                    intent.putExtra("budget_id", budget.getId());
                    startActivityForResult(intent, REQUEST_EDIT);
                },
                id -> {
                    dbHelper.deleteBudget(id);
                    loadBudgets();
                });
        recyclerView.setAdapter(adapter);

        // 직접 입력 버튼 → AddBudgetActivity (여행 선택 필수)
        view.findViewById(R.id.btnAddBudget).setOnClickListener(v -> {
            if (selectedTripId == -1) {
                Toast.makeText(requireContext(), "여행을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(requireContext(), AddBudgetActivity.class);
            intent.putExtra("trip_id", selectedTripId);
            startActivityForResult(intent, REQUEST_ADD);
        });

        // 영수증 스캔 버튼 → ReceiptScanActivity
        view.findViewById(R.id.btnScanReceipt).setOnClickListener(v -> {
            if (selectedTripId == -1) {
                Toast.makeText(requireContext(), "여행을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(requireContext(), ReceiptScanActivity.class);
            intent.putExtra("trip_id", selectedTripId);
            startActivityForResult(intent, REQUEST_SCAN);
        });

        loadTrips();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SCAN && data != null) {
                // 영수증 스캔 완료 → 인식된 금액을 AddBudgetActivity로 전달 (자동 입력)
                double amount = data.getDoubleExtra("amount", 0);
                Intent intent = new Intent(requireContext(), AddBudgetActivity.class);
                intent.putExtra("trip_id", selectedTripId);
                intent.putExtra("auto_amount", amount);
                startActivityForResult(intent, REQUEST_ADD);
            } else if (requestCode == REQUEST_ADD || requestCode == REQUEST_EDIT) {
                loadBudgets(); // 추가/수정 완료 → 목록 갱신
            }
        }
    }

    // 탭 복귀 시 새 여행이 추가된 경우를 위해 Spinner 갱신
    @Override
    public void onResume() {
        super.onResume();
        loadTrips();
    }

    // DB에서 전체 여행 목록 조회 → Spinner에 여행 이름 목록 세팅
    private void loadTrips() {
        tripList = dbHelper.getAllTrips();
        if (tripList.isEmpty()) return;

        List<String> tripNames = new ArrayList<>();
        for (Trip t : tripList) tripNames.add(t.getTitle());

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, tripNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrip.setAdapter(spinnerAdapter);

        selectedTripId = tripList.get(0).getId(); // 첫 번째 여행 자동 선택
        loadBudgets();

        // Spinner 선택 변경 → 해당 여행 예산 로드
        spinnerTrip.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTripId = tripList.get(position).getId();
                loadBudgets();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // 선택된 여행의 예산 항목을 조회해 요약 수치 + 어댑터 갱신
    private void loadBudgets() {
        if (selectedTripId == -1 || recyclerView == null) return;
        Trip trip = dbHelper.getTripById(selectedTripId);
        if (trip == null) return;

        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.KOREA);
        List<Budget> budgets = dbHelper.getBudgetsByTrip(selectedTripId);

        // 카테고리별 지출 합계 계산 (type='expense'인 것만)
        double used = 0, food = 0, transport = 0, lodge = 0, shopping = 0, activity = 0, etc = 0;
        for (Budget b : budgets) {
            if ("expense".equals(b.getType())) {
                used += b.getAmount();
                switch (b.getCategory()) {
                    case "식비": food += b.getAmount(); break;
                    case "교통": transport += b.getAmount(); break;
                    case "숙박": lodge += b.getAmount(); break;
                    case "쇼핑": shopping += b.getAmount(); break;
                    case "관광": activity += b.getAmount(); break;
                    default: etc += b.getAmount(); break;
                }
            }
        }

        double remain = trip.getBudget() - used;

        // 요약 수치 업데이트 (잔액 초과 시 빨간색)
        tvBudgetTotal.setText(fmt.format(trip.getBudget()) + "원");
        tvBudgetUsed.setText(fmt.format(used) + "원");
        tvBudgetRemain.setText(fmt.format(remain) + "원");
        tvBudgetRemain.setTextColor(remain < 0 ? Color.RED : Color.parseColor("#333333"));
        tvBudgetCount.setText("총 " + budgets.size() + "건");

        // 카테고리별 지출액 업데이트
        tvCatFood.setText(fmt.format(food) + "원");
        tvCatTransport.setText(fmt.format(transport) + "원");
        tvCatLodge.setText(fmt.format(lodge) + "원");
        tvCatShopping.setText(fmt.format(shopping) + "원");
        tvCatActivity.setText(fmt.format(activity) + "원");
        tvCatEtc.setText(fmt.format(etc) + "원");

        // 어댑터 교체 (수정 + 삭제 콜백 포함)
        adapter = new BudgetAdapter(requireContext(), budgets,
                budget -> {
                    Intent intent = new Intent(requireContext(), AddBudgetActivity.class);
                    intent.putExtra("trip_id", selectedTripId);
                    intent.putExtra("budget_id", budget.getId());
                    startActivityForResult(intent, REQUEST_EDIT);
                },
                id -> {
                    dbHelper.deleteBudget(id);
                    loadBudgets();
                });
        recyclerView.setAdapter(adapter);
    }
}
