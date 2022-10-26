package com.example.myapplication.mainfragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.RecyclerItemClickListener;
import com.example.myapplication.common_functionality.HideItemsInterface;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.example.myapplication.datahandlers.TransactionModel;
import com.example.myapplication.datahandlers.RecyclerTransactionAdapter;
import com.example.myapplication.inter.EditTransactionActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransactionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionsFragment extends Fragment {

    private RecyclerView transactionListRecyclerView;
    private List<TransactionModel> transactions;
    private ActivityResultLauncher<Intent> launcher;

    private HideItemsInterface hideItemsInterface;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment TransactionsFragment.
     */
    public static TransactionsFragment newInstance() {
        TransactionsFragment fragment = new TransactionsFragment();
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        hideItemsInterface = (HideItemsInterface)context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve data from database
        this.dataInitialize();

        // Find the recycler view
        this.transactionListRecyclerView = view.findViewById(R.id.all_transaction_recycler_view);
        //transactionListRecyclerView.setHasFixedSize(true);

        //
        this.setTransactionAdapter(this.transactions);
        this.configureRecyclerView();
    }

    protected void updateTransactionView(){
        dataInitialize();
        this.setTransactionAdapter(this.transactions);
    }

    protected void configureRecyclerView(){

        //Define behaviour after item is pressed
        this.transactionListRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), this.transactionListRecyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // Open a information window about the transaction pressed by user.
                        RecyclerTransactionAdapter ra = (RecyclerTransactionAdapter) transactionListRecyclerView.getAdapter();
                        if (ra != null) {
                            TransactionModel model = ra.getItem(position);
                            if (model != null){
                                // Open edit activity

                                //Toast.makeText(MainActivity.this,"Pressed",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), EditTransactionActivity.class);
                                // Put the id
                                intent.putExtra("id",model.getId());
                                launcher.launch(intent);
                            }
                        }
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // Get the item id
                        RecyclerTransactionAdapter ra = (RecyclerTransactionAdapter) TransactionsFragment.this.transactionListRecyclerView.getAdapter();
                        if (ra != null) {
                            TransactionModel model = ra.getItem(position);
                            final int transaction_id=model.getId();
                            // When long click is pressed delete the transaction
                            new AlertDialog.Builder(getActivity())
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Deleting Transaction")
                                    .setMessage("Are you sure you want to delete this transaction?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            TransactionHandler db_helper = new TransactionHandler(getActivity());
                                            final boolean success = db_helper.deleteTransaction(transaction_id);
                                            if(success){
                                                Toast.makeText(getActivity(),"Successfully deleted",Toast.LENGTH_SHORT).show();
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

        // Define launcher behaviour for coming back from activities that modify database of transactions
        // Update the view
        this.launcher = registerForActivityResult(
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


        // Define behaviour when user reaches end of the list. Send message to upper class to hide overlapping items
        this.transactionListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy >0) {
                    // Scroll Down
                    hideItemsInterface.hideItems();
                }
                else if (dy <0) {
                    hideItemsInterface.unHideItems();
                }
            }
        });
    }

    protected void setTransactionAdapter(List<TransactionModel> transactions){
        RecyclerTransactionAdapter adapter = new RecyclerTransactionAdapter(new ArrayList<TransactionModel>(transactions));
        transactionListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        transactionListRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void dataInitialize() {
        // Create an instance of the database manager using the activity associated with the fragment
        // CALL THIS ONLY AFTER ON_ATTACH HAS BEEN CALLED
        TransactionHandler th = new TransactionHandler(getActivity());

        // Get all the transactions stored in the database ordered by date and store
         this.transactions = th.getAllTransactions(true);
    }
}