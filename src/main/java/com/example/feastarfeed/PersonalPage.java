package com.example.feastarfeed;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PersonalPage extends Fragment {

    private RecyclerView rvVideoPreview;
    public List<Video> videoList;
    private VideoAdapter adapter;

    private DatabaseReference personalvideoRef;
    private TextView usernameTextView, bioTextView;
    private DatabaseReference userRef;
    private EditText bioEditText;
    private Button logoutButton,BioButton,BioCompleteButton;
    private  String username;
    private ArrayList<String> previewArrayList;
    private static final String TAG = "PersonalPage";

    PersonalPageAdapter personalPageAdapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        videoList = new ArrayList<>();
        previewArrayList = new ArrayList<>();
        bioTextView = view.findViewById(R.id.bio);
        bioEditText = view.findViewById(R.id.bioEditText);

        rvVideoPreview = view.findViewById(R.id.rvVideoPreview);
        rvVideoPreview.setLayoutManager(new GridLayoutManager(getContext(),3));
        personalPageAdapter = new PersonalPageAdapter(getContext(),previewArrayList);
        rvVideoPreview.setAdapter(personalPageAdapter);

        usernameTextView = view.findViewById(R.id.username);
        // 取得 username
        username = SharedPreferencesUtils.getUsername(requireContext());
        usernameTextView.setText(username);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        personalvideoRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("ownVideos");


//        logoutButton = view.findViewById(R.id.logoutbutton);
//        BioButton = view.findViewById(R.id.bioButton);
//        BioCompleteButton = view.findViewById(R.id.bioCompleteButton);
        bioEditText.setText(bioTextView.getText().toString()); //



//        FragmentManager fragmentManager = getChildFragmentManager();
//        adapter = new VideoAdapter(videoList, personalvideoRef, fragmentManager, null);



        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesUtils.clearUserData(requireActivity());
                Intent intent = new Intent(requireActivity(), login.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        BioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 點擊 bioButton 時顯示 bioEditText 和 bioCompleteButton，並隱藏 bioButton
                bioEditText.setVisibility(View.VISIBLE);
                bioTextView.setVisibility(view.GONE);
                BioCompleteButton.setVisibility(View.VISIBLE);
                BioButton.setVisibility(View.GONE);
                bioEditText.setText(bioTextView.getText().toString()); // 將原有的 bio 文字設置到 EditText 中
            }
        });

        BioCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newBio = bioEditText.getText().toString();
                if (newBio.length() > 20) {
                    Toast.makeText(requireContext(), "字數不能超過 20 個字", Toast.LENGTH_SHORT).show();
                } else {
                    bioEditText.setVisibility(View.GONE);
                    BioCompleteButton.setVisibility(View.GONE);
                    bioTextView.setVisibility(view.VISIBLE);
                    BioButton.setVisibility(View.VISIBLE);
                    userRef.child(username).child("bio").setValue(newBio);
                    bioTextView.setText(newBio);
                }
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
//        loadVideosFromFirebase();
        Log.d("TTT", "測試測試 2");
        loadPreviews();

        return view;
    }
//    public void loadVideosFromFirebase() {
//        personalvideoRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                videoList.clear();
//
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//
//                    String title = snapshot.child("title").getValue(String.class);
//                    String address = snapshot.child("address").getValue(String.class);
//                    String date = snapshot.child("date").getValue(String.class);
//                    String price = snapshot.child("price").getValue(String.class);
//                    String videoUrl = snapshot.child("videoUrl").getValue(String.class);
//
//                    Long idprevention = snapshot.child("id").getValue(Long.class);
//                    long id = (idprevention != null) ? idprevention : 0; // 如果为空，则设置为默认值 0
//                    String uploader = snapshot.child("uploader").getValue(String.class);
//
//                    if (title == null || address == null || date == null || price == null || videoUrl == null || idprevention == null) {
//                        // 如果有任何一个字段为空，则跳过当前影片的处理 不然上傳會出錯
//                        continue;
//                    }
//                    Video video = new Video(videoUrl,title, address, date, price,id,uploader);
//                    videoList.add(video);
//                }
//                Collections.shuffle(videoList);
////                adapter.notifyDataSetChanged();
//                Log.d("videoList", "經過了");
//                Log.d("videoList", String.valueOf(videoList.size()));
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to read value.", error.toException());
//            }
//
//
//        });
//    }

    public void loadPreviews() {
        personalvideoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                previewArrayList.clear(); // 清空列表
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String videoPic = snapshot.child("videoPic").getValue(String.class);
                    Log.d("BottomView", "Pic path: " + videoPic); // 添加日志輸出
                    previewArrayList.add(videoPic);
                }
                //  Log.d("BottomView", "PicList : " + previewArrayList);
                personalPageAdapter.notifyDataSetChanged();
//                adapter.notifyDataSetChanged();


//                personalPageAdapter.setOnItemClickListener(new RecyclerviewAdapter.OnItemClickListener() {
//                   @Override
//                    public void onClick(String string) {
//                       Intent intent = new Intent(requireActivity(), SearchVideoActivity.class);
//                        startActivity(intent);
//                    }
//                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }

        });
    }
}
