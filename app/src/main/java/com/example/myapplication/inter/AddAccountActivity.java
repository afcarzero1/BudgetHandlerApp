package com.example.myapplication.inter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.example.myapplication.datahandlers.models.AccountModel;
import com.example.myapplication.datahandlers.models.CategoriesModel;

public class AddAccountActivity extends Activity {

    private EditText mInitialBalance;
    private Spinner mCurrency;
    private EditText mAccountName;
    private Button mAddButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        this.mInitialBalance = (EditText) findViewById(R.id.initial_balance_field);
        this.mCurrency = findViewById(R.id.currency_spinner);
        this.mAccountName = findViewById(R.id.account_name_field);
        this.mAddButton = findViewById(R.id.add_account_button);

        configureButton();
    }

    protected void configureButton(){

        this.mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAccount();
            }
        });


    }

    protected void addAccount(){
        if(!validateFields()){return;}

        AccountModel am;
        TransactionHandler th = new TransactionHandler(this);

        am = retrieveDataFromFields();
        boolean success = th.addAccount(am);

        if (success){
            Toast.makeText(this,"Account added", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"Error",Toast.LENGTH_LONG).show();
        }

        setResult(Activity.RESULT_OK);
        finish();
    }

    protected AccountModel retrieveDataFromFields(){

        String lvAccountName = this.mAccountName.getText().toString();
        String lvAccountCurrency = "eur";
        Float lvInitialBalance = Float.parseFloat(String.valueOf(this.mInitialBalance.getText()));

        return new AccountModel(lvAccountName,lvAccountCurrency,lvInitialBalance);
    }

    protected boolean validateFields(){
        // Check that the name is not empty
        checkNotEmpty(this.mInitialBalance);
        return checkNotEmpty(this.mAccountName) && checkNotEmpty(mInitialBalance);
    }

    private boolean checkNotEmpty(EditText field){
        if(field.getText().length()==0){
            field.setError("This field must have a value");
            return false;
        }
        return true;
    }
}