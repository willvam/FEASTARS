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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class OthersAccountFragment extends Fragment {

    private RecyclerView rvVideoPreview;
    private DatabaseReference personalvideoRef;
    private TextView usernameTextView, bioTextView;
    private DatabaseReference userRef;
    private EditText bioEditText;
    private Button FollowedButton;
    private  String username;
    private ArrayList<String> previewArrayList;
    private static final String TAG = "PersonalPage";
    public static int count = 0;
    int color = 0;
    String uploader;

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

        videoList = new ArrayList<>();
        previewArrayList = new ArrayList<>();
        rvVideoPreview.setLayoutManager(layoutManager);
        othersAccountAdapter = new OthersAccountAdapter(getContext(),previewArrayList);
        rvVideoPreview.setAdapter(othersAccountAdapter);

        // 取得 username
        username = uploader;
        usernameTextView.setText(username);


        userRef = FirebaseDatabase.getInstance().getReference("Users");
        personalvideoRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("ownVideos");

        FollowedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        DatabaseReference bioRef = userRef.child(username).child("bio");
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


        loadPersonalVideos();

        return view;
    }

    public void loadPersonalVideos() {
        personalvideoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                previewArrayList.clear(); // 清空列表
                videoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
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
                                Log.d("AccountFragment", "videoUrl: " + videoUrl);

                                if (videoUrl.equals(videoName)){
                                    Video video = new Video(videoUrl,title, address, date, price, id, uploader);
                                    videoList.add(video);
                                    Log.d("previewArrayList", "previewArrayList: " + previewArrayList);
                                    Log.d("VideoList", "videoList: " + videoList);
                                }
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