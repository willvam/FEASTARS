package com.example.feastarfeed;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public Boolean setDoFav;
    public Boolean setDoTag;
    List<Video> videoList;
    DatabaseReference videosRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference mDatabase;

    private final DatabaseReference cmtDatabase;

    public ArrayList<Comment> commentArrayList;

    public CommentAdapter commentAdapter;

    private DatabaseReference pDatabase;
    public Boolean getDoFav() {
        return setDoFav;
    }

    public Boolean getDoTag() {
        return setDoTag;
    }

    public VideoAdapter(List<Video> VideoList, DatabaseReference videosRef){
        this.videoList = VideoList;
        this.videosRef = videosRef;
        mDatabase = database.getReference("Videos");
        pDatabase = database.getReference("User");
        cmtDatabase = database.getReference("Comments");
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_video , parent , false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video video = videoList.get(position);
        holder.setVideoViewData(video);

        holder.fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the value of doFav for the clicked video
                toggleDoFav(holder.getAdapterPosition(),holder, video);
            }
        });

        DatabaseReference videoLike = database.getReference("videoCont"+ (position+1) ).child("doFav");
        videoLike.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean doFav = snapshot.getValue(Boolean.class);
                if (doFav != null) {
                    // 设置初始的 doFav 值
                    // 更新图标
                    if (doFav) {
                        // 设置喜欢图标
                        holder.fav.setImageResource(R.drawable.baseline_favorite_40);
                        setDoFav = true;
                    } else {
                        // 设置不喜欢图标
                        holder.fav.setImageResource(R.drawable.baseline_favorite_border_40);
                        setDoFav = false;
                    }
                }
                Log.d("VideoAdapter", "setDoFav123 value: " + setDoFav); // 添加一个 Log 语句来输出 setDoFav 的值
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 处理 onCancelled
            }
        });

        holder.tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the value of doFav for the clicked video
                toggleDoTag(holder.getAdapterPosition(), holder, video);
            }
        });

        DatabaseReference videoTag = database.getReference("videoCont"+ (position+1) ).child("doFav");
        videoTag.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean doTag = snapshot.getValue(Boolean.class);
                if (doTag != null) {
                    // 设置初始的 doFav 值
                    // 更新图标
                    if (doTag) {
                        // 设置喜欢图标
                        holder.tag.setImageResource(R.drawable.baseline_bookmark_40);
                        setDoTag = true;
                    } else {
                        // 设置不喜欢图标
                        holder.tag.setImageResource(R.drawable.baseline_bookmark_border_40);
                        setDoTag = false;
                    }
                }
                Log.d("VideoAdapter", "setDoTag123 value: " + setDoTag); // 添加一个 Log 语句来输出 setDoFav 的值
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 处理 onCancelled
            }
        });

        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDoChat(holder, video);
                View view1 = LayoutInflater.from(v.getContext()).inflate(R.layout.add_comment, null, false);
                TextInputLayout contentLayout;
                contentLayout = view1.findViewById(R.id.contentLayout);
                TextInputEditText contentET;
                contentET = view1.findViewById(R.id.contentET);
                AlertDialog alertDialog = new AlertDialog.Builder(v.getContext())
                        .setTitle("留言")
                        .setView(view1)
                        .setPositiveButton("新增", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                if (contentET.getTag() != null && contentET.getTag().toString().isEmpty()) {
                                    contentLayout.setError("This field is required");
                                } else {
                                    Comment comment = new Comment();
                                    comment.setContent(contentET.getText().toString());
                                    cmtDatabase.child("comments").push().setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            dialogInterface.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create();
                alertDialog.show();
            }



        });

        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
        View recyclerViewLayout = inflater.inflate(R.layout.add_comment, null);
        RecyclerView recyclerView = recyclerViewLayout.findViewById(R.id.recycler);
        commentArrayList = new ArrayList<>();

        cmtDatabase.child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentArrayList.clear();

                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Comment comment = dataSnapshot.getValue(Comment.class);

                    Objects.requireNonNull(comment).setKey(dataSnapshot.getKey());
                    commentArrayList.add(comment);
                    Log.d("comments", "Comment count: " + commentArrayList.size());
                }
                if (!commentArrayList.isEmpty()) {
                    // 数据不为空时设置Adapter
                    commentAdapter = new CommentAdapter(holder.itemView.getContext(), commentArrayList);
                    LinearLayoutManager llm =  new LinearLayoutManager(holder.itemView.getContext(),LinearLayoutManager.VERTICAL,false);
                    recyclerView.setLayoutManager(llm);
                    recyclerView.setAdapter(commentAdapter);
                } else {
                    // 数据为空时不设置Adapter
                    Log.d("TAG", "No comments available");
                }

                commentAdapter.setOnItemClickListener(new CommentAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(Comment comment) {
                        TextInputLayout contentLayout;
                        TextInputEditText contentET;
                        contentET = recyclerViewLayout.findViewById(R.id.contentET);
                        contentLayout = recyclerViewLayout.findViewById(R.id.contentLayout);

                        contentET.setText(comment.getContent());

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(holder.itemView.getContext());
                        AlertDialog alertDialog = alertDialogBuilder
                                .setTitle("Edit")
                                .setView(recyclerViewLayout)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        if (contentET.getTag() != null && contentET.getTag().toString().isEmpty()) {
                                            contentLayout.setError("This field is required");
                                        } else {
                                            dialogInterface.dismiss();
                                        }
                                    }
                                })
                                .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        dialogInterface.dismiss();
                                    }
                                }).create();
                        alertDialog.show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }


    public class VideoViewHolder extends RecyclerView.ViewHolder{
        VideoView videoView;

        TextView title, address , date ,  price;

        ImageView fav, tag, chat, foods;

        ViewPager2 viewPager2;



        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            videoView = itemView.findViewById(R.id.videoView);
            title = itemView.findViewById(R.id.video_title);
            address = itemView.findViewById(R.id.video_address);
            date = itemView.findViewById(R.id.video_date);
            price = itemView.findViewById(R.id.video_price);
            fav = itemView.findViewById(R.id.heartImageView);
            tag = itemView.findViewById(R.id.tagImageView);
            chat = itemView.findViewById(R.id.chatImageView);
            foods = itemView.findViewById(R.id.foodsearchImageView);
            viewPager2 = itemView.findViewById(R.id.viewPager2);
        }

        public  void setVideoViewData(Video video){
            title.setText(video.getTitle());
            address.setText(video.getAddress());
            date.setText(video.getDate());
            price.setText(video.getPrice());
            videoView.setVideoPath(video.getVideoUrl());

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();

                    float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
                    float screenRatio = videoView.getWidth() / (float) videoView.getHeight();

                    float scale = screenRatio/videoRatio;
                    if ( scale >= 1f ){
                        videoView.setScaleX(scale);
                    }else{
                        videoView.setScaleY(1f/scale);
                    }

                }
            });

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.start();
                }
            });

        }

    }
    public void toggleDoFav(int position, VideoViewHolder holder, Video video) {
        DatabaseReference videoRef = database.getReference("videoCont"+(position+1)).child("doFav");
        videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean doFav = dataSnapshot.getValue(Boolean.class);
                if (doFav != null) {
                    // 切换 doFav 的值
                    videoRef.setValue(!doFav);
                    // 更新图标
                    if (!doFav) {
                        holder.fav.setImageResource(R.drawable.baseline_favorite_40); // 喜爱图标
                        setDoFav = true;

//////演算法/////////////////////////////////////////////////////////////////
                        //演算法
                        //DatabaseReference preferencesRef = pDatabase.child("preferences");
                        // 獲取 "tag" 節點的引用
                        //DatabaseReference tagRef = database.getReference("Videos/V1/tag");
                        DatabaseReference tagRef =database.getReference("Videos").child("V"+(position+1)).child("tag");

                        tagRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String value = dataSnapshot.getValue(String.class);//前面有宣告
                                    DatabaseReference preferRef = pDatabase.child("preferences").child(value);//tag位置
                                    preferRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                long currentValue = dataSnapshot.getValue(Long.class);
                                                long newValue = currentValue + 10;
                                                preferRef.setValue(newValue);
                                            } else {
                                                preferRef.setValue(10);
                                            }
                                        }


                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Handle onCancelled
                                        }
                                    });
                                }
                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // 處理錯誤


                            }
                        });
////////演算法end//////
                    } else {
                        holder.fav.setImageResource(R.drawable.baseline_favorite_border_40); // 不喜爱图标
                        setDoFav = false;

                        //演算法
                        //DatabaseReference preferencesRef = pDatabase.child("preferences");
                        // 獲取 "tag" 節點的引用
                        // DatabaseReference tagRef = database.getReference("Videos/V1/tag");
                        DatabaseReference tagRef =database.getReference("Videos").child("V"+(position+1)).child("tag");

                        tagRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String value = dataSnapshot.getValue(String.class);//前面有宣告
                                    DatabaseReference preferRef = pDatabase.child("preferences").child(value);//tag位置
                                    preferRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                long currentValue = dataSnapshot.getValue(Long.class);
                                                long newValue = currentValue - 10;
                                                preferRef.setValue(newValue);
                                            } else {
                                                preferRef.setValue(0);
                                            }
                                        }


                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Handle onCancelled
                                        }
                                    });
                                }
                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // 處理錯誤


                            }
                        });
                    }
                    Log.d("VideoAdapter", "setDoFav value: " + setDoFav); // 添加一个 Log 语句来输出 setDoFav 的值
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });




    }



    public void toggleDoTag(int position, VideoViewHolder holder, Video video) {

        DatabaseReference videoRef = database.getReference("videoCont"+(position+1)).child("doTag");
        videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean doTag = dataSnapshot.getValue(Boolean.class);
                if (doTag != null) {
                    videoRef.setValue(!doTag);
                    if (!doTag) {
                        holder.tag.setImageResource(R.drawable.baseline_bookmark_40); // 喜爱图标
                        setDoTag = true;
                    } else {
                        holder.tag.setImageResource(R.drawable.baseline_bookmark_border_40); // 不喜爱图标
                        setDoTag = false;
                    }
                }
                Log.d("VideoAdapter", "setDoTag value: " + setDoTag);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });
    }

    public void toggleDoChat(VideoViewHolder holder, Video video) {

        DatabaseReference videoRef = mDatabase.child("V1").child("doChat");
        videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean doChat = dataSnapshot.getValue(Boolean.class);
                if (doChat != null) {
                    // Toggle the value of doFav
                    videoRef.setValue(!doChat);
                    if (!doChat) {
                        holder.chat.setImageResource(R.drawable.baseline_chat_40); // 喜爱图标
                    } else {
                        holder.chat.setImageResource(R.drawable.baseline_chat_bubble_outline_40); // 不喜爱图标
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });
    }

    public void updateData(List<Video> newVideoList) {
        videoList = newVideoList;
        notifyDataSetChanged();
    }


}
