package com.example.myapplication.inter;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.datahandlers.models.AccountModel;
import com.example.myapplication.datahandlers.models.CategoriesModel;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.example.myapplication.datahandlers.models.TransactionModel;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddTransactionActivity extends AppCompatActivity {

    final Calendar myCalendar = Calendar.getInstance();
    EditText mStartDateEditText;
    EditText mCategoryEditText;
    EditText mValueEditText;
    Spinner mRecurrenceTypeSpinner;
    EditText mRecurrenceValueEditText;
    EditText mAccount;
    String mTransactionType;

    private int mIdOfSelectedCategory;
    private List<CategoriesModel> mAvailableCategories;
    private List<AccountModel> mAvailableAccounts;

    static final String[] RECURRENCE_TYPES = {"none", "days","weeks","months"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        //Set up the correct title
        Bundle extras = getIntent().getExtras();
        if (extras!=null){
           mTransactionType = extras.getString("type");
        }else{
            mTransactionType = "Transaction";
        }
        mIdOfSelectedCategory =-1; // Set invalid id

        // Retrieve available categories and accounts
        TransactionHandler ch = new TransactionHandler(AddTransactionActivity.this);
        mAvailableCategories =  ch.getAllCategories(mTransactionType);
        mAvailableAccounts = ch.getAllAccounts(true);

        //Retrieve objects
        fetchEditText();

        // Set up title of transaction and button
        ((TextView)findViewById(R.id.popup_title_add_expense)).setText(mTransactionType);

        // Setup button. Define function to be called when it is pressed. Finish activity and add transaction to database
        Button btn = (Button)findViewById(R.id.delete_button);
        btn.setOnClickListener(new View.OnClickListener()
        {
            /**
             * Called when the add button is pressed. Checks validity of data and adds it to the database.
             * @param v The button that called it
             */
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v)
            {
               addTransaction();
            }
        });

        // Set up calendar
        setUpCalendar();

        // Set up the spinner box
        setUpRecurrenceType();

        // Setup the category field
        setUpCategoryField();
        setUpAccountField();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void addTransaction(){
        // Validate fields
        if (!validateFields()){
            return;
        }

        // Retrieve data
        TransactionModel transaction_model;
        TransactionHandler th = new TransactionHandler(AddTransactionActivity.this);
        try {
            transaction_model = retrieveDataFromFields();
            // Add to the database
            th.addTransaction(transaction_model);

            // Show success to user
            Toast.makeText(AddTransactionActivity.this ,"Transaction added", Toast.LENGTH_SHORT).show();

            // Go back to main menu

            setResult(Activity.RESULT_OK);
            finish();
        }catch (Exception e){
            Toast.makeText(AddTransactionActivity.this ,"Transaction failed", Toast.LENGTH_SHORT).show();
        }

    }

    protected void setUpAccountField(){
        mAccount.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddTransactionActivity.this);
                        builder.setTitle("Choose...");
                        String[] arr = new String[mAvailableAccounts.size()];
                        int i =0;
                        for (AccountModel am : mAvailableAccounts){
                            arr[i] = am.getName();
                            i++;
                        }
                        final ArrayAdapter<String> array_adapter = new ArrayAdapter<String>(AddTransactionActivity.this, android.R.layout.select_dialog_item,arr);

                        // Srt buttons behaviour
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
                                mAccount.setText(strName);
                            }
                        });
                        builder.show();
                    }
                }
        );


    }

    protected void setUpCategoryField(){
        // Setup the dialog for choosing the category
        mCategoryEditText.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Open Category Picker
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddTransactionActivity.this);

                        //builder.setIcon(androidx.transition.R.drawable.abc_ic_star_black_36dp);
                        builder.setTitle("Choose...");

                        //todo : transfor into a recycler adapter for handling icon
                        TransactionHandler ch = new TransactionHandler(AddTransactionActivity.this);
                        List<CategoriesModel> categories = ch.getAllCategories(mTransactionType);
                        String [] arr= new String[categories.size()];
                        int i=0;
                        for (CategoriesModel cm : categories){
                            arr[i]=cm.getName();
                            i++;
                        }


                        final ArrayAdapter<String> array_adapter = new ArrayAdapter<String>(AddTransactionActivity.this, android.R.layout.select_dialog_item,arr);


                        // Srt buttons behaviour
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

                                // This is the category selected
                                AddTransactionActivity.this.mIdOfSelectedCategory =which;
                                mCategoryEditText.setText(strName);
                            }
                        });
                        builder.show();



                        // Retrieve categories
                    }
                }
        );
        

    }

    /**
     *  Setups the calendar that pop-up when user presses the field for the date transaction.
     */
    protected void setUpCalendar() {

        // Retrieve the date input text
        mStartDateEditText = (EditText) findViewById(R.id.popup_date_input);
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                updateLabel();
            }
        };
        mStartDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddTransactionActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    /**
     *  Updates the text after calendar is shown to user
     */
    private void updateLabel(){
        String myFormat="MM/dd/yy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        mStartDateEditText.setText(dateFormat.format(myCalendar.getTime()));
    }

    /**
     *  Set up the spin-box used for recurrence
     */
    protected void setUpRecurrenceType(){

        Spinner s = (Spinner) findViewById(R.id.popup_recurrency_type_input);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, RECURRENCE_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);


        // Setup action on item selection
        s.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // your code here

                        String selected = RECURRENCE_TYPES[position];
                        EditText ed = (EditText) findViewById(R.id.popup_recurrency_input);
                        if (Objects.equals(selected, RECURRENCE_TYPES[0])){
                            ed.getText().clear();
                            ed.setEnabled(false);
                        }else{
                            ed.setEnabled(true);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }
                }

        );

    }


    protected void fetchEditText(){
         mStartDateEditText = (EditText) findViewById(R.id.popup_date_input);
         mCategoryEditText = (EditText) findViewById(R.id.popup_category_input);
         mValueEditText = (EditText) findViewById(R.id.popup_value_input);
         mRecurrenceValueEditText = (EditText) findViewById(R.id.popup_recurrency_input);
         mRecurrenceTypeSpinner = (Spinner) findViewById(R.id.popup_recurrency_type_input);
         mAccount = (EditText) findViewById(R.id.account_transaction_edit_text);
    }


    protected boolean validateFields(){
        // Category, date and value must have a value!
        // The check is repeated because of the compiler optimization

        checkNotEmpty(mCategoryEditText);
        checkNotEmpty(mStartDateEditText);
        checkNotEmpty(mValueEditText);
        checkNotEmpty(mAccount);

        return checkNotEmpty(mAccount) && checkNotEmpty(mCategoryEditText) && checkNotEmpty(mValueEditText) && checkNotEmpty(mStartDateEditText) && mIdOfSelectedCategory !=-1;
    }

    private boolean checkNotEmpty(EditText field){
        if(field.getText().length()==0){
            field.setError("This field must have a value");

            return false;
        }
        return true;
    }


    /**
     * Function for getting the data from the fields in the GUI
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected TransactionModel retrieveDataFromFields(){
        TransactionModel transaction_model;
        // Create the customer model

        String category = mCategoryEditText.getText().toString();
        String date = mStartDateEditText.getText().toString();
        // Transform date to compatible format
        date = this.transformDateFromFieldsToDatabase(date);


        String recurrence_type = mRecurrenceTypeSpinner.getSelectedItem().toString();
        Float recurrence_value ;
        Float value;

        // Assign dummy variable when no recurrence is required
        if(recurrence_type.equals(RECURRENCE_TYPES[0])){
            recurrence_value = (float)0.0;
        }else{
            recurrence_value = Float.parseFloat(mRecurrenceValueEditText.getText().toString());
        }

        if(mValueEditText.getText().toString().isEmpty()){
            value = (float) 0.0;
        }else{
            value = Float.parseFloat(mValueEditText.getText().toString());
        }
        // id used here is not real id of the transaction

        transaction_model = new TransactionModel(0, mTransactionType,category,mAccount.getText().toString(),date,recurrence_type,recurrence_value,value);

        return transaction_model;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void putModelOnFields(int transaction_model_id){

        TransactionHandler lvTransactionHandler = new TransactionHandler(AddTransactionActivity.this);
        TransactionModel lvTransactionModel = lvTransactionHandler.getTransaction(transaction_model_id);

        // Return in case the model is null
        if (lvTransactionModel == null){return;}

        // Update the type we are dealing with
        mTransactionType = lvTransactionModel.getType();

        // Set the category
        mAvailableCategories =  lvTransactionHandler.getAllCategories(mTransactionType);

        List<String> arr= new ArrayList<String>();
        for (CategoriesModel cm : mAvailableCategories){
            arr.add(cm.getName());
        }
        // Set category id
        mIdOfSelectedCategory =arr.indexOf(lvTransactionModel.getCategory());

        // Set the account
        mAvailableAccounts = lvTransactionHandler.getAllAccounts(true);
        mAccount.setText(lvTransactionModel.getAccount());

        //Set the text
        mCategoryEditText.setText(lvTransactionModel.getCategory());



        mStartDateEditText.setText(this.transformDateFromDatabaseToFields(lvTransactionModel.getInitialDate()));
        mValueEditText.setText(String.valueOf(lvTransactionModel.getValue()));
        mRecurrenceValueEditText.setText(String.valueOf(lvTransactionModel.getRecurrenceValue()));


        mRecurrenceTypeSpinner.setSelection(((ArrayAdapter) mRecurrenceTypeSpinner.getAdapter()).getPosition(lvTransactionModel.getRecurrenceCategory()));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected String transformDateFromFieldsToDatabase(String date){
        final String OLD_FORMAT = "MM/dd/yy";
        final String NEW_FORMAT = "yyyy-MM-dd";

        LocalDate datetime = LocalDate.parse(date, DateTimeFormatter.ofPattern(OLD_FORMAT));
        String newstring = datetime.format(DateTimeFormatter.ofPattern(NEW_FORMAT));

        return newstring;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected String transformDateFromDatabaseToFields(String date){
        final String NEW_FORMAT = "MM/dd/yy";
        final String OLD_FORMAT = "yyyy-MM-dd";

        LocalDate datetime = LocalDate.parse(date, DateTimeFormatter.ofPattern(OLD_FORMAT));
        String newstring = datetime.format(DateTimeFormatter.ofPattern(NEW_FORMAT));

        return newstring;

    }
}