package com.example.feastarfeed;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Bottom_VIdeo_View extends Fragment {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference("Videos");

    ArrayList<String> previewArrayList;

    RecyclerviewAdapter adapter;

    String placeName = SearchFragment.placeName;
    String placeAddress = SearchFragment.placeAddress;

    public Bottom_VIdeo_View() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bottom_video_view, container, false);
        TextView name = view.findViewById(R.id.text);
        TextView address = view.findViewById(R.id.text1);

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建 Uri 对象，传递地址信息
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(placeName));
                // 创建 Intent 对象，设置动作为 VIEW，设置数据为 Uri 地址
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // 设置 Intent 的包名为 Google 地图应用程序的包名
                mapIntent.setPackage("com.google.android.apps.maps");
                // 检查设备上是否安装了 Google 地图应用程序
                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // 如果安装了 Google 地图应用程序，则启动该应用程序
                    startActivity(mapIntent);
                } else {
                    // 如果设备上没有安装 Google 地图应用程序，则显示提示信息
                    Toast.makeText(getContext(), "未安装 Google 地图应用程序", Toast.LENGTH_SHORT).show();
                }
            }
        });

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建 Uri 对象，传递地址信息
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(placeName));
                // 创建 Intent 对象，设置动作为 VIEW，设置数据为 Uri 地址
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // 设置 Intent 的包名为 Google 地图应用程序的包名
                mapIntent.setPackage("com.google.android.apps.maps");
                // 检查设备上是否安装了 Google 地图应用程序
                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // 如果安装了 Google 地图应用程序，则启动该应用程序
                    startActivity(mapIntent);
                } else {
                    // 如果设备上没有安装 Google 地图应用程序，则显示提示信息
                    Toast.makeText(getContext(), "未安装 Google 地图应用程序", Toast.LENGTH_SHORT).show();
                }
            }
        });

        name.setText(placeName);
        address.setText(placeAddress);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        previewArrayList = new ArrayList<>();
        loadPreviews();
        adapter = new RecyclerviewAdapter(getContext(),previewArrayList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void loadPreviews() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String videoPic = snapshot.child("videoPic").getValue(String.class);
                    Log.d("BottomView", "Pic path: " + videoPic); // 添加日志输出
                    String videoName = snapshot.child("title").getValue(String.class);
                    Log.d("BottomView", "VideoName: " + videoName);
                    if (videoName.equals(placeName)){
                        Log.d("BottomView","placeName:"+ placeName);
                        previewArrayList.add(videoPic);
                        Log.d("BottomView", "PicList : " + previewArrayList);
                    }
                }
                adapter.notifyDataSetChanged();


                adapter.setOnItemClickListener(new RecyclerviewAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(String string) {
                        Intent intent = new Intent(requireActivity(), SearchVideoActivity.class);
                        startActivity(intent);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }


        });
    }

}