package com.example.myapplication.inter;


import com.example.myapplication.R;
import com.example.myapplication.datahandlers.CategoriesHandler;
import com.example.myapplication.datahandlers.CategoriesModel;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AddCategoryActivity extends Activity {

    private EditText et_category_name;
    private ToggleButton tb_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        this.setTitle("Add category");
        this.et_category_name = (EditText) findViewById(R.id.category_name_edit_text);
        this.tb_type = (ToggleButton) findViewById(R.id.toggleButton);
        configureButton();
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
        CategoriesHandler ch = new CategoriesHandler(AddCategoryActivity.this);


        try {
            cm = retrieveDataFromFields();
            ch.addCategory(cm);
            Toast.makeText(AddCategoryActivity.this,"Category added", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
            finish();
        }catch (Exception e){
            Toast.makeText(AddCategoryActivity.this,"Error",Toast.LENGTH_LONG).show();
        }



    }

    protected boolean validateFields(){
        // Check that the name is not empty
        return checkNotEmpty(this.et_category_name);
    }

    protected CategoriesModel retrieveDataFromFields(){
        return new CategoriesModel(-1, this.et_category_name.getText().toString(),tb_type.getText().toString());
    }


    private boolean checkNotEmpty(EditText field){
        if(field.getText().length()==0){
            field.setError("This field must have a value");

            return false;
        }
        return true;
    }

}