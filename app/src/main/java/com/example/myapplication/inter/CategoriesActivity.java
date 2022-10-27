package com.example.myapplication.inter;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.RecyclerItemClickListener;
import com.example.myapplication.datahandlers.CategoriesHandler;
import com.example.myapplication.datahandlers.CategoriesModel;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//todo : Functionalities of classes dealing with recycler view can be implemented in an upper class and
// unified using inheritance (see this class and MainActivity)
public class CategoriesActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> launcher;
    private BarChart categories_bar_chart;
    private TextView et_type;
    private TextView et_date;


    private int monthToShow;
    private int yearToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        this.setTitle("Categories Manager");

        //new MonthYearPickerDialog().show(getSupportFragmentManager(),"about");

        // Find the graph
        this.categories_bar_chart = (BarChart) findViewById(R.id.categories_bar_chart);


        //Get date from user as soon as entered
        this.onSelectMonth(null);
/*        Button button = (Button) findViewById(R.id.button_select_month);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CategoriesActivity.this.onSelectMonth(v);
            }
        });*/

        this.configureDateField();
        this.configureTypeField();

        // Display past expenses divided in categories
        this.updateGraph();
    }

    protected void updateGraph(){

        // Access the database
        CategoriesHandler ch = new CategoriesHandler(CategoriesActivity.this);

        // Get the current date
        int currentYear = this.yearToShow;
        int currentMonth = this.monthToShow;

        // Expenses of past months
        TransactionHandler th = new TransactionHandler(CategoriesActivity.this);


        String type_to_retrieve=String.valueOf(et_type.getText());
        Map<String,Float> categories_to_expense = ch.getAllCategoriesSum(th, type_to_retrieve, currentYear, currentMonth);


        // Add categories that do not appear (todo : solve this uing left outer join )
        List<CategoriesModel> categories=ch.getAllCategories(type_to_retrieve);

        if (categories.isEmpty()){
            return;
        }

        for (CategoriesModel cm : categories){
            String category_name = cm.getName();
            if(!categories_to_expense.containsKey(category_name)){
                categories_to_expense.put(category_name, (float) 0);
            }
        }

        this.plotValues2(categories_to_expense);
    }

    protected void plotValues(Map<String,Float> to_plot){

        ArrayList<Float> valuesList = new ArrayList<Float>();
        final ArrayList<String> xAxisLabel = new ArrayList<>();

        ArrayList<BarEntry> entries = new ArrayList<>();
        int i=0;
        float maxValue = 0;
        for (Map.Entry<String,Float> entry : to_plot.entrySet()){
            // For every entry add the label and the value
            xAxisLabel.add(entry.getKey());
            valuesList.add(entry.getValue());

            if(entry.getValue() > maxValue){
                maxValue = entry.getValue();}

            BarEntry barEntry = new BarEntry(i+1, entry.getValue());
            entries.add(barEntry);
            i++;
        }
        xAxisLabel.add(""); //empty label for the last vertical grid line on Y-Right Axis


        // Get xaxis of the bar graph and do manipulations
        XAxis xAxis = this.categories_bar_chart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.setDrawGridLines(true);
        //xAxis.setGranularity(1f);
        //xAxis.setGranularityEnabled(true);
        xAxis.setAxisMinimum(0 + 0.5f); //to center the bars inside the vertical grid lines we need + 0.5 step
        xAxis.setAxisMaximum(entries.size() + 0.5f); //to center the bars inside the vertical grid lines we need + 0.5 step
        xAxis.setLabelCount(xAxisLabel.size(), true); //draw x labels for 13 vertical grid lines
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setXOffset(0f); //labels x offset in dps
        xAxis.setYOffset(0f); //labels y offset in dps
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return xAxisLabel.get((int) value);
            }
        });

        //initialize Y-Right-Axis
        YAxis rightAxis = this.categories_bar_chart.getAxisRight();
        rightAxis.setTextColor(Color.BLACK);
        rightAxis.setTextSize(14);
        rightAxis.setDrawAxisLine(true);
        rightAxis.setAxisLineColor(Color.BLACK);
        rightAxis.setDrawGridLines(true);
        rightAxis.setGranularity(1f);
        rightAxis.setGranularityEnabled(true);
        rightAxis.setAxisMinimum(0);
        rightAxis.setAxisMaximum(maxValue+2f);
        rightAxis.setLabelCount(4, true); //draw y labels (Y-Values) for 4 horizontal grid lines starting from 0 to 6000f (step=2000)
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);


        //initialize Y-Left-Axis
        YAxis leftAxis = this.categories_bar_chart.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setLabelCount(0, true);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "";
            }
        });

        //set the BarDataSet
        BarDataSet barDataSet = new BarDataSet(entries, "Categories");
        barDataSet.setColor(Color.BLUE);
        barDataSet.setFormSize(15f);
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(12f);


        //set the BarData to chart
        BarData data = new BarData(barDataSet);


        if( categories_bar_chart.getData() != null){
            this.categories_bar_chart.getData().clearValues();
            this.categories_bar_chart.invalidate();
            this.categories_bar_chart.setData(data);
        }else{
            this.categories_bar_chart.setData(data);
        }

        this.categories_bar_chart.setScaleEnabled(false);
        this.categories_bar_chart.getLegend().setEnabled(false);
        this.categories_bar_chart.setDrawBarShadow(false);
        this.categories_bar_chart.getDescription().setEnabled(false);
        this.categories_bar_chart.setPinchZoom(false);
        this.categories_bar_chart.setDrawGridBackground(true);
        this.categories_bar_chart.notifyDataSetChanged();
        this.categories_bar_chart.invalidate();
    }



    protected void plotValues2(Map<String,Float> to_plot){

        // Build the entries
        ArrayList<BarEntry> entries = transformToPlotArray(to_plot);

        // Build dataset from entries
        BarDataSet barDataSet = buildDataset(entries);

        // Configure the axis
        configureYAxis(to_plot);
        configureXAxis(to_plot);

        // Set the dataset
        categories_bar_chart.setData(new BarData(barDataSet));



        this.categories_bar_chart.setScaleEnabled(false);
        this.categories_bar_chart.getLegend().setEnabled(false);
        this.categories_bar_chart.setDrawBarShadow(false);
        this.categories_bar_chart.getDescription().setEnabled(false);
        this.categories_bar_chart.setPinchZoom(false);
        this.categories_bar_chart.setDrawGridBackground(true);
        // Plot it by notifying change and re-drawing
        categories_bar_chart.notifyDataSetChanged();
        categories_bar_chart.invalidate();



    }

    protected void configureYAxis(Map<String,Float> to_plot){

        float maxValue=0;
        for (Map.Entry<String,Float> entry : to_plot.entrySet()){
            if(maxValue < entry.getValue()){maxValue=entry.getValue();}
        }


        YAxis rightAxis = this.categories_bar_chart.getAxisRight();
        rightAxis.setTextColor(Color.BLACK);
        rightAxis.setTextSize(14);
        rightAxis.setDrawAxisLine(true);
        rightAxis.setAxisLineColor(Color.BLACK);
        rightAxis.setDrawGridLines(true);
        rightAxis.setGranularity(1f);
        rightAxis.setGranularityEnabled(true);
        rightAxis.setAxisMinimum(0);
        rightAxis.setAxisMaximum(maxValue+2f);
        rightAxis.setLabelCount(4, true); //draw y labels (Y-Values) for 4 horizontal grid lines starting from 0 to 6000f (step=2000)
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);


        YAxis leftAxis = this.categories_bar_chart.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setLabelCount(0, true);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "";
            }
        });

    }

    protected void configureXAxis(Map<String,Float> to_plot){
        ArrayList<String> xAxisLabel = this.transformToCategories(to_plot);
        xAxisLabel.add(""); // For the last
        // Create mapping for indices
        Map<Float,Integer> mapToIndex = new HashMap<Float, Integer>();

        final float division = (float)xAxisLabel.size() / (float)(xAxisLabel.size());
        float start = 0.5f;

        for (int i=0;i<xAxisLabel.size();i++){
            mapToIndex.put(start,i);
            start+=division;
        }


        // Get xaxis of the bar graph and do manipulations
        XAxis xAxis = this.categories_bar_chart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.setDrawGridLines(true);
        //xAxis.setGranularity(1f);
        //xAxis.setGranularityEnabled(true);
        xAxis.setAxisMinimum(0 + 0.5f); //to center the bars inside the vertical grid lines we need + 0.5 step
        xAxis.setAxisMaximum(to_plot.size() + 0.5f); //to center the bars inside the vertical grid lines we need + 0.5 step
        xAxis.setLabelCount(xAxisLabel.size(), true); //draw x labels for 13 vertical grid lines
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setXOffset(0f); //labels x offset in dps
        xAxis.setYOffset(0f); //labels y offset in dps
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (!mapToIndex.containsKey(value)){
                    return "";
                }
                int index2 = mapToIndex.get(value);

                return xAxisLabel.get(index2);
            }
        });



    }

    protected BarDataSet buildDataset(ArrayList<BarEntry> entries){
        BarDataSet barDataSet = new BarDataSet(entries, "Categories");

        barDataSet.setColor(Color.BLUE);
        barDataSet.setFormSize(15f);
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(12f);
        return barDataSet;
    }

    protected ArrayList<String> transformToCategories(Map<String,Float> to_plot){
        ArrayList<String> entries = new ArrayList<>();

        for (Map.Entry<String,Float> entry : to_plot.entrySet()){
            entries.add(entry.getKey());
        }

        return entries;
    }

    protected ArrayList<BarEntry> transformToPlotArray(Map<String,Float> to_plot){
        ArrayList<BarEntry> entries = new ArrayList<>();
        int i=0;
        for (Map.Entry<String,Float> entry : to_plot.entrySet()){
            BarEntry barEntry = new BarEntry(i+1, entry.getValue());
            entries.add(barEntry);
            i++;
        }

        return entries;
    }

    protected void configureDateField(){
        this.et_date = findViewById(R.id.date_categories);

        this.et_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show dialog for selecting the month
                onSelectMonth(view);
            }
        });
    }

    protected void configureTypeField(){
        // Find the view
        this.et_type = (TextView) findViewById(R.id.type);

        // Set default type
        this.et_type.setText(CategoriesHandler.TYPE_EXPENSE);

        // Set the listener for when the text is clicked
        this.et_type.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Show to user an interface to choose the type
                        AlertDialog.Builder builder = new AlertDialog.Builder(CategoriesActivity.this);
                        builder.setTitle("Choose...");

                        // Setup the adapter
                        String [] arr = new String[]{CategoriesHandler.TYPE_EXPENSE,CategoriesHandler.TYPE_INCOME};
                        final ArrayAdapter<String> array_adapter = new ArrayAdapter<String>(CategoriesActivity.this, android.R.layout.select_dialog_item,arr);

                        // Setup buttons behaviour
                        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        builder.setAdapter(array_adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String strName = array_adapter.getItem(which);
                                et_type.setText(strName);
                                updateGraph();
                            }
                        });
                        builder.show();
                    }
                }
        );
    }

    protected void onSelectMonth(View view){
        final int yearSelected;
        int monthSelected;


        TextView dateTextView = (TextView) findViewById(R.id.date_categories);

        //Set default values
        Calendar calendar = Calendar.getInstance();
        yearSelected = calendar.get(Calendar.YEAR);
        monthSelected = calendar.get(Calendar.MONTH);

        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment
                .getInstance(monthSelected, yearSelected);

        // Set action when selected value changes
        dialogFragment.setOnDateSetListener(new MonthYearPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int year, int monthOfYear) {
                CategoriesActivity.this.monthToShow = monthOfYear +1; // months start at 0 (0-11)
                CategoriesActivity.this.yearToShow = year;

                String to_show = String.valueOf(year) + '/' + String.valueOf(monthOfYear+1);
                dateTextView.setText(to_show);
                CategoriesActivity.this.updateGraph();
            }
        });

        // Show the dialog
        dialogFragment.show(getSupportFragmentManager(), null);

        Log.d("DATE_PICKER",String.valueOf(yearSelected)+'/'+String.valueOf(monthSelected));
}
}