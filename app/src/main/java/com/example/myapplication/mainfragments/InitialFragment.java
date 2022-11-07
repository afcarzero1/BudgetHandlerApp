package com.example.myapplication.mainfragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InitialFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitialFragment extends Fragment {

    protected int mCurrentYear;
    protected int mCurrentMonth;
    protected  List<String> mDatesToPrint;
    protected BarChart mIncomeExpenseBarChart;

    private static final int mMonthsToDisplay=6;

    public InitialFragment() {
        // Required empty public constructor
    }

    public static InitialFragment newInstance() {
        InitialFragment fragment = new InitialFragment();

        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildDatesToDisplay();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_initial, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mIncomeExpenseBarChart = (BarChart) view.findViewById(R.id.income_expense_bar_chart);


        updateDateGraph();

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateGraph(){
        buildDatesToDisplay();
        updateDateGraph();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void updateDateGraph(){
        TransactionHandler th = new TransactionHandler(getContext());

        // Get the mapping yearmonth -> value
        Map<String,Float> lvYearMonthToExpense = th.groupTransactionsByMonthYear(TransactionHandler.TYPE_EXPENSE);
        Map<String,Float> lvYearMonthToIncome = th.groupTransactionsByMonthYear(TransactionHandler.TYPE_INCOME);


        // Create the dataset
        Pair<ArrayList<BarEntry>,ArrayList<BarEntry>> lvExpenseIncome = transformToPlotArray(lvYearMonthToExpense,lvYearMonthToIncome);
        ArrayList<BarEntry> entriesExpense = lvExpenseIncome.first;
        ArrayList<BarEntry> entriesIncome = lvExpenseIncome.second;

        BarDataSet datasetExpenses = buildDataset(entriesExpense,TransactionHandler.TYPE_EXPENSE, Color.RED);
        BarDataSet datasetIncomes = buildDataset(entriesIncome,TransactionHandler.TYPE_INCOME,Color.GREEN);

        BarData data = new BarData(datasetExpenses,datasetIncomes); //todo : use also income


        float groupSpace = 0.14f;
        float barSpace = 0.02f; // x2 dataset
        float barWidth = 0.41f; // x2 dataset

        data.setBarWidth(barWidth);
// (0.02 + 0.45) * 2 + 0.06 = 1.00 -> interval per "group"
        // CONFIGURE X AXIS
        XAxis xAxis = mIncomeExpenseBarChart.getXAxis();
        ArrayList<String> xAxisLabel = (ArrayList<String>) mDatesToPrint;
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if(value<0 || value>=xAxisLabel.size()){return "";}
                return xAxisLabel.get(index);
            }
        });
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.BLACK);

        xAxis.setAxisMaximum(6);
        xAxis.setAxisMinimum(0);

        mIncomeExpenseBarChart.setData(data);
        mIncomeExpenseBarChart.setPinchZoom(false);

        mIncomeExpenseBarChart.groupBars(0f, groupSpace, barSpace);
        mIncomeExpenseBarChart.notifyDataSetChanged();
        mIncomeExpenseBarChart.invalidate();


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected Pair<ArrayList<BarEntry>,ArrayList<BarEntry>> transformToPlotArray(Map<String,Float> toPlotExpenses, Map<String,Float> toPlotIncome){
        ArrayList<BarEntry> entriesExpense = new ArrayList<>();
        ArrayList<BarEntry> entriesIncome = new ArrayList<>();
        int i=0;
        for(String date : mDatesToPrint){

            BarEntry entryExpense = new BarEntry(i,toPlotExpenses.getOrDefault(date,(float)0));
            BarEntry entryIncome = new BarEntry(i,toPlotIncome.getOrDefault(date,(float)0));

            entriesExpense.add(entryExpense);
            entriesIncome.add(entryIncome);
            i++;
        }

        return new Pair<>(entriesExpense,entriesIncome);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void buildDatesToDisplay(){
        // Build the dates that we will display
        mCurrentYear = Calendar.getInstance().get(Calendar.YEAR);
        mCurrentMonth = Calendar.getInstance().get(Calendar.MONTH)+1;
        mDatesToPrint = new ArrayList<>();

        YearMonth currentMonth = YearMonth.of(mCurrentYear, mCurrentMonth);

        for(int i=0;i<mMonthsToDisplay;i++){
            YearMonth backThen = currentMonth.minusYears(0).minusMonths(i);
            // todo : solve bug here
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
            try{
                mDatesToPrint.add(backThen.format(formatter));
            }catch(Exception e){
                String cause = e.toString();
            }

        }
        Collections.reverse(mDatesToPrint);
    }

    protected BarDataSet buildDataset(ArrayList<BarEntry> entries, String datasetName, int color){
        BarDataSet barDataSet = new BarDataSet(entries,datasetName);
        barDataSet.setColor(color);
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(12f);
        return barDataSet;
    }

}