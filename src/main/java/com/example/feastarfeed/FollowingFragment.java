package com.example.feastarfeed;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FollowingFragment extends Fragment  {

    private RecyclerView rvFollowing;
    private FollowingAdapter followingAdapter;
    private DatabaseReference usersRef;
    private String username;

    private OnFollowingClickedListener onFollowingClickedListener;


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
        View view = inflater.inflate(R.layout.fragment_following, container, false);

        rvFollowing = view.findViewById(R.id.rv_following);

        rvFollowing.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        username = SharedPreferencesUtils.getUsername(requireContext());
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        followingAdapter = new FollowingAdapter();
//        followingAdapter.setOnFollowingClickListener(this); // 設置 OnFollowingClickListener

        // 設置 Adapter

        rvFollowing.setAdapter(followingAdapter);


        loadFollowing();
        Log.d("FollowerListFragment", "username = " + username);

        return view;
    }


    private void loadFollowing() {
        usersRef.child(username).child("followed")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> following = new ArrayList<>();
                        int count = 0;

                        for (DataSnapshot child : snapshot.getChildren()) {
                            count+=1;

                            String followingUser = child.getValue(String.class);
                            following.add(followingUser);
                            Log.d("FollowerListFragment", "followed = "+count );
                        }
                        followingAdapter.setFollowing(following);
                        Log.d("FollowerListFragment", "following777 = "+count );

                        followingAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }
}