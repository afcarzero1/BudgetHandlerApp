package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.myapplication.common_functionality.HideItemsInterface;
import com.example.myapplication.datahandlers.models.AccountModel;
import com.example.myapplication.datahandlers.models.CategoriesModel;
import com.example.myapplication.datahandlers.models.CurrencyModel;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.example.myapplication.datahandlers.TransactionModel;
import com.example.myapplication.inter.AddCategoryActivity;
import com.example.myapplication.inter.AddTransactionActivity;
import com.example.myapplication.inter.CategoriesActivity;
import com.example.myapplication.mainfragments.CategoriesFragment;
import com.example.myapplication.mainfragments.MainFragmentAdapter;
import com.example.myapplication.mainfragments.TransactionsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainActivity extends AppCompatActivity implements HideItemsInterface {

    private ActivityResultLauncher<Intent> launcher_transaction;
    private ActivityResultLauncher<Intent> launcher_categories;

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    MainFragmentAdapter mainFragmentAdapter;


    public enum Tabs {
        HOME(0),
        TRANSACTIONS(1),
        CATEGORIES(2);

        private final int value;

        Tabs(int value) {
            this.value = value;
        }

        String getIndex(){return String.valueOf(value);}

        @RequiresApi(api = Build.VERSION_CODES.N)
        public static Optional<Tabs> valueOf(int value) {
            return Arrays.stream(values())
                    .filter(tab -> tab.value == value)
                    .findFirst();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the layout and title
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Budget Handler");

        TransactionHandler th = new TransactionHandler(this);
        th.addCurrency(new CurrencyModel("eur"));
        List<AccountModel> a = th.getAllAccounts(true);
        th.addAccount(new AccountModel("test","eur",(float)0));
        a = th.getAllAccounts(true);

        // Add root categories
        th.addCategory(new CategoriesModel("base",TransactionHandler.TYPE_EXPENSE,null,null));
        th.addCategory(new CategoriesModel("base",TransactionHandler.TYPE_INCOME,null,null));

        List<CategoriesModel> l = th.getAllCategories(true);

        // Configure buttons
        this.configureButtons();

        //updateTransactionView();
        configureViewPager();
    }

    protected void configureButtons(){
        FloatingActionButton btn_category = (FloatingActionButton) findViewById(R.id.handle_categories_button);
        FloatingActionButton btn_expense = (FloatingActionButton) findViewById(R.id.add_expense_floating_button);
        FloatingActionButton btn_income = (FloatingActionButton) findViewById(R.id.add_income_floating_button);

        launcher_transaction = registerForActivityResult(
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

        launcher_categories = registerForActivityResult(
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

        btn_expense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tabLayout.getSelectedTabPosition() == 2){
                    launchAddCategory();
                    return;
                }

                MainActivity.this.launchAddTransaction(view);
            }
        });

        btn_income.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.launchAddTransaction(view);
            }
        });

        btn_category.setOnClickListener(new View.OnClickListener()
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

    protected void configureViewPager(){
        this.tabLayout = findViewById(R.id.main_tab);
        this.viewPager2 = findViewById(R.id.main_view_pager);


        mainFragmentAdapter = new MainFragmentAdapter(this);
        viewPager2.setAdapter(mainFragmentAdapter);


        // Setup the tab layout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Move the tab layout
                tabLayout.getTabAt(position).select();


                //Change buttons if necessary

            }
        });

    }

    public void launchAddTransaction(View view) {
        //Check what type of transaction is it and launch activity

        // Define launcher for updating list on completion
        try {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            if(view.getId() == R.id.add_expense_floating_button){
                intent.putExtra("type", TransactionHandler.TYPE_EXPENSE);
            }else if (view.getId() == R.id.add_income_floating_button){
                intent.putExtra("type",TransactionHandler.TYPE_INCOME);
            }
            // Launch the activity
            launcher_transaction.launch(intent);
        }catch (Exception e){
            Log.d("exception",e.toString());
        }



    }

    public void launchAddCategory(){
        Intent intent = new Intent(MainActivity.this, AddCategoryActivity.class);
        launcher_categories.launch(intent);
    }

    protected void updateTransactionView(){

        // Create an instance of the database manager
        TransactionHandler th = new TransactionHandler(MainActivity.this);

        // Get all the transactions stored in the database ordered by date
        List<TransactionModel> transactions = th.getAllTransactions(true);

        // Update the recycler view
        setTransactionAdapter(transactions);

    }

    protected void updateCategoriesView(){

        List<CategoriesModel> categories = new TransactionHandler(MainActivity.this).getAllCategories(true);

        this.setCategoriesAdapter(categories);

    }

    protected void setTransactionAdapter(List<TransactionModel> transactions){
        //todo : implement cleaner way for retireveing the recycler adapter
        TransactionsFragment myFragment = (TransactionsFragment)getSupportFragmentManager().findFragmentByTag("f"+Tabs.TRANSACTIONS.getIndex());
        if(myFragment != null && myFragment.isAdded()){
            myFragment.setTransactionAdapter(transactions);
        }

    }

    protected void setCategoriesAdapter(List<CategoriesModel> categories){
        CategoriesFragment categoriesFragment = (CategoriesFragment) getSupportFragmentManager().findFragmentByTag("f"+Tabs.CATEGORIES.getIndex());
        if(categoriesFragment != null && categoriesFragment.isAdded()){
            categoriesFragment.setTransactionAdapter(categories);
        }
    }

    @Override
    public void hideItems() {
        // The only floating itmes we have are the floating buttons
        FloatingActionButton eb = findViewById(R.id.add_expense_floating_button);
        FloatingActionButton ib = findViewById(R.id.add_income_floating_button);
        FloatingActionButton cb = findViewById(R.id.handle_categories_button);

        hideButton(eb);
        hideButton(ib);
        hideButton(cb);
    }

    @Override
    public void unHideItems() {
        FloatingActionButton eb = findViewById(R.id.add_expense_floating_button);
        FloatingActionButton ib = findViewById(R.id.add_income_floating_button);
        FloatingActionButton cb = findViewById(R.id.handle_categories_button);

        unHideButton(eb);
        unHideButton(ib);
        unHideButton(cb);
    }

    protected void unHideButton(FloatingActionButton btn){
        if(!btn.isShown()){btn.show();}
    }

    protected void hideButton(FloatingActionButton btn){
        // Hide the button and make it reappear after 5 seconds
        if (btn.isShown()){
            btn.hide();
            btn.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btn.show();
                }
            },5000);
        }
    }
}