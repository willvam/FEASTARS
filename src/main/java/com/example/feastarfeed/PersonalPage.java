package com.example.feastarfeed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class PersonalPage extends AppCompatActivity {

    private RecyclerView rvVideoPreview;

    private List<VideoMetadata> videoList;
    private TextView usernameTextView,bio;
    private ImageButton Back_button;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_account);

        bio = findViewById(R.id.bio);


        rvVideoPreview = findViewById(R.id.rvVideoPreview);
        rvVideoPreview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        videoList = new ArrayList<>();
        //videoPreviewAdapter = new VideoPreviewAdapter(this, videoList);
        //rvVideoPreview.setAdapter(videoPreviewAdapter);
        usernameTextView = findViewById(R.id.username);
        Back_button = findViewById(R.id.back_button);

        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");


        logoutButton = findViewById(R.id.logoutbutton);

        // 取得username
        usernameTextView.setText(SharedPreferencesUtils.getUsername(PersonalPage.this));

        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                SharedPreferencesUtils.clearUserData(PersonalPage.this);
                Intent intent = new Intent(getApplicationContext(),login.class);
                startActivity(intent);
                finish();
            }
        });
        Back_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent MainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(MainActivityIntent);
                finish();
            }
        });
    }

//    private void queryVideosFromDatabase(String username) {
//        userRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    String uid = snapshot.child("uid").getValue(String.class);
//                    if (uid != null) {
//                        queryVideosFromDatabaseByUID(uid);
//                    } else {
//                        Toast.makeText(PersonalPage.this, "錯誤", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(PersonalPage.this, "錯誤", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(PersonalPage.this, "錯誤", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void queryVideosFromDatabaseByUID(String uid) {
//        DatabaseReference videosRef = FirebaseDatabase.getInstance().getReference().child("videos");
//        videosRef.orderByChild("Uploader").equalTo(uid)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        videoList.clear();
//                        for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
//                            String videoUrl = videoSnapshot.child("videoUrl").getValue(String.class);
//                            String title = videoSnapshot.child("title").getValue(String.class);
//                            String thumbnailUrl = videoSnapshot.child("thumbnailUrl").getValue(String.class);
//                            VideoMetadata video = new VideoMetadata(title, videoUrl, thumbnailUrl);
//                            videoList.add(video);
//                        }
//                        //videoPreviewAdapter.notifyDataSetChanged();
//                        Toast.makeText(PersonalPage.this, "成功", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Toast.makeText(PersonalPage.this, "錯誤", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    // VideoPreviewAdapter 和 VideoPreviewViewHolder 類別保持不變

    private static class VideoMetadata {
        String title;
        String videoUrl;
        String thumbnailUrl;

        public VideoMetadata(String title, String videoUrl, String thumbnailUrl) {
            this.title = title;
            this.videoUrl = videoUrl;
            this.thumbnailUrl = thumbnailUrl;
        }
    }
}