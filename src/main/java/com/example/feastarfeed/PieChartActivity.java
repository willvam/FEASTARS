package com.example.feastarfeed;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PieChartActivity extends AppCompatActivity {

    private PieChart pieChart;
    private String selectedAd;
    private TextView uploaderTextView, dateTextView;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart);

        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.topic));

        pieChart = findViewById(R.id.pieChart);
        uploaderTextView = findViewById(R.id.uploaderTextView);
        dateTextView = findViewById(R.id.dateTextView);

        ImageView button1 = findViewById(R.id.button1);//返回
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PieChartActivity.this, adchart.class);
                startActivity(intent);
                finish();
            }
        });


        // 獲取選中的廣告ID
        selectedAd = getIntent().getStringExtra("selectedAd");

        // 從Firebase獲取該廣告的觀看狀況數據
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference adRef = database.getReference("TotalWatch/Advertise/notag/" + selectedAd);
        DatabaseReference adRefwithtag = database.getReference("TotalWatch/Advertise/withtag/" + selectedAd);

        adRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int watchedOnce = dataSnapshot.child("才看一下").getValue(Integer.class);
                    int watchedFully = dataSnapshot.child("看完").getValue(Integer.class);
                    int watchedPartially = dataSnapshot.child("觀看一段時間").getValue(Integer.class);
                    int totalViews = dataSnapshot.child("總觀看數").getValue(Integer.class);
                    String uploader = dataSnapshot.child("uploader").getValue(String.class);
                    String date = dataSnapshot.child("date").getValue(String.class);

                    // 設置圓餅圖數據
                    List<PieEntry> entries = new ArrayList<>();
                    entries.add(new PieEntry(watchedOnce, "才看一下"));
                    entries.add(new PieEntry(watchedFully, "看完"));
                    entries.add(new PieEntry(watchedPartially, "觀看一段時間"));

                    PieDataSet dataSet = new PieDataSet(entries, "觀看狀況");

                    // 設置每個 PieEntry 的顏色
                    ArrayList<Integer> colors = new ArrayList<>();
                    colors.add(Color.CYAN);
                    colors.add(Color.GRAY);
                    colors.add(Color.MAGENTA);
                    dataSet.setColors(colors);

                    // 設置元素數值的字體大小
                    dataSet.setValueTextSize(20f); // 調整這個值來改變字體大小

                    PieData pieData = new PieData(dataSet);
                    pieChart.setData(pieData);
                    pieChart.setCenterText("總觀看數: " + totalViews); // 設置中心文字
                    pieChart.setCenterTextSize(34);

                    // 設置上傳者和上傳日期
                    uploaderTextView.setText("上傳者： " + uploader);
                    dateTextView.setText("上傳日期： " + date);


                    // 隱藏 Description Label
                    pieChart.getDescription().setEnabled(false);

                    pieChart.invalidate(); // 刷新圓餅圖
                } else {
                    //Toast.makeText(PieChartActivity.this, "無法獲取觀看狀況數據", Toast.LENGTH_SHORT).show();
                    adRefwithtag.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int watchedOnce = dataSnapshot.child("才看一下").getValue(Integer.class);
                                int watchedFully = dataSnapshot.child("看完").getValue(Integer.class);
                                int watchedPartially = dataSnapshot.child("觀看一段時間").getValue(Integer.class);
                                int totalViews = dataSnapshot.child("總觀看數").getValue(Integer.class);
                                String uploader = dataSnapshot.child("uploader").getValue(String.class);
                                String date = dataSnapshot.child("date").getValue(String.class);

                                // 設置圓餅圖數據
                                List<PieEntry> entries = new ArrayList<>();
                                entries.add(new PieEntry(watchedOnce, "才看一下"));
                                entries.add(new PieEntry(watchedFully, "看完"));
                                entries.add(new PieEntry(watchedPartially, "觀看一段時間"));

                                PieDataSet dataSet = new PieDataSet(entries, "觀看狀況");

                                // 設置每個 PieEntry 的顏色
                                ArrayList<Integer> colors = new ArrayList<>();
                                colors.add(Color.CYAN);
                                colors.add(Color.GRAY);
                                colors.add(Color.MAGENTA);
                                dataSet.setColors(colors);

                                // 設置元素數值的字體大小
                                dataSet.setValueTextSize(20f); // 調整這個值來改變字體大小

                                // 啟用百分比顯示
                                //dataSet.setValueFormatter(new PercentFormatter());

                                PieData pieData = new PieData(dataSet);
                                pieChart.setData(pieData);
                                pieChart.setCenterText("總觀看數： " + totalViews); // 設置中心文字
                                pieChart.setCenterTextSize(34);

                                // 設置上傳者和上傳日期
                                uploaderTextView.setText("上傳者: " + uploader);
                                dateTextView.setText("上傳日期: " + date);

                                // 隱藏 Description Label
                                pieChart.getDescription().setEnabled(false);

                                pieChart.invalidate(); // 刷新圓餅圖
                            } else {
                                Toast.makeText(PieChartActivity.this, "無法獲取觀看狀況數據", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // 處理錯誤
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 處理錯誤
            }
        });



    }
}