package com.example.feastarfeed;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.UUID;

public class Register extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword, editTextUsername;
    ProgressBar progressbar;
    TextView textView,buttonReg;

    FirebaseDatabase database;
    DatabaseReference userRef,videoRef,totalFoodTagRef,preferencesRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.topic));

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference defaultPictureRef = storage.getReference().child("defaultpicture/S__65437759.jpg");
        database = FirebaseDatabase.getInstance();

        userRef = database.getReference("Users");
        videoRef = database.getReference("Videos");
        totalFoodTagRef = database.getReference("totalfoodtag");
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextUsername = findViewById(R.id.username);
        buttonReg = findViewById(R.id.button_register);
        textView = findViewById(R.id.loginNow);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
                finish();// 切換到登入頁面
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password, username;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                username = String.valueOf(editTextUsername.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(Register.this, "Enter username", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 生成隨機的 UID
                String uid = UUID.randomUUID().toString();

                // 獲取預設圖片的下載 URL
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("defaultpicture/S__65437759.jpg");
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String profileImageUrl = uri.toString();

                        // 將使用者資料存入 Realtime Database
                        HashMap<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        user.put("password", password);
                        user.put("uid", uid); // 將 UID 加入使用者資料
                        user.put("username", username);
                        user.put("profileImageUrl", profileImageUrl);
                        userRef.child(username).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
//                                progressbar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    videoRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            // 獲取 Video 節點下的子節點數量
                                            int videoCount = (int) snapshot.getChildrenCount();

                                            // 獲取 Users-username-Cont 節點的引用
                                            DatabaseReference contRef = database.getReference("Users").child(username).child("Cont");

                                            // 生成對應數量的 cont1、cont2 節點
                                            for (int i = 15; i <= videoCount+14; i++) {
                                                DatabaseReference contChild = contRef.child("cont" + i);
                                                contChild.child("Tag").setValue(false);
                                                contChild.child("Fav").setValue(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // 錯誤處理
                                        }
                                    });
                                    totalFoodTagRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            // 清空之前的 valueIncrease 節點
                                            preferencesRef = database.getReference("Users").child(username).child("preferences");


                                            // 遍歷 totalfoodtag 節點下的子節點
                                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                String tagValue = childSnapshot.getValue(String.class);
                                                preferencesRef.child(tagValue).setValue(0);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // 錯誤處理
                                        }
                                    });

                                    Toast.makeText(Register.this, "註冊成功", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), login.class);
                                    startActivity(intent);
                                    finish();
                                    // 註冊成功後的操作，如切換頁面
                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(Register.this, "Registration failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        // 存入資料
                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // 處理獲取下載 URL 失敗的情況
                        // ...
                    }
                });
            }
        });
    }
}