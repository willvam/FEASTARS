package com.example.feastarfeed;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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
    private HomeFragment.IdPassCallback idPassCallback;
    private long id;
    private long parameterFav=10;
    private long parameterTAG=15;
    private DatabaseReference pDatabase;
    public Boolean getDoFav() {
        return setDoFav;
    }

    public Boolean getDoTag() {
        return setDoTag;
    }

    private FragmentManager fragmentManager;
    boolean bottomVideoViewVisible = false;
    boolean tagViewVisible = false;
    private Context context;
    private  String username;
    private  String uploader;
    CircleImageView profileImage;



    //    private List<SharedPreferencesUtils> userList;
//
//    public void UserAdapter(Context context, List<SharedPreferencesUtils> userList) {
//        this.context = context;
//        this.userList = userList;
//    }
private OnProfileImageClickListener onProfileImageClickListener;



    public void setOnProfileImageClickListener(OnProfileImageClickListener listener) {
        this.onProfileImageClickListener = listener;
    }
    public interface OnProfileImageClickListener {
        void OnProfileImageClick(String uploader);
    }




    public VideoAdapter(List<Video> VideoList, DatabaseReference videosRef, FragmentManager fragmentManager ,HomeFragment.IdPassCallback callback){
        this.fragmentManager = fragmentManager;
        this.videoList = VideoList;

        this.videosRef = videosRef;
        mDatabase = database.getReference("Videos");
        pDatabase = database.getReference("Users");
        cmtDatabase = database.getReference("Comments");
        this.idPassCallback = callback;


    }
    /////在这里调用接口回调
    public void setIdPass(long idPass) {
        this.id = idPass;
        if (idPassCallback != null) {
            idPassCallback.onIdPassChanged(id);
        }
        Log.d("位置tag", String.valueOf(id));
        // 在这里根据需要进行相应的操作,比如刷新UI等
//        notifyDataSetChanged();
    }
//    public VideoViewHolder getViewHolderForPosition(int position) { //抓viewholder
//        return (VideoViewHolder) recyclerView.findViewHolderForLayoutPosition(position);
//    }

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

        holder.tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION && onProfileImageClickListener != null) {
                    onProfileImageClickListener.OnProfileImageClick(uploader);                }
            }
        });
// 獲取 username
        if (context == null) {
            // 从 ViewHolder.itemView 获取一个非空的 Context
            context = holder.itemView.getContext();
        }
         username = SharedPreferencesUtils.getUsername(context);
        Log.d("username2", username);


        holder.fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the value of doFav for the clicked video
                toggleDoFav(holder.getAdapterPosition(),holder);
            }
        });



        //id = video.getId();
        //Log.d("位置tag", String.valueOf(id));
        DatabaseReference videoLike = database.getReference("Users").child(username).child("Cont"+id).child("Fav");
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
                    } else {
                        // 设置不喜欢图标
                        holder.fav.setImageResource(R.drawable.baseline_favorite_border_40);
                    }
                }
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
                toggleDoTag(holder.getAdapterPosition(), holder);
            }
        });



        DatabaseReference videoTag = database.getReference("Users").child(username).child("Cont"+id).child("Tag");
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
                    } else {
                        // 设置不喜欢图标
                        holder.tag.setImageResource(R.drawable.baseline_bookmark_border_40);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 处理 onCancelled
            }
        });

        holder.videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment bottomVideoViewFragment = fragmentManager.findFragmentById(R.id.frame_layout1);
                Fragment tagViewFragment = fragmentManager.findFragmentById(R.id.frame_layout2);

                if (bottomVideoViewFragment != null) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);
                    fragmentTransaction.remove(bottomVideoViewFragment);
                    fragmentTransaction.commit();
                }
                bottomVideoViewVisible = false;

                if (tagViewFragment != null) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.slide_in2, R.anim.slide_out2);
                    fragmentTransaction.remove(tagViewFragment);
                    fragmentTransaction.commit();
                }

                tagViewVisible = false;
            }

        });

        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the value of doFav for the clicked video
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in, R.anim.slide_out);

                if (bottomVideoViewVisible) {
                    // 隐藏 Bottom_VIdeo_View
                    Fragment bottomVideoViewFragment = fragmentManager.findFragmentById(R.id.frame_layout1);
                    if (bottomVideoViewFragment != null) {
                        fragmentTransaction.remove(bottomVideoViewFragment);
                        bottomVideoViewVisible = false;
                    }
                } else {
                    // 显示 Bottom_VIdeo_View
                    fragmentTransaction.replace(R.id.frame_layout1, new CommentFragment());
                    bottomVideoViewVisible = true;
                }

                fragmentTransaction.commit();
            }
        });

        holder.foods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in2, R.anim.slide_out2);
                if (tagViewVisible) {
                    // 隐藏 Bottom_VIdeo_View
                    Fragment tagViewFragment = fragmentManager.findFragmentById(R.id.frame_layout2);
                    if (tagViewFragment != null) {
                        fragmentTransaction.remove(tagViewFragment);
                        tagViewVisible = false;
                    }
                } else {
                    // 显示 Bottom_VIdeo_View
                    fragmentTransaction.replace(R.id.frame_layout2, new TagFragment());
                    tagViewVisible = true;
                }

                fragmentTransaction.commit();

            }
        });

    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public VideoViewHolder getViewHolder(int position, RecyclerView recyclerView) {
        return (VideoViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
    }


    public class VideoViewHolder extends RecyclerView.ViewHolder{
//        private ExoPlayer player;
//        PlayerView playerView;
        VideoView videoView;


        TextView title, address , date ,  price;

        ImageView fav, tag, chat, foods;

        ViewPager2 viewPager2;

        String vurl;

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
            profileImage = itemView.findViewById(R.id.profile_image);


//            // 創建 ExoPlayer 實例
//            player = new ExoPlayer.Builder(itemView.getContext()).build();
//
//            // 將 ExoPlayer 與 PlayerView 關聯
//            playerView.setPlayer(player);

        }



//        public void stopPlayback() { //停止撥放
//            if (player != null) {
//                Log.d("VideoViewHolder", "stopPlayback called");
//                player.setPlayWhenReady(false);
//                player.stop();
//            } else {
//                Log.d("VideoViewHolder", "stopPlayback called, but player is null");
//            }
//        }

        public void setVideoViewData(Video video){
            title.setText(video.getTitle());
            address.setText(video.getAddress());
            date.setText(video.getDate());
            price.setText(video.getPrice());
            videoView.setVideoPath(video.getVideoUrl());
            uploader = video.getUploader();
            Log.d("uploader", uploader);
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("連結個人葉", "點擊123");

                    if (onProfileImageClickListener != null) {
                        Log.d("連結個人葉", "點擊");
                        onProfileImageClickListener.OnProfileImageClick(uploader);
                    }
                }
            });

            //playerView.setVideoPath(video.getVideoUrl());
            //vurl = video.getVideoUrl();

//            // 構建媒體源
//            MediaSource mediaSource = buildMediaSource();
//
//            // 準備 ExoPlayer 並播放
//            player.setMediaSource(mediaSource);
//            player.prepare();
//            player.setPlayWhenReady(true);
//
//            player.addListener(new Player.Listener() {
//                @Override
//                public void onPlaybackStateChanged(int playbackState) {
//                    if (playbackState == Player.STATE_ENDED) {
//                        // 视频播放完成，重新开始播放
//                        player.seekTo(0);
//                        player.setPlayWhenReady(true);
//                    }
//                }
//
//            });
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



//        public void onViewDetachedFromWindow() {
//            //super.onViewDetachedFromWindow();
//            releasePlayer();
//        }
//
//
//        private void releasePlayer() {
//            if (player != null) {
//                player.release();
//                player = null;
//            }
//        }


//        private MediaSource buildMediaSource() {
//
//        // 從 Firebase Storage 獲取視頻的流式傳輸 URL
//        //vurl = "https://firebasestorage.googleapis.com/v0/b/feastars-1861e.appspot.com/o/Videos%2Fa.mp4?alt=media&token=b7067703-908c-4a98-87a2-b01105a9c8f9";
//
//        // 創建 ProgressiveMediaSource 用於播放 MP4 文件
//        ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(
//                new DefaultHttpDataSource.Factory())
//                .createMediaSource(MediaItem.fromUri(vurl));
//
//        return mediaSource;
//        }
    }

    private void updatePreferenceValueFAV(String tag, boolean setDoFav) {
        DatabaseReference preferRef = pDatabase.child(username).child("preferences").child(tag);
        preferRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long currentValue = dataSnapshot.getValue(Long.class);
                    long newValue = setDoFav ? currentValue + parameterFav : currentValue - parameterFav;
                    preferRef.setValue(newValue);
                } else {
                    preferRef.setValue(setDoFav ? parameterFav : 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });
    }

    private void updatePreferenceValueTAG(String tag, boolean setDoFav) {
        DatabaseReference preferRef = pDatabase.child(username).child("preferences").child(tag);
        preferRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long currentValue = dataSnapshot.getValue(Long.class);
                    long newValue = setDoFav ? currentValue + parameterTAG : currentValue - parameterTAG;
                    preferRef.setValue(newValue);
                } else {
                    preferRef.setValue(setDoFav ? parameterTAG : 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });
    }
    public void toggleDoFav(int position, VideoViewHolder holder) {
        DatabaseReference videoRef = database.getReference("Users").child(username).child("Cont"+id).child("Fav");
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
                        // 獲取 "tag" 節點的引用
                        DatabaseReference tagRef =database.getReference("Videos").child("V"+id).child("Foodtags");
                        Log.d("tagid", String.valueOf(id));
                        Log.d("tag", String.valueOf(tagRef));
                        tagRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // 如果是单个标签值
                                    if (dataSnapshot.getValue() instanceof String) {
                                        String value = dataSnapshot.getValue(String.class);
                                        updatePreferenceValueFAV(value, setDoFav);
                                    } else { // 如果是多个标签值
                                        for (DataSnapshot tagSnapshot : dataSnapshot.getChildren()) {
                                            String value = tagSnapshot.getValue(String.class);
                                            updatePreferenceValueFAV(value, setDoFav);
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // 处理错误
                            }
                        });
////////演算法end//////
                    } else {
                        holder.fav.setImageResource(R.drawable.baseline_favorite_border_40); // 不喜爱图标
                        setDoFav = false;
                        // 獲取 "tag" 節點的引用
                        DatabaseReference tagRef =database.getReference("Videos").child("V"+id).child("Foodtags");
                        Log.d("tagid", String.valueOf(id));
                        for (DataSnapshot tagSnapshot : dataSnapshot.getChildren()) {
                            Log.d("tag", String.valueOf(tagSnapshot));
                        }

                        tagRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // 如果是单个标签值
                                    if (dataSnapshot.getValue() instanceof String) {
                                        String value = dataSnapshot.getValue(String.class);
                                        updatePreferenceValueFAV(value, setDoFav);
                                    } else { // 如果是多个标签值
                                        for (DataSnapshot tagSnapshot : dataSnapshot.getChildren()) {
                                            String value = tagSnapshot.getValue(String.class);
                                            updatePreferenceValueFAV(value, setDoFav);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // 处理错误
                            }
                        });
                    }
                    Log.d("VideoAdapter", "setDoFav value: " + setDoFav); // 添加一个 Log 语句来输出 setDoFav 的值
                }else{

                    videoRef.setValue(true);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });
    }



////////////////////////////
    public void toggleDoTag(int position, VideoViewHolder holder) {
        DatabaseReference videoRef = database.getReference("Users").child(username).child("Cont"+id).child("Tag");
        videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean doTag = dataSnapshot.getValue(Boolean.class);
                if (doTag != null) {
                    videoRef.setValue(!doTag);
                    if (!doTag) {
                        holder.tag.setImageResource(R.drawable.baseline_bookmark_40); // 喜爱图标
                        setDoTag = true;
//////演算法/////////////////////////////////////////////////////////////////
                        // 獲取 "tag" 節點的引用
                        DatabaseReference tagRef =database.getReference("Videos").child("V"+id).child("Foodtags");
                        Log.d("tagid", String.valueOf(id));
                        Log.d("tag", String.valueOf(tagRef));
                        tagRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // 如果是单个标签值
                                    if (dataSnapshot.getValue() instanceof String) {
                                        String value = dataSnapshot.getValue(String.class);
                                        updatePreferenceValueTAG(value, setDoTag);
                                    } else { // 如果是多个标签值
                                        for (DataSnapshot tagSnapshot : dataSnapshot.getChildren()) {
                                            String value = tagSnapshot.getValue(String.class);
                                            updatePreferenceValueTAG(value, setDoTag);
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // 处理错误
                            }
                        });
////////演算法end//////
                    } else {
                        holder.tag.setImageResource(R.drawable.baseline_bookmark_border_40); // 不喜爱图标
                        setDoTag = false;
                        //演算法
                        // 獲取 "tag" 節點的引用
                        DatabaseReference tagRef =database.getReference("Videos").child("V"+id).child("Foodtags");
                        Log.d("tagid", String.valueOf(id));
                        for (DataSnapshot tagSnapshot : dataSnapshot.getChildren()) {
                            Log.d("tag", String.valueOf(tagSnapshot));
                        }
                        tagRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // 如果是单个标签值
                                    if (dataSnapshot.getValue() instanceof String) {
                                        String value = dataSnapshot.getValue(String.class);
                                        updatePreferenceValueTAG(value, setDoTag);
                                    } else { // 如果是多个标签值
                                        for (DataSnapshot tagSnapshot : dataSnapshot.getChildren()) {
                                            String value = tagSnapshot.getValue(String.class);
                                            updatePreferenceValueTAG(value, setDoTag);
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // 处理错误
                            }
                        });
                    }
                    Log.d("VideoAdapter", "setDoTag value: " + setDoTag); // 添加一个 Log 语句来输出 setDoFav 的值
                }else{

                    videoRef.setValue(true);

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
