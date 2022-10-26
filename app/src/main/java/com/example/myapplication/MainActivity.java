package com.example.myapplication;

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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.myapplication.datahandlers.CategoriesHandler;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.example.myapplication.datahandlers.TransactionModel;
import com.example.myapplication.datahandlers.recyclerTransactionAdapter;
import com.example.myapplication.inter.AddCategoryActivity;
import com.example.myapplication.inter.AddTransactionActivity;
import com.example.myapplication.inter.CategoriesActivity;
import com.example.myapplication.inter.EditTransactionActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView transaction_list;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the layout and title
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Budget Handler");

        // Find the Recycler view and setup method to be called when item is pressed
        transaction_list = (RecyclerView) findViewById(R.id.transaction_recycler_view);

        // Add action when an item in the list is pressed. Open a window and allow to delete or edit it.
        // On short click it opens window to edit the transaction. On long click opens window to delete it.
        transaction_list.addOnItemTouchListener(
                new RecyclerItemClickListener(this, transaction_list ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // Open a information window about the transaction pressed by user.
                        recyclerTransactionAdapter ra = (recyclerTransactionAdapter) transaction_list.getAdapter();
                        if (ra != null) {
                            TransactionModel model = ra.getItem(position);
                            if (model != null){
                                // Open edit activity

                                Toast.makeText(MainActivity.this,"Pressed",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, EditTransactionActivity.class);
                                // Put the id
                                intent.putExtra("id",model.getId());
                                launcher.launch(intent);
                            }
                        }


                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // Get the item id
                        recyclerTransactionAdapter ra = (recyclerTransactionAdapter) transaction_list.getAdapter();
                        if (ra != null) {
                            TransactionModel model = ra.getItem(position);
                            final int transaction_id=model.getId();
                            // When long click is pressed delete the transaction
                            new AlertDialog.Builder(MainActivity.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Deleting Transaction")
                                    .setMessage("Are you sure you want to delete this transaction?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            TransactionHandler db_helper = new TransactionHandler(MainActivity.this);
                                            final boolean success = db_helper.deleteTransaction(transaction_id);
                                            if(success){
                                                Toast.makeText(MainActivity.this,"Successfully deleted",Toast.LENGTH_SHORT).show();
                                            }
                                            updateTransactionView();
                                        }
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }

                    }
                })
        );

        // Define launcher for coming back from activities that modify database of transactions and update the view shown in the
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // Handle the returned Uri
                        if (result.getResultCode() == Activity.RESULT_OK){
                            // Update the view
                            updateTransactionView();
                        }
                    }
                }
        );

        // Configure buttons
        this.configureButtons();

        updateTransactionView();
    }

    protected void configureButtons(){
        FloatingActionButton btn = (FloatingActionButton) findViewById(R.id.handle_categories_button);

        btn.setOnClickListener(new View.OnClickListener()
        {
            /**
             * Called when the button is pressed. It launched activity for handling categories
             * @param v The button that called it
             */
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
                startActivity(intent);
            }
        });


    }


    public void launchAddTransaction(View view) {
        //Check what type of transaction is it and launch activity

        // Define launcher for updating list on completion
        try {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            if(view.getId() == R.id.add_expense_floating_button){
                intent.putExtra("type", CategoriesHandler.TYPE_EXPENSE);
            }else if (view.getId() == R.id.add_income_floating_button){
                intent.putExtra("type",CategoriesHandler.TYPE_INCOME);
            }
            // Launch the activity
            launcher.launch(intent);
        }catch (Exception e){
            Log.d("exception",e.toString());
        }



    }

/*    protected void runPythonAnalysis(){
        // Start Python
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }



        Python py = Python.getInstance();

        PyObject pyobj = py.getModule("BudgetHandler");
        PyObject  python_budget_handler = pyobj.callAttr("BudgetHandler");

        // TRy to run some Python code
        try {
            python_budget_handler.callAttr("readFile","ExampleTransactions/transactions.csv");
        }catch (PyException e){
            Log.d("pyexception","File not read");
        }
    }*/

    protected void updateTransactionView(){

        // Create an instance of the database manager
        TransactionHandler th = new TransactionHandler(MainActivity.this);

        // Get all the transactions stored in the database ordered
        List<TransactionModel> transactions = th.getAllTransactions(true);

        // Update the recycler view
        setTransactionAdapter(transactions);

    }

    protected void setTransactionAdapter(List<TransactionModel> transactions){
        recyclerTransactionAdapter adapter = new recyclerTransactionAdapter(new ArrayList<TransactionModel>(transactions));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        transaction_list.setLayoutManager(layoutManager);
        transaction_list.setItemAnimator(new DefaultItemAnimator());
        transaction_list.setAdapter(adapter);
    }





}