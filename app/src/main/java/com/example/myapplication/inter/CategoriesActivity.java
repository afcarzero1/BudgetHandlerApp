package com.example.myapplication.inter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.datahandlers.adapters.RecyclerTransactionAdapter;
import com.example.myapplication.datahandlers.models.CategoriesModel;
import com.example.myapplication.datahandlers.models.TransactionModel;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//todo : Functionalities of classes dealing with recycler view can be implemented in an upper class and
// unified using inheritance (see this class and MainActivity)
public class CategoriesActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> launcher;
    private HorizontalBarChart mCategoriesBarChart;
    private RecyclerView mTransactionsView;
    private TextView mTypeTextView;
    private TextView mDateTextView;
    private Spinner mBaseCategorySpinner;


    private int monthToShow;
    private int yearToShow;

    private List<String> mExpenseCategoriesNames;
    private List<String> mIncomeCategoriesNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        this.setTitle("Categories Manager");

        //new MonthYearPickerDialog().show(getSupportFragmentManager(),"about");

        retrieveAvailableCategories();
        // Find the graph
        this.mCategoriesBarChart = (HorizontalBarChart) findViewById(R.id.categories_bar_chart);
        this.mTransactionsView = (RecyclerView) findViewById(R.id.month_transactions);

        //Get date from user as soon as entered
        this.onSelectMonth(null);

        this.configureDateField();
        this.configureTypeField();
        this.configureCategoryField();

        //todo set here current month and year and not hard coded
        this.yearToShow=2022;
        this.monthToShow=10;

        // Display past expenses divided in categories
        this.updateGraph();
    }



    protected void updateList(){
        TransactionHandler th = new TransactionHandler(CategoriesActivity.this);
        List<TransactionModel> transactions= th.getAllTransactions(mTypeTextView.getText().toString(),this.yearToShow,this.monthToShow,true);

        RecyclerTransactionAdapter adapter = new RecyclerTransactionAdapter(new ArrayList<TransactionModel>(transactions));
        this.mTransactionsView.setLayoutManager(new LinearLayoutManager(CategoriesActivity.this));
        this.mTransactionsView.setItemAnimator(new DefaultItemAnimator());
        this.mTransactionsView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    protected void updateGraph(){
        updateList();
        // Access the database
        TransactionHandler ch = new TransactionHandler(CategoriesActivity.this);

        // Get the current date
        int currentYear = this.yearToShow;
        int currentMonth = this.monthToShow;

        String typeToRetrieve=String.valueOf(mTypeTextView.getText());
        String baseCategory = mBaseCategorySpinner.getSelectedItem().toString();

        Map<String,Float> categories_to_expense = ch.groupTransactionsByCategory(baseCategory,typeToRetrieve, TransactionModel.FIELDS.CATEGORY.getSqlName() ,currentYear, currentMonth);

        this.plotValues2(categories_to_expense);
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
        mCategoriesBarChart.setData(new BarData(barDataSet));



        this.mCategoriesBarChart.setScaleEnabled(false);
        this.mCategoriesBarChart.getLegend().setEnabled(false);
        this.mCategoriesBarChart.setDrawBarShadow(false);
        this.mCategoriesBarChart.getDescription().setEnabled(false);
        this.mCategoriesBarChart.setPinchZoom(false);
        this.mCategoriesBarChart.setDrawGridBackground(true);
        // Plot it by notifying change and re-drawing
        mCategoriesBarChart.notifyDataSetChanged();
        mCategoriesBarChart.invalidate();
    }

    protected void configureYAxis(Map<String,Float> to_plot){

        float maxValue=0;
        for (Map.Entry<String,Float> entry : to_plot.entrySet()){
            if(maxValue < entry.getValue()){maxValue=entry.getValue();}
        }



        YAxis rightAxis = this.mCategoriesBarChart.getAxisRight();
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


        YAxis leftAxis = this.mCategoriesBarChart.getAxisLeft();
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
        XAxis xAxis = this.mCategoriesBarChart.getXAxis();
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
        this.mDateTextView = findViewById(R.id.date_categories);

        this.mDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show dialog for selecting the month
                onSelectMonth(view);
            }
        });
    }

    protected void configureTypeField(){
        //COnfigure also spinner
        mBaseCategorySpinner = (Spinner) findViewById(R.id.spinner_category_base);
        setAdapterSpinner(mExpenseCategoriesNames);

        // Find the view
        this.mTypeTextView = (TextView) findViewById(R.id.type);

        // Set default type
        this.mTypeTextView.setText(TransactionHandler.TYPE_EXPENSE);

        // Set the listener for when the text is clicked
        this.mTypeTextView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Show to user an interface to choose the type
                        AlertDialog.Builder builder = new AlertDialog.Builder(CategoriesActivity.this);
                        builder.setTitle("Choose...");

                        // Setup the adapter
                        String [] arr = new String[]{TransactionHandler.TYPE_EXPENSE,TransactionHandler.TYPE_INCOME};
                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(CategoriesActivity.this, android.R.layout.select_dialog_item,arr);

                        // Setup buttons behaviour
                        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String strName = arrayAdapter.getItem(which);
                                if(strName==TransactionHandler.TYPE_EXPENSE){
                                    setAdapterSpinner(mExpenseCategoriesNames);
                                }else{
                                    setAdapterSpinner(mIncomeCategoriesNames);
                                }

                                mTypeTextView.setText(strName);
                                updateGraph();
                            }
                        });

                        builder.show();
                    }
                }
        );
    }

    protected void configureCategoryField(){
        mBaseCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    protected void setAdapterSpinner(List<String> spinnerValues){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(CategoriesActivity.this, android.R.layout.simple_spinner_item,spinnerValues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBaseCategorySpinner.setAdapter(adapter);

        mBaseCategorySpinner.setSelection(adapter.getPosition("base"));

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

    //todo : introduce this in a helper class to avoid code rpetition
    protected void retrieveAvailableCategories(){
        TransactionHandler th = new TransactionHandler(this);
        List<CategoriesModel> allCategories = th.getAllCategories(TransactionHandler.TYPE_EXPENSE);
        List<CategoriesModel> incomeCategories = th.getAllCategories(TransactionHandler.TYPE_INCOME);

        mExpenseCategoriesNames = new ArrayList<>();
        mIncomeCategoriesNames = new ArrayList<>();

        for(CategoriesModel cm : allCategories){
            mExpenseCategoriesNames.add(cm.getName());
        }
        for(CategoriesModel cm : incomeCategories){
            mIncomeCategoriesNames.add(cm.getName());
        }
    }
}