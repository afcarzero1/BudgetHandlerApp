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
    EditText et_start_date;
    EditText et_category;
    EditText et_value;
    Spinner et_recurrence_type;
    EditText et_recurrence_value;
    String transaction_type;

    private int category_id_selected;
    private List<CategoriesModel> availableCategories;

    static final String[] RECURRENCE_TYPES = {"none", "days","weeks","months"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        //Set up the correct title
        Bundle extras = getIntent().getExtras();
        if (extras!=null){
           transaction_type = extras.getString("type");
        }else{
            transaction_type = "Transaction";
        }
        category_id_selected=-1;

        TransactionHandler ch = new TransactionHandler(AddTransactionActivity.this);
        availableCategories =  ch.getAllCategories(transaction_type);

        //Retrieve objects
        fetchEditText();

        // Set up title of transaction and button
        ((TextView)findViewById(R.id.popup_title_add_expense)).setText(transaction_type);

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

        setUpCategory();
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

    protected void setUpCategory(){

        // Setup the dialog for choosing the category
        et_category.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Open Category Picker

                        AlertDialog.Builder builder = new AlertDialog.Builder(AddTransactionActivity.this);

                        //builder.setIcon(androidx.transition.R.drawable.abc_ic_star_black_36dp);
                        builder.setTitle("Choose...");

                        //todo : transfor into a recycler adapter for handling icon
                        TransactionHandler ch = new TransactionHandler(AddTransactionActivity.this);
                        List<CategoriesModel> categories = ch.getAllCategories(transaction_type);
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
                                AddTransactionActivity.this.category_id_selected=which;
                                et_category.setText(strName);
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
        et_start_date = (EditText) findViewById(R.id.popup_date_input);
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                updateLabel();
            }
        };
        et_start_date.setOnClickListener(new View.OnClickListener() {
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
        et_start_date.setText(dateFormat.format(myCalendar.getTime()));
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
        et_start_date = (EditText) findViewById(R.id.popup_date_input);
         et_category= (EditText) findViewById(R.id.popup_category_input);
         et_value= (EditText) findViewById(R.id.popup_value_input);
         et_recurrence_value= (EditText) findViewById(R.id.popup_recurrency_input);
         et_recurrence_type = (Spinner) findViewById(R.id.popup_recurrency_type_input);
    }


    protected boolean validateFields(){
        // Category, date and value must have a value!
        // The check is repeated because of the compiler optimization

        checkNotEmpty(et_category);
        checkNotEmpty(et_start_date);
        checkNotEmpty(et_value);

        return checkNotEmpty(et_category) && checkNotEmpty(et_value) && checkNotEmpty(et_start_date) && category_id_selected!=-1;
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

        String category = et_category.getText().toString();
        String date = et_start_date.getText().toString();
        // Transform date to compatible format
        date = this.transformDateFromFieldsToDatabase(date);


        String recurrence_type = et_recurrence_type.getSelectedItem().toString();
        Float recurrence_value ;
        Float value;

        // Assign dummy variable when no recurrence is required
        if(recurrence_type.equals(RECURRENCE_TYPES[0])){
            recurrence_value = (float)0.0;
        }else{
            recurrence_value = Float.parseFloat(et_recurrence_value.getText().toString());
        }

        if(et_value.getText().toString().isEmpty()){
            value = (float) 0.0;
        }else{
            value = Float.parseFloat(et_value.getText().toString());
        }
        // id used here is not real
        transaction_model = new TransactionModel(0,transaction_type,category,"",date,recurrence_type,recurrence_value,value);

        return transaction_model;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void putModelOnFields(int transaction_model_id){

        TransactionHandler db_helper = new TransactionHandler(this);
        TransactionModel transaction_model = db_helper.getTransaction(transaction_model_id);

        // Return in case the model is null
        if (transaction_model == null){return;}

        // Update the type we are dealing with
        transaction_type = transaction_model.getType();
        TransactionHandler ch = new TransactionHandler(AddTransactionActivity.this);
        availableCategories =  ch.getAllCategories(transaction_type);

        List<String> arr= new ArrayList<String>();
        for (CategoriesModel cm : availableCategories){
            arr.add(cm.getName());
        }

        //Set the text
        et_category.setText(transaction_model.getCategory());

        // Set category id
        this.category_id_selected=arr.indexOf(transaction_model.getCategory());

        et_start_date.setText(this.transformDateFromDatabaseToFields(transaction_model.getInitialDate()));
        et_value.setText(String.valueOf(transaction_model.getValue()));
        et_recurrence_value.setText(String.valueOf(transaction_model.getRecurrenceValue()));


        et_recurrence_type.setSelection(((ArrayAdapter)et_recurrence_type.getAdapter()).getPosition(transaction_model.getRecurrenceCategory()));
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