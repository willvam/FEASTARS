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
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class    HomeFragment extends Fragment implements VideoAdapter.OnProfileImageClickListener  {
    //private RecyclerView recyclerView;
    public List<Video> videoList;
    private List<Video> videoListRE;
    private List<Video> ADList;
    private List<Video> NoTagADList;
    private VideoAdapter adapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference videosRef;
    private DatabaseReference ADRef1;
    private DatabaseReference ADRef;
    private DatabaseReference userRef;
    private DatabaseReference preferencesRef;
    private List<Video> mergedList;
    private List<Video>     ADmergedList;
    private long id;
    public boolean tag1;
    public boolean fav1;
    private long parameterTime=5; //大於小於後要+-多少分數
    private long parameterTimeLOW=4; //小於幾秒-parameterTime
    private long parameterTimeHIGH=10;//大於幾秒-parameterTime
    private long parameterRecom=3;//選擇喜好分數前幾名的加入"影片"推薦列表
    ///////////以下為最後影片
    private long parameterVideoListTotal=6;/////以下兩個的比例要輪迴幾次
    private long parameterVideoList = 1; // 最終影片清單:先放入幾个videoList元素(演算法比例調整)
    private long parameterVideoListRE = 1; // 最終影片清單:然后放入幾个videoListRE元素
    private long parameterTotalAD = 1;//最終影片清單:放入幾廣告
    private long ADparameterRecom=3;//無用。(選喜好前幾名加入"廣告"推薦) "目前和影片興趣共用和，請更改parameterRecom"


    private long parameterADList = 1; // 先放入幾个沒tag廣告元素(演算法比例調整)
    private long parameterNoTagADList = 1; // 然后放入幾个有tag廣告元素

    private long VideoViewCount;
    public static long idpass1;
    //public ArrayList<String> id = new ArrayList<>();
    private long nodeCount;
    //private List<Video> mergedList2;

    public static ArrayList<Tag> tagArrayList;
    public static ArrayList<Comment> commentArrayList;

    public static ArrayList<Comment> userArrayList;
    public static int page;
    private String username;
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
            Log.d("HomeFragment","有近來這個 = "+uploader);

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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ViewPager2 viewPager2 = view.findViewById(R.id.viewPager2);
        //RecyclerView recyclerView = view.findViewById(R.id.recyclerView); 4/25


        // Initialize videoList and adapter (you may want to pass data from MainActivity or fetch it here)
        videoList = new ArrayList<>();
        videoListRE = new ArrayList<>();
        NoTagADList = new ArrayList<>();
        ADList = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        videosRef = database.getReference("Videos");
        ADRef1 = database.getReference("advertise").child("withtag");
        ADRef = database.getReference("advertise").child("notag");
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
        // id = video.getId();
        FragmentManager fragmentManager = getChildFragmentManager();

        ////////////取得使用者名稱
        Context context = requireContext();
        username = SharedPreferencesUtils.getUsername(context);
        userRef= database.getReference("Users").child(username).child("Cont");
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
                    Log.d("idid", String.valueOf(idpass1));
                    Log.d("ididp", String.valueOf(position));
                } else {
                    // 当切换到其他视频时，计算上一个视频的播放时间
                    long durationMillis = System.currentTimeMillis() - videoStartTime;
                    long durationSeconds = durationMillis / 1000; // 将毫秒转换为秒
                    Log.d("id", "Video played for " + durationSeconds + " seconds");

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

                    String title ="null";
                    String address = "null";
                    String date = "null";
                    String price = "null";
                    String uploader = snapshot.child("uploader").getValue(String.class);

                    if (ADUrl == null || Aidprevention == null) {
                        // 如果有任何一个字段为空，则跳过当前影片的处理 不然上傳會出錯
                        continue;
                    }
                    Video ad = new Video(ADUrl,title, address, date, price,Aid,uploader);
                    ADList.add(ad);
                }
                Collections.shuffle(ADList);
                adapter.notifyDataSetChanged();
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
    public void loadVideosFromFirebase() {//製作有所有影片的list
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
                    Video video = new Video(videoUrl,title, address, date, price,id,uploader);
                    videoList.add(video);
                }
                Collections.shuffle(videoList);
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


    public void loadVideosFromFirebaseADVANCE() {//製作只有演算法喜好前幾名的list
        List<String> preferencesList = new ArrayList<>();
        Log.d("經過了re", "經過了");
        preferencesRef = database.getReference("Users").child(username).child("preferences");

        // 獲取 "User/preferences" 節點
        preferencesRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                videoListRE.clear();
                NoTagADList.clear();
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

                                    Video video = new Video(videoUrl, title, address, date, price, id, uploader);
                                    videoListRE.add(video);

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

                                    for (DataSnapshot tagValue : tagSnapshot.getChildren()) {
                                        String videoTag = tagValue.getValue(String.class);
                                        if (preferencesList.contains(videoTag)) {

                                            String ADUrl = videoSnapshot.child("ADUrl").getValue(String.class);
                                            Long Aidprevention = videoSnapshot.child("Aid").getValue(Long.class);
                                            long Aid = (Aidprevention != null) ? Aidprevention : 0; // 如果为空，则设置为默认值 0
                                            String title = "null";
                                            String address = "null";
                                            String date = "null";
                                            String price = "null";
                                            String uploader = videoSnapshot.child("uploader").getValue(String.class);

                                            if (ADUrl == null || Aidprevention == null) {
                                                // 如果有任何一个字段为空，则跳过当前影片的处理 不然上傳會出錯
                                                continue;
                                            }
                                            Video AD = new Video(ADUrl,title, address, date, price,Aid,uploader);
                                            NoTagADList.add(AD);

                                            break;
                                        }
                                    }
                                }
                                // 打亂列表順序
                                Collections.shuffle(NoTagADList);
                                Log.d("NoTagADList長度", String.valueOf(NoTagADList.size()));
                                //Log.d("parameterRecom", String.valueOf(id));
                                // ADMIX();


                                loadVideosMIX();
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
        int maxSize = Math.max(NoTagADList.size(), ADList.size());
        int ADListIndex = 0;
        int NoTagADListIndex = 0;
        int cycle = 0; // 记录当前循环次数

        while (ADListIndex < ADList.size() || NoTagADListIndex < NoTagADList.size()) {
            for (int i = 0; i < parameterADList && ADListIndex < ADList.size(); i++) {
                ADmergedList.add(ADList.get(ADListIndex));
                Log.d("位置id", String.valueOf(ADList.get(ADListIndex).getId()));
                ADListIndex++;
            }

            for (int i = 0; i < parameterNoTagADList && NoTagADListIndex < NoTagADList.size(); i++) {
                ADmergedList.add(NoTagADList.get(NoTagADListIndex));
                Log.d("位置id", String.valueOf(NoTagADList.get(NoTagADListIndex).getId()));
                NoTagADListIndex++;
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
                }else{
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


}
