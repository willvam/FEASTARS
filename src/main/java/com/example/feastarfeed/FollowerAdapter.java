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


public class FollowerAdapter extends RecyclerView.Adapter<FollowerAdapter.FollowerViewHolder> {
    private List<String> followers = new ArrayList<>();

    private List<String> profileImageUrls = new ArrayList<>();

    public interface OnFollowerClickListener {
        void onFollowerClick(String follower);
    }
    @Override
    public int getItemCount() {
        return followers.size();
    }

    private OnFollowerClickListener onFollowerClickListener;
    public void setFollowers(List<String> followers) {
        this.followers = new ArrayList<>(followers);
        notifyItemRangeChanged(0, followers.size());
    }
    public void setData(List<String> followers) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        this.followers = followers;
        this.profileImageUrls.clear();

        for (String follower : followers) {
            usersRef.child(follower).child("profileImageUrl")
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

    public void setOnFollowerClickListener(OnFollowerClickListener listener) {
        this.onFollowerClickListener = listener;
    }

    @NonNull
    @Override
    public FollowerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new FollowerViewHolder(view, followers, onFollowerClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowerViewHolder holder, int position) {
        String follower = followers.get(position);
        ((FollowerViewHolder) holder).bind(follower);
    }



    public static class FollowerViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivProfileImage;
        private TextView tvUsername;
        private OnFollowerClickListener listener;
        private List<String> followers; // 移除了這一行

        public FollowerViewHolder(@NonNull View itemView, List<String> followers, OnFollowerClickListener listener) {
            super(itemView);
            this.followers = followers; // 添加這一行
            this.listener = listener;
            ivProfileImage = itemView.findViewById(R.id.iv_profile_image);
            tvUsername = itemView.findViewById(R.id.tv_username);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String follower = followers.get(getAdapterPosition()); // 使用 this.followers 訪問
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
        private void loadProfileImage(String follower) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
            usersRef.child(follower).child("profileImageUrl")
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