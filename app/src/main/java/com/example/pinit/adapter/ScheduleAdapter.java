package com.example.pinit.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinit.R;
import com.example.pinit.model.Schedule;

import java.util.List;

// [어댑터] 일정 목록 단순 표시용 (롱클릭 삭제만 지원)
// TripDetailActivity의 ScheduleDetailAdapter가 더 풍부한 기능(지도/수정/삭제)을 제공하므로
// 현재 이 어댑터는 직접 사용 경로가 제한적
// 각 항목: 왼쪽 색상 바 + 제목 + 시간 + 장소명 + 날짜
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    public interface OnDeleteListener { void onDelete(int id); }

    private Context context;
    private List<Schedule> list;
    private OnDeleteListener listener;

    public ScheduleAdapter(Context context, List<Schedule> list, OnDeleteListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Schedule s = list.get(position);
        holder.tvTitle.setText(s.getTitle());
        holder.tvTime.setText(s.getTime() != null ? s.getTime() : "");
        holder.tvPlace.setText(s.getPlaceName() != null ? "📍 " + s.getPlaceName() : "");
        holder.tvDate.setText(s.getDate());

        // 왼쪽 색상 바: schedule.color로 설정, 파싱 실패 시 파란색 대체
        try { holder.viewColor.setBackgroundColor(Color.parseColor(s.getColor())); }
        catch (Exception e) { holder.viewColor.setBackgroundColor(Color.parseColor("#1976D2")); }

        // 롱클릭 → 삭제 (클릭 이벤트는 없음)
        holder.itemView.setOnLongClickListener(v -> { listener.onDelete(s.getId()); return true; });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvPlace, tvDate;
        View viewColor; // 카드 왼쪽의 색상 인디케이터 바

        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvTime = view.findViewById(R.id.tvTime);
            tvPlace = view.findViewById(R.id.tvMemo);   // 레이아웃에서 tvMemo id를 장소명으로 재활용
            tvDate = view.findViewById(R.id.tvDate) != null ? view.findViewById(R.id.tvDate) : view.findViewById(R.id.tvTitle);
            viewColor = view.findViewById(R.id.viewColor);
        }
    }
}
