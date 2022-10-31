package com.example.myapplication.mainfragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.myapplication.R;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.example.myapplication.datahandlers.adapters.RecyclerAccountsAdapter;
import com.example.myapplication.datahandlers.models.AccountModel;

import java.util.ArrayList;
import java.util.List;


public class AccountsFragment extends Fragment {

    private RecyclerView mAccountListRecyclerView;
    private List<AccountModel> mAccounts;

    public AccountsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_accounts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mAccountListRecyclerView = (RecyclerView) view.findViewById(R.id.account_list);

        updateAccountView();
    }

    public void updateAccountView(){
        dataInitialize();
        this.setTransactionAdapter(this.mAccounts);
    }

    public void setTransactionAdapter(List<AccountModel> pmAccountList){
        RecyclerAccountsAdapter adapter = new RecyclerAccountsAdapter(new ArrayList<AccountModel>(pmAccountList));

        mAccountListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAccountListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAccountListRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void dataInitialize(){
        TransactionHandler th = new TransactionHandler(getActivity());
        this.mAccounts = th.getAllAccountsBalance(true);
    }
}