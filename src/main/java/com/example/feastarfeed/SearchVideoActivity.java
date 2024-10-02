package com.example.feastarfeed;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchVideoActivity extends AppCompatActivity {

    private List<Video> videoList;

    private VideoAdapter adapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    DatabaseReference videosRef = database.getReference("Videos");

    String placeTitle = SearchFragment.placeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_video);
        ViewPager2 viewPager2 = findViewById(R.id.viewPager);
        FragmentManager fragmentManager = getSupportFragmentManager();

        videoList = new ArrayList<>();
        adapter = new VideoAdapter(videoList, videosRef, fragmentManager, null, null, null);
        viewPager2.setAdapter(adapter);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 当滑动到下一个视频时执行适当的操作
                // 您可以在这里执行任何您想要的操作，例如更新UI、加载数据等
                Log.d("ViewPager2", "Page selected: " + position);

                // 获取当前视频对象

                if (adapter != null) {
                    Boolean doFav = adapter.getDoFav();
                    Boolean doTag = adapter.getDoTag();
                    Log.d("HomeFragment", "doFav value: " + doFav);
                    Log.d("HomeFragment", "doTag value: " + doTag);
                    if (doFav != null && position > 0) {
                        // 更新数据库中的 doFav 值
                        videosRef.child("V" + position).child("doFav").setValue(doFav);
                    }
                    if (doTag != null && position > 0) {
                        // 更新数据库中的 doFav 值
                        videosRef.child("V" + position).child("doTag").setValue(doTag);
                    }
                }
            }
        });

        loadVideosFromFirebase();
    }

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
                    Long id = snapshot.child("id").getValue(Long.class);
                    String uploader = snapshot.child("Uploader").getValue(String.class);
                    String videoPic = snapshot.child("videoPic").getValue(String.class);
                    DatabaseReference userRef= FirebaseDatabase.getInstance().getReference("Users");

                    userRef.child(uploader).child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String profileImageUrl = snapshot.getValue(String.class);
                            if (title.equals(placeTitle)){
                                Log.d("SearchVideo","placeName:"+ placeTitle);
                                Video video = new Video(videoUrl,title, address, date, price, id,uploader,profileImageUrl,videoPic);
                                videoList.add(video);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // 處理錯誤
                        }
                    });
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }


        });
    }
}