package com.example.feastarfeed;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.pm.PackageManager;
import android.location.Location;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.feastarfeed.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity implements TagFragment.OnCountChangeListener, MealSuggestFragment.OnCountChangeListenerPop, HomeFragment.OnUploaderClickListener
{
    ActivityMainBinding binding;

    private FirebaseDatabase database;
    private DatabaseReference userRef;
    public String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");
        String email = SharedPreferencesUtils.getEmail(MainActivity.this);
        String username = SharedPreferencesUtils.getUsername(MainActivity.this);
        if (!email.isEmpty()) {
            Log.d("videoList", username);
        } else {
            Toast.makeText(MainActivity.this, "請先登入", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(getApplicationContext(), login.class);
//            startActivity(intent);
//            finish();
        }

        binding.bottomNavigationView.setSelectedItemId(R.id.home);


        replaceFragment(new HomeFragment());
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int menuItemId = item.getItemId();

            if (menuItemId == R.id.home) {
                if (ThrottleUtils.skip()) {
                    Log.d("忽略成功", "2秒");

                    // 忽略這次點擊事件
                    return true;
                }
                replaceFragment(new HomeFragment());
                return true;
            } else if (menuItemId == R.id.search) {
                if (ThrottleUtils.skip()) {
                    Log.d("忽略成功", "2秒");

                    return true;
                }
                replaceFragment(new SearchFragment());
                return true;
            } else if (menuItemId == R.id.add) {
                if (ThrottleUtils.skip()) {
                    Log.d("忽略成功", "2秒");

                    return true;
                }
                startActivity(new Intent(this, upload.class));
                return true;
            } else if (menuItemId == R.id.account) {
                if (ThrottleUtils.skip()) {
                    Log.d("忽略成功", "2秒");

                    return true;
                }
                replaceFragment(new AccountFragment());
                return true;
            }
            return false;
        });

        FirebaseApp.initializeApp(this);

        TagFragment tagFragment = new TagFragment();
        tagFragment.setOnCountChangeListener(this);

        //accountFragment.setOnCountChangeListener(this);

//        VideoAdapter videoAdapter = new VideoAdapter();
//        videoAdapter.setOnCountChangedotherListener(this);
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 在這裡重新加載HomeFragment
        loadHomeFragment();

        boolean shouldReloadHomeFragment = intent.getBooleanExtra("RELOAD_HOME_FRAGMENT", false);
        if (shouldReloadHomeFragment) {
            // 重新加載HomeFragment
            loadHomeFragment();
        }
    }

    private void loadHomeFragment() {
        // 獲取 HomeFragment 的實例
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("home_fragment");

        if (homeFragment == null) {
            // 如果 HomeFragment 不存在,創建一個新的實例
            homeFragment = new HomeFragment();
        }

        // 開始 HomeFragment 的加載
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, homeFragment, "home_fragment");
        fragmentTransaction.commit();
    }

    @Override
    public void onCountChangedPop(int count, String text) {
        Log.d("MainActivity", "Count changed: " + count);
        SearchFragment searchFragment = new SearchFragment();
        Bundle bundle = new Bundle();
        bundle.putString("text", text); // 将查询文本放入 Bundle 中
        searchFragment.setArguments(bundle); // 将 Bundle 设置为 Fragment 的参数
        replaceFragment(searchFragment); // 切换到 SearchFragment
    }

    @Override
    public void onCountChanged(int count, String tag) {
        Log.d("MainActivity", "Count changed: " + count);
        SearchFragment searchFragment = new SearchFragment();
        Bundle bundle = new Bundle();
        bundle.putString("tag", tag); // 将查询文本放入 Bundle 中
        searchFragment.setArguments(bundle); // 将 Bundle 设置为 Fragment 的参数
        replaceFragment(searchFragment); // 切换到 SearchFragment
    }

    //    @Override
//    public void onCountChangedother(String uploader) {
//        OthersAccountFragment othersAccount = new OthersAccountFragment();
//        Bundle bundle = new Bundle();
//        bundle.putString("uploader", uploader); // 将上传者放入 Bundle 中
//        othersAccount.setArguments(bundle); // 将 Bundle 设置为 Fragment 的参数
//        replaceFragment(othersAccount); // 切换到 OthersAccountFragment
//    }
    @Override
    public void onUploaderClicked(String uploader) {
        String username = SharedPreferencesUtils.getUsername(MainActivity.this);
        Log.d("MainActivity","上傳者= "+uploader +"使用者="+username);


        if (uploader.equals(username)){
            Log.d("MainActivity","是自己的頁面 = "+uploader);
            AccountFragment accountFragment = new AccountFragment();
            Bundle bundle = new Bundle();
            bundle.putString("uploader", uploader);
            accountFragment.setArguments(bundle);
            replaceFragment(accountFragment);

        }

        else{
            Log.d("MainActivity","他人的頁面 = "+uploader);
            OthersAccountFragment othersAccount = new OthersAccountFragment();
            Bundle bundle = new Bundle();
            bundle.putString("uploader", uploader);
            othersAccount.setArguments(bundle);
            replaceFragment(othersAccount);
        }
    }
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // 獲取當前顯示的 Fragment
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);

        // 獲取 BottomNavigationView 實例
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // 如果當前顯示的不是目標 Fragment
        if (!(currentFragment instanceof com.example.feastarfeed.HomeFragment)) {
            // 替換為目標 Fragment
            com.example.feastarfeed.HomeFragment targetFragment = new com.example.feastarfeed.HomeFragment();
            fragmentTransaction.replace(R.id.frame_layout, targetFragment);
            fragmentTransaction.commit();

            // 設置 BottomNavigationView 選中項為主頁
            bottomNavigationView.setSelectedItemId(R.id.home);

        } else {
            // 如果已經是目標 Fragment，執行默認的返回操作
            super.onBackPressed();
        }
    }

}