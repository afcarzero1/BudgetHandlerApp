package com.example.myapplication.inter;

import com.example.myapplication.R;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.example.myapplication.datahandlers.TransactionModel;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class EditTransactionActivity extends AddTransactionActivity {

    int transaction_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Change button from add to delete
        ((Button)findViewById(R.id.delete_button)).setText("Update");

        // Get the id of the transaction
        //todo : add exception in case not possible
        transaction_id=getIntent().getExtras().getInt("id");

        putModelOnFields(transaction_id);
    }

    @Override
    protected void addTransaction(){
        // Update the item


        TransactionHandler db_helper = new TransactionHandler(this);

        // Validate fields content is correct
        if(!validateFields()){
            return;
        }

        TransactionModel transaction_model = retrieveDataFromFields();

        boolean success = db_helper.updateTransaction(transaction_id,transaction_model);
        if (success){
            Toast.makeText(EditTransactionActivity.this,"Successfully updated",Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
        }

        // Finish the activity
        finish();
    }





}