package com.example.feastarfeed;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;

import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

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
import com.google.firebase.database.GenericTypeIndicator;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class    HomeFragment extends Fragment {

    private List<Video> videoList;
    private List<Video> videoListRE;
    private VideoAdapter adapter;
    private DatabaseReference videosRef;
    private DatabaseReference preferencesRef;
    private List<Video> mergedList;

    private long nodeCount;
    //private List<Video> mergedList2;
    public HomeFragment() {

    }
    private DatabaseReference pDatabase;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ViewPager2 viewPager2 = view.findViewById(R.id.viewPager2);


        // Initialize videoList and adapter (you may want to pass data from MainActivity or fetch it here)
        videoList = new ArrayList<>();
        videoListRE = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        videosRef = database.getReference("Videos");
        Log.d("videoList", "passssssssssssssssssssss");


        videosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /////////////////節點數量v1v2v3v4...
                nodeCount = dataSnapshot.getChildrenCount();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //mergedList = new ArrayList<>();
        //mergedList2 = new ArrayList<>();
        // mergedList2=videoList;
        // 使用合併後的 List 創建 VideoAdapter
        adapter = new VideoAdapter(new ArrayList<>(), videosRef);
        viewPager2.setAdapter(adapter);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            private long videoStartTime = 0L; // 记录视频开始播放时间/////////計時

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 当滑动到下一个视频时执行适当的操作
                // 您可以在这里执行任何您想要的操作，例如更新UI、加载数据等
                Log.d("ViewPager2", "Page selected: " + position);

// 当切换到第一个视频时，记录开始播放时间////////////////以下計時
                if (position == 0) {
                    videoStartTime = System.currentTimeMillis();
                } else {
                    // 当切换到其他视频时，计算上一个视频的播放时间
                    long durationMillis = System.currentTimeMillis() - videoStartTime;
                    long durationSeconds = durationMillis / 1000; // 将毫秒转换为秒
                    Log.d("VideoPlayTime", "Video played for " + durationSeconds + " seconds");

                    // 在這裡添加判斷durationSeconds是否小於3秒的邏輯
                    // 獲取 "tag" 節點的引用
                    pDatabase = database.getReference("User");
                    //DatabaseReference tagRef = database.getReference("Videos/V1/tag");
                    ////這邊的position不用+1，和VideoAdapter不一樣
                    DatabaseReference tagRef =database.getReference("Videos").child("V"+(position)).child("tag");
                    tagRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String value = dataSnapshot.getValue(String.class);//前面有宣告
                                DatabaseReference preferRef = pDatabase.child("preferences").child(value);//tag位置
                                Log.d("tag", "succes");
                                preferRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {
                                            ////////////<3秒不喜歡
                                            if (durationSeconds < 3) {
                                                long currentValue = dataSnapshot.getValue(Long.class);
                                                long newValue = currentValue -5;
                                                preferRef.setValue(newValue);
                                                ///////////////>10秒喜歡
                                            } else if (durationSeconds >10) {
                                                long currentValue = dataSnapshot.getValue(Long.class);
                                                long newValue = currentValue +5;
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
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // 處理錯誤
                        }
                    });
////////演算法end//////


                    // 重置计时器
                    videoStartTime = System.currentTimeMillis();

                    // 获取当前视频对象
                    if (adapter != null) {
                        Boolean doFav = adapter.getDoFav();
                        Boolean doTag = adapter.getDoTag();
                        if (doFav != null && position > 0 && position <= nodeCount) {
                            videosRef.child("V" + position).child("doFav").setValue(doFav);
                        }
                        if (doTag != null && position > 0 && position <= nodeCount) {
                            videosRef.child("V" + position).child("doTag").setValue(doTag);
                        }
                    }

                }



            }

        });



        loadVideosFromFirebase();
        loadVideosFromFirebaseADVANCE();
        //loadVideosMIX();
        // return view;

        // Log.d("mergedList", String.valueOf(mergedList.size()));
        return view;

    }//////////////// oncreateView END


    ///////一般的
    public void loadVideosFromFirebase() {
        videosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                videoList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String title = snapshot.child("title").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String date = snapshot.child("date").getValue(String.class);
                    String price = snapshot.child("price").getValue(String.class);
                    String videoUrl = snapshot.child("videoUrl").getValue(String.class);

                    Video video = new Video(videoUrl,title, address, date, price);
                    videoList.add(video);
                }
                adapter.notifyDataSetChanged();
                Log.d("videoList", "經過了");
                Log.d("videoList", String.valueOf(videoList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }


        });
    }

    ///////////////////////前三大


    public void loadVideosFromFirebaseADVANCE(){
        List<String> preferencesList = new ArrayList<>();
        videoListRE.clear();
        preferencesRef = database.getReference("User").child("preferences");
        // 獲取 "User/preferences" 節點下前三大的子節點值
        preferencesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<Map<String, Long>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Long>>() {};
                Map<String, Long> preferencesMap = snapshot.getValue(genericTypeIndicator);

                if (preferencesMap != null) {
                    List<Map.Entry<String, Long>> preferenceEntries = new ArrayList<>(preferencesMap.entrySet());
                    Collections.sort(preferenceEntries, Collections.reverseOrder(Map.Entry.comparingByValue()));

                    for (int i = 0; i < 3 && i < preferenceEntries.size(); i++) {
                        preferencesList.add(preferenceEntries.get(i).getKey());
                    }
                }

                // 比對 "Videos" 節點下每個視頻的 "tag" 節點
                videosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
                            String videoTag = videoSnapshot.child("tag").getValue(String.class);
                            if (videoTag != null && preferencesList.contains(videoTag)) {
                                String title = videoSnapshot.child("title").getValue(String.class);
                                String address = videoSnapshot.child("address").getValue(String.class);
                                String date = videoSnapshot.child("date").getValue(String.class);
                                String price = videoSnapshot.child("price").getValue(String.class);
                                String videoUrl = videoSnapshot.child("videoUrl").getValue(String.class);

                                Video video = new Video(videoUrl, title, address, date, price);
                                videoListRE.add(video);////////符合地都在這了
                                // Log.d("videoListRE", String.valueOf(videoListRE.size()));
                            }
                        }

                        // 打亂列表順序
                        Collections.shuffle(videoListRE);
                        Log.d("videoListRE", String.valueOf(videoListRE.size()));

                        // 在這裡調用 loadVideosMIX()
                        loadVideosMIX();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // 處理錯誤
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 處理錯誤
            }
        });
    }

    public void loadVideosMIX() {
        // mergedList.clear(); // 清空 mergedList
        mergedList = new ArrayList<>();
        if (videoListRE != null && videoList != null) {
            int maxSize = Math.max(videoListRE.size(), videoList.size());

            for (int i = 0; i < maxSize; i++) {
                if (i < videoListRE.size()) {
                    //Log.d("videoListRE", String.valueOf(videoListRE.size()));
                    mergedList.add(videoListRE.get(i));
                    String firstVideoTitle = videoListRE.get(i).getTitle();
                    Log.d("videoListRE", firstVideoTitle);
                }
                if (i < videoList.size()) {
                    // Log.d("videoList", String.valueOf(videoList.size()));
                    mergedList.add(videoList.get(i));
                    String firstVideoTitle = videoList.get(i).getTitle();
                    Log.d("videoList", firstVideoTitle);
                }
            }
        }

        Log.d("mergedList", String.valueOf(mergedList.size()));
//創建新的 VideoAdapter 實例並設置給 viewPager2

        // 在這裡更新 adapter 的數據
        adapter.updateData(mergedList);
    }
//////////////////////


}
