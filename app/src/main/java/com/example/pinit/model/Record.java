package com.example.pinit.model;

// [모델] 여행 기록(일기) 하나를 나타내는 데이터 클래스
// SQLite records 테이블의 한 행과 1:1 대응
// TripDetailActivity에서 목록으로 표시, TripRecordActivity에서 추가/수정
public class Record {
    private int id;           // DB 자동증가 고유번호
    private int tripId;       // 어느 여행의 기록인지 (trips.id 참조)
    private String title;     // 기록 제목 (예: "라멘 맛집 발견!")
    private String date;      // 날짜 "yyyy-MM-dd"
    private String content;   // 본문 내용 (자유 텍스트)
    private String imagePath; // 첨부 이미지 파일 경로 (현재 UI 미표시, 필드만 있음)
    private String placeName; // 장소명 (선택 입력)

    public Record() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }
}
