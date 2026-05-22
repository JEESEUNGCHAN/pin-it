package com.example.traveltracker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveltracker.R;

// [프래그먼트] 마이페이지 - 내가 작성한 게시물 목록 표시
// 현재: 빈 어댑터(MyPostAdapter)로 UI 뼈대만 구성, DB 연동 시 실제 데이터 바인딩 예정
public class MyPageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_page, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.myPostRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new MyPostAdapter());

        return view;
    }

    private static class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_page_post, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return 0;
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
