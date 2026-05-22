package com.example.pinit.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinit.R;
import com.example.pinit.activity.TripDetailActivity;
import com.example.pinit.activity.TripSummaryActivity;
import com.example.pinit.model.Trip;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// [어댑터] 홈 화면의 여행 카드 목록 (HomeFragment → RecyclerView)
// 각 카드: 여행 이름, 목적지+날짜, 예산, D-Day 배지, 상세보기/요약 버튼
// 클릭: TripDetailActivity, 롱클릭: 삭제 확인 (HomeFragment에서 AlertDialog 처리)
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    public interface OnClickListener { void onClick(Trip trip); }

    private Context context;
    private List<Trip> list;
    private OnClickListener clickListener;     // 카드 클릭 (TripDetailActivity 이동)
    private OnClickListener longClickListener; // 카드 롱클릭 (삭제 다이얼로그)

    public TripAdapter(Context context, List<Trip> list,
                       OnClickListener clickListener, OnClickListener longClickListener) {
        this.context = context;
        this.list = list;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void updateList(List<Trip> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip t = list.get(position);
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.KOREA);

        holder.tvTitle.setText(t.getTitle());
        holder.tvDestination.setText("📍 " + t.getDestination()
                + "  📅 " + t.getStartDate() + " - " + t.getEndDate());
        holder.tvDate.setText("");
        holder.tvBudget.setText("예산: " + fmt.format(t.getBudget()) + "원");

        // D-Day 배지: 여행 전이면 "D-n", 당일이면 "D-Day", 이후면 "D+n"
        String dday = calcDday(t.getStartDate());
        if (dday != null) {
            holder.tvDday.setVisibility(View.VISIBLE);
            holder.tvDday.setText(dday);
        } else {
            holder.tvDday.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> clickListener.onClick(t));
        holder.itemView.setOnLongClickListener(v -> { longClickListener.onClick(t); return true; });

        // "상세보기" 버튼 → TripDetailActivity
        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, TripDetailActivity.class);
            intent.putExtra("trip_id", t.getId());
            context.startActivity(intent);
        });

        // "여행요약" 버튼 → TripSummaryActivity
        holder.btnSummary.setOnClickListener(v -> {
            Intent intent = new Intent(context, TripSummaryActivity.class);
            intent.putExtra("trip_id", t.getId());
            context.startActivity(intent);
        });
    }

    // 시작일 기준 D-Day 계산 (밀리초 → 일 단위 변환)
    private String calcDday(String startDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            Date startDate = sdf.parse(startDateStr);
            Date today = new Date();
            long diff = startDate.getTime() - today.getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            if (days > 0) return "D-" + days;
            else if (days == 0) return "D-Day";
            else return "D+" + Math.abs(days);
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDestination, tvDate, tvBudget, tvDday;
        Button btnDetail, btnSummary;

        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvDestination = view.findViewById(R.id.tvDestination);
            tvDate = view.findViewById(R.id.tvDate);
            tvBudget = view.findViewById(R.id.tvBudget);
            tvDday = view.findViewById(R.id.tvDday);
            btnDetail = view.findViewById(R.id.btnDetail);
            btnSummary = view.findViewById(R.id.btnSummary);
        }
    }
}
