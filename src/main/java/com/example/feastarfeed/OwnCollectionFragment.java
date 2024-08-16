package com.example.feastarfeed;

import static com.example.feastarfeed.AccountFragment.string;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OwnCollectionFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private String username;
    private ArrayList<String> previewArrayList;
    private DatabaseReference collectionRef;
    private List<String> videoIds;
    public static List<Video> videoList,videoListClicked;
    private RecyclerView rvVideoPreview;

    private LinearLayout containerLayout;
    private Map<String, List<String>> groupedVideos;
    OwnCollectionAdapter ownCollectionAdapter;
    int count =0;

    public OwnCollectionFragment() {
        // Required empty public constructor
    }

    public static OwnCollectionFragment newInstance(String param1, String param2) {
        OwnCollectionFragment fragment = new OwnCollectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_own_collection, container, false);


        ClickedFragment.string = string;

        containerLayout = view.findViewById(R.id.containerLayout);
        rvVideoPreview = view.findViewById(R.id.rvVideoPreview);

        username = SharedPreferencesUtils.getUsername(requireContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);

        videoList = new ArrayList<>();
        previewArrayList = new ArrayList<>();
        rvVideoPreview.setLayoutManager(layoutManager);
        ownCollectionAdapter = new OwnCollectionAdapter(getContext(),previewArrayList);
        rvVideoPreview.setAdapter(ownCollectionAdapter);



        collectionRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("collection");
//        loadVideoIds();

        ownCollectionAdapter.setOnItemClickListener(new OwnCollectionAdapter.OnItemClickListener() {
            @Override
            public void onClick(String string, int position) {
                Video video = videoList.get(position);
                videoListClicked = new ArrayList<>();
                videoListClicked.add(video);

                //Intent intent = new Intent(requireActivity(),PersonalVideoActivity.class);
                //startActivity(intent);

                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in2, R.anim.slide_out2);
                fragmentTransaction.replace(R.id.OwnCollection_layout, new ClickedFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });
        loadCollectionVideos();
        return view;
    }

//    private void loadVideoIds() {
//        collectionRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
//                    String videoId = childSnapshot.getKey();
//                    videoIds.add(videoId);
//                }
//                processVideoIds(videoIds);
//                Log.d("collection", "loadVideoIds end");
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // 處理錯誤
//            }
//        });
//    }
//
//    private void processVideoIds(List<String> videoIds) {
//        groupedVideos = new HashMap<>();
//
//        for (String videoId : videoIds) {
//            collectionRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    if (snapshot.exists()) {
//
//                        String date = snapshot.child("date").getValue(String.class);
//                        if (date != null) {
//                            String yearMonth = date.substring(0, 7); // 提取年份和月份
//                            addVideoToGroup(yearMonth, videoId);
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    // 處理錯誤
//                }
//            });
//        }
//    }
//    private synchronized void addVideoToGroup(String yearMonth, String videoId) {
//        if (groupedVideos.containsKey(yearMonth)) {
//            groupedVideos.get(yearMonth).add(videoId);
//        } else {
//            List<String> videos = new ArrayList<>();
//            videos.add(videoId);
//            groupedVideos.put(yearMonth, videos);
//        }
//
//        // 检查是否所有视频都已处理完毕，如果是，则显示结果
//        if (groupedVideos.size() == videoIds.size()) {
//            displayGroupedVideos();
//        }
//    }
//
//
//
//    private void displayGroupedVideos() {
//        containerLayout.removeAllViews(); // 清空容器
//
//        for (Map.Entry<String, List<String>> entry : groupedVideos.entrySet()) {
//            String yearMonth = entry.getKey();
//            List<String> videos = entry.getValue();
//
//            // 創建 TextView
//            TextView textView = new TextView(requireContext());
//            textView.setText(yearMonth);
//            textView.setTextSize(18);
//            textView.setPadding(16, 16, 16, 16);
//            containerLayout.addView(textView);
//
//            // 創建 RecyclerView
//            RecyclerView recyclerView = new RecyclerView(requireContext());
//            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
//            recyclerView.setLayoutManager(layoutManager);
//            OwnCollectionAdapter adapter = new OwnCollectionAdapter(requireContext(), new ArrayList<>(videos));
//            recyclerView.setAdapter(adapter);
//            containerLayout.addView(recyclerView);
//            Log.d("collection", "displayGroupedVideos end");
//
//        }
//    }

    public void loadCollectionVideos() {
        collectionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                previewArrayList.clear(); // 清空列表
                videoList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String videoPic = snapshot.child("videoPic").getValue(String.class);
                    String videoName = snapshot.child("videoUrl").getValue(String.class);
                    previewArrayList.add(videoPic);
                  //  Log.d("OwnCollectionFragment", "videoName: " + videoName);

                    String title = snapshot.child("title").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String date = snapshot.child("date").getValue(String.class);
                    String price = snapshot.child("price").getValue(String.class);
                    String videoUrl = snapshot.child("videoUrl").getValue(String.class);
                    Long id = snapshot.child("id").getValue(Long.class);
                    String uploader = snapshot.child("Uploader").getValue(String.class);
                    Log.d("AccountFragment", "videoUrl: " + videoUrl);

                    Video video = new Video(videoUrl,title, address, date, price, id, uploader);
                    videoList.add(video);
                }
                ownCollectionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 处理错误
            }
        });
    }

}