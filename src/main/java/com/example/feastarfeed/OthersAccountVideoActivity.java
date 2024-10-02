package com.example.feastarfeed;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class OthersAccountVideoActivity extends AppCompatActivity {

    private VideoAdapter adapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference videosRef = database.getReference("Videos");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_video);
        ViewPager2 viewPager2 = findViewById(R.id.viewPager);
        FragmentManager fragmentManager = getSupportFragmentManager();

        // 从OthersAccountFragment中获取视频列表
        List<Video> videoList = OthersAccountFragment.videoList;

        Log.d("OthersAccount", "videoList: " + videoList);
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
    }
}
