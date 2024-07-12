package com.example.feastarfeed;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CommentFragment extends Fragment {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    public ArrayList<Comment> commentArrayList = HomeFragment.commentArrayList;

    public CommentAdapter commentAdapter;

    DatabaseReference cmtDatabase = database.getReference("Comments");

    RecyclerView recyclerView;

    int page = HomeFragment.page;

    public CommentFragment() {
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
        View view = inflater.inflate(R.layout.fragment_comment, container, false);

        recyclerView = view.findViewById(R.id.recyclerView1);

        TextInputLayout contentLayout;
        contentLayout = view.findViewById(R.id.contentLayout);

        TextInputEditText contentET;
        contentET = view.findViewById(R.id.contentET);

        ImageButton newButton = view.findViewById(R.id.newButton);

        DatabaseReference cmtRef = database.getReference("videoCont"+(page+1)).child("comments");

        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = contentET.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                } else {
                    contentLayout.setError(null);
                    String commentString = null;
                    Comment comment = new Comment();
                    comment.setContent(contentET.getText().toString());

                    cmtRef.push().setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            contentET.setText("");
                            Toast.makeText(getActivity(), "Comment uploaded successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Failed to upload comment!", Toast.LENGTH_SHORT).show();
                        }

                    });
                }
            }
        });

        if (!commentArrayList.isEmpty()) {
            // 数据不为空时设置Adapter
            commentAdapter = new CommentAdapter(getContext(), commentArrayList);
            LinearLayoutManager llm =  new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
            recyclerView.setLayoutManager(llm);
            recyclerView.setAdapter(commentAdapter);
        } else {
            // 数据为空时不设置Adapter
            Log.d("TAG", "No comments available");
        }

        return view;
    }

}