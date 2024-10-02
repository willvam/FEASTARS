package com.example.feastarfeed;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import java.util.List;
import java.util.Map;

public class barchart extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinnerMonth;
    private BarChart barChart;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barchart);

        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.topic));

        spinnerMonth = findViewById(R.id.spinner_month);
        //spinnerMonth.setOnItemSelectedListener(this);

        barChart = findViewById(R.id.chart);
        barChart.getAxisRight().setDrawLabels(false);

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
                Intent intent = new Intent(barchart.this, superuser.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void loadMonthData(String month) {
        DatabaseReference tagrankRef = database.child("tagrank");
        DatabaseReference monthRef = tagrankRef.child(month);
        monthRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
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
                        if (index < 5) {  //要列出幾個改這邊
                            entries.add(new BarEntry(index, entry.getValue().floatValue()));
                            xValues.add(entry.getKey());
                            index++;
                        } else {
                            break;
                        }
                    }

                    updateBarChart(barChart, entries, xValues);
                }else {
                    // 資料不存在,清空圖表或顯示提示訊息
                    barChart.clear();
                    barChart.setNoDataText("No data available for this month.");}
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
}