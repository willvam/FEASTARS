package com.example.feastarfeed;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TagFragment extends Fragment {

    TagAdapter tagAdapter;
    ArrayList<Tag> tagArrayList;

    private OnCountChangeListener onCountChangeListener;

    public static int count;

    public static String text;

    public static int num;

    public TagFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        count = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tag, container, false);

        if (num == 0){
            tagArrayList = HomeFragment.tagArrayList;
        } else if (num == 1){
            tagArrayList = ClickedFragment.tagArrayList;
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTag);
        tagAdapter = new TagAdapter(getContext(),tagArrayList);
        tagAdapter.setOnTagClickListener(this::onTagClick);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
        recyclerView.setAdapter(tagAdapter);

        return view;
    }

    public void onTagClick(String tag){
        count = 1;
        text = tag;
        if (onCountChangeListener != null) {
            onCountChangeListener.onCountChanged(count,tag);
        }
        Log.d("tagFragment","tag = "+tag);
        Log.d("tagFragment","count = "+count);
    }

    public interface OnCountChangeListener {
        void onCountChanged(int count, String tag);
    }

    public void setOnCountChangeListener(OnCountChangeListener listener) {
        this.onCountChangeListener = listener;
    }
}