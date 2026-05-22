package com.example.traveltracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveltracker.R;
import com.example.traveltracker.model.Schedule;

import java.util.List;

// [어댑터] TripDetailActivity의 날짜별 일정 상세 목록
// 기능: 지도 열기(장소 클릭) / 수정(항목 클릭) / 삭제(롱클릭)
// ScheduleAdapter보다 더 많은 상호작용 지원 (TripDetailActivity에서 사용)
public class ScheduleDetailAdapter extends RecyclerView.Adapter<ScheduleDetailAdapter.ViewHolder> {

    public interface OnMapClickListener { void onMapClick(Schedule schedule); }
    public interface OnDeleteListener { void onDelete(int id); }
    public interface OnEditListener { void onEdit(Schedule schedule); }

    private Context context;
    private List<Schedule> list;
    private OnMapClickListener mapClickListener; // "지도 보기" 텍스트 클릭 → Google Maps 열기
    private OnDeleteListener deleteListener;     // 롱클릭 → 삭제 확인 다이얼로그
    private OnEditListener editListener;         // 항목 클릭 → AddScheduleActivity(수정)

    public ScheduleDetailAdapter(Context context, List<Schedule> list,
                                 OnMapClickListener mapClickListener,
                                 OnDeleteListener deleteListener,
                                 OnEditListener editListener) {
        this.context = context;
        this.list = list;
        this.mapClickListener = mapClickListener;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    public void updateList(List<Schedule> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_schedule_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Schedule s = list.get(position);

        // 시간이 없으면 "--:--" 표시
        holder.tvTime.setText(s.getTime() != null && !s.getTime().isEmpty() ? s.getTime() : "--:--");
        holder.tvTitle.setText(s.getTitle());
        holder.tvPlace.setText(s.getPlaceName() != null && !s.getPlaceName().isEmpty()
                ? "📍 " + s.getPlaceName() : "");

        // "지도 보기" 텍스트 클릭 → Google Maps에서 장소 검색
        holder.tvOpenMap.setOnClickListener(v -> mapClickListener.onMapClick(s));

        // 카드 클릭 → 수정
        holder.itemView.setOnClickListener(v -> editListener.onEdit(s));

        // 롱클릭 → 삭제 확인
        holder.itemView.setOnLongClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("일정 삭제")
                    .setMessage("'" + s.getTitle() + "' 일정을 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (d, w) -> deleteListener.onDelete(s.getId()))
                    .setNegativeButton("취소", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTitle, tvPlace, tvOpenMap;

        ViewHolder(View view) {
            super(view);
            tvTime = view.findViewById(R.id.tvTime);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvPlace = view.findViewById(R.id.tvPlace);
            tvOpenMap = view.findViewById(R.id.tvOpenMap); // "지도 보기" 링크 텍스트
        }
    }
}
