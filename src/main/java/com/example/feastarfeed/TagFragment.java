package com.example.feastarfeed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TagFragment extends Fragment {

    TagAdapter tagAdapter;
    ArrayList<Tag> tagArrayList = HomeFragment.tagArrayList ;

    public TagFragment() {
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
        View view = inflater.inflate(R.layout.fragment_tag, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTag);

        tagAdapter = new TagAdapter(getContext(),tagArrayList);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),4));
        recyclerView.setAdapter(tagAdapter);
        return view;
    }


}