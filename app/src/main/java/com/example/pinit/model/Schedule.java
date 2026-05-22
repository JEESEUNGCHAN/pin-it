package com.example.pinit.model;

// [모델] 여행 일정 하나를 나타내는 데이터 클래스
// SQLite schedules 테이블의 한 행과 1:1 대응
// TripDetailActivity의 날짜 탭별 일정 목록에 사용
public class Schedule {
    private int id;           // DB 자동증가 고유번호
    private int tripId;       // 어느 여행의 일정인지 (trips.id 참조)
    private String title;     // 일정 제목 (예: "도쿄 타워 방문")
    private String date;      // 날짜 "yyyy-MM-dd"
    private String time;      // 시간 "HH:mm" (선택 입력)
    private String placeName; // 장소명 또는 주소
    private String memo;      // 메모
    private String color;     // 일정 색상 (hex, 예: "#FFDA44")

    public Schedule() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
