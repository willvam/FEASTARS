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

public class OwnCollectionFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private String username;
    private ArrayList<String> previewArrayList;
    private DatabaseReference collectionRef,userRef,videoNameRef;
    private List<String> tagvideoIds = new ArrayList<>();

    private List<String> videoIds;
    public static List<Video> videoList,videoListClicked;
    private RecyclerView rvVideoPreview;
    int tagvideos = 0;
    private LinearLayout containerLayout;
    private Map<String, List<String>> groupedVideos;
    OwnCollectionAdapter ownCollectionAdapter;

    public static String string = "collection";

    ImageView close;

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
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        rvVideoPreview = view.findViewById(R.id.rvVideoPreview);
        username = SharedPreferencesUtils.getUsername(requireContext());

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3); // 每行3個項目

        videoList = new ArrayList<>();
        previewArrayList = new ArrayList<>();
        rvVideoPreview.setLayoutManager(layoutManager);

        ownCollectionAdapter = new OwnCollectionAdapter(getContext(),previewArrayList);
        rvVideoPreview.setAdapter(ownCollectionAdapter);


        videoNameRef = FirebaseDatabase.getInstance().getReference("Videos");

        collectionRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("collection");

        ownCollectionAdapter.setOnItemClickListener(new OwnCollectionAdapter.OnItemClickListener() {
            @Override
            public void onClick(String string, int position) {
                Video video = videoList.get(position);
                Log.d("OwnCollectionFragment","position="+position);

                videoListClicked = new ArrayList<>();
                videoListClicked.add(video);

                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in2, R.anim.slide_out2);
                fragmentTransaction.replace(R.id.clickedVideoContainer, new ClickedFragment());
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

        loadCollectionVideos();
        return view;
    }
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
                                String videoPic = dataSnapshot1.child("videoPic").getValue(String.class);

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
                ownCollectionAdapter.notifyDataSetChanged();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

}