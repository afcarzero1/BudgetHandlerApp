package com.example.myapplication.datahandlers.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.datahandlers.models.AccountModel;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RecyclerAccountsAdapter extends RecyclerView.Adapter<RecyclerAccountsAdapter.MyViewHolder> {

    private ArrayList<AccountModel> mAccountList;

    public AccountModel getItem(int position){return this.mAccountList.get(position);}

    public ArrayList<AccountModel> getArray() {return this.mAccountList;}

    public RecyclerAccountsAdapter(ArrayList<AccountModel> pmAccountList){
        mAccountList = pmAccountList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_accounts,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.mBalance.setText(mAccountList.get(position).getInitialBalance().toString());
        holder.mName.setText(mAccountList.get(position).getName());
        holder.mCurrency.setText(mAccountList.get(position).getCurrency());
    }

    @Override
    public int getItemCount() {
        return mAccountList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        final private TextView mName;
        final private TextView mCurrency;
        final private TextView mBalance;


        public MyViewHolder(final View view){
            super(view);
            mName = (TextView) view.findViewById(R.id.account_name);
            mCurrency = (TextView) view.findViewById(R.id.currency);
            mBalance = (TextView) view.findViewById(R.id.initial_balance);
        }
    }
}
