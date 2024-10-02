package com.example.feastarfeed;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class shopbarchart extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinnerMonth;
    private BarChart barChart1;
    private BarChart barChart2;
    private BarChart barChart3;
    private BarChart barChart4;
    private Integer counterA = 0;

    private DatabaseReference database;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopbarchart);

        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.topic));

        spinnerMonth = findViewById(R.id.spinner_month);
        //spinnerMonth.setOnItemSelectedListener(this);

        barChart1 = findViewById(R.id.chart1);
        barChart2 = findViewById(R.id.chart2);
        barChart3 = findViewById(R.id.chart3);
        barChart4 = findViewById(R.id.chart4);

        barChart1.getAxisRight().setDrawLabels(false);
        barChart2.getAxisRight().setDrawLabels(false);
        barChart3.getAxisRight().setDrawLabels(false);

        database = FirebaseDatabase.getInstance().getReference();

        // 初始化時加載當前月份的數據
        loadMonthData(getCurrentMonth());

        // 設置預設選中的月份
        int currentMonthIndex = Arrays.asList(getResources().getStringArray(R.array.months_array)).indexOf(getCurrentMonth());
        spinnerMonth.setSelection(currentMonthIndex);
        spinnerMonth.setOnItemSelectedListener(this);

        ImageView button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(shopbarchart.this, superuser.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void loadMonthData(String month) {
        DatabaseReference shoprankRef = database.child("shoprank");
        DatabaseReference shopViewRef = database.child("shopView");
        DatabaseReference shopFavRef = database.child("shopFav");

        DatabaseReference monthRef = shoprankRef.child(month);
        DatabaseReference monthViewRef = shopViewRef.child(month);
        DatabaseReference monthFavRef = shopFavRef.child(month);

        // 存儲各表的加權數據
        Map<String, Integer> tagWeightedData = new HashMap<>();
        Map<String, Integer> favWeightedData = new HashMap<>();
        Map<String, Integer> viewWeightedData = new HashMap<>();


        //shop tag數排名表
        monthRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    // 將資料轉換為 Map
                    Map<String, Integer> data = new HashMap<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        data.put(child.getKey(), child.getValue(Integer.class));
                    }

                    // 按照數值大小降序排列
                    List<Map.Entry<String, Integer>> sortedData = new ArrayList<>(data.entrySet());
                    sortedData.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

                    // 取前N個最大的數據項目
                    List<BarEntry> entries = new ArrayList<>();
                    List<String> xValues = new ArrayList<>();
                    int index = 0;
                    for (Map.Entry<String, Integer> entry : sortedData) {
                        if (index < 3) {  //要列出幾個改這邊
                            entries.add(new BarEntry(index, entry.getValue().floatValue()));
                            xValues.add(entry.getKey());
                            index++;
                        } else {
                            break;
                        }
                    }

                    updateBarChart(barChart1, entries, xValues);

                    //加權數據
                    for (DataSnapshot child : snapshot.getChildren()) {
                        tagWeightedData.put(child.getKey(), child.getValue(Integer.class)*4);
                        Log.d("tagWeightedData", child.getKey()+","+child.getValue(Integer.class)*4);
                    }
                    //做愛心數表
                    loadFavData();
                }else {
                    // 資料不存在,清空圖表或顯示提示訊息
                    barChart1.clear();
                    barChart1.setNoDataText("No data available for this month.");
                }

            }

            public void loadFavData() {
                //shop 愛心數排名
                monthFavRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            // 將資料轉換為 Map
                            Map<String, Integer> data = new HashMap<>();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                data.put(child.getKey(), child.getValue(Integer.class));
                            }

                            // 按照數值大小降序排列
                            List<Map.Entry<String, Integer>> sortedData = new ArrayList<>(data.entrySet());
                            sortedData.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

                            // 取前N個最大的數據項目
                            List<BarEntry> entries = new ArrayList<>();
                            List<String> xValues = new ArrayList<>();
                            int index = 0;
                            for (Map.Entry<String, Integer> entry : sortedData) {
                                if (index < 3) {  //要列出幾個改這邊
                                    entries.add(new BarEntry(index, entry.getValue().floatValue()));
                                    xValues.add(entry.getKey());
                                    index++;
                                } else {
                                    break;
                                }
                            }

                            updateBarChart(barChart2, entries, xValues);

                            //加權
                            for (DataSnapshot child : snapshot.getChildren()) {
                                favWeightedData.put(child.getKey(), child.getValue(Integer.class) * 2);
                                Log.d("favWeightedData", child.getKey() + "," + child.getValue(Integer.class) * 2);
                            }

                        } else {
                            // 資料不存在,清空圖表或顯示提示訊息
                            barChart2.clear();
                            barChart2.setNoDataText("No data available for this month.");
                        }

                        loadViewData();
                    }

                    //
                    private void loadViewData() {
                        //shop 觀看數排名
                        monthViewRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.hasChildren()) {
                                    // 將資料轉換為 Map
                                    Map<String, Integer> data = new HashMap<>();
                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        data.put(child.getKey(), child.getValue(Integer.class));
                                    }

                                    // 按照數值大小降序排列
                                    List<Map.Entry<String, Integer>> sortedData = new ArrayList<>(data.entrySet());
                                    sortedData.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

                                    // 取前N個最大的數據項目
                                    List<BarEntry> entries = new ArrayList<>();
                                    List<String> xValues = new ArrayList<>();
                                    int index = 0;
                                    for (Map.Entry<String, Integer> entry : sortedData) {
                                        if (index < 3) {  //要列出幾個改這邊
                                            entries.add(new BarEntry(index, entry.getValue().floatValue()));
                                            xValues.add(entry.getKey());
                                            index++;
                                        } else {
                                            break;
                                        }
                                    }

                                    updateBarChart(barChart3, entries, xValues);

                                    //加權數據
                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        viewWeightedData.put(child.getKey(), child.getValue(Integer.class) * 1);
                                        Log.d("viewWeightedData", child.getKey() + "," + child.getValue(Integer.class) * 1);
                                    }

                                } else {
                                    // 資料不存在,清空圖表或顯示提示訊息
                                    barChart3.clear();
                                    barChart3.setNoDataText("No data available for this month.");
                                }

                                mixChart(tagWeightedData, favWeightedData, viewWeightedData);
                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // 處理錯誤
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // 處理錯誤
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 處理錯誤
            }
        });

    }

    private String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int monthIndex = calendar.get(Calendar.MONTH) + 1; // 月份從 0 開始,所以加 1
        String monthName = String.format("%02d", monthIndex); // 使用 %02d 確保月份是兩位數字
        return year + "/" + monthName;
    }

    private String getMonthName(int monthIndex) {
        String[] months = getResources().getStringArray(R.array.months_array);
        return months[monthIndex];
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedMonth = parent.getItemAtPosition(position).toString();
        loadMonthData(selectedMonth);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // 不做任何操作
    }

    private void updateBarChart(BarChart barChart, List<BarEntry> entries, List<String> xValues) {
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setAxisMaximum(0f);
        yAxis.setAxisMaximum(Collections.max(entries, Comparator.comparingDouble(BarEntry::getY)).getY() + 10);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10);

        BarDataSet dataSet = new BarDataSet(entries, "Subjects");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        barChart.getDescription().setEnabled(false);
        barChart.invalidate();

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xValues));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
    }

    private void mixChart(Map<String, Integer> tagWeightedData, Map<String, Integer> favWeightedData, Map<String, Integer> viewWeightedData) {

        Log.d("MixChartData", "tagWeightedData: " + tagWeightedData);
        Log.d("MixChartData", "favWeightedData: " + favWeightedData);
        Log.d("MixChartData", "viewWeightedData: " + viewWeightedData);


        // 合併加權數據
        Map<String, Integer> weightedData = new HashMap<>();
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(tagWeightedData.keySet());
        allKeys.addAll(favWeightedData.keySet());
        allKeys.addAll(viewWeightedData.keySet());


        // 遍歷所有鍵，將對應的值相加並放入 weightedData
        for (String key : allKeys) {
            int tagValue = tagWeightedData.getOrDefault(key, 0);
            int favValue = favWeightedData.getOrDefault(key, 0);
            int viewValue = viewWeightedData.getOrDefault(key, 0);
            int weightedValue = tagValue + favValue + viewValue;
            weightedData.put(key, weightedValue);
        }

        Log.d("MixChartData", "Final weightedData: " + weightedData);

        // 按照加權數值大小降序排列
        List<Map.Entry<String, Integer>> sortedData = new ArrayList<>(weightedData.entrySet());
        sortedData.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        // 取前 N 個最大的數據項目
        List<BarEntry> entries = new ArrayList<>();
        List<String> xValues = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Integer> entry : sortedData) {
            if (index < 3) { // 要列出幾個改這邊
                entries.add(new BarEntry(index, entry.getValue().floatValue()));
                xValues.add(entry.getKey());
                index++;
            } else {
                break;
            }
        }

        updateBarChart(barChart4, entries, xValues);
    }


}
