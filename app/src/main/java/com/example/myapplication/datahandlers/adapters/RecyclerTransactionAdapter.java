package com.example.myapplication.datahandlers.adapters;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.example.myapplication.datahandlers.models.TransactionModel;

import java.util.ArrayList;


//<a href="https://iconscout.com/icons/expenses" target="_blank">Expenses Icon</a> by <a href="https://iconscout.com/contributors/surang">Surangkana Jomjunyong</a> on <a href="https://iconscout.com">IconScout</a>
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
        final private TextView mCategoryTextView;
        final private TextView mAccountTextView;
        final private TextView mDateTextView;
        final private TextView mValueTextView;
        final private ImageView transaction_icon;
        private int mId;

        final private Context context;


        public MyViewHolder(final View view ){
            super(view);
            mCategoryTextView = view.findViewById(R.id.category_text);
            mDateTextView = view.findViewById(R.id.date_text);
            mAccountTextView = view.findViewById(R.id.type_text);
            mValueTextView = view.findViewById(R.id.value_text);
            transaction_icon= view.findViewById(R.id.transaction_icon_image);
            //Set icon depending on the type
            mId =-1;
            context=view.getContext();
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
        String date = transactionList.get(position).getInitialDate();
        String type = transactionList.get(position).getType();
        String value = transactionList.get(position).getValue().toString();


        holder.mCategoryTextView.setText(category);
        holder.mDateTextView.setText(date);
        holder.mAccountTextView.setText(transactionList.get(position).getAccount());
        holder.mValueTextView.setText(value);
        holder.mId = transactionList.get(position).getId();

        Drawable expense_icon = ContextCompat.getDrawable(holder.context,R.drawable.expense_icon);

        if(type.equals(TransactionHandler.TYPE_EXPENSE)){
            holder.transaction_icon.setImageDrawable(expense_icon);
        }else if(type.equals(TransactionHandler.TYPE_INCOME)){
            expense_icon = ContextCompat.getDrawable(holder.context,R.drawable.income_icon);
            holder.transaction_icon.setImageDrawable(expense_icon);
        }
    }

    @Override
    public int getItemCount() {
        // Give the size of the list
        return transactionList.size();
    }
}
