package com.example.feastarfeed;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bluehomestudio.luckywheel.WheelItem;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class OthersAccountFragment extends Fragment {

    private RecyclerView rvVideoPreview;
    private DatabaseReference personalvideoRef,userRef;
    private TextView usernameTextView, bioTextView,FollowedButton,savevideoTextview,postTextview;
    private EditText bioEditText;
    private List<String> tagvideoIds = new ArrayList<>();

    private  String username;
    private ArrayList<String> previewArrayList;
    private static final String TAG = "PersonalPage";
    public static int count = 0;
    int color = 0;
    int post = 0;
    int tagvideos = 0;
    String uploader;
    CircleImageView profileImage;

    private long parameterRecom=4;//選擇喜好分數前幾名的加入輪盤


    OthersAccountAdapter othersAccountAdapter;

    DatabaseReference videoNameRef = FirebaseDatabase.getInstance().getReference("Videos");

    public static String string = "others";

    public static List<Video> videoList, videoListClicked;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_other_account, container, false);

        ClickedFragment.string = string;

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        Bundle args = getArguments();
        if (args != null) {
            uploader = args.getString("uploader", "");
            // 在这里使用获取到的 uploader 数据
        }

        count = 0;
        bioTextView = view.findViewById(R.id.bio);
        bioEditText = view.findViewById(R.id.bioEditText);
        rvVideoPreview = view.findViewById(R.id.rvVideoPreview);
        usernameTextView = view.findViewById(R.id.username);
        FollowedButton = view.findViewById(R.id.followed);
        savevideoTextview =view.findViewById(R.id.Savedvideo);
        postTextview = view.findViewById(R.id.post_count);
        videoList = new ArrayList<>();
        previewArrayList = new ArrayList<>();
        rvVideoPreview.setLayoutManager(layoutManager);
        othersAccountAdapter = new OthersAccountAdapter(getContext(),previewArrayList);
        rvVideoPreview.setAdapter(othersAccountAdapter);
        profileImage = view.findViewById(R.id.profile_image);
        //username是目前使用者
        username = SharedPreferencesUtils.getUsername(requireContext());

        //這是當前葉面的uplaoder的username
        usernameTextView.setText(uploader);


        userRef = FirebaseDatabase.getInstance().getReference("Users");

        personalvideoRef = FirebaseDatabase.getInstance().getReference("Users").child(uploader).child("ownVideos");
        DatabaseReference UserFollowedRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("followed");
        UserFollowedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFollowed = false;
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    if (childSnapshot.getValue().equals(uploader)) {
                        isFollowed = true;
                        break;
                    }
                }
                FollowedButton.setText(isFollowed ? "追蹤中" : "追蹤");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 處理錯誤
            }
        });
        FollowedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String currentText = FollowedButton.getText().toString();
                if (currentText.equals("追蹤")) {
                    // 新增到資料庫
                    DatabaseReference currentUserFollowedRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("followed");
                    currentUserFollowedRef.push().setValue(uploader);

                    DatabaseReference uploaderFollowersRef = FirebaseDatabase.getInstance().getReference("Users").child(uploader).child("followers");
                    uploaderFollowersRef.push().setValue(username);

                    FollowedButton.setText("追蹤中");
                } else {
                    // 從資料庫移除
                    DatabaseReference currentUserFollowedRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("followed");
                    currentUserFollowedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                if (childSnapshot.getValue().equals(uploader)) {
                                    childSnapshot.getRef().removeValue();
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // 處理錯誤
                        }
                    });

                    DatabaseReference uploaderFollowersRef = FirebaseDatabase.getInstance().getReference("Users").child(uploader).child("followers");
                    uploaderFollowersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                if (childSnapshot.getValue().equals(username)) {
                                    childSnapshot.getRef().removeValue();
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // 處理錯誤
                        }
                    });

                    FollowedButton.setText("追蹤");
                }
            }
        });

        DatabaseReference bioRef = userRef.child(uploader).child("bio");
        bioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String bio = snapshot.getValue(String.class);
                    bioTextView.setText(bio);
                    bioEditText.setText(bio);
                } else {
                    // 如果 bio 数据不存在, 则创建一个新的数据并填入默认值
                    String defaultBio = "個人檔案";
                    bioRef.setValue(defaultBio);
                    bioTextView.setText(defaultBio);
                    bioEditText.setText(defaultBio);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 处理读取失败的情况
            }
        });
        Log.d("TTT", "測試測試 ");
        Log.d("TTT", "測試測試 2");

        othersAccountAdapter.setOnItemClickListener(new OthersAccountAdapter.OnItemClickListener() {
            @Override
            public void onClick(String string, int position) {
                Video video = videoList.get(position);
                videoListClicked = new ArrayList<>();
                videoListClicked.add(video);

                //Intent intent = new Intent(requireActivity(),PersonalVideoActivity.class);
                //startActivity(intent);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in2, R.anim.slide_out2);
                fragmentTransaction.replace(R.id.frame_layout, new ClickedFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }

        });
        DatabaseReference contRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uploader).child("Cont");
        contRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 清空 videoIds 陣列
                tagvideoIds.clear();

                // 遍歷所有 cont 節點
                for (DataSnapshot contSnapshot : dataSnapshot.getChildren()) {
                    String contKey = contSnapshot.getKey(); // 獲取 cont 節點的 key (cont1, cont2, ...)

                    // 檢查 Tag 是否為 true
                    boolean isTagged = (boolean) contSnapshot.child("Tag").getValue();
                    if (isTagged) {
                        // 如果 Tag 為 true，則記錄下來
                        tagvideos = tagvideos+1;
                        String videoId = "V" + contKey.substring(4); // 提取 cont 編號
                        tagvideoIds.add(videoId); // 將 videoId 添加到陣列中
                    }
                }
                String tagvideoString = Integer.toString(tagvideos);
                savevideoTextview.setText(tagvideoString);

                // 在這裡處理 videoIds 陣列
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 處理錯誤
            }
        });




//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference preferencesRef;
//
//        //製作只有演算法喜好前幾名的list
//        List<String> preferencesList = new ArrayList<>();
//        preferencesRef = database.getReference("Users").child(username).child("preferences");
//        wheelItems = new ArrayList<>();
//        DatabaseReference nameRef = database.getReference("Videos");
//
//
//        // 獲取 "User/preferences" 節點
//        preferencesRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//            @Override
//            public void onSuccess(DataSnapshot snapshot) {
//
//                GenericTypeIndicator<Map<String, Long>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Long>>() {};
//                Map<String, Long> preferencesMap = snapshot.getValue(genericTypeIndicator);//<String, Long>前者是標籤名稱後者是值
//
//                if (preferencesMap != null) {
//                    List<Map.Entry<String, Long>> preferenceEntries = new ArrayList<>(preferencesMap.entrySet());
//                    Collections.sort(preferenceEntries, Collections.reverseOrder(Map.Entry.comparingByValue()));//根據偏好值降序排列映射條目列表。
//
//                    for (int i = 0; i < parameterRecom && i < preferenceEntries.size(); i++) {//將前幾名添加到preferencesList
//                        preferencesList.add(preferenceEntries.get(i).getKey());
//                    }
//                    Log.d("parameterRecom", String.valueOf(preferencesList));
//                }
//
//
//                // 獲取 "Videos" 節點
//                nameRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//                    @Override
//                    public void onSuccess(DataSnapshot snapshot) {
//                        for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
//                            DataSnapshot tagSnapshot = videoSnapshot.child("Foodtags");
//
//                            for (DataSnapshot tagValue : tagSnapshot.getChildren()) {
//                                String videoTag = tagValue.getValue(String.class);
//                                if (preferencesList.contains(videoTag)) {
//                                    String title = videoSnapshot.child("title").getValue(String.class);
//
//                                    count++;
//                                    if (color == 0){
//                                        wheelItems.add(new WheelItem(Color.parseColor("#FF0067"), BitmapFactory.decodeResource(getResources(),R.drawable.marker), title));
//                                        color = 1 ;
//                                    }
//                                    else if (color == 1){
//                                        wheelItems.add(new WheelItem(Color.parseColor("#FFA5CA"), BitmapFactory.decodeResource(getResources(),R.drawable.marker),title));
//                                        color = 0;
//                                    }
//                                    break;
//                                }
//                            }
//
//                        }
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // 處理錯誤
//                    }
//                });
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                // 處理錯誤
//            }
//        });

        loadProfileImage();

        loadPersonalVideos();

        return view;
    }
    private void getNextKey(final DatabaseReference ref, final OnKeyObtainedListener listener) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> keys = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    keys.add(child.getKey());
                }
                Collections.sort(keys);
                int nextIndex = 1;
                for (String key : keys) {
                    if (key.equals("followed" + nextIndex) || key.equals("follower" + nextIndex)) {
                        nextIndex++;
                    } else {
                        break;
                    }
                }
                String nextKey = nextIndex == 1 ? "followed1" : "followed" + nextIndex;
                listener.onKeyObtained(nextKey);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 處理錯誤
            }
        });
    }
    private void loadProfileImage() {


        DatabaseReference userimageRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uploader);
        userimageRef.child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String imageUrl = snapshot.getValue(String.class);
                    Glide.with(getActivity())
                            .load(imageUrl)
                            .into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 處理錯誤
            }
        });
    }

    interface OnKeyObtainedListener {
        void onKeyObtained(String key);
    }
    public void loadPersonalVideos() {
        personalvideoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                previewArrayList.clear(); // 清空列表
                videoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    post = post +1;
                    String postString = Integer.toString(post);
                    postTextview.setText(postString);

                    String videoPic = snapshot.child("videoPic").getValue(String.class);
                    String videoName = snapshot.child("videoUrl").getValue(String.class);
                    previewArrayList.add(videoPic);
                    Log.d("AccountFragment", "videoName: " + videoName);

                    videoNameRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                                String title = dataSnapshot1.child("title").getValue(String.class);
                                String address = dataSnapshot1.child("address").getValue(String.class);
                                String date = dataSnapshot1.child("date").getValue(String.class);
                                String price = dataSnapshot1.child("price").getValue(String.class);
                                String videoUrl = dataSnapshot1.child("videoUrl").getValue(String.class);
                                Long id = dataSnapshot1.child("id").getValue(Long.class);
                                String uploader = dataSnapshot1.child("Uploader").getValue(String.class);
                                String videoPic = snapshot.child("videoPic").getValue(String.class);
                                Log.d("AccountFragment", "videoUrl: " + videoUrl);
                                userRef.child(uploader).child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String profileImageUrl = snapshot.getValue(String.class);
                                        if (videoUrl.equals(videoName)){
                                            Video video = new Video(videoUrl,title, address, date, price, id, uploader,profileImageUrl,videoPic);
                                            videoList.add(video);
                                            Log.d("previewArrayList", "previewArrayList: " + previewArrayList);
                                            Log.d("VideoList", "videoList: " + videoList);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        // 處理錯誤
                                    }
                                });

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                othersAccountAdapter.notifyDataSetChanged();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }

        });
    }

}