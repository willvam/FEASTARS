package com.example.feastarfeed;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
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


public class PersonalPage extends Fragment {

    private RecyclerView rvVideoPreview;


    private TextView usernameTextView, bioTextView;
    private DatabaseReference userRef;
    private EditText bioEditText;
    private Button logoutButton,BioButton,BioCompleteButton;
    private  String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        bioTextView = view.findViewById(R.id.bio);
        bioEditText = view.findViewById(R.id.bioEditText);

        rvVideoPreview = view.findViewById(R.id.rvVideoPreview);
        rvVideoPreview.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        usernameTextView = view.findViewById(R.id.username);
        userRef = FirebaseDatabase.getInstance().getReference("Users");


        logoutButton = view.findViewById(R.id.logoutbutton);
        BioButton = view.findViewById(R.id.bioButton);
        BioCompleteButton = view.findViewById(R.id.bioCompleteButton);

        bioEditText.setText(bioTextView.getText().toString()); //


        // 取得 username
        username = SharedPreferencesUtils.getUsername(requireContext());
        usernameTextView.setText(username);
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

        return view;
    }
}
