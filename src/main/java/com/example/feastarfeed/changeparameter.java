package com.example.feastarfeed;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class changeparameter extends AppCompatActivity {

    private static final String PREF_NAME = "parameters";
    private static final String PREF_PARAM1 = "param1";
    private static final String PREF_PARAM2 = "param2";
    private static final String PREF_PARAM3 = "param3";
    private static final String PREF_PARAM4 = "param4";
    private static final String PREF_PARAM5 = "param5";
    private static String lastParameterValue; // 保存最後設定的值

    private long newValue1=4;//小於幾秒(-parameterTime
    private long newValue2=8;//大於幾秒(-parameterTime
    private long newValue3=5;//大於小於後要+-多少分數
    private long newValue4=1;// 最終影片清單:然后放入幾个videoListRE元素
    private long newValue5=1;// 然后放入幾个金主爸爸廣告元素
    private long newValue6=10;// 然后放入幾个金主爸爸廣告元素
    private long newValue7=15;// 然后放入幾个金主爸爸廣告元素
    String paraString1;
    String paraString2;
    String paraString3;
    String paraString4;
    String paraString5;
    String paraString6;
    String paraString7;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_changeparameter);

        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.topic));
////////////////////

        ImageView button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(changeparameter.this, superuser.class);
                startActivity(intent);
                finish();
            }
        });

        TextView button = findViewById(R.id.button);//近期熱門tag
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//小於幾秒(-parameterTime
                EditText para1 = findViewById(R.id.editTextNumber1);
                if (para1 != null && !TextUtils.isEmpty(para1.getText()) && TextUtils.isDigitsOnly(para1.getText())) {
                    paraString1 = para1.getText().toString().trim();
                    newValue1 = Long.parseLong(paraString1);
                }else {
                    newValue1   =  SharedPreferencesUtils.getPARALow(changeparameter.this);

                }
//大於幾秒(-parameterTime
                EditText para2 = findViewById(R.id.editTextNumber2);
                if (para2 != null && !TextUtils.isEmpty(para2.getText()) && TextUtils.isDigitsOnly(para2.getText())) {
                    paraString2 = para2.getText().toString().trim();
                    newValue2 = Long.parseLong(paraString2);
                }else {
                    newValue2   =  SharedPreferencesUtils.getPARAHigh(changeparameter.this);
                }
//大於小於後要+-多少分數
                EditText para3 = findViewById(R.id.editTextNumber3);
                if (para3 != null && !TextUtils.isEmpty(para3.getText()) && TextUtils.isDigitsOnly(para3.getText())) {
                    paraString3 = para3.getText().toString().trim();
                    newValue3 = Long.parseLong(paraString3);
                }else {
                    newValue3   =  SharedPreferencesUtils.getPARATimeScore(changeparameter.this);
                }
// 最終影片清單:然后放入幾个videoListRE元素
                EditText para4 = findViewById(R.id.editTextNumber4);
                if (para4 != null && !TextUtils.isEmpty(para4.getText()) && TextUtils.isDigitsOnly(para4.getText())) {
                    paraString4 = para4.getText().toString().trim();
                    newValue4 = Long.parseLong(paraString4);
                }else {
                    newValue4   =  SharedPreferencesUtils.getPARA_RE(changeparameter.this);
                }
// 然后放入幾个金主爸爸廣告元素
                EditText para5 = findViewById(R.id.editTextNumber5);
                if (para5 != null && !TextUtils.isEmpty(para5.getText()) && TextUtils.isDigitsOnly(para5.getText())) {
                    paraString5 = para5.getText().toString().trim();
                    newValue5 = Long.parseLong(paraString5);
                }else {
                    newValue5   =  SharedPreferencesUtils.getPARA_DAD(changeparameter.this);
                }
//FAV按讚分數
                EditText para6 = findViewById(R.id.editTextNumber6);
                if (para6 != null && !TextUtils.isEmpty(para6.getText()) && TextUtils.isDigitsOnly(para6.getText())) {
                    paraString6 = para6.getText().toString().trim();
                    newValue6 = Long.parseLong(paraString6);
                }else {
                    newValue6   =  SharedPreferencesUtils.getPARA_Like(changeparameter.this);
                }
//按收藏分數
                EditText para7 = findViewById(R.id.editTextNumber7);
                if (para7 != null && !TextUtils.isEmpty(para7.getText()) && TextUtils.isDigitsOnly(para7.getText())) {
                    paraString7 = para7.getText().toString().trim();
                    newValue7 = Long.parseLong(paraString7);
                }else {
                    newValue7   =  SharedPreferencesUtils.getPARA_TAG(changeparameter.this);
                }





                SharedPreferencesUtils.savechangeparameter(changeparameter.this,newValue1, newValue2, newValue3, newValue4, newValue5, newValue6, newValue7 );
            }


        });

        SharedPreferencesUtils.savechangeparameter(changeparameter.this,newValue1, newValue2, newValue3, newValue4, newValue5, newValue6, newValue7 );
    }


}





