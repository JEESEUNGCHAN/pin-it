package com.example.pinit.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pinit.R;
import com.example.pinit.database.PlacesApiHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Map;

// [화면] 장소 상세 정보 화면
// Intent로 place_id, place_name 수신
// Google Places API Detail 호출 → 이름/주소/평점/전화/웹사이트/영업시간/리뷰 표시
// FragmentContainerView 안의 SupportMapFragment로 지도에 핀 표시
// tvHours/tvOpenNow/tvReviews는 레이아웃에 없을 수 있어 null 체크 필수
public class PlaceDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private PlacesApiHelper apiHelper;
    private TextView tvName, tvAddress, tvRating, tvPhone, tvWebsite, tvHours, tvOpenNow, tvReviews;
    private ProgressBar progressBar;
    private GoogleMap googleMap;
    private LatLng placeLatLng; // API에서 받은 장소 좌표 (지도 핀 표시용)
    private String placeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getIntent().getStringExtra("place_name"));
        }

        apiHelper = new PlacesApiHelper();
        placeName = getIntent().getStringExtra("place_name");

        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvRating = findViewById(R.id.tvRating);
        tvPhone = findViewById(R.id.tvPhone);
        tvWebsite = findViewById(R.id.tvWebsite);
        tvHours = findViewById(R.id.tvHours);       // 레이아웃에 없으면 null
        tvOpenNow = findViewById(R.id.tvOpenNow);   // 레이아웃에 없으면 null
        tvReviews = findViewById(R.id.tvReviews);   // 레이아웃에 없으면 null
        progressBar = findViewById(R.id.progressBar);

        // 지도 초기화 (FragmentContainerView → SupportMapFragment)
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        String placeId = getIntent().getStringExtra("place_id");
        if (placeId != null) loadDetail(placeId);

        // 웹사이트 클릭 → 브라우저 열기
        tvWebsite.setOnClickListener(v -> {
            String url = tvWebsite.getText().toString();
            if (!url.isEmpty() && !url.equals("-")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });

        // 전화번호 클릭 → 전화 앱 열기
        tvPhone.setOnClickListener(v -> {
            String phone = tvPhone.getText().toString();
            if (!phone.isEmpty() && !phone.equals("-")) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
            }
        });
    }

    // Places API Detail 호출 → UI 바인딩 + 지도 핀 표시
    private void loadDetail(String placeId) {
        progressBar.setVisibility(View.VISIBLE);
        apiHelper.getPlaceDetail(placeId, new PlacesApiHelper.PlacesCallback() {
            @Override
            public void onSuccess(List<Map<String, String>> places) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (places.isEmpty()) return;
                    Map<String, String> detail = places.get(0);

                    tvName.setText(detail.getOrDefault("name", ""));
                    tvAddress.setText("📍 " + detail.getOrDefault("address", "-"));
                    tvRating.setText("⭐ " + detail.getOrDefault("rating", "-") + " 점");
                    tvPhone.setText("📞 " + detail.getOrDefault("phone", "-"));
                    tvWebsite.setText(detail.getOrDefault("website", "-"));
                    if (tvHours != null) tvHours.setText(detail.getOrDefault("hours", "-"));

                    // 영업 중/종료 여부: 초록/빨강 색상으로 구분
                    String openNow = detail.getOrDefault("open_now", "");
                    if (tvOpenNow != null) {
                        tvOpenNow.setText(openNow);
                        tvOpenNow.setTextColor(openNow.equals("영업 중")
                                ? android.graphics.Color.parseColor("#4CAF50")
                                : android.graphics.Color.parseColor("#F44336"));
                    }

                    if (tvReviews != null) tvReviews.setText(detail.getOrDefault("reviews", "리뷰 없음"));

                    // 좌표가 있으면 바로 지도에 핀 표시, 없으면 주소로 지오코딩
                    String latStr = detail.getOrDefault("lat", "");
                    String lngStr = detail.getOrDefault("lng", "");
                    if (!latStr.isEmpty() && !lngStr.isEmpty()) {
                        try {
                            double lat = Double.parseDouble(latStr);
                            double lng = Double.parseDouble(lngStr);
                            placeLatLng = new LatLng(lat, lng);
                            showOnMap(placeLatLng, detail.getOrDefault("name", placeName));
                        } catch (NumberFormatException ignored) {}
                    } else {
                        String address = detail.getOrDefault("address", "");
                        if (!address.isEmpty()) geocodeAndShow(address, detail.getOrDefault("name", placeName));
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PlaceDetailActivity.this, "정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        // API 응답보다 지도가 먼저 준비되면 여기서 핀 표시
        if (placeLatLng != null) {
            showOnMap(placeLatLng, placeName);
        }
    }

    // 지도에 노란 핀 마커 추가 후 줌 이동
    private void showOnMap(LatLng latLng, String name) {
        if (googleMap == null || latLng == null) return;
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
    }

    // 좌표 없을 때: Google Geocoding API로 주소 → 좌표 변환 후 지도 표시
    private void geocodeAndShow(String address, String name) {
        new Thread(() -> {
            try {
                String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                        + java.net.URLEncoder.encode(address, "UTF-8")
                        + "&language=ko&key=" + PlacesApiHelper.API_KEY;
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
                okhttp3.Response response = client.newCall(request).execute();
                String body = response.body().string();

                org.json.JSONObject json = new org.json.JSONObject(body);
                org.json.JSONArray results = json.optJSONArray("results");
                if (results != null && results.length() > 0) {
                    org.json.JSONObject loc = results.getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location");
                    double lat = loc.getDouble("lat");
                    double lng = loc.getDouble("lng");
                    placeLatLng = new LatLng(lat, lng);
                    runOnUiThread(() -> showOnMap(placeLatLng, name));
                }
            } catch (Exception ignored) {}
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
