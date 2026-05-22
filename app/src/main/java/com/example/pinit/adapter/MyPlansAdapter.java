package com.example.pinit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pinit.R;
import com.example.pinit.model.MyPlan;
import java.util.List;

// [어댑터] 마이플랜 목록 - MyPlansBottomSheetFragment에서 내 여행 목록을 카드로 표시
// 카드 클릭 시 OnPlanSelectedListener 콜백으로 선택된 MyPlan 전달
public class MyPlansAdapter extends RecyclerView.Adapter<MyPlansAdapter.ViewHolder> {

    private List<MyPlan> planList;
    private OnPlanSelectedListener listener;

    public interface OnPlanSelectedListener {
        void onPlanSelected(MyPlan plan);
    }

    public MyPlansAdapter(List<MyPlan> planList, OnPlanSelectedListener listener) {
        this.planList = planList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyPlan plan = planList.get(position);
        holder.tvTitle.setText(plan.getTitle());
        holder.tvDate.setText(plan.getDate());
        holder.tvCountry.setText(plan.getCountry());

        // 카드 전체를 눌렀을 때 일정 선택
        holder.itemView.setOnClickListener(v -> listener.onPlanSelected(plan));
    }

    @Override
    public int getItemCount() { return planList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvCountry;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPlanTitle);
            tvDate = itemView.findViewById(R.id.tvPlanDate);
            tvCountry = itemView.findViewById(R.id.tvPlanCountry);

        }
    }
}