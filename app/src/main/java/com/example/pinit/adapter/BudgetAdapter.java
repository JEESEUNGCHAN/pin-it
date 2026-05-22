package com.example.pinit.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pinit.R;
import com.example.pinit.model.Budget;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

// [어댑터] 예산 탭(BudgetFragment)의 지출/수입 항목 목록
// 수입은 초록색(+), 지출은 빨간색(-), 카테고리별 이모지 아이콘 표시
// 클릭 → 수정(AddBudgetActivity), 롱클릭 → 삭제 확인 다이얼로그
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    public interface OnEditListener { void onEdit(Budget budget); }
    public interface OnDeleteListener { void onDelete(int id); }

    private final Context context;
    private List<Budget> list;
    private final OnEditListener editListener;
    private final OnDeleteListener deleteListener;

    // editListener 없이 생성 (삭제만 지원하는 경우)
    public BudgetAdapter(Context context, List<Budget> list, OnDeleteListener deleteListener) {
        this.context = context;
        this.list = list;
        this.editListener = null;
        this.deleteListener = deleteListener;
    }

    // editListener 포함 생성 (수정 + 삭제 모두 지원)
    public BudgetAdapter(Context context, List<Budget> list, OnEditListener editListener, OnDeleteListener deleteListener) {
        this.context = context;
        this.list = list;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget b = list.get(position);
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.KOREA);
        holder.tvTitle.setText(b.getTitle());
        holder.tvCategory.setText(b.getCategory() + " · " + b.getDate());

        // 수입: 초록색 + 금액 / 지출: 빨간색 - 금액 + 카테고리 이모지
        if ("income".equals(b.getType())) {
            holder.tvAmount.setText("+" + fmt.format(b.getAmount()) + "원");
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50"));
            holder.tvIcon.setText("💰");
        } else {
            holder.tvAmount.setText("-" + fmt.format(b.getAmount()) + "원");
            holder.tvAmount.setTextColor(Color.parseColor("#F44336"));
            holder.tvIcon.setText(getCategoryEmoji(b.getCategory()));
        }

        // 클릭 → 수정 (editListener가 있을 때만)
        holder.itemView.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(b);
        });

        // 롱클릭 → 삭제 확인
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("지출 삭제")
                    .setMessage("'" + b.getTitle() + "'을(를) 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (d, w) -> deleteListener.onDelete(b.getId()))
                    .setNegativeButton("취소", null)
                    .show();
            return true;
        });
    }

    // 카테고리명 → 이모지 아이콘 변환
    private String getCategoryEmoji(String cat) {
        if (cat == null) return "💸";
        switch (cat) {
            case "식비": return "🍽️";
            case "교통": return "🚌";
            case "숙박": return "🏨";
            case "쇼핑": return "🛍️";
            case "관광": return "🏛️";
            default: return "💸";
        }
    }

    public void updateList(List<Budget> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle, tvCategory, tvAmount;
        ViewHolder(View view) {
            super(view);
            tvIcon = view.findViewById(R.id.tvIcon);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvCategory = view.findViewById(R.id.tvCategory);
            tvAmount = view.findViewById(R.id.tvAmount);
        }
    }
}
