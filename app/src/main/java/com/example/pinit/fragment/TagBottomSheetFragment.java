package com.example.pinit.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pinit.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

// [프래그먼트] 게시글 작성 시 태그 선택 BottomSheet (현재 UI 뼈대만 구현)
// CreatePostFragment의 "태그 삽입" 버튼에서 show()로 표시
// layout_tag_bottom_sheet.xml에 태그 목록 UI가 정의되어 있음
// 향후: 선택된 태그를 CreatePostFragment의 입력 필드에 삽입하는 기능 구현 예정
public class TagBottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_tag_bottom_sheet, container, false);
    }
}
