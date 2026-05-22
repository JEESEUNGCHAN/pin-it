package com.example.pinit.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pinit.R;
import com.example.pinit.database.DatabaseHelper;
import com.example.pinit.model.Schedule;

import java.util.Calendar;
import java.util.Locale;

// [화면] 일정 추가/수정 폼
// Intent로 trip_id(필수), schedule_id(수정 시), default_date(기본 날짜) 수신
// scheduleId == -1이면 추가 모드, 그 외는 수정 모드
public class AddScheduleActivity extends AppCompatActivity {

    private EditText etTitle, etDate, etTime, etPlaceName, etMemo;
    private DatabaseHelper dbHelper;
    private int tripId;
    private int scheduleId = -1; // -1이면 추가 모드, 그 외는 수정 모드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getIntExtra("trip_id", -1);
        scheduleId = getIntent().getIntExtra("schedule_id", -1);
        String defaultDate = getIntent().getStringExtra("default_date"); // TripDetailActivity에서 선택된 탭 날짜

        etTitle = findViewById(R.id.etTitle);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etPlaceName = findViewById(R.id.etPlaceName);
        etMemo = findViewById(R.id.etMemo);

        if (scheduleId != -1) {
            // 수정 모드: 기존 일정 데이터 불러와 필드 채우기
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("일정 수정");
            Schedule existing = dbHelper.getScheduleById(scheduleId);
            if (existing != null) {
                etTitle.setText(existing.getTitle());
                etDate.setText(existing.getDate());
                etTime.setText(existing.getTime());
                etPlaceName.setText(existing.getPlaceName());
                etMemo.setText(existing.getMemo());
            }
        } else {
            // 추가 모드: 날짜는 TripDetailActivity에서 받은 탭 날짜 또는 오늘
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("일정 추가");
            if (defaultDate != null) {
                etDate.setText(defaultDate);
            } else {
                Calendar cal = Calendar.getInstance();
                etDate.setText(String.format(Locale.KOREA, "%d-%02d-%02d",
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
            }
        }

        // 날짜 필드 클릭 시 DatePickerDialog 표시
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) ->
                    etDate.setText(String.format(Locale.KOREA, "%d-%02d-%02d", year, month + 1, day)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        findViewById(R.id.btnSave).setOnClickListener(v -> saveSchedule());
    }

    // 입력값 검증 후 추가/수정 분기 처리 → 완료 후 RESULT_OK 반환 (TripDetailActivity가 수신해 목록 갱신)
    private void saveSchedule() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "일정 제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        Schedule s = new Schedule();
        s.setTripId(tripId);
        s.setTitle(title);
        s.setDate(etDate.getText().toString());
        s.setTime(etTime.getText().toString());
        s.setPlaceName(etPlaceName.getText().toString());
        s.setMemo(etMemo.getText().toString());
        s.setColor("#FFDA44"); // 앱 기본 accent 색상

        if (scheduleId != -1) {
            s.setId(scheduleId);
            dbHelper.updateSchedule(s);
            Toast.makeText(this, "일정이 수정되었습니다!", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.insertSchedule(s);
            Toast.makeText(this, "일정이 추가되었습니다!", Toast.LENGTH_SHORT).show();
        }
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
