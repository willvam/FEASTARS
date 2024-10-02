package com.example.feastarfeed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class login extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    TextView buttonLogin;
    TextView textView;

    FirebaseDatabase database;
    DatabaseReference userRef;

    ConstraintLayout constraintLayout;

    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.topic));

        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.button_login);
        textView = findViewById(R.id.registerNow);

        constraintLayout = findViewById(R.id.con);
        linearLayout = findViewById(R.id.linear);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(login.this, "請輸入電子信箱", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(login.this, "請輸入密碼", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 檢查是否為 superuser
                if (email.equals("superuser") && password.equals("superuser")) {
                    // 跳轉到 barchart Activity
                    Intent intent = new Intent(login.this, superuser.class);
                    startActivity(intent);
                    finish(); // 可選擇是否結束當前 Activity
                    return;
                }

                // 從 Realtime Database 中查找匹配的使用者資料
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean userFound = false;
                        String username = null;
                        String username2 = null;
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            HashMap<String, Object> user = (HashMap<String, Object>) userSnapshot.getValue();
                            String dbEmail = (String) user.get("email");
                            String dbPassword = (String) user.get("password");
                            username = (String) user.get("uid");//亂碼
                            username2 = (String) user.get("username");//使用者名稱

                            if (dbEmail != null && dbPassword != null && dbEmail.equals(email) && dbPassword.equals(password)) {
                                userFound = true;
                                break;
                            }
                        }

                        if (userFound) {
                            Toast.makeText(getApplicationContext(), "登入成功", Toast.LENGTH_SHORT).show();

                            // 傳遞 currentUserUsername 給 PersonalPage
                            AccountFragment accountFragment = new AccountFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("currentUserUsername",username);
                            bundle.putString("currentUserUsername2",username2);
                            accountFragment.setArguments(bundle);

                            // 傳遞 currentUserUsername 給 upload
                            Intent uploadIntent = new Intent(getApplicationContext(), upload.class);
                            uploadIntent.putExtra("currentUserUsername", username);
                            uploadIntent.putExtra("currentUserUsername2", username2);
                            startActivity(uploadIntent);

                            // 傳遞 currentUserEmail 給 MainActivity
                            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                            mainActivityIntent.putExtra("currentUserEmail", email);
                            startActivity(mainActivityIntent);

                            SharedPreferencesUtils.saveUserData(login.this, email, password, username2);

                            finish();
                        } else {
                            Toast.makeText(login.this, "電子信箱或密碼錯誤", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(login.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        autoLogin();
    }

    private void autoLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("email", "");
        String savedPassword = sharedPreferences.getString("password", "");

        if (TextUtils.isEmpty(savedEmail) || TextUtils.isEmpty(savedPassword)) {
            // 如果本地沒有保存用戶資料,則直接返回
            Log.d("login-autoLogin","autofail");

            return;
        }

        if (!TextUtils.isEmpty(savedEmail) && !TextUtils.isEmpty(savedPassword)) {
            // 如果存在保存的 email 和 password，則進行登錄驗證
            linearLayout.setBackgroundResource(R.drawable.autologin_bkg);
            constraintLayout.setVisibility(View.GONE);
            editTextEmail.setVisibility(View.GONE);
            editTextPassword.setVisibility(View.GONE);
            buttonLogin.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            Log.d("login-autoLogin","autoaccess");

            performAutoLogin(savedEmail, savedPassword);
        }
    }
    private void performAutoLogin(String email, String password) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userFound = false;
                String username = null;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    HashMap<String, Object> user = (HashMap<String, Object>) userSnapshot.getValue();
                    String dbEmail = (String) user.get("email");
                    String dbPassword = (String) user.get("password");
                    username = userSnapshot.getKey(); // 获取用户名作为键值

                    if (dbEmail != null && dbPassword != null && dbEmail.equals(email) && dbPassword.equals(password)) {
                        userFound = true;
                        break;
                    }
                }

                if (userFound) {
                    // 如果找到匹配的用户,则自动登录
                    Log.d("login-performAutoLogin","autoaccess");
                    // 传递 currentUserUsername 给 PersonalPage

                    AccountFragment accountFragment = new AccountFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("currentUserUsername",username);
                    accountFragment.setArguments(bundle);

                    // 傳遞 currentUserEmail 給 MainActivity
                    Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                    mainActivityIntent.putExtra("currentUserEmail", email);
                    startActivity(mainActivityIntent);

                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(login.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

