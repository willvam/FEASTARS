package com.example.feastarfeed;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class superuser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superuser);

        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.topic));

        ImageView button1 = findViewById(R.id.button1);//返回
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(superuser.this, login.class);
                startActivity(intent);
                finish();
            }
        });

        TextView button2 = findViewById(R.id.button2);//近期熱門tag
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(superuser.this, barchart.class);
                startActivity(intent);
                finish();
            }
        });

        TextView button3 = findViewById(R.id.button3);//近期熱門店家
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(superuser.this, shopbarchart.class);
                startActivity(intent);
                finish();
            }
        });

        TextView button4 = findViewById(R.id.button4);//最受喜愛tag
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(superuser.this, favoritebarchart.class);
                startActivity(intent);
                finish();
            }
        });

        TextView button5 = findViewById(R.id.button5);//廣告瀏覽狀況
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(superuser.this, adchart.class);
                startActivity(intent);
                finish();
            }
        });

//        Button button6 = findViewById(R.id.button6);//調整喜好參數
//        button6.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(superuser.this, shopbarchart.class);
//                startActivity(intent);
//                finish();
//            }
//        });
        TextView uploadad = findViewById(R.id.uploadad);
        uploadad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(superuser.this, UploadAdvertiseActivity.class);
                startActivity(intent);
                finish();
            }
        });

        TextView adjust = findViewById(R.id.adjust);
        adjust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(superuser.this, changeparameter.class);
                startActivity(intent);
                finish();
            }
        });



    }
}