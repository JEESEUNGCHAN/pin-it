package com.example.traveltracker.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveltracker.R;
import com.example.traveltracker.model.Record;

import java.util.List;

// [어댑터] TripDetailActivity 하단의 여행 기록 목록
// 각 항목: 제목, 날짜, 본문, 장소명 표시
// 클릭 → TripRecordActivity(수정), 롱클릭 → 삭제 확인 다이얼로그
public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    public interface OnEditListener { void onEdit(Record record); }
    public interface OnDeleteListener { void onDelete(int id); }

    private final Context context;
    private List<Record> list;
    private final OnEditListener editListener;
    private final OnDeleteListener deleteListener;

    // editListener 없이 생성 (삭제만 지원)
    public RecordAdapter(Context context, List<Record> list, OnDeleteListener deleteListener) {
        this.context = context;
        this.list = list;
        this.editListener = null;
        this.deleteListener = deleteListener;
    }

    // editListener 포함 생성 (수정 + 삭제 모두 지원)
    public RecordAdapter(Context context, List<Record> list, OnEditListener editListener, OnDeleteListener deleteListener) {
        this.context = context;
        this.list = list;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Record r = list.get(position);
        holder.tvTitle.setText(r.getTitle());
        holder.tvDate.setText("📅 " + r.getDate());
        holder.tvContent.setText(r.getContent() != null ? r.getContent() : "");
        // 장소명이 있을 때만 표시
        holder.tvPlace.setText(r.getPlaceName() != null && !r.getPlaceName().isEmpty()
                ? "📍 " + r.getPlaceName() : "");

        // 클릭 → 수정
        holder.itemView.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(r);
        });

        // 롱클릭 → 삭제 확인
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("기록 삭제")
                    .setMessage("'" + r.getTitle() + "'을(를) 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (d, w) -> deleteListener.onDelete(r.getId()))
                    .setNegativeButton("취소", null)
                    .show();
            return true;
        });
    }

    public void updateList(List<Record> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvContent, tvPlace;
        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvDate = view.findViewById(R.id.tvDate);
            tvContent = view.findViewById(R.id.tvContent);
            tvPlace = view.findViewById(R.id.tvPlace);
        }
    }
}
