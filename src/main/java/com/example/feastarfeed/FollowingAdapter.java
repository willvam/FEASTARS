package com.example.feastarfeed;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.FollowingViewHolder> {
    private List<String> following = new ArrayList<>();
    private List<String> profileImageUrls = new ArrayList<>();

    public interface OnFollowingClickListener {
        void onFollowerClick(String follower);
    }
    public void setFollowing(List<String> following) {
        this.following = new ArrayList<>(following);
        notifyItemRangeChanged(0, following.size());
    }

    private OnFollowingClickListener onFollowingClickListener;

    public void setData(List<String> following) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        this.following = following;
        this.profileImageUrls.clear();

        for (String followed : following) {
            usersRef.child(followed).child("profileImageUrl")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String profileImageUrl = snapshot.getValue(String.class);
                            profileImageUrls.add(profileImageUrl);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error
                        }
                    });
        }
    }

    public void setOnFollowerClickListener(OnFollowingClickListener listener) {
        this.onFollowingClickListener = listener;
    }

    @NonNull
    @Override
    public FollowingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new FollowingViewHolder(view, following, onFollowingClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowingViewHolder holder, int position) {
        String followingUser = following.get(position);
        ((FollowingViewHolder) holder).bind(followingUser);
    }

    @Override
    public int getItemCount() {
        return following.size();
    }

    public static class FollowingViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivProfileImage;
        private TextView tvUsername;
        private OnFollowingClickListener listener;
        private List<String> following; // 移除了這一行

        public FollowingViewHolder(@NonNull View itemView, List<String> following, OnFollowingClickListener listener) {
            super(itemView);
            this.following = following; // 添加這一行
            this.listener = listener;
            ivProfileImage = itemView.findViewById(R.id.iv_profile_image);
            tvUsername = itemView.findViewById(R.id.tv_username);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String follower = following.get(getAdapterPosition()); // 使用 this.followers 訪問
                    if (listener != null) {
                        listener.onFollowerClick(follower);
                    }
                }
            });
        }

        public void bind(String follower) {
            tvUsername.setText(follower);
            loadProfileImage(follower);
        }
        private void loadProfileImage(String followingUser) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
            usersRef.child(followingUser).child("profileImageUrl")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String profileImageUrl = snapshot.getValue(String.class);
                            Glide.with(itemView.getContext()).load(profileImageUrl).into(ivProfileImage);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // 處理錯誤
                        }
                    });
        }
    }

}