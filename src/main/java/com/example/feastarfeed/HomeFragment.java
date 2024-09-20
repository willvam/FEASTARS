package com.example.feastarfeed;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.Calendar;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class    HomeFragment extends Fragment implements VideoAdapter.OnProfileImageClickListener  {
    //private RecyclerView recyclerView;
    public List<Video> videoList;
    private List<Video> videoListRE;
    private List<Video> ADList;
    private List<Video> AdDADList;
    private List<Video> AdALLList;
    private List<Video> WithTagADList;
    private VideoAdapter adapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference videosRef;
    private DatabaseReference ADRef1;
    private DatabaseReference ADRef;
    private DatabaseReference watchRecordsRef;
    private DatabaseReference  UsrwatchRecordsRef;
    private DatabaseReference PeriodWatchRef;
    private DatabaseReference userRef;
    private DatabaseReference TotalWatchRef;
    private DatabaseReference preferencesRef;
    private List<Video> mergedList;
    private List<Video>     ADmergedList;
    private long id;
    public boolean tag1;
    public boolean fav1;
    private long parameterTime=5; //大於小於後要+-多少分數
    private long parameterTimeLOW=4; //小於幾秒-parameterTime
    private long parameterTimeHIGH=8;//大於幾秒-parameterTime
    private long parameterRecom=3;//選擇喜好分數前幾名的加入"影片"推薦列表
    ///////////以下為最後影片
    private long parameterVideoListTotal=3;/////以下兩個的比例要輪迴幾次
    private long parameterVideoList = 1; // 最終影片清單:先放入幾个videoList元素(演算法比例調整)
    private long parameterVideoListRE = 1; // 最終影片清單:然后放入幾个videoListRE元素
    private long parameterTotalAD = 1;//最終影片清單:放入幾廣告
    private long ADparameterRecom=3;//無用。(選喜好前幾名加入"廣告"推薦) "目前和影片興趣共用和，請更改parameterRecom"


    private long parameterADList = 1; // 先放入幾个沒tag廣告元素(演算法比例調整)
    private long parameterNoTagADList = 1; // 然后放入幾个有tag廣告元素
    private long parameterAdALLList = 1;// 然后放入幾个"所有廣告"元素
    private long parameterAdDADList = 1;// 然后放入幾个金主爸爸廣告元素
    private long parameterTimePeriod = 7 * 24 * 60 * 60 * 1000;////要近期幾天內的觀看數

    private long VideoViewCount;
    private long  currentTimestamp;
    public static long idpass1;
    //public ArrayList<String> id = new ArrayList<>();
    private long nodeCount;
    //private List<Video> mergedList2;

    public static ArrayList<Tag> tagArrayList;
    public static ArrayList<Comment> commentArrayList;

    public static ArrayList<Comment> userArrayList;
    public static int page, num = 0;
    private String username;
    private String shopname;
    private String shopname1;
    private String shopnamefuck;
    Boolean doFav,doTag;

    private OnUploaderClickListener onUploaderClickListener;
    public void setOnUploaderClickListener(OnUploaderClickListener listener) {
        this.onUploaderClickListener = listener;
    }
    public interface OnUploaderClickListener {
        void onUploaderClicked(String uploader);
    }

    public void OnProfileImageClick(String uploader){
        if (onUploaderClickListener != null) {
            onUploaderClickListener.onUploaderClicked(uploader);
            Log.d("HomeFragment","有進來這個 = "+uploader);

        }
        Log.d("HomeFragment","uplaoder = "+uploader);

    }
    // private long id;
    public HomeFragment() {

    }
    public interface IdPassCallback {
        void onIdPassChanged(long idpass);
    }
    public IdPassCallback idPassCallback; // 声明一个接口实例变量
    private Video currentVideo;
    private DatabaseReference pDatabase;
    private String getStartKeyFor7DaysAgo() {
        long lastWeek = new Date().getTime() - (parameterTimePeriod);// 7天前的時間戳記
        return String.valueOf(lastWeek);
    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ViewPager2 viewPager2 = view.findViewById(R.id.viewPager2);
        //RecyclerView recyclerView = view.findViewById(R.id.recyclerView); 4/25

        TagFragment.num = num;
        CommentFragment.num = num;

        // Initialize videoList and adapter (you may want to pass data from MainActivity or fetch it here)
        videoList = new ArrayList<>();
        videoListRE = new ArrayList<>();
        WithTagADList = new ArrayList<>();
        ADList = new ArrayList<>();
        AdDADList = new ArrayList<>();
        AdALLList = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        videosRef = database.getReference("Videos");
        ADRef1 = database.getReference("advertise").child("withtag");
        ADRef = database.getReference("advertise").child("notag");
        Log.d("videoList", "passssssssssssssssssssss");
        watchRecordsRef= database.getReference("watchRecord");
        TotalWatchRef= database.getReference("TotalWatch");
        PeriodWatchRef= database.getReference("PeriodWatch");



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
        // id = video.getId();
        FragmentManager fragmentManager = getChildFragmentManager();

        ////////////取得使用者名稱
        Context context = requireContext();
        username = SharedPreferencesUtils.getUsername(context);
        userRef= database.getReference("Users").child(username).child("Cont");
        UsrwatchRecordsRef= database.getReference("Users").child(username).child("watchRecord");
        Log.d("username", username);

        // 使用合併後的 List 創建 VideoAdapter
        adapter = new VideoAdapter(new ArrayList<>(), videosRef, fragmentManager, idPassCallback,null);
        adapter.setOnProfileImageClickListener(this);

        if (getActivity() instanceof OnUploaderClickListener) {
            setOnUploaderClickListener((OnUploaderClickListener) getActivity());
        }

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
                currentVideo = mergedList.get(position);
                //idpass是當前影片的ID
                id = currentVideo.getId();
                shopname = currentVideo.getTitle();
                Log.d("HomeFragment", "Ashopname: " + shopname);
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

                page = position;

// 当切换到第一个视频时，记录开始播放时间////////////////以下計時
                if (position == 0) {
                    videoStartTime = System.currentTimeMillis();

                    // 獲取該 Video 物件的 id
                    currentVideo = mergedList.get(position);
                    //idpass1世上一部影片的ID
                    idpass1 = currentVideo.getId();
                    shopname1 = currentVideo.getTitle();
                    Log.d("HomeFragment111111111", "Ashopname1: " + shopname1);
                    Log.d("idid", String.valueOf(idpass1));
                    Log.d("ididp", String.valueOf(position));
                } else {
                    // 当切换到其他视频时，计算上一个视频的播放时间
                    long durationMillis = System.currentTimeMillis() - videoStartTime;
                    long durationSeconds = durationMillis / 1000; // 将毫秒转换为秒
                    Log.d("id", "Video played for " + durationSeconds + " seconds");
                    //Log.d("idid", String.valueOf(idpass1));
                    // Log.d("HomeFragment111111111", "Ashopname1: " + shopname1);
                    VideoViewCount=VideoViewCount+1;

                    // 在這裡添加判斷durationSeconds是否小於3秒的邏輯
                    // 獲取 "tag" 節點的引用
                    pDatabase = database.getReference("Users");
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


                    //////近期觀看次數 目前只有廣告
                    DatabaseReference notagPeriodWatchRef = PeriodWatchRef.child("Advertise").child("notag").child("ad"+idpass1);
                    DatabaseReference withtagPeriodWatchRef = PeriodWatchRef.child("Advertise").child("withtag").child("ad"+idpass1);

                    DatabaseReference NoTagADRecordRef2 = watchRecordsRef.child("Advertise").child("notag").child("ad"+idpass1);
                    DatabaseReference WithTagADRecordRef2 = watchRecordsRef.child("Advertise").child("withtag").child("ad"+idpass1);
                    //////總共觀看
                    DatabaseReference NoTagADRecordTotalRef = TotalWatchRef.child("Advertise").child("notag").child("ad"+idpass1);
                    DatabaseReference WithTagADRecordTotalRef = TotalWatchRef.child("Advertise").child("withtag").child("ad"+idpass1);

                    /////////////////////////////////影片時間戳記紀錄
                    ////////////<3秒不喜歡
                    if (durationSeconds < parameterTimeLOW) {
///////////紀錄看影片時長次數
                        if((idpass1/100000)==1){
                            /////////notag廣告
                            notagPeriodWatchRef.child("才看一下").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        notagPeriodWatchRef.child("才看一下").setValue(newValue);


                                    } else {
                                        notagPeriodWatchRef.child("才看一下").setValue(1);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });


                        }else if((idpass1/100000)==2){
                            /////////withtag廣告

                            withtagPeriodWatchRef.child("才看一下").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        withtagPeriodWatchRef.child("才看一下").setValue(newValue);


                                    } else {
                                        withtagPeriodWatchRef.child("才看一下").setValue(1);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });

                        }else{
                        }

                        //////總共觀看時間次數 目前只有廣告
                        if((idpass1/100000)==1){
                            /////////notag廣告
                            NoTagADRecordTotalRef.child("才看一下").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        NoTagADRecordTotalRef.child("才看一下").setValue(newValue);


                                    } else {
                                        NoTagADRecordTotalRef.child("才看一下").setValue(1);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });
                        }else if((idpass1/100000)==2){
                            /////////withtag廣告

                            WithTagADRecordTotalRef.child("才看一下").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        WithTagADRecordTotalRef.child("才看一下").setValue(newValue);
                                    } else {
                                        WithTagADRecordTotalRef.child("才看一下").setValue(1);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });
                        }


                        ///////////////>10秒喜歡
                    } else if (durationSeconds > parameterTimeHIGH) {
                        Log.d("idpass是多少", String.valueOf(idpass1));
                        Log.d("idp是多少", String.valueOf(id));
                        currentTimestamp= System.currentTimeMillis();
                        DatabaseReference newVidWatchRecordRef = watchRecordsRef.child("Videos").child("V"+idpass1).child(String.valueOf(currentTimestamp));
                        DatabaseReference newNoTagADRecordRef = watchRecordsRef.child("Advertise").child("notag").child("ad"+idpass1).child(String.valueOf(currentTimestamp));
                        DatabaseReference newWithTagADRecordRef = watchRecordsRef.child("Advertise").child("withtag").child("ad"+idpass1).child(String.valueOf(currentTimestamp));

                        DatabaseReference newUSRVidRecordRef = UsrwatchRecordsRef.child("Videos").child(String.valueOf(currentTimestamp));
                        DatabaseReference newUSRNoTagADRecordRef = UsrwatchRecordsRef.child("Advertise").child("notag").child(String.valueOf(currentTimestamp));
                        DatabaseReference newUSRWithTagADRecordRef = UsrwatchRecordsRef.child("Advertise").child("withtag").child(String.valueOf(currentTimestamp));

                        ///觀看次數
                        DatabaseReference newVidecordTotalRef = TotalWatchRef.child("Videos").child("V"+idpass1);
                        DatabaseReference shopViewRef = FirebaseDatabase.getInstance().getReference().child("shopView");
                        String dateMonth = getCurrentMonth();
                        DatabaseReference monthShopViewRef = shopViewRef.child(dateMonth);

//                    newVidWatchRecordRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            for (DataSnapshot tagSnapshot : dataSnapshot.getChildren()) {
//                                Log.d("tag", String.valueOf(tagSnapshot.getKey()));
//                            }
//                        }
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//                            // 处理错误
//                        }
//                    });

                        //////總共觀看時間次數 目前只有廣告
                        if((idpass1/100000)==1){
                            /////////notag廣告
                            NoTagADRecordTotalRef.child("看完").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        NoTagADRecordTotalRef.child("看完").setValue(newValue);


                                    } else {
                                        NoTagADRecordTotalRef.child("看完").setValue(1);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });
                        }else if((idpass1/100000)==2){
                            /////////withtag廣告

                            WithTagADRecordTotalRef.child("看完").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        WithTagADRecordTotalRef.child("看完").setValue(newValue);
                                    } else {
                                        WithTagADRecordTotalRef.child("看完").setValue(1);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });
                        }

                        /////觀看時間次數----期間
                        if((idpass1/100000)==1){
                            /////////notag廣告
                            notagPeriodWatchRef.child("看完").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        notagPeriodWatchRef.child("看完").setValue(newValue);


                                    } else {
                                        notagPeriodWatchRef.child("看完").setValue(1);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });


                        }else if((idpass1/100000)==2){
                            /////////withtag廣告

                            withtagPeriodWatchRef.child("看完").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        withtagPeriodWatchRef.child("看完").setValue(newValue);
                                    } else {
                                        withtagPeriodWatchRef.child("看完").setValue(1);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });

                        }

//////////時間戳記
                        if((idpass1/100000)==1){
                            /////////notag廣告
                            newNoTagADRecordRef.setValue(username)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("WatchRecord", "Time stamp saved successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("WatchRecord", "Failed to save time stamp", e);
                                        }
                                    });
                        }else if((idpass1/100000)==2){
                            /////////withtag廣告
                            newWithTagADRecordRef.setValue(username)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("WatchRecord", "Time stamp saved successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("WatchRecord", "Failed to save time stamp", e);
                                        }
                                    });

                        }else{
                            //////video那邊
                            newVidWatchRecordRef.setValue(username)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("WatchRecord", "Time stamp saved successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("WatchRecord", "Failed to save time stamp", e);
                                        }
                                    });
                        }

///user那邊
                        if((idpass1/100000)==1){
                            /////////notag廣告
                            newUSRNoTagADRecordRef.setValue("V"+idpass1)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("WatchRecord", "Time stamp saved successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("WatchRecord", "Failed to save time stamp", e);
                                        }
                                    });
                        }else if((idpass1/100000)==2){
                            /////////withtag廣告
                            newUSRWithTagADRecordRef.setValue("V"+idpass1)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("WatchRecord", "Time stamp saved successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("WatchRecord", "Failed to save time stamp", e);
                                        }
                                    });
                        }else{
                            //////video那邊
                            newUSRVidRecordRef.setValue("V"+idpass1)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("WatchRecord", "Time stamp saved successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("WatchRecord", "Failed to save time stamp", e);
                                        }
                                    });
                        }


/////算總觀看次數
                        if((idpass1/100000)==1){
                            /////////notag廣告
                            NoTagADRecordTotalRef.child("總觀看數").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        NoTagADRecordTotalRef.child("總觀看數").setValue(newValue);

                                        ///////////////>10秒喜歡

                                    } else {
                                        NoTagADRecordTotalRef.child("總觀看數").setValue(1);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });
                        }else if((idpass1/100000)==2){
                            /////////withtag廣告
                            WithTagADRecordTotalRef.child("總觀看數").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        WithTagADRecordTotalRef.child("總觀看數").setValue(newValue);

                                        ///////////////>10秒喜歡

                                    } else {
                                        WithTagADRecordTotalRef.child("總觀看數").setValue(1);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });
                        }else{
                            //////video那邊
                            newVidecordTotalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        newVidecordTotalRef.setValue(newValue);

                                        makeTotalTagRankData();

                                        ///////////////>10秒喜歡

                                    } else {
                                        newVidecordTotalRef.setValue(1);
                                    }
                                    Log.d("HomeFragment77777777", "INshopname: " + shopnamefuck);
                                    /////////店家估看樹
                                    monthShopViewRef.child(shopnamefuck).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            int currentCount = snapshot.getValue(Integer.class) != null ? snapshot.getValue(Integer.class) : 0;
                                            monthShopViewRef.child(shopnamefuck).setValue(currentCount + 1);
                                            Log.d("HomeFragment55555555", "INshopname: " + shopnamefuck);

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // 處理錯誤
                                        }
                                    });
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });

                            Log.d("HomeFragment", "Fshopname1: " + shopname1);
                            Log.d("HomeFragment", "Fshopname: " + shopname);

                        }



                        //////////.////////////

                        //////近期觀看次數 近期觀看次數 近期觀看次數目前只有廣告

                        if((idpass1/100000)==1){
                            /////////notag廣告

                            NoTagADRecordRef2.orderByKey().startAt(getStartKeyFor7DaysAgo()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    int count = 0;
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {/////處理篩選後7天內的值
                                        //snapshot.getKey();
                                        count++;
                                    }

                                    int finalCount = count;
                                    notagPeriodWatchRef.child("近期觀看數").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                notagPeriodWatchRef.child("近期觀看數").setValue(finalCount);

                                            } else {
                                                notagPeriodWatchRef.child("近期觀看數").setValue(finalCount);
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Handle onCancelled
                                        }
                                    });


                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }else if((idpass1/100000)==2){
                            /////////withtag廣告

                            WithTagADRecordRef2.orderByValue().startAt(getStartKeyFor7DaysAgo()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    int count = 0;
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        count++;
                                    }

                                    int finalCount = count;
                                    withtagPeriodWatchRef.child("近期觀看數").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                withtagPeriodWatchRef.child("近期觀看數").setValue(finalCount);

                                            } else {
                                                withtagPeriodWatchRef.child("近期觀看數").setValue(finalCount);
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Handle onCancelled
                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }






                    } ////////////////////////>10秒喜歡END

                    else {
                        ///////////觀看時間在中間值的
                        if((idpass1/100000)==1){
                            /////////notag廣告
                            notagPeriodWatchRef.child("觀看一段時間").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        notagPeriodWatchRef.child("觀看一段時間").setValue(newValue);


                                    } else {
                                        notagPeriodWatchRef.child("觀看一段時間").setValue(1);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });


                        }else if((idpass1/100000)==2){
                            /////////withtag廣告

                            withtagPeriodWatchRef.child("觀看一段時間").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        withtagPeriodWatchRef.child("觀看一段時間").setValue(newValue);

                                    } else {
                                        withtagPeriodWatchRef.child("觀看一段時間").setValue(1);

                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });

                        }

                        //////總共觀看時間次數 目前只有廣告
                        if((idpass1/100000)==1){
                            /////////notag廣告
                            NoTagADRecordTotalRef.child("觀看一段時間").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        NoTagADRecordTotalRef.child("觀看一段時間").setValue(newValue);


                                    } else {
                                        NoTagADRecordTotalRef.child("觀看一段時間").setValue(1);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });
                        }else if((idpass1/100000)==2){
                            /////////withtag廣告

                            WithTagADRecordTotalRef.child("觀看一段時間").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        long currentValue = dataSnapshot.getValue(Long.class);
                                        long newValue = currentValue +1 ; ///////觀看次數加 1
                                        WithTagADRecordTotalRef.child("觀看一段時間").setValue(newValue);
                                    } else {
                                        WithTagADRecordTotalRef.child("觀看一段時間").setValue(1);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle onCancelled
                                }
                            });
                        }


                    }///////////觀看時間在中間值的end




                    Log.d("HomeFragment", "Bshopname1: " + shopname1);
                    Log.d("HomeFragment", "Bshopname: " + shopname);
                    shopnamefuck = shopname1;
                    shopname1 = shopname;
                    // 重置计时器
                    videoStartTime = System.currentTimeMillis();

                    //idpass1世上一部影片的ID
                    idpass1 = id;

                    Log.d("HomeFragment", "Cshopname1: " + shopname1);
                    Log.d("HomeFragment", "Cshopname: " + shopname);

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

                tagArrayList = new ArrayList<>();

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
                            Log.d("HomeFragment","tags count : "+tagArrayList.size());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                commentArrayList = new ArrayList<>();
                userArrayList = new ArrayList<>();

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

        //loadAdvertiseFromFirebaseADVANCE();
        loadAdvertiseFromFirebase();

        loadVideosFromFirebase();


        loadVideosFromFirebaseADVANCE();
        return view;

    }//////////////// oncreateView END






    private DatabaseReference preferencesRef222 ;
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference totalTagRankRef = rootRef.child("TotalTagRank");
    private Map<String, Object> updates = new HashMap<>();
    private Map<String, Long> tagCountMap = new HashMap<>();
    String tag;

    private static final int TOP_X = 4; // 設置要取前 X 名的數量

    private void makeTotalTagRankData() {
        DatabaseReference preferencesRef = rootRef.child("Users");

        preferencesRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d("TotalTag", "11111");
                    //String currentPath1 = preferencesRef.getKey();
                    //Log.d("TotalTag", currentPath1);

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String username = userSnapshot.getKey();
                        Log.d("TotalTag111", username);
                        preferencesRef222 = userSnapshot.child("preferences").getRef();
                        String currentPath = preferencesRef222.getKey();
                        Log.d("TotalTag", currentPath);


                        preferencesRef222.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot preferencesSnapshot) {
                                if (preferencesSnapshot.exists()) {
                                    Map<String, Integer> data = (Map<String, Integer>) preferencesSnapshot.getValue();

                                    if (data != null) {
                                        List<Map.Entry<String, Integer>> sortedData = new ArrayList<>(data.entrySet());
                                        sortedData.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
                                        Log.d("tag777", String.valueOf(sortedData.size()));

                                        //  updates = new HashMap<>();
                                        for (int i = 0; i < TOP_X && i < sortedData.size(); i++) {
                                            Map.Entry<String, Integer> entry = sortedData.get(i);
                                            Log.d("tag777", username);
                                            Log.d("tag777", "設了");
                                            tag = entry.getKey();
                                            Long value = ((Number) entry.getValue()).longValue();

                                            tagCountMap.put(tag, tagCountMap.getOrDefault(tag, 0L) + 1);


//                                            SETTotalTagRankData();
                                            Log.d("tag777","經過tagCountMap" );
                                            // 在所有 preferences 节点都被遍历完毕后，进行统计和写入操作
                                            Map<String, Object> updates = new HashMap<>();
                                            for (Map.Entry<String, Long> entry1 : tagCountMap.entrySet()) {
                                                updates.put(entry1.getKey(), entry1.getValue());
                                                Log.d("tag777", tagCountMap.entrySet().toString());
                                            }
                                            totalTagRankRef.updateChildren(updates);

                                        }

//                                        totalTagRankRef.updateChildren(updates);
                                    }
                                }
                            }


                        });
//                        addPreferencesListener(preferencesRef222);


                    }
                }


                ///...

            }

            public void onCancelled(@NonNull DatabaseError error) {
                // 處理錯誤
            }
        });

    }

//    private void SETTotalTagRankData() {
//        DatabaseReference tagRef = totalTagRankRef.child(tag);
//        Log.d("tag777", tag);
//        tagRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//            @Override
//            public void onSuccess(DataSnapshot tagSnapshot) {
//                Log.d("tag777", "進了");
//                Long currentValue = tagSnapshot.getValue(Long.class);
//                if (currentValue == null) {
//                    tagRef.setValue(0L); // 如果節點不存在,創建新節點並設值為 0
//                } else {
//                    updates.put(tag, currentValue + 1);
//                    totalTagRankRef.updateChildren(updates);
//                }
//            }
//        });
//
//    }
//    public void loadLikeFromFirebase() {
//        userRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    if (snapshot.hasChild("Fav")) {
//                        doFav = snapshot.child("Fav").getValue(boolean.class);
//                        //Log.d("TAGggg", String.valueOf(doFav));
//                        if (doFav != null) {
//                            tag1 = doFav;
//                            // Log.d("why", "doFav"+String.valueOf(doFav));
//                            Log.d("why", "fav1"+String.valueOf(fav1));
//
//                        } else {
//                            tag1 = false;
//                            Log.d("gttg", "777777777777777777777777777777777");
//                        }
//                    }
//
//                    if (snapshot.hasChild("Tag")) {
//                        doTag = snapshot.child("Tag").getValue(boolean.class);
//                        // Log.d("TAGggg", String.valueOf(doTag));
//                        if (doTag != null) {
//                            tag1 = doTag;
//                            // Log.d("why", "doTag"+String.valueOf(doTag));
//                            Log.d("why", "tag1"+String.valueOf(tag1));
//                        } else {
//                            tag1 = false;
//                            Log.d("gttg", "777777777777777777777777777777777");
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to read value.", error.toException());
//            }
//        });
//    }

    //////////////////製作沒tag的所有"廣告"的list
    boolean p = false;
    public void loadAdvertiseFromFirebase() {

        ADRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ADList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String ADUrl = snapshot.child("ADUrl").getValue(String.class);
                    Long Aidprevention = snapshot.child("Aid").getValue(Long.class);
                    long Aid = (Aidprevention != null) ? Aidprevention : 0; // 如果为空，则设置为默认值 0

                    String title =" ";
                    String address = "  ";
                    String date = " ";
                    String price = "   ";
                    String uploader =" ";
                    String profileImageUrl = " ";

                    if (ADUrl == null || Aidprevention == null) {
                        // 如果有任何一个字段为空，则跳过当前影片的处理 不然上傳會出錯
                        continue;
                    }
                    Video ad = new Video(ADUrl,title, address, date, price,Aid,uploader,profileImageUrl);
                    ADList.add(ad);
                    AdALLList.add(ad);///////////全部廣告列表
                }
                Collections.shuffle(ADList);
                adapter.notifyDataSetChanged();
                Log.d("AdALLList長度11", String.valueOf(AdALLList.size()));
                Log.d("ADList", "經過了");
                Log.d("ADList", String.valueOf(ADList.size()));

                // loadAdvertiseFromFirebaseADVANCE();
                p=true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }


        });

//if(p==false) {
        ////////////////////////////////////////////////////// adlist with tag
//    List<String> ADpreferencesList = new ArrayList<>();
//    Log.d("經過了adre", "經過了");
//    preferencesRef = database.getReference("Users").child(username).child("preferences");
//
//    // 獲取 "User/preferences" 節點
//    preferencesRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//        @Override
//        public void onSuccess(DataSnapshot snapshot) {
//            NoTagADList.clear();
//            GenericTypeIndicator<Map<String, Long>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Long>>() {
//            };
//            Map<String, Long> preferencesMap = snapshot.getValue(genericTypeIndicator);//<String, Long>前者是標籤名稱後者是值
//
//            if (preferencesMap != null) {
//                List<Map.Entry<String, Long>> preferenceEntries = new ArrayList<>(preferencesMap.entrySet());
//                Collections.sort(preferenceEntries, Collections.reverseOrder(Map.Entry.comparingByValue()));//根據偏好值降序排列映射條目列表。
//
//                for (int i = 0; i < ADparameterRecom && i < preferenceEntries.size(); i++) {//將前幾名添加到preferencesList
//                    ADpreferencesList.add(preferenceEntries.get(i).getKey());
//                }
//                Log.d("parameterRecomAD", String.valueOf(ADpreferencesList));
//            }
//            //DatabaseReference ADRef2 = ADRef0.child("withtag");
//            // 獲取 "AD TAG" 節點
//            ADRef1.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//                @Override
//
//                public void onSuccess(DataSnapshot snapshot) {
//                    Log.d("NoTagADList長度", "1111111");
//                    for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
//                        DataSnapshot tagSnapshot = videoSnapshot.child("adtags");
////
////                            for (DataSnapshot tagValue : tagSnapshot.getChildren()) {
////                                String videoTag = tagValue.getValue(String.class);
////                                if (ADpreferencesList.contains(videoTag)) {
//
//                        String ADUrl = videoSnapshot.child("ADUrl").getValue(String.class);
//                        Long Aidprevention = videoSnapshot.child("Aid").getValue(Long.class);
//                        long Aid = (Aidprevention != null) ? Aidprevention : 0; // 如果为空，则设置为默认值 0
//                        String title = "null";
//                        String address = "null";
//                        String date = "null";
//                        String price = "null";
//                        String uploader = videoSnapshot.child("uploader").getValue(String.class);
//
//                        if (ADUrl == null || Aidprevention == null) {
//                            // 如果有任何一个字段为空，则跳过当前影片的处理 不然上傳會出錯
//                            continue;
//                        }
//                        Video AD = new Video(ADUrl, title, address, date, price, Aid, uploader);
//                        NoTagADList.add(AD);
//
//                        break;
////                                }
////                         }
//
//                    }
//                    // 打亂列表順序
//                    Collections.shuffle(NoTagADList);
//                    Log.d("NoTagADList長度", String.valueOf(NoTagADList.size()));
//                    //Log.d("parameterRecom", String.valueOf(id));
//                    // ADMIX();
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    // 處理錯誤
//                    Log.e("NoTagADList長度", "失敗原因: " + e.getMessage());
//                }
//            });
//        }
//    }).addOnFailureListener(new OnFailureListener() {
//        @Override
//        public void onFailure(@NonNull Exception e) {
//            // 處理錯誤
//        }
//    });
//}else{
//    Log.d("NoTagADList","進來else囉" );
//}
        ////with tag END



    }
//    ////////有tag的餐廳廣告
//    public void loadAdvertiseFromFirebaseADVANCE() {
//        List<String> ADpreferencesList = new ArrayList<>();
//        Log.d("經過了adre", "經過了");
//        preferencesRef = database.getReference("Users").child(username).child("preferences");
//
//        // 獲取 "User/preferences" 節點
//        preferencesRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//            @Override
//            public void onSuccess(DataSnapshot snapshot) {
//                NoTagADList.clear();
//                GenericTypeIndicator<Map<String, Long>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Long>>() {};
//                Map<String, Long> preferencesMap = snapshot.getValue(genericTypeIndicator);//<String, Long>前者是標籤名稱後者是值
//
//                if (preferencesMap != null) {
//                    List<Map.Entry<String, Long>> preferenceEntries = new ArrayList<>(preferencesMap.entrySet());
//                    Collections.sort(preferenceEntries, Collections.reverseOrder(Map.Entry.comparingByValue()));//根據偏好值降序排列映射條目列表。
//
//                    for (int i = 0; i < ADparameterRecom && i < preferenceEntries.size(); i++) {//將前幾名添加到preferencesList
//                        ADpreferencesList.add(preferenceEntries.get(i).getKey());
//                    }
//                    Log.d("parameterRecomAD", String.valueOf(ADpreferencesList));
//                }
//                //DatabaseReference ADRef2 = ADRef0.child("withtag");
//                // 獲取 "AD TAG" 節點
//                ADRef1.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//                    @Override
//
//                    public void onSuccess(DataSnapshot snapshot) {
//                        Log.d("NoTagADList長度", "1111111");
//                        for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
//                            DataSnapshot tagSnapshot = videoSnapshot.child("adtags");
////
////                            for (DataSnapshot tagValue : tagSnapshot.getChildren()) {
////                                String videoTag = tagValue.getValue(String.class);
////                                if (ADpreferencesList.contains(videoTag)) {
//
//                                    String ADUrl = videoSnapshot.child("ADUrl").getValue(String.class);
//                                    Long Aidprevention = videoSnapshot.child("Aid").getValue(Long.class);
//                                    long Aid = (Aidprevention != null) ? Aidprevention : 0; // 如果为空，则设置为默认值 0
//                                    String title = "null";
//                                    String address = "null";
//                                    String date = "null";
//                                    String price = "null";
//                                    String uploader = videoSnapshot.child("uploader").getValue(String.class);
//
//                                    if (ADUrl == null || Aidprevention == null) {
//                                        // 如果有任何一个字段为空，则跳过当前影片的处理 不然上傳會出錯
//                                        continue;
//                                    }
//                                    Video AD = new Video(ADUrl,title, address, date, price,Aid,uploader);
//                                    NoTagADList.add(AD);
//
//                                    break;
////                                }
////                         }
//
//                        }
//                        // 打亂列表順序
//                        Collections.shuffle(NoTagADList);
//                        Log.d("NoTagADList長度", String.valueOf(NoTagADList.size()));
//                        //Log.d("parameterRecom", String.valueOf(id));
//                       // ADMIX();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // 處理錯誤
//                        Log.e("NoTagADList長度", "失敗原因: " + e.getMessage());
//                    }
//                });
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                // 處理錯誤
//            }
//        });
//    }
    //////////////有tag的餐廳廣告ENd

    /////////////////////做一個廣告的混合列表
    //幾筆幾比例改mix這邊
//    public void ADMIX() {
//        Log.d("ADmergedList", "經過ADmergedList");
//        //ADmergedList.clear(); // 清空 mergedList
//        ADmergedList = new ArrayList<>();//////廣告的混合列表
//       // if (NoTagADList != null && ADList != null) {
//            int maxSize = Math.max(NoTagADList.size(), ADList.size());
//            int ADListIndex = 0;
//            int NoTagADListIndex = 0;
//            int cycle = 0; // 记录当前循环次数
//
//            while (ADListIndex < ADList.size() || NoTagADListIndex < NoTagADList.size()) {
//                for (int i = 0; i < parameterADList && ADListIndex < ADList.size(); i++) {
//                    ADmergedList.add(ADList.get(ADListIndex));
//                    Log.d("位置id", String.valueOf(ADList.get(ADListIndex).getId()));
//                    ADListIndex++;
//                }
//
//                for (int i = 0; i < parameterNoTagADList && NoTagADListIndex < NoTagADList.size(); i++) {
//                    ADmergedList.add(NoTagADList.get(NoTagADListIndex));
//                    Log.d("位置id", String.valueOf(NoTagADList.get(NoTagADListIndex).getId()));
//                    NoTagADListIndex++;
//                }
//                cycle++;
//            }
//      //  }
//        Log.d("ADmergedList", String.valueOf(ADmergedList.size()));
//    }
/////////////////////做一個廣告的混合列表END

    //////////////////////////////////以下影片區
    //////////////////////////一般的
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
                    String uploader = snapshot.child("Uploader").getValue(String.class);

                    Long idprevention = snapshot.child("id").getValue(Long.class);
                    long id = (idprevention != null) ? idprevention : 0; // 如果为空，则设置为默认值 0

                    if (title == null || address == null || date == null || price == null || videoUrl == null || idprevention == null||uploader==null) {
                        // 如果有任何一个字段为空，则跳过当前影片的处理 不然上傳會出錯
                        continue;
                    }
                    userRef = FirebaseDatabase.getInstance().getReference("Users");
                    // 在这里查询数据库以获取 profileImageUrl
                    userRef.child(uploader).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                            // 创建一个包含 profileImageUrl 的 Video 对象
                            Video video = new Video(videoUrl, title, address, date, price, id, uploader, profileImageUrl);
                            videoList.add(video);
                            Collections.shuffle(videoList);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Failed to read value.", error.toException());
                        }
                    });
                }
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


    public void loadVideosFromFirebaseADVANCE() {//製作只有演算法喜好前幾名的list
        List<String> preferencesList = new ArrayList<>();
        Log.d("經過了re", "經過了");
        preferencesRef = database.getReference("Users").child(username).child("preferences");

        // 獲取 "User/preferences" 節點
        preferencesRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                videoListRE.clear();
                WithTagADList.clear();
                GenericTypeIndicator<Map<String, Long>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Long>>() {};
                Map<String, Long> preferencesMap = snapshot.getValue(genericTypeIndicator);//<String, Long>前者是標籤名稱後者是值

                if (preferencesMap != null) {
                    List<Map.Entry<String, Long>> preferenceEntries = new ArrayList<>(preferencesMap.entrySet());
                    Collections.sort(preferenceEntries, Collections.reverseOrder(Map.Entry.comparingByValue()));//根據偏好值降序排列映射條目列表。

                    for (int i = 0; i < parameterRecom && i < preferenceEntries.size(); i++) {//將前幾名添加到preferencesList
                        preferencesList.add(preferenceEntries.get(i).getKey());
                    }
                    Log.d("parameterRecom", String.valueOf(preferencesList));
                }



                // 獲取 "Videos" 節點
                videosRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot snapshot) {
                        for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
                            DataSnapshot tagSnapshot = videoSnapshot.child("Foodtags");

                            for (DataSnapshot tagValue : tagSnapshot.getChildren()) {
                                String videoTag = tagValue.getValue(String.class);
                                if (preferencesList.contains(videoTag)) {
                                    String title = videoSnapshot.child("title").getValue(String.class);
                                    String address = videoSnapshot.child("address").getValue(String.class);
                                    String date = videoSnapshot.child("date").getValue(String.class);
                                    String price = videoSnapshot.child("price").getValue(String.class);
                                    String videoUrl = videoSnapshot.child("videoUrl").getValue(String.class);
                                    String uploader = videoSnapshot.child("Uploader").getValue(String.class);

                                    long id = videoSnapshot.child("id").getValue(long.class);
                                    userRef.child(uploader).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                                            // 创建一个包含 profileImageUrl 的 Video 对象
                                            Video video = new Video(videoUrl, title, address, date, price, id, uploader,profileImageUrl);
                                            videoListRE.add(video);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e(TAG, "Failed to read value.", error.toException());
                                        }
                                    });

                                    break;
                                }
                            }
                        }
                        // 打亂列表順序
                        Collections.shuffle(videoListRE);
                        Log.d("videoListRE長度", String.valueOf(videoListRE.size()));
                        //Log.d("parameterRecom", String.valueOf(id));

                        //loadAdvertiseFromFirebase();
                        //loadAdvertiseFromFirebaseADVANCE();

                        // 在這裡調用 loadVideosMIX()
                        //loadVideosMIX();


////使用有tag的廣告製作播放清單
                        ////////////////////////////////////////////////////////
                        ADRef1.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override

                            public void onSuccess(DataSnapshot snapshot) {
                                Log.d("NoTagADList長度", "1111111");
                                for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
                                    DataSnapshot tagSnapshot = videoSnapshot.child("adtags");



                                    String ADUrl = videoSnapshot.child("ADUrl").getValue(String.class);
                                    Long Aidprevention = videoSnapshot.child("Aid").getValue(Long.class);
                                    long Aid = (Aidprevention != null) ? Aidprevention : 0; // 如果为空，则设置为默认值 0
                                    String title = "    ";
                                    String address = "  ";
                                    String date = " ";
                                    String price = "    ";
                                    String uploader = videoSnapshot.child("uploader").getValue(String.class);
                                    String profileImageUrl = "   ";

                                    if (ADUrl == null || Aidprevention == null) {
                                        // 如果有任何一个字段为空，则跳过当前影片的处理 不然上傳會出錯
                                        continue;
                                    }

                                    Video AD = new Video(ADUrl,title, address, date, price,Aid,uploader,profileImageUrl);

                                    for (DataSnapshot tagValue : tagSnapshot.getChildren()) {
                                        String videoTag = tagValue.getValue(String.class);
                                        if (preferencesList.contains(videoTag)) {
                                            WithTagADList.add(AD);
                                            break;
                                        }
                                    }
                                    AdALLList.add(AD);///////////全部廣告列表
                                }
                                // 打亂列表順序
                                Collections.shuffle(WithTagADList);
                                Collections.shuffle(AdALLList);
                                Log.d("WithTagADList長度", String.valueOf(WithTagADList.size()));
                                Log.d("AdALLList長度", String.valueOf(AdALLList.size()));
                                //Log.d("parameterRecom", String.valueOf(id));
                                // ADMIX();


                                /////////金主爸爸廣告
                                ////////////////////////////////////////////////////////把沒tag金主放入金主列表
                                ADRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        //ADList.clear();
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                            if ( snapshot.child("dad").getValue(Long.class)==1){

                                                String ADUrl = snapshot.child("ADUrl").getValue(String.class);
                                                Long Aidprevention = snapshot.child("Aid").getValue(Long.class);
                                                long Aid = (Aidprevention != null) ? Aidprevention : 0; // 如果为空，则设置为默认值 0

                                                String title = " ";
                                                String address = "  ";
                                                String date = " ";
                                                String price = "   ";
                                                String uploader = "  ";
                                                String profileImageUrl = "   ";

                                                if (ADUrl == null || Aidprevention == null) {
                                                    // 如果有任何一个字段为空，则跳过当前影片的处理 不然上傳會出錯
                                                    continue;
                                                }
                                                Video ad = new Video(ADUrl, title, address, date, price, Aid, uploader,profileImageUrl);
                                                AdDADList.add(ad);
                                                //AdALLList.add(ad);///////////全部廣告列表
                                            }
                                        }
//                                        Collections.shuffle(ADList);
                                        adapter.notifyDataSetChanged();
                                        Log.d("AdDADListNotag", "經過了");
                                        Log.d("AdDADListNotag", String.valueOf(AdDADList.size()));

                                        // loadAdvertiseFromFirebaseADVANCE();


                                        ////////////////////////////////////////////////////////把有tag金主放入金主列表
                                        ADRef1.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                            @Override

                                            public void onSuccess(DataSnapshot snapshot) {
                                                Log.d("NoTagADList長度", "1111111");
                                                for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
                                                    if (videoSnapshot.child("dad").getValue(Long.class) == 1){
                                                        DataSnapshot tagSnapshot = videoSnapshot.child("adtags");

                                                        for (DataSnapshot tagValue : tagSnapshot.getChildren()) {
                                                            String videoTag = tagValue.getValue(String.class);

                                                            String ADUrl = videoSnapshot.child("ADUrl").getValue(String.class);
                                                            Long Aidprevention = videoSnapshot.child("Aid").getValue(Long.class);
                                                            long Aid = (Aidprevention != null) ? Aidprevention : 0; // 如果为空，则设置为默认值 0
                                                            String title = "    ";
                                                            String address = "  ";
                                                            String date = " ";
                                                            String price = "    ";
                                                            String uploader = " ";
                                                            String profileImageUrl = "   ";

                                                            if (ADUrl == null || Aidprevention == null) {
                                                                // 如果有任何一个字段为空，则跳过当前影片的处理 不然上傳會出錯
                                                                continue;
                                                            }
                                                            Video AD = new Video(ADUrl, title, address, date, price, Aid, uploader,profileImageUrl);
                                                            AdDADList.add(AD);
                                                            //AdALLList.add(AD);///////////全部廣告列表

                                                        }
                                                        Log.d("AdDADListwithtag", "經過了");
                                                        Log.d("AdDADListwithtag", String.valueOf(AdDADList.size()));
                                                    }
                                                }

                                                // 打亂列表順序
                                                Collections.shuffle(AdDADList);
                                                Log.d("AdDADList長度", String.valueOf(AdDADList.size()));
                                                Log.d("經過AdDad列表最後階段", "77777");
                                                //Log.d("parameterRecom", String.valueOf(id));
                                                // ADMIX();
                                                loadVideosMIX();
                                            }
                                        });





                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e(TAG, "Failed to read value.", error.toException());
                                    }
                                });





                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // 處理錯誤
                                Log.e("NoTagADList長度", "失敗原因: " + e.getMessage());
                            }
                        });
/////////////////////////////////////////////////////

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 處理錯誤
                    }
                });





            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // 處理錯誤
            }
        });
    }
    ////////////////////////////影片區 END


    //幾筆幾比例改mix這邊
    public void loadVideosMIX() {

/////廣告total混合列表
        Log.d("ADmergedList", "經過ADmergedList");
        //ADmergedList.clear(); // 清空 mergedList
        ADmergedList = new ArrayList<>();//////廣告的混合列表
        // if (NoTagADList != null && ADList != null) {
        int maxSize = Math.max(WithTagADList.size(), ADList.size());
        int ADListIndex = 0;
        int WithTagADListIndex = 0;
        int ALLADListIndex = 0;
        int AdDADListIndex = 0;


        int cycle = 0; // 记录当前循环次数

        while (ADListIndex < ADList.size() || WithTagADListIndex < WithTagADList.size() || ALLADListIndex < AdALLList.size() || AdDADListIndex <  AdDADList.size()) {
            ///////全不沒tag廣告
            for (int i = 0; i < parameterADList && ADListIndex < ADList.size(); i++) {
                ADmergedList.add(ADList.get(ADListIndex));
                Log.d("位置id_ADList", String.valueOf(ADList.get(ADListIndex).getId()));
                ADListIndex++;
            }
/////////全部有tag廣告
            for (int i = 0; i < parameterNoTagADList && WithTagADListIndex < WithTagADList.size(); i++) {
                ADmergedList.add(WithTagADList.get(WithTagADListIndex));
                Log.d("位置id_WithTagADList", String.valueOf(WithTagADList.get(WithTagADListIndex).getId()));
                WithTagADListIndex++;
            }
            ////金主爸爸廣告
            for (int i = 0; i < parameterAdDADList && AdDADListIndex < AdDADList.size(); i++) {
                ADmergedList.add(AdDADList.get(AdDADListIndex));
                Log.d("位置id_AdDADList", String.valueOf(AdDADList.get(AdDADListIndex).getId()));
                AdDADListIndex++;
            }
            ////全部廣告隨機
            for (int i = 0; i < parameterAdALLList && ALLADListIndex < AdALLList.size(); i++) {
                ADmergedList.add(AdALLList.get(ALLADListIndex));
                Log.d("位置id_AdALLList", String.valueOf(AdALLList.get(ALLADListIndex).getId()));
                ALLADListIndex++;
            }

            cycle++;
        }
        //  }
        Log.d("ADmergedList", String.valueOf(ADmergedList.size()));

        //////////////////////////////////////////////////////////////


        // mergedList.clear(); // 清空 mergedList
        mergedList = new ArrayList<>();
        if (videoListRE != null && videoList != null) {
            //int maxSize = Math.max(videoListRE.size(), videoList.size());
            int videoListIndex = 0;
            int videoListREIndex = 0;
            int ADmergedListIndex = 0;
            // int cycle = 0; // 记录当前循环次数

            while (videoListIndex < videoList.size() || videoListREIndex < videoListRE.size()) {
                for (int j = 0; j < parameterVideoListTotal; j++) {

                    for (int i = 0; i < parameterVideoList && videoListIndex < videoList.size(); i++) {
                        mergedList.add(videoList.get(videoListIndex));
                        Log.d("n位置id", String.valueOf(videoList.get(videoListIndex).getId()));
                        videoListIndex++;
                    }
                    for (int i = 0; i < parameterVideoListRE && videoListREIndex < videoListRE.size(); i++) {
                        mergedList.add(videoListRE.get(videoListREIndex));
                        Log.d("re位置id", String.valueOf(videoListRE.get(videoListREIndex).getId()));
                        videoListREIndex++;
                    }
                }
                if (ADmergedList != null) {
                    for (int i = 0; i < parameterTotalAD && ADmergedListIndex < ADmergedList.size(); i++) {
                        mergedList.add(ADmergedList.get(ADmergedListIndex));
                        Log.d("ad位置id", String.valueOf(ADmergedList.get(ADmergedListIndex).getId()));
                        ADmergedListIndex++;
                    }
                } else {
                    Log.d("ADmergedList", "is null");


                }

                cycle++;

//                if (cycle == 1) {
////                    parameterVideoList = 2; // 第二次循环时调整比例
////                    parameterVideoListRE = 3;
//                }
            }

        }

        Log.d("mergedList", String.valueOf(mergedList.size()));
//創建新的 VideoAdapter 實例並設置給 viewPager2

        // 在這裡更新 adapter 的數據

        adapter.updateData(mergedList);
    }
    //////////////////////
    private String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int monthIndex = calendar.get(Calendar.MONTH) + 1; // 月份從 0 開始,所以加 1
        String monthName = String.format("%02d", monthIndex); // 使用 %02d 確保月份是兩位數字
        return year + "/" + monthName;
    }


}

