package com.example.pinit.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pinit.R;

// [프래그먼트] 커뮤니티 게시글 작성 화면 (현재 UI 뼈대만 구현)
// CommunityFragment의 FAB 버튼으로 진입
// "태그 삽입" 버튼 클릭 → TagBottomSheetFragment 표시 (위치/여행 태그 선택)
// 향후: Firebase Firestore에 게시글 저장 기능 구현 예정
public class CreatePostFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_post, container, false);

        // "태그 삽입" 버튼 → 태그 선택 BottomSheet 표시
        Button btnInsertTag = view.findViewById(R.id.btnInsertTag);
        btnInsertTag.setOnClickListener(v -> {
            TagBottomSheetFragment bottomSheet = new TagBottomSheetFragment();
            bottomSheet.show(getParentFragmentManager(), "TagBottomSheet");
        });

        return view;
    }
}
