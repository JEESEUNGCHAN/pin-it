package com.example.pinit.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pinit.R;
import com.example.pinit.database.DatabaseHelper;
import com.example.pinit.model.Trip;

import java.util.Calendar;
import java.util.Locale;

// [화면] 새 여행 추가 폼
// 여행 이름, 목적지, 시작일, 종료일, 예산, 메모를 입력받아 SQLite에 저장
// HomeFragment에서 "새 여행" 버튼을 누르면 진입
public class AddTripActivity extends AppCompatActivity {

    private EditText etTitle, etDestination, etStartDate, etEndDate, etBudget, etMemo;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("여행 추가");
        }

        dbHelper = new DatabaseHelper(this);
        etTitle = findViewById(R.id.etTitle);
        etDestination = findViewById(R.id.etDestination);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etBudget = findViewById(R.id.etBudget);
        etMemo = findViewById(R.id.etMemo);

        // 시작일/종료일 기본값: 오늘 날짜
        Calendar cal = Calendar.getInstance();
        String today = String.format(Locale.KOREA, "%d-%02d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH));
        etStartDate.setText(today);
        etEndDate.setText(today);

        // 날짜 필드 클릭 시 DatePickerDialog 표시
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveTrip());
    }

    // DatePickerDialog를 열어 선택한 날짜를 "yyyy-MM-dd" 형식으로 EditText에 설정
    private void showDatePicker(EditText et) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                et.setText(String.format(Locale.KOREA, "%d-%02d-%02d", year, month+1, day)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // 입력값 검증 후 Trip 객체 생성 → DB 저장 → 화면 닫기
    private void saveTrip() {
        String title = etTitle.getText().toString().trim();
        String destination = etDestination.getText().toString().trim();
        if (title.isEmpty() || destination.isEmpty()) {
            Toast.makeText(this, "여행 이름과 목적지를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        Trip t = new Trip();
        t.setTitle(title);
        t.setDestination(destination);
        t.setStartDate(etStartDate.getText().toString());
        t.setEndDate(etEndDate.getText().toString());
        t.setBudget(etBudget.getText().toString().isEmpty() ? 0 : Double.parseDouble(etBudget.getText().toString()));
        t.setMemo(etMemo.getText().toString());
        dbHelper.insertTrip(t);
        Toast.makeText(this, "여행이 추가되었습니다!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
