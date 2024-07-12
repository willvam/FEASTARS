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
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    private FirebaseDatabase database;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");
        String email = SharedPreferencesUtils.getEmail(MainActivity.this);
        if (!email.isEmpty()) {

            Toast.makeText(MainActivity.this, "目前的Email: " + email, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "請先登入", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(getApplicationContext(), login.class);
//            startActivity(intent);
//            finish();
        }


        replaceFragment(new HomeFragment());
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int menuItemId = item.getItemId();

            if (menuItemId == R.id.home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (menuItemId == R.id.search) {
                replaceFragment(new SearchFragment());

                return true;
            } else if (menuItemId == R.id.add) {
                startActivity(new Intent(this, upload.class));
                return true;
            } else if (menuItemId == R.id.account) {
                startActivity(new Intent(this, PersonalPage.class));
                //replaceFragment(new PersonalPage());
                return true;
            }
            return false;
        });

        FirebaseApp.initializeApp(this);

    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}