package com.example.myapplication.datahandlers;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;

public class RecyclerTransactionAdapter extends RecyclerView.Adapter<RecyclerTransactionAdapter.MyViewHolder> {
    private ArrayList<TransactionModel> transactionList;

    public RecyclerTransactionAdapter(ArrayList<TransactionModel> transaction_list){
        this.transactionList =transaction_list;
    }

    public TransactionModel getItem(int position){
        return this.transactionList.get(position);
    }

    public ArrayList<TransactionModel> getArray(){
        return this.transactionList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        final private TextView category_textView;
        final private TextView type;
        final private TextView date;
        final private TextView value;
        private int id;

        public MyViewHolder(final View view ){
            super(view);
            category_textView = view.findViewById(R.id.category_text);
            date = view.findViewById(R.id.date_text);
            type = view.findViewById(R.id.type_text);
            value = view.findViewById(R.id.value_text);
            id=-1;
        }
    }


    @NonNull
    @Override
    public RecyclerTransactionAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_items,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerTransactionAdapter.MyViewHolder holder, int position) {
        // Here we bind the data to the view
        String category = transactionList.get(position).getCategory();
        String date = transactionList.get(position).getInitial_date();
        String type = transactionList.get(position).getType();
        String value = transactionList.get(position).getValue().toString();


        holder.category_textView.setText(category);
        holder.date.setText(date);
        holder.type.setText(type);
        holder.value.setText(value);
        holder.id= transactionList.get(position).getId();

    }

    @Override
    public int getItemCount() {
        // Give the size of the list
        return transactionList.size();
    }
}
