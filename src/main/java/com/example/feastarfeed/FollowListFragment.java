package com.example.feastarfeed;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class FollowListFragment extends Fragment implements FollowerAdapter.OnFollowerClickListener,FollowingAdapter.OnFollowingClickListener {



    private DatabaseReference usersRef;
    private String username;
    private OnFollowerClickedListener onFollowerClickedListener;
    private OnFollowingClickedListener onFollowingClickedListener;
    private TextView tvFollowers, tvFollowing;

    private FrameLayout frameFollowlist;


    public interface OnFollowerClickedListener {
        void onFollowerClicked(String follower);
    }

    @Override
    public void onFollowerClick(String follower) {
//        navigateToOthersAccountFragment(follower);
    }

    public interface OnFollowingClickedListener {
        void onFollowingClicked(String following);
    }

//    @Override
//    public void onFollowingClick(String following) {
//        // 處理點擊 following 的邏輯
//    }

//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFollowingClickedListener) {
//            onFollowingClickedListener = (OnFollowingClickedListener) context;
//        } else if (context instanceof OnFollowerClickedListener) {
//            onFollowerClickedListener = (OnFollowerClickedListener) context;
//        } else {
//            throw new RuntimeException(context.toString() + " must implement OnFollowerClickedListener or OnFollowingClickedListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFollowerClickedListener = null;
        onFollowingClickedListener = null;
    }

//    private void initFollowerAdapter() {
//        followerListAdapter = new FollowerListAdapter();
//        followerListAdapter.setOnFollowerClickListener(new FollowerListAdapter.OnFollowerClickListener() {
//            @Override
//            public void onFollowerClick(String follower) {
//                if (onFollowerClickedListener != null) {
//                    onFollowerClickedListener.onFollowerClicked(follower);
//                }
//            }
//        });
//        rvFollowers.setAdapter(followerListAdapter);
//    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following_list, container, false);

        username = SharedPreferencesUtils.getUsername(requireContext());
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        tvFollowers = view.findViewById(R.id.tv_followers);
        tvFollowing = view.findViewById(R.id.tv_following);
        frameFollowlist = view.findViewById(R.id.frame_followlist);

        tvFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFollowerFragment();
            }
        });

        tvFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFollowingFragment();
            }
        });

        showFollowerFragment();


        Log.d("FollowerListFragment", "username = " + username);
        return view;
    }


    private void showFollowerFragment() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_followlist, new FollowerFragment());
        transaction.commit();
    }
    private void showFollowingFragment() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_followlist, new FollowingFragment());
        transaction.commit();
    }
}