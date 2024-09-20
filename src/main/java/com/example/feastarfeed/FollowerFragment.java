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


public class FollowerFragment extends Fragment implements FollowerAdapter.OnFollowerClickListener {

    private RecyclerView rvFollowers;
    private FollowerAdapter followerAdapter;
    private DatabaseReference usersRef;
    private String username;
    private OnFollowerClickedListener onFollowerClickedListener;


    public interface OnFollowerClickedListener {
        void onFollowerClicked(String follower);
    }

    @Override
    public void onFollowerClick(String follower) {
//        OthersAccountFragment fragment = OthersAccountFragment.newInstance(follower);
//        getParentFragmentManager()
//                .beginTransaction()
//                .replace(R.id.rv_followers, fragment)
//                .addToBackStack(null)
//                .commit();
    }



//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        if  (context instanceof OnFollowerClickedListener) {
//            onFollowerClickedListener = (OnFollowerClickedListener) context;
//        }
//         else {
//            throw new RuntimeException(context.toString() + " must implement OnFollowerClickedListener or OnFollowingClickedListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFollowerClickedListener = null;

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
        View view = inflater.inflate(R.layout.fragment_follower, container, false);

        rvFollowers = view.findViewById(R.id.rv_followers);


        rvFollowers.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        username = SharedPreferencesUtils.getUsername(requireContext());
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        followerAdapter = new FollowerAdapter();
        followerAdapter.setOnFollowerClickListener(this); // 設置 OnFollowerClickListener

        // 設置 Adapter
        rvFollowers.setAdapter(followerAdapter);

        loadFollowers();
        Log.d("FollowerListFragment", "username = " + username);

        return view;
    }


    private void loadFollowers() {
        usersRef.child(username).child("followers")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> followers = new ArrayList<>();
                        int count = 0;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            count+=1;
                            String follower = child.getValue(String.class);
                            followers.add(follower);
                            Log.d("FollowerListFragment", "followers = "+ count);
                        }

                        followerAdapter.setFollowers(followers);

                        followerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }



}