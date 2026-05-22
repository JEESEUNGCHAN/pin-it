package com.example.traveltracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.traveltracker.model.Budget;
import com.example.traveltracker.model.Record;
import com.example.traveltracker.model.Schedule;
import com.example.traveltracker.model.Trip;

import java.util.ArrayList;
import java.util.List;

// [DB] 앱의 로컬 SQLite 데이터베이스 관리 클래스
// 4개 테이블(trips, schedules, budgets, records)의 CRUD를 담당
// 향후 Firebase/REST API로 교체 시 이 클래스만 수정하면 됨
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "traveltracker.db";  // DB 파일명
    private static final int DB_VERSION = 1;                    // 스키마 버전 (변경 시 onUpgrade 호출)

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // 앱 최초 실행 시 테이블 생성
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE trips (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, destination TEXT, start_date TEXT, end_date TEXT, budget REAL, memo TEXT, cover_image TEXT)");
        db.execSQL("CREATE TABLE schedules (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, title TEXT, date TEXT, time TEXT, place_name TEXT, memo TEXT, color TEXT)");
        db.execSQL("CREATE TABLE budgets (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, title TEXT, amount REAL, category TEXT, date TEXT, type TEXT, memo TEXT)");
        db.execSQL("CREATE TABLE records (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, title TEXT, date TEXT, content TEXT, image_path TEXT, place_name TEXT)");
    }

    // DB 버전 변경 시 기존 테이블 삭제 후 재생성 (데이터 전체 초기화됨)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS trips");
        db.execSQL("DROP TABLE IF EXISTS schedules");
        db.execSQL("DROP TABLE IF EXISTS budgets");
        db.execSQL("DROP TABLE IF EXISTS records");
        onCreate(db);
    }

    // ========== TRIP ==========

    // 새 여행 저장, 생성된 row id 반환
    public long insertTrip(Trip t) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", t.getTitle()); v.put("destination", t.getDestination());
        v.put("start_date", t.getStartDate()); v.put("end_date", t.getEndDate());
        v.put("budget", t.getBudget()); v.put("memo", t.getMemo());
        v.put("cover_image", t.getCoverImage());
        long id = db.insert("trips", null, v);
        db.close(); return id;
    }

    // 전체 여행 목록 조회 (최신순 = id DESC)
    public List<Trip> getAllTrips() {
        List<Trip> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("trips", null, null, null, null, null, "id DESC");
        if (c.moveToFirst()) do { list.add(cursorToTrip(c)); } while (c.moveToNext());
        c.close(); db.close(); return list;
    }

    // 특정 여행 1건 조회
    public Trip getTripById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("trips", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        Trip t = null;
        if (c.moveToFirst()) t = cursorToTrip(c);
        c.close(); db.close(); return t;
    }

    // 여행 삭제 시 연관된 일정/예산/기록도 함께 삭제 (cascade 대신 수동 삭제)
    public void deleteTrip(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("trips", "id=?", new String[]{String.valueOf(id)});
        db.delete("schedules", "trip_id=?", new String[]{String.valueOf(id)});
        db.delete("budgets", "trip_id=?", new String[]{String.valueOf(id)});
        db.delete("records", "trip_id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Cursor 행 → Trip 객체 변환 (내부 헬퍼)
    private Trip cursorToTrip(Cursor c) {
        Trip t = new Trip();
        t.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        t.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
        t.setDestination(c.getString(c.getColumnIndexOrThrow("destination")));
        t.setStartDate(c.getString(c.getColumnIndexOrThrow("start_date")));
        t.setEndDate(c.getString(c.getColumnIndexOrThrow("end_date")));
        t.setBudget(c.getDouble(c.getColumnIndexOrThrow("budget")));
        t.setMemo(c.getString(c.getColumnIndexOrThrow("memo")));
        t.setCoverImage(c.getString(c.getColumnIndexOrThrow("cover_image")));
        return t;
    }

    // ========== SCHEDULE ==========

    // 새 일정 저장
    public long insertSchedule(Schedule s) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("trip_id", s.getTripId()); v.put("title", s.getTitle());
        v.put("date", s.getDate()); v.put("time", s.getTime());
        v.put("place_name", s.getPlaceName()); v.put("memo", s.getMemo());
        v.put("color", s.getColor());
        long id = db.insert("schedules", null, v);
        db.close(); return id;
    }

    // 특정 일정 1건 조회 (수정 화면 진입 시 사용)
    public Schedule getScheduleById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("schedules", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        Schedule s = null;
        if (c.moveToFirst()) s = cursorToSchedule(c);
        c.close(); db.close(); return s;
    }

    // 특정 여행의 모든 일정 조회 (날짜·시간 오름차순)
    public List<Schedule> getSchedulesByTrip(int tripId) {
        List<Schedule> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("schedules", null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "date ASC, time ASC");
        if (c.moveToFirst()) do { list.add(cursorToSchedule(c)); } while (c.moveToNext());
        c.close(); db.close(); return list;
    }

    // 일정 수정 (id 기준)
    public void updateSchedule(Schedule s) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", s.getTitle()); v.put("date", s.getDate());
        v.put("time", s.getTime()); v.put("place_name", s.getPlaceName());
        v.put("memo", s.getMemo()); v.put("color", s.getColor());
        db.update("schedules", v, "id=?", new String[]{String.valueOf(s.getId())});
        db.close();
    }

    // 일정 삭제
    public void deleteSchedule(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("schedules", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Cursor 행 → Schedule 객체 변환 (내부 헬퍼)
    private Schedule cursorToSchedule(Cursor c) {
        Schedule s = new Schedule();
        s.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        s.setTripId(c.getInt(c.getColumnIndexOrThrow("trip_id")));
        s.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
        s.setDate(c.getString(c.getColumnIndexOrThrow("date")));
        s.setTime(c.getString(c.getColumnIndexOrThrow("time")));
        s.setPlaceName(c.getString(c.getColumnIndexOrThrow("place_name")));
        s.setMemo(c.getString(c.getColumnIndexOrThrow("memo")));
        s.setColor(c.getString(c.getColumnIndexOrThrow("color")));
        return s;
    }

    // ========== BUDGET ==========

    // 새 예산/지출 항목 저장
    public long insertBudget(Budget b) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("trip_id", b.getTripId()); v.put("title", b.getTitle());
        v.put("amount", b.getAmount()); v.put("category", b.getCategory());
        v.put("date", b.getDate()); v.put("type", b.getType()); v.put("memo", b.getMemo());
        long id = db.insert("budgets", null, v);
        db.close(); return id;
    }

    // 특정 여행의 모든 예산 항목 조회 (최신순)
    public List<Budget> getBudgetsByTrip(int tripId) {
        List<Budget> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("budgets", null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "date DESC");
        if (c.moveToFirst()) do { list.add(cursorToBudget(c)); } while (c.moveToNext());
        c.close(); db.close(); return list;
    }

    // 특정 여행의 총 지출 합계 계산 (type='expense'인 것만)
    public double getTotalExpense(int tripId) {
        SQLiteDatabase db = getReadableDatabase();
        double total = 0;
        Cursor c = db.rawQuery("SELECT SUM(amount) FROM budgets WHERE trip_id=? AND type='expense'", new String[]{String.valueOf(tripId)});
        if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0);
        c.close(); db.close(); return total;
    }

    // 특정 예산 항목 1건 조회 (수정 화면 진입 시 사용)
    public Budget getBudgetById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("budgets", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        Budget b = null;
        if (c.moveToFirst()) b = cursorToBudget(c);
        c.close(); db.close(); return b;
    }

    // 예산 항목 수정
    public void updateBudget(Budget b) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", b.getTitle()); v.put("amount", b.getAmount());
        v.put("category", b.getCategory()); v.put("date", b.getDate());
        v.put("type", b.getType()); v.put("memo", b.getMemo());
        db.update("budgets", v, "id=?", new String[]{String.valueOf(b.getId())});
        db.close();
    }

    // 예산 항목 삭제
    public void deleteBudget(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("budgets", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Cursor 행 → Budget 객체 변환 (내부 헬퍼)
    private Budget cursorToBudget(Cursor c) {
        Budget b = new Budget();
        b.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        b.setTripId(c.getInt(c.getColumnIndexOrThrow("trip_id")));
        b.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
        b.setAmount(c.getDouble(c.getColumnIndexOrThrow("amount")));
        b.setCategory(c.getString(c.getColumnIndexOrThrow("category")));
        b.setDate(c.getString(c.getColumnIndexOrThrow("date")));
        b.setType(c.getString(c.getColumnIndexOrThrow("type")));
        b.setMemo(c.getString(c.getColumnIndexOrThrow("memo")));
        return b;
    }

    // ========== RECORD ==========

    // 새 여행 기록 저장
    public long insertRecord(Record r) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("trip_id", r.getTripId()); v.put("title", r.getTitle());
        v.put("date", r.getDate()); v.put("content", r.getContent());
        v.put("image_path", r.getImagePath()); v.put("place_name", r.getPlaceName());
        long id = db.insert("records", null, v);
        db.close(); return id;
    }

    // 특정 여행의 모든 기록 조회 (최신순)
    public List<Record> getRecordsByTrip(int tripId) {
        List<Record> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("records", null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "date DESC");
        if (c.moveToFirst()) do { list.add(cursorToRecord(c)); } while (c.moveToNext());
        c.close(); db.close(); return list;
    }

    // 특정 기록 1건 조회 (수정 화면 진입 시 사용)
    public Record getRecordById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("records", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        Record r = null;
        if (c.moveToFirst()) r = cursorToRecord(c);
        c.close(); db.close(); return r;
    }

    // 여행 기록 수정
    public void updateRecord(Record r) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", r.getTitle()); v.put("date", r.getDate());
        v.put("content", r.getContent()); v.put("image_path", r.getImagePath());
        v.put("place_name", r.getPlaceName());
        db.update("records", v, "id=?", new String[]{String.valueOf(r.getId())});
        db.close();
    }

    // 여행 기록 삭제
    public void deleteRecord(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("records", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Cursor 행 → Record 객체 변환 (내부 헬퍼)
    private Record cursorToRecord(Cursor c) {
        Record r = new Record();
        r.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        r.setTripId(c.getInt(c.getColumnIndexOrThrow("trip_id")));
        r.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
        r.setDate(c.getString(c.getColumnIndexOrThrow("date")));
        r.setContent(c.getString(c.getColumnIndexOrThrow("content")));
        r.setImagePath(c.getString(c.getColumnIndexOrThrow("image_path")));
        r.setPlaceName(c.getString(c.getColumnIndexOrThrow("place_name")));
        return r;
    }
}
