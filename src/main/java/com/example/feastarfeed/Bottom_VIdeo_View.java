package com.example.feastarfeed;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    CharSequence placeName = SearchFragment.placeName;
    CharSequence placeAddress = SearchFragment.placeAddress;

    ArrayList<String> nameArraylist = SearchFragment.nameArraylist;

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
//                    if (videoName.equals(placeName1)){
//                        Log.d("BottomView","placeName:"+ placeName1);
//                        previewArrayList.add(videoPic);
//                        Log.d("BottomView", "PicList : " + previewArrayList);
//                    }
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