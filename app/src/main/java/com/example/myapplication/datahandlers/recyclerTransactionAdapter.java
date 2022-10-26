package com.example.myapplication.datahandlers;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;

public class recyclerTransactionAdapter extends RecyclerView.Adapter<recyclerTransactionAdapter.MyViewHolder> {
    private ArrayList<TransactionModel> transaction_list;

    public recyclerTransactionAdapter(ArrayList<TransactionModel> transaction_list){
        this.transaction_list =transaction_list;
    }

    public TransactionModel getItem(int position){
        return this.transaction_list.get(position);
    }

    public ArrayList<TransactionModel> getArray(){
        return this.transaction_list;
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
    public recyclerTransactionAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_items,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull recyclerTransactionAdapter.MyViewHolder holder, int position) {
        // Here we bind the data to the view
        String category = transaction_list.get(position).getCategory();
        String date = transaction_list.get(position).getInitial_date();
        String type = transaction_list.get(position).getType();
        String value = transaction_list.get(position).getValue().toString();


        holder.category_textView.setText(category);
        holder.date.setText(date);
        holder.type.setText(type);
        holder.value.setText(value);
        holder.id=transaction_list.get(position).getId();

    }

    @Override
    public int getItemCount() {
        // Give the size of the list
        return transaction_list.size();
    }
}
