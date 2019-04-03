package com.example.myapplication;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;


public class PieChartActivity extends Activity
{
    float expenses[]  = {3.0f , 4.0f , 5.0f , 2.5f};
    String types[] = {"bills" , "fun" , "food", "supplies"};
    PieChart chart;
    @Override
    protected void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piechart);
        chart = findViewById(R.id.pieChart);
        setupPieChart();

    }

    private void setupPieChart()
    {
        List<PieEntry> pieEntries =  new ArrayList<>();
        for(int i=0 ; i<expenses.length;i++)
        {
            pieEntries.add(new PieEntry(expenses[i] , types[i]));
        }
        PieDataSet dataSet =  new PieDataSet(pieEntries , "Your Expenditure");

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

//        for (int c : ColorTemplate.JOYFUL_COLORS)
//            colors.add(c);
//
//        for (int c : ColorTemplate.COLORFUL_COLORS)
//            colors.add(c);
//
//        for (int c : ColorTemplate.LIBERTY_COLORS)
//            colors.add(c);

//        for (int c : ColorTemplate.PASTEL_COLORS)
//            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(chart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);


        chart.setData(data);
        chart.invalidate();
    }
}
