package com.example.feastarfeed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OwnVideoFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private String username;
    private ArrayList<String> previewArrayList;
    private DatabaseReference ownvideoRef,userRef,videoNameRef,collectionRef,personalvideoRef;
    private List<String> tagvideoIds = new ArrayList<>();

    private List<String> videoIds;
    public static List<Video> videoList,videoListClicked;
    private RecyclerView rvVideoPreview;
    int tagvideos = 0;
    private LinearLayout containerLayout;
    private Map<String, List<String>> groupedVideos;
    OwnVideoAdapter ownVideoAdapter;

    public static String string = "ownvideo";

    ImageView close;

    int count =0;

    public OwnVideoFragment() {
        // Required empty public constructor
    }

    public static OwnVideoFragment newInstance(String param1, String param2) {
        OwnVideoFragment fragment = new OwnVideoFragment();
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
        View view = inflater.inflate(R.layout.fragment_own_video, container, false);


        ClickedFragment.string = string;
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        rvVideoPreview = view.findViewById(R.id.rvVideoPreview);
        username = SharedPreferencesUtils.getUsername(requireContext());

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3); // 每行3個項目

        videoList = new ArrayList<>();
        previewArrayList = new ArrayList<>();

        rvVideoPreview.setLayoutManager(layoutManager);

        ownVideoAdapter = new OwnVideoAdapter(getContext(),previewArrayList);
        rvVideoPreview.setAdapter(ownVideoAdapter);

        personalvideoRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("ownVideos");


        videoNameRef = FirebaseDatabase.getInstance().getReference("Videos");

        ownvideoRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("ownVideos");

        ownVideoAdapter.setOnItemClickListener(new OwnVideoAdapter.OnItemClickListener() {
            @Override
            public void onClick(String string, int position) {
                Video video = videoList.get(position);
                Log.d("OwnVideoFragment","position="+position);

                videoListClicked = new ArrayList<>();
                videoListClicked.add(video);

                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in2, R.anim.slide_out2);
                fragmentTransaction.replace(R.id.clickedVideoContain, new ClickedFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });

        close = view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in2, R.anim.slide_out2);
                fragmentTransaction.replace(R.id.frame_layout,new AccountFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

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
                    Log.d("OwnVideoFragment", "previewArrayList圖片: " + videoPic);
                    Log.d("OwnVideoFragment", "videoName: " + videoName);

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


                                if (videoUrl.equals(videoName)) {
                                    // 根據 uploader 查詢 profileImageUrl
                                    userRef.child(username).child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String profileImageUrl = snapshot.getValue(String.class);
                                            Video video = new Video(videoUrl, title, address, date, price, id, uploader, profileImageUrl,videoPic);
                                            videoList.add(video);
                                            Log.d("previewArrayList", "previewArrayList: " + previewArrayList);
                                            Log.d("VideoList", "videoList: " + videoList);

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // 處理錯誤
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                ownVideoAdapter.notifyDataSetChanged();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e( "Failed to read value.", error.toException());
            }

        });
    }


}