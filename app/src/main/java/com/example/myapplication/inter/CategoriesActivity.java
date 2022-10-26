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
import android.widget.Button;
import android.widget.EditText;
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
import java.util.List;
import java.util.Map;


//todo : Functionalities of classes dealing with recycler view can be implemented in an upper class and
// unified using inheritance (see this class and MainActivity)
public class CategoriesActivity extends AppCompatActivity {

    private RecyclerView categories_view;
    private ActivityResultLauncher<Intent> launcher;
    private BarChart categories_bar_chart;
    private TextView et_type;


    private int monthToShow;
    private int yearToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        this.setTitle("Categories Manager");

        //new MonthYearPickerDialog().show(getSupportFragmentManager(),"about");

        // Find the recycler view
        this.categories_view = (RecyclerView) findViewById(R.id.recycler_view_categories);
        this.categories_bar_chart = (BarChart) findViewById(R.id.categories_bar_chart);

        // Configure the button for adding categories and configure the functionality for deleting categories
        this.configureAddFunctionality();
        this.configureDeleteFunctionality();

        // Display current categories
        this.updateCategoriesView();

        //Get date
        this.onSelectMonth(null);
/*        Button button = (Button) findViewById(R.id.button_select_month);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CategoriesActivity.this.onSelectMonth(v);
            }
        });*/


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
        List<CategoriesModel> categories=ch.getAllCategories(CategoriesHandler.TYPE_EXPENSE);

        if (categories.isEmpty()){
            return;
        }

        for (CategoriesModel cm : categories){
            String category_name = cm.getName();
            if(!categories_to_expense.containsKey(category_name)){
                categories_to_expense.put(category_name, (float) 0);
            }
        }

        this.plotValues(categories_to_expense);
    }

    protected void plotValues(Map<String,Float> to_plot){
        //input Y data (Months Data - 12 Values)
        ArrayList<Float> valuesList = new ArrayList<Float>();
        final ArrayList<String> xAxisLabel = new ArrayList<>();

        int  numberEntries = to_plot.size();
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
        this.categories_bar_chart.setData(data);
        this.categories_bar_chart.setScaleEnabled(false);
        this.categories_bar_chart.getLegend().setEnabled(false);
        this.categories_bar_chart.setDrawBarShadow(false);
        this.categories_bar_chart.getDescription().setEnabled(false);
        this.categories_bar_chart.setPinchZoom(false);
        this.categories_bar_chart.setDrawGridBackground(true);
        this.categories_bar_chart.invalidate();
    }

    protected void configureDeleteFunctionality(){
        categories_view.addOnItemTouchListener(
                new RecyclerItemClickListener(this, categories_view ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {

                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // Get the item id
                        recyclerCategoriesAdapter ra = (recyclerCategoriesAdapter) categories_view.getAdapter();
                        if (ra != null) {
                            CategoriesModel model = ra.getItem(position);
                            final int category_id=model.getId();
                            // When long click is pressed delete the transaction
                            new AlertDialog.Builder(CategoriesActivity.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Deleting Category")
                                    .setMessage("Are you sure you want to delete this category?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            CategoriesHandler db = new CategoriesHandler(CategoriesActivity.this);
                                            final boolean success = db.deleteCategory(category_id);
                                            if(success){
                                                Toast.makeText(CategoriesActivity.this,"Successfully deleted",Toast.LENGTH_SHORT).show();
                                            }
                                            updateCategoriesView();
                                        }
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }

                    }
                })
        );



    }

    protected void configureAddFunctionality(){

        // Find the button
        FloatingActionButton add_button = (FloatingActionButton) findViewById(R.id.add_category_button);

        add_button.setOnClickListener(new View.OnClickListener()
        {
            /**
             * Called when the add button is pressed. Starts the dialog for category
             * @param v The button that called it
             */
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(CategoriesActivity.this,AddCategoryActivity.class);
                launcher.launch(intent);
            }
        });

        // Launcher to update
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // Handle the returned Uri
                        if (result.getResultCode() == Activity.RESULT_OK){
                            // Update the view
                            updateCategoriesView();
                        }
                    }
                }
        );

    }

    protected void configureTypeField(){
        // Find the view
        this.et_type = (TextView) findViewById(R.id.type);

        // Set default
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

    /**
     * Update the categories view. Retrieve data from the application database and display on the screen.
     */
    protected void updateCategoriesView(){

        // Access the database
        CategoriesHandler ch = new CategoriesHandler(CategoriesActivity.this);

        // Get all the categories
        List<CategoriesModel> categories = ch.getAllCategories();

        // Use them to set the adapter
        this.setCategoriesAdapter(categories);
    }

    /**
     * Function to set the adapter of the recycler view of the categories
     * @param categories
     */
    protected void setCategoriesAdapter(List<CategoriesModel> categories){
        // Create an adapter from the categories passed
        recyclerCategoriesAdapter adapter = new recyclerCategoriesAdapter(new ArrayList<CategoriesModel>(categories));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        // Set the adapter on the categories recycler view
        categories_view.setLayoutManager(layoutManager);
        categories_view.setItemAnimator(new DefaultItemAnimator());
        categories_view.setAdapter(adapter);
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


        dialogFragment.show(getSupportFragmentManager(), null);

        Log.d("DATE_PICKER",String.valueOf(yearSelected)+'/'+String.valueOf(monthSelected));
}
}