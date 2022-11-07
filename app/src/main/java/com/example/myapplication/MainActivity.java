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
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.example.myapplication.common_functionality.HideItemsInterface;
import com.example.myapplication.common_functionality.UpdateItemsInterface;
import com.example.myapplication.common_functionality.tree.Node;
import com.example.myapplication.datahandlers.models.AccountModel;
import com.example.myapplication.datahandlers.models.CategoriesModel;
import com.example.myapplication.datahandlers.models.CurrencyModel;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.example.myapplication.datahandlers.models.TransactionModel;
import com.example.myapplication.inter.AddAccountActivity;
import com.example.myapplication.inter.AddCategoryActivity;
import com.example.myapplication.inter.AddTransactionActivity;
import com.example.myapplication.inter.CategoriesActivity;
import com.example.myapplication.mainfragments.AccountsFragment;
import com.example.myapplication.mainfragments.CategoriesFragment;
import com.example.myapplication.mainfragments.InitialFragment;
import com.example.myapplication.mainfragments.MainFragmentAdapter;
import com.example.myapplication.mainfragments.TransactionsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainActivity extends AppCompatActivity implements HideItemsInterface, UpdateItemsInterface {

    private ActivityResultLauncher<Intent> mTransactionLauncher;
    private ActivityResultLauncher<Intent> mCategoryLauncher;
    private ActivityResultLauncher<Intent> mAccountLauncher;

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    MainFragmentAdapter mainFragmentAdapter;
    private FloatingActionButton btnCategory;
    private FloatingActionButton btnExpense;
    private FloatingActionButton btnIncome;


    public enum Tabs {
        HOME(0),
        TRANSACTIONS(1),
        CATEGORIES(2),
        ACCOUNTS(3);

        private final int value;

        Tabs(int value) {
            this.value = value;
        }

        String getIndex(){return String.valueOf(value);}
        int getIntIndex(){return value;}

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


        // Add root categories
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", false)) {
            TransactionHandler th = new TransactionHandler(this);
            th.addCurrency(new CurrencyModel("eur")); // todo : modify for more flexible currency support
            th.addCategory(new CategoriesModel("base",TransactionHandler.TYPE_EXPENSE,null,null));
            th.addCategory(new CategoriesModel("base",TransactionHandler.TYPE_INCOME,null,null));
        }


        //List<CategoriesModel> l = th.getAllCategories(true);

        // Configure buttons
        this.configureButtons();

        //updateTransactionView();
        this.configureViewPager();
    }

    protected void configureButtons(){
        btnCategory = (FloatingActionButton) findViewById(R.id.handle_categories_button);
        btnExpense = (FloatingActionButton) findViewById(R.id.add_expense_floating_button);
        btnIncome = (FloatingActionButton) findViewById(R.id.add_income_floating_button);

        mTransactionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    @RequiresApi(api = Build.VERSION_CODES.O)
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

        mCategoryLauncher = registerForActivityResult(
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

        mAccountLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == Activity.RESULT_OK){
                            updateAccountView();
                        }

                    }
                });

        btnExpense.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                int lvSelectedTab = tabLayout.getSelectedTabPosition();


                Optional<Tabs> val = Tabs.valueOf(lvSelectedTab);
                Tabs v =val.get();

                switch (v){
                    case ACCOUNTS:
                        launchAddAccount();
                        break;
                    case CATEGORIES:
                        launchAddCategory();
                        break;
                    default:
                        MainActivity.this.launchAddTransaction(view);
                }
            }
        });

        btnIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.launchAddTransaction(view);
            }
        });

        btnCategory.setOnClickListener(new View.OnClickListener()
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
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
                changeButtonOnTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Move the tab layout
                tabLayout.getTabAt(position).select();

                //Change buttons if necessary
                changeButtonOnTab(position);

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void changeButtonOnTab(int position){
        Optional<Tabs> val = Tabs.valueOf(position);
        Tabs v =val.get();

        switch (v){
            case TRANSACTIONS:
                unHideButton(btnIncome);
                btnExpense.setBackgroundTintList(MainActivity.this.getResources().getColorStateList(R.color.holo_red_dark));
                break;
            case CATEGORIES:
                hideButton(btnIncome,true);
                btnExpense.setBackgroundTintList(MainActivity.this.getResources().getColorStateList(R.color.holo_blue_dark));
                break;
            case ACCOUNTS:
                hideButton(btnIncome,true);
                btnExpense.setBackgroundTintList(MainActivity.this.getResources().getColorStateList(R.color.holo_purple));
                break;
            default:
                unHideButton(btnIncome);
                btnExpense.setBackgroundTintList(MainActivity.this.getResources().getColorStateList(R.color.holo_red_dark));
        }
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
            mTransactionLauncher.launch(intent);
        }catch (Exception e){
            Log.d("exception",e.toString());
        }



    }

    public void launchAddCategory(){
        Intent intent = new Intent(MainActivity.this, AddCategoryActivity.class);
        mCategoryLauncher.launch(intent);
    }

    public void launchAddAccount(){
        Intent intent = new Intent(MainActivity.this, AddAccountActivity.class);
        mAccountLauncher.launch(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void updateTransactionView(){

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateAccountView() {
        List<AccountModel> accounts = new TransactionHandler(MainActivity.this).getAllAccountsBalance(true);
        this.setAccountsAdapater(accounts);
    }

    //todo : refactor this methods using some abstract class (?)
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void setTransactionAdapter(List<TransactionModel> transactions){
        //todo : implement cleaner way for retireveing the recycler adapter
        TransactionsFragment myFragment = (TransactionsFragment)getSupportFragmentManager().findFragmentByTag("f"+Tabs.TRANSACTIONS.getIndex());
        if(myFragment != null && myFragment.isAdded()){
            myFragment.setTransactionAdapter(transactions);
        }

        InitialFragment iniFrag = (InitialFragment) getSupportFragmentManager().findFragmentByTag("f"+Tabs.HOME);
        if(iniFrag != null && iniFrag.isAdded()){
            iniFrag.updateGraph();
        }
    }

    protected void setCategoriesAdapter(List<CategoriesModel> categories){
        CategoriesFragment categoriesFragment = (CategoriesFragment) getSupportFragmentManager().findFragmentByTag("f"+Tabs.CATEGORIES.getIndex());
        if(categoriesFragment != null && categoriesFragment.isAdded()){
            categoriesFragment.setTransactionAdapter(categories);
        }
    }

    protected void setAccountsAdapater(List<AccountModel> accounts){
        AccountsFragment accountsFragment = (AccountsFragment) getSupportFragmentManager().findFragmentByTag("f"+Tabs.ACCOUNTS.getIndex());
        if(accountsFragment != null && accountsFragment.isAdded()){
            accountsFragment.setTransactionAdapter(accounts);
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

    protected void hideButton(FloatingActionButton btn,boolean permanently){
        // Hide the button
        if (btn.isShown()){
            btn.hide();
        }
    }
}