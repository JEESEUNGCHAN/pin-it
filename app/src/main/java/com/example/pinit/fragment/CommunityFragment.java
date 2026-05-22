package com.example.pinit.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pinit.R;

// [프래그먼트] BottomNavigation 커뮤니티 탭 (4번째 탭)
// FeedFragment를 호스팅하는 컨테이너 역할
public class CommunityFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.communityContainer, new FeedFragment())
                .commit();

        return view;
    }
}
