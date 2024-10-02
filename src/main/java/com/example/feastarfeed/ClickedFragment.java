package com.example.feastarfeed;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

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

public class ClickedFragment extends Fragment {

    private VideoAdapter adapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    DatabaseReference videosRef = database.getReference("Videos");

    List<Video> videoList;

    Long idpass1;

    String username;

    public static int num = 1;

    public static String string;

    private long parameterTime=5; //大於小於後要+-多少分數
    private long parameterTimeLOW=4; //小於幾秒-parameterTime
    private long parameterTimeHIGH=10;//大於幾秒-parameterTime
    private long parameterRecom=3;//選擇喜好分數前幾名的加入推薦列表
    private long parameterVideoList = 1; // 先放入幾个videoList元素(演算法比例調整)
    private long parameterVideoListRE = 1; // 然后放入幾个videoListRE元素

    public static ArrayList<Tag> tagArrayList;
    public static ArrayList<Comment> commentArrayList;
    public static ArrayList<Comment> userArrayList;

    public IdPassCallback idPassCallback;

    public interface IdPassCallback {
        void onIdPassChanged(long idpass);
    }

    public ClickedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_clicked, container, false);

        TagFragment.num = num;
        CommentFragment.num = num;

        if (string.equals("account")){
            videoList = AccountFragment.videoListClicked;
        }else if (string.equals("search")){
            videoList = Bottom_VIdeo_View.videoListClicked;
        } else if (string.equals("others")) {
            videoList = OthersAccountFragment.videoListClicked;
        }
        else if(string.equals("collection")){
            videoList = OwnCollectionFragment.videoListClicked;
        }
        else if(string.equals("ownvideo")){
            videoList = OwnVideoFragment.videoListClicked;
        }
        tagArrayList = new ArrayList<>();

        username = SharedPreferencesUtils.getUsername(requireContext());

        ViewPager2 viewPager2 = view.findViewById(R.id.viewPager2Clicked);
        FragmentManager fragmentManager = getChildFragmentManager();

        Log.d("Personal", "videoList: " + videoList);

        adapter = new VideoAdapter(videoList, videosRef, fragmentManager, null, idPassCallback, null);
        viewPager2.setAdapter(adapter);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private long videoStartTime = 0L; // 记录视频开始播放时间/////////計時

            //private int previousPosition = -1; // 记录上一个播放的视频位置
            private VideoAdapter.VideoViewHolder previousViewHolder = null;
            //private VideoAdapter.VideoViewHolder currentViewHolder;

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);


                // 獲取該 Video 物件的 id
                Video currentVideo = videoList.get(position);
                //idpass是當前影片的ID
                Long id = currentVideo.getId();
                ////////////// 在这里调用接口回调
                idPassCallback = new IdPassCallback() {
                    @Override
                    public void onIdPassChanged(long idPass) {
                        if (adapter != null) {
                            adapter.setIdPass(id);
                        }
                    }
                };

                if (idPassCallback != null) {
                    idPassCallback.onIdPassChanged(id);
                }
                IdPassCallback idPassCallback = new IdPassCallback() {
                    @Override
                    public void onIdPassChanged(long idPass) {
                        // 在这里更新 VideoAdapter 中的 idpass 值
                        idPass=id;
                    }

                };
////////end
                // 停止上一个视频的播放

//                if (previousViewHolder != null) {
//                    Log.d("ViewPager2", "testifin " + position);
//                    previousViewHolder.stopPlayback();
//                }

//                // 获取当前视频的ViewHolder
//                VideoAdapter.VideoViewHolder currentViewHolder = adapter.getViewHolderForPosition(position);
//
//                // 停止上一個 VideoViewHolder 中的播放
//                if (previousViewHolder != null && previousViewHolder != currentViewHolder) {
//                    previousViewHolder.stopPlayback();
//                }

                // 当滑动到下一个视频时执行适当的操作
                // 您可以在这里执行任何您想要的操作，例如更新UI、加载数据等
                Log.d("ViewPager2", "Page selected: " + position);

                int page = position;

// 当切换到第一个视频时，记录开始播放时间////////////////以下計時
                if (position == 0) {
                    videoStartTime = System.currentTimeMillis();

                    // 獲取該 Video 物件的 id
                    currentVideo = videoList.get(position);
                    //idpass1世上一部影片的ID
                    idpass1 = currentVideo.getId();
                    Log.d("personalidid", String.valueOf(idpass1));
                    Log.d("personalididp", String.valueOf(position));
                } else {
                    // 当切换到其他视频时，计算上一个视频的播放时间
                    long durationMillis = System.currentTimeMillis() - videoStartTime;
                    long durationSeconds = durationMillis / 1000; // 将毫秒转换为秒
                    Log.d("id", "Video played for " + durationSeconds + " seconds");

                    // 在這裡添加判斷durationSeconds是否小於3秒的邏輯
                    // 獲取 "tag" 節點的引用
                    DatabaseReference pDatabase = database.getReference("Users");
                    //DatabaseReference tagRef = database.getReference("Videos/V1/tag");

                    //會變成用上一部觀看時間判斷當前影片標千

                    DatabaseReference tagRef =database.getReference("Videos").child("V"+idpass1).child("Foodtags");
                    tagRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot tagSnapshot : dataSnapshot.getChildren()) {
                                    String value = tagSnapshot.getValue(String.class);

                                    DatabaseReference preferRef = pDatabase.child(username).child("preferences").child(value);//tag位置
                                    preferRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                ////////////<3秒不喜歡
                                                if (durationSeconds < parameterTimeLOW) {
                                                    long currentValue = dataSnapshot.getValue(Long.class);
                                                    long newValue = currentValue - parameterTime;
                                                    preferRef.setValue(newValue);
                                                    ///////////////>10秒喜歡
                                                } else if (durationSeconds > parameterTimeHIGH) {
                                                    long currentValue = dataSnapshot.getValue(Long.class);
                                                    long newValue = currentValue + parameterTime;
                                                    preferRef.setValue(newValue);
                                                }

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
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // 處理錯誤
                        }
                    });
////////演算法end//////


                    // 重置计时器
                    videoStartTime = System.currentTimeMillis();

                    //idpass1世上一部影片的ID
                    idpass1 = id;

//                    // 获取当前视频对象
//                    if (adapter != null) {
//                        Boolean doFav = adapter.getDoFav();
//                        Boolean doTag = adapter.getDoTag();
//                        if (doFav != null && position > 0 && position <= nodeCount) {
//                            videosRef.child("V" + position).child("doFav").setValue(doFav);
//                        }
//                        if (doTag != null && position > 0 && position <= nodeCount) {
//                            videosRef.child("V" + position).child("doTag").setValue(doTag);
//                        }
//                    }

                }

                DatabaseReference videoFoods = database.getReference("Videos").child("V"+idpass1).child("Foodtags");
                videoFoods.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tagArrayList.clear();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            String tagString = dataSnapshot.getValue(String.class);
                            Log.d("HomeFragment", "Tag content: " + tagString);

                            Tag tag = new Tag(tagString);
                            tagArrayList.add(tag);
                            //Log.d("HomeFragment", "Tag content: " + tag);
                            Log.d("tagFragment","tags count : "+tagArrayList.size());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                commentArrayList = new ArrayList<>();
                userArrayList = new ArrayList<>();
                Log.d("personalidid", String.valueOf(idpass1));

                DatabaseReference videoComments = database.getReference("videoCont"+idpass1);
                videoComments.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentArrayList.clear();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            String commentString = dataSnapshot.child("content").getValue(String.class);
                            String user = dataSnapshot.child("user").getValue(String.class);
                            Comment comment = new Comment(commentString,user);
                            Log.d("HomeFragment", "Comment content: " + comment);
                            //Objects.requireNonNull(comment).setKey(dataSnapshot.getKey());
                            commentArrayList.add(comment);
                            userArrayList.add(comment);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //previousViewHolder = currentViewHolder;

            }




        });

        return view;
    }
}