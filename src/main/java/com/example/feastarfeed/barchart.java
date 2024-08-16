package com.example.feastarfeed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class barchart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barchart);

        BarChart barChart = findViewById(R.id.chart);
        barChart.getAxisRight().setDrawLabels(false);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mayRef = database.child("May");

        mayRef.addValueEventListener(new ValueEventListener() {
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

                    // 取前三個最大的數據項目
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 處理錯誤
            }
        });
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