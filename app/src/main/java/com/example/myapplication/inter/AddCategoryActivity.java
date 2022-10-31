package com.example.myapplication.inter;


import com.example.myapplication.R;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.example.myapplication.datahandlers.models.CategoriesModel;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class AddCategoryActivity extends Activity {

    private EditText mCategoryNameEdittext;
    private ToggleButton mCategoryTypeButton;
    private Spinner mCategoryParentSpinner;

    private List<String> mExpenseCategoriesNames;
    private List<String> mIncomeCategoriesNames;
    private List<String> mListToDisplay;
    public boolean mExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        this.setTitle("Add category");
        this.mCategoryNameEdittext = (EditText) findViewById(R.id.category_name_edit_text);
        this.mCategoryTypeButton = (ToggleButton) findViewById(R.id.toggleButton);
        mCategoryParentSpinner = (Spinner) findViewById(R.id.parent_category_input_field);


        this.mCategoryTypeButton.setText(TransactionHandler.TYPE_EXPENSE);
        mExpense = true;

        this.mCategoryTypeButton.setTextOff(TransactionHandler.TYPE_EXPENSE);
        this.mCategoryTypeButton.setTextOn(TransactionHandler.TYPE_INCOME);
        configureButton();
        retrieveAvailableCategories();
        configureToggleButton();
        //configureParentEditText();

    }

    protected void configureToggleButton(){
        mListToDisplay=mExpenseCategoriesNames;
        setAdapterSpinner(mListToDisplay);

        mCategoryTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clear the parent field to avoid undesired input
                //Change the list
                if(mExpense){
                    mListToDisplay = mIncomeCategoriesNames;

                    setAdapterSpinner(mListToDisplay);
                }else{
                    mListToDisplay = mExpenseCategoriesNames;

                    // Set spinner options
                    setAdapterSpinner(mListToDisplay);

                }
                mExpense=!mExpense;
            }
        });

    }

    protected void setAdapterSpinner(List<String> spinnerValues){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddCategoryActivity.this, R.layout.spinner_item,spinnerValues);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        mCategoryParentSpinner.setAdapter(adapter);

        mCategoryParentSpinner.setSelection(adapter.getPosition("base"));
    }

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

    protected void configureButton(){
    Button btn = (Button) findViewById(R.id.confirm_add_cattegory_button);

    btn.setOnClickListener(new View.OnClickListener()
        {
            /**
             * Called when the add button is pressed. Checks validity of data and adds it to the database.
             * @param v The button that called it
             */
            @Override
            public void onClick(View v)
            {
                addCategory();
            }
        });
    }

    protected void addCategory(){

        // Validate it is not empty
        if(!validateFields()){
            return;
        }

        // Proceed to adding it
        CategoriesModel cm;
        TransactionHandler th = new TransactionHandler(AddCategoryActivity.this);

        try {
            cm = retrieveDataFromFields();
            boolean success = th.addCategory(cm);
            if (success){
                Toast.makeText(AddCategoryActivity.this,"Category added", Toast.LENGTH_SHORT).show();}
            else{
                Toast.makeText(AddCategoryActivity.this,"Error",Toast.LENGTH_LONG).show();
            }
            setResult(Activity.RESULT_OK);
            finish();
        }catch (Exception e){
            Toast.makeText(AddCategoryActivity.this,"Error",Toast.LENGTH_LONG).show();
        }
    }

    protected boolean validateFields(){
        // Check that the name is not empty
        return checkNotEmpty(this.mCategoryNameEdittext);
    }

    protected CategoriesModel retrieveDataFromFields(){
        //todo : add here support for parent class
        return new CategoriesModel(this.mCategoryNameEdittext.getText().toString(),
                mCategoryTypeButton.getText().toString(),
                mCategoryParentSpinner.getSelectedItem().toString(),
                mCategoryTypeButton.getText().toString());
    }


    private boolean checkNotEmpty(EditText field){
        if(field.getText().length()==0){
            field.setError("This field must have a value");
            return false;
        }
        return true;
    }
}