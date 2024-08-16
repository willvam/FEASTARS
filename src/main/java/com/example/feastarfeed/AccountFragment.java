package com.example.feastarfeed;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bluehomestudio.luckywheel.WheelItem;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {

    private RecyclerView rvVideoPreview;
    public static List<Video> videoList, videoListClicked;
    private TextView usernameTextView, bioTextView,savevideoTextview,postTextview;
    private DatabaseReference userRef,personalvideoRef,collectionRef,videoNameRef;
    private EditText bioEditText;
    private Button logoutButton,BioButton,BioCompleteButton,LuckyWheel;
    private  String username;
    private ArrayList<String> previewArrayList;
    private static final String TAG = "PersonalPage";
    public static int count = 0;
    int color = 0;
    int post = 0;
    int tagvideos = 0;
    private List<String> tagvideoIds = new ArrayList<>();

    private long parameterRecom=5;//選擇喜好分數前幾名的加入輪盤
    public static List<WheelItem> wheelItems;
    PersonalPageAdapter personalPageAdapter;
    private CircleImageView profileImageView;
    private Uri selectedImageUri;
    private static final int REQUEST_CODE_PICK_IMAGE = 101;

    public static String string = "account";

    public AccountFragment() {
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
        View view =  inflater.inflate(R.layout.fragment_account, container, false);

        ClickedFragment.string = string;

        count = 0;
        bioTextView = view.findViewById(R.id.bio);
        bioEditText = view.findViewById(R.id.bioEditText);
        savevideoTextview =view.findViewById(R.id.Savedvideo);
        postTextview = view.findViewById(R.id.post_count);

        videoList = new ArrayList<>();

        rvVideoPreview = view.findViewById(R.id.rvVideoPreview);
        usernameTextView = view.findViewById(R.id.username);

        previewArrayList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        rvVideoPreview.setLayoutManager(layoutManager);
        personalPageAdapter = new PersonalPageAdapter(getContext(),previewArrayList);

        rvVideoPreview.setAdapter(personalPageAdapter);
        // 取得 username
        username = SharedPreferencesUtils.getUsername(requireContext());
        usernameTextView.setText(username);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        personalvideoRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("ownVideos");
        collectionRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("collection");
        videoNameRef = FirebaseDatabase.getInstance().getReference("Videos");

        logoutButton = view.findViewById(R.id.logoutbutton);
        BioButton = view.findViewById(R.id.bioButton);
        BioCompleteButton = view.findViewById(R.id.bioCompleteButton);
        bioEditText.setText(bioTextView.getText().toString());

        loadProfileImage();


        profileImageView = view.findViewById(R.id.profile_image);
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });
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
        Log.d("TTT", "測試測試 ");
        Log.d("TTT", "測試測試 2");

        LuckyWheel = view.findViewById(R.id.LuckyWheel);

        LuckyWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in2, R.anim.slide_out2);
                fragmentTransaction.replace(R.id.frame_layout, new MealSuggestFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference preferencesRef;

        //製作只有演算法喜好前幾名的list
        List<String> preferencesList = new ArrayList<>();
        preferencesRef = database.getReference("Users").child(username).child("preferences");
        wheelItems = new ArrayList<>();
        DatabaseReference nameRef = database.getReference("Videos");


        // 獲取 "User/preferences" 節點
        preferencesRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {

                GenericTypeIndicator<Map<String, Long>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Long>>() {};
                Map<String, Long> preferencesMap = snapshot.getValue(genericTypeIndicator);//<String, Long>前者是標籤名稱後者是值

                if (preferencesMap != null) {
                    List<Map.Entry<String, Long>> preferenceEntries = new ArrayList<>(preferencesMap.entrySet());
                    Collections.sort(preferenceEntries, Collections.reverseOrder(Map.Entry.comparingByValue()));//根據偏好值降序排列映射條目列表。

                    for (int i = 0; i < parameterRecom && i < preferenceEntries.size(); i++) {//將前幾名添加到preferencesList
                        preferencesList.add(preferenceEntries.get(i).getKey());
                    }
                    Log.d("parameterRecom", String.valueOf(preferencesList));
                }


                // 獲取 "Videos" 節點
                nameRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot snapshot) {
                        for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
                            DataSnapshot tagSnapshot = videoSnapshot.child("Foodtags");

                            for (DataSnapshot tagValue : tagSnapshot.getChildren()) {
                                String videoTag = tagValue.getValue(String.class);
                                if (preferencesList.contains(videoTag)) {
                                    String title = videoSnapshot.child("title").getValue(String.class);

                                    count++;
                                    if (color == 0){
                                        wheelItems.add(new WheelItem(Color.parseColor("#FF0067"), BitmapFactory.decodeResource(getResources(),R.drawable.marker), title));
                                        color = 1 ;
                                    }
                                    else if (color == 1){
                                        wheelItems.add(new WheelItem(Color.parseColor("#FFA5CA"), BitmapFactory.decodeResource(getResources(),R.drawable.marker),title));
                                        color = 0;
                                    }
                                    break;
                                }
                            }

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 處理錯誤
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // 處理錯誤
            }
        });

        personalPageAdapter.setOnItemClickListener(new PersonalPageAdapter.OnItemClickListener() {
            @Override
            public void onClick(String string, int position) {
                Video video = videoList.get(position);
                videoListClicked = new ArrayList<>();
                videoListClicked.add(video);

                //Intent intent = new Intent(requireActivity(),PersonalVideoActivity.class);
                //startActivity(intent);

                FragmentManager fragmentManager = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in2, R.anim.slide_out2);
                fragmentTransaction.replace(R.id.frame_layout, new ClickedFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });
        savevideoTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in2, R.anim.slide_out2);
                fragmentTransaction.replace(R.id.frame_layout, new OwnCollectionFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });



        DatabaseReference contRef = FirebaseDatabase.getInstance().getReference().child("Users").child(username).child("Cont");
        contRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 清空 videoIds 陣列
                tagvideoIds.clear();

                // 遍歷所有 cont 節點
                for (DataSnapshot contSnapshot : dataSnapshot.getChildren()) {
                    String contKey = contSnapshot.getKey(); // 獲取 cont 節點的 key (cont1, cont2, ...)

                    // 檢查 Tag 是否為 true
                    boolean isTagged = (boolean) contSnapshot.child("Tag").getValue();
                    if (isTagged) {
                        // 如果 Tag 為 true，則記錄下來
                        tagvideos = tagvideos+1;
                        String videoId = "V" + contKey.substring(4); // 提取 cont 編號
                        tagvideoIds.add(videoId); // 將 videoId 添加到陣列中
                    }
                }
                savevideoTextview.setText(tagvideos + "收藏");

                // 在這裡處理 videoIds 陣列
                processVideoIds(tagvideoIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 處理錯誤
            }
        });


        loadPersonalVideos();

        return view;
    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_PICK_IMAGE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);
            uploadImageToFirebase();
        }
    }
    private void uploadImageToFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();


        // 創建一個唯一的文件名
        String fileName = "profile_" + username + ".jpg";
        StorageReference imageRef = storageRef.child("personalimages/" + fileName);

        // 上傳圖片
        UploadTask uploadTask = imageRef.putFile(selectedImageUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // 獲取圖片的下載URL
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageUrl = uri.toString();

                        // 將圖片的下載URL存儲在 Firebase Realtime Database 中
                        userRef.child(username).child("profileImageUrl").setValue(imageUrl);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // 處理錯誤
            }
        });
    }
    private void loadProfileImage() {


        DatabaseReference userimageRef = FirebaseDatabase.getInstance().getReference().child("Users").child(username);
        userimageRef.child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String imageUrl = snapshot.getValue(String.class);
                    Glide.with(getActivity())
                            .load(imageUrl)
                            .into(profileImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 處理錯誤
            }
        });
    }

    private void processVideoIds(List<String> videoIds) {
        // 遍歷 videoIds 陣列
        for (String videoId : videoIds) {
            videoNameRef.child(videoId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // 獲取視頻數據
                    if (dataSnapshot.exists()) {
                        //拿到影片資訊
                        String videoPic = dataSnapshot.child("videoPic").getValue(String.class);
                        String videoUrl = dataSnapshot.child("videoUrl").getValue(String.class);

                        String title = dataSnapshot.child("title").getValue(String.class);
                        String address = dataSnapshot.child("address").getValue(String.class);
                        String date = dataSnapshot.child("date").getValue(String.class);
                        String price = dataSnapshot.child("price").getValue(String.class);
                        Long id = dataSnapshot.child("id").getValue(Long.class);
                        String uploader = dataSnapshot.child("Uploader").getValue(String.class);
                        // 將視頻數據存入 Users/username/collection/videoId
                        DatabaseReference collectionRef = FirebaseDatabase.getInstance().getReference("Users").child(username).child("collection").child(videoId);
                        collectionRef.child("videoPic").setValue(videoPic);
                        collectionRef.child("videoUrl").setValue(videoUrl);

                        collectionRef.child("title").setValue(title);
                        collectionRef.child("address").setValue(address);
                        collectionRef.child("date").setValue(date);
                        collectionRef.child("price").setValue(price);
                        collectionRef.child("id").setValue(id);
                        collectionRef.child("Uploader").setValue(uploader);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // 處理錯誤
                }
            });
        }
    }
    public void loadPersonalVideos() {
        personalvideoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                previewArrayList.clear(); // 清空列表

                videoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    post = post +1;
                    postTextview.setText(post + "貼文");
                    String videoPic = snapshot.child("videoPic").getValue(String.class);
                    String videoName = snapshot.child("videoUrl").getValue(String.class);
                    previewArrayList.add(videoPic);
                    Log.d("AccountFragment", "videoName: " + videoName);

                    videoNameRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                                String title = dataSnapshot1.child("title").getValue(String.class);
                                String address = dataSnapshot1.child("address").getValue(String.class);
                                String date = dataSnapshot1.child("date").getValue(String.class);
                                String price = dataSnapshot1.child("price").getValue(String.class);
                                String videoUrl = dataSnapshot1.child("videoUrl").getValue(String.class);
                                Long id = dataSnapshot1.child("id").getValue(Long.class);
                                String uploader = dataSnapshot1.child("Uploader").getValue(String.class);
                                Log.d("AccountFragment", "videoUrl: " + videoUrl);

                                if (videoUrl.equals(videoName)){
                                    Video video = new Video(videoUrl,title, address, date, price, id, uploader);
                                    videoList.add(video);
                                    Log.d("previewArrayList", "previewArrayList: " + previewArrayList);
                                    Log.d("VideoList", "videoList: " + videoList);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                personalPageAdapter.notifyDataSetChanged();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }

        });
    }
}