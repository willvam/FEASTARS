package com.example.feastarfeed;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagSearchActivity extends AppCompatActivity {

    private EditText queryEditText;
    private ListView queryResultList;
    private ArrayAdapter<String> queryResultAdapter;
    private ListView selectedTagList;
    private ArrayList<String> selectedTags;
    private CustomTagAdapter selectedTagAdapter;

    private DatabaseReference totalFoodTagRef;

    private TextView backButton;
    String savevideotag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_search);

        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.topic));

        totalFoodTagRef = FirebaseDatabase.getInstance().getReference("totalfoodtag");

        queryEditText = findViewById(R.id.queryEditText);
        queryResultList = findViewById(R.id.queryResultList);
        queryResultAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        queryResultList.setAdapter(queryResultAdapter);

        selectedTags = new ArrayList<>();
        selectedTagAdapter = new CustomTagAdapter(this, selectedTags);
        selectedTagList = findViewById(R.id.selectedTagList);
        selectedTagList.setAdapter(selectedTagAdapter);

        backButton = findViewById(R.id.backButton);
        String savedTagsString = SharedPreferencesUtils.getVideotag(TagSearchActivity.this);
        if (!TextUtils.isEmpty(savedTagsString)) {
            String[] tagsArray = savedTagsString.split(",\\s*");
            Collections.addAll(selectedTags, tagsArray);
            selectedTagAdapter.notifyDataSetChanged();
        }
        loadTagList();
        queryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();
                totalFoodTagRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> results = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String value = data.getValue(String.class);
                            if (value != null && value.toLowerCase().contains(query)) {
                                results.add(value);
                            }
                        }
                        queryResultAdapter.clear();
                        queryResultAdapter.addAll(results);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // 處理錯誤
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        queryResultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedTag = queryResultAdapter.getItem(position);
                if (!selectedTags.contains(selectedTag)) {
                    selectedTags.add(selectedTag);
                    selectedTagAdapter.notifyDataSetChanged();
                }
                queryEditText.setText("");
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putStringArrayListExtra("selectedTags", selectedTags);
                setResult(upload.RESULT_OK, resultIntent);

                String tagsString = TextUtils.join(",", selectedTags);
                SharedPreferencesUtils.saveVideotag(TagSearchActivity.this, tagsString);


                finish();
            }
        });
    }

    private class CustomTagAdapter extends ArrayAdapter<String> {
        public CustomTagAdapter(Context context, List<String> tags) {
            super(context, 0, tags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tag, parent, false);
            }

            TextView tagTextView = convertView.findViewById(R.id.tagTextView);
            ImageView deleteButton = convertView.findViewById(R.id.deleteButton);

            String tag = getItem(position);
            tagTextView.setText(tag);

            deleteButton.setOnClickListener(v -> {
                selectedTags.remove(tag);
                notifyDataSetChanged();
            });

            return convertView;
        }
    }
    //載入tag
    private void loadTagList() {
        totalFoodTagRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> results = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String value = data.getValue(String.class);
                    if (value != null) {
                        results.add(value);
                    }
                }
                queryResultAdapter.clear();
                queryResultAdapter.addAll(results);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 處理錯誤
            }
        });
    }
}