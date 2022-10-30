package com.example.myapplication.datahandlers.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import com.example.myapplication.datahandlers.models.CategoriesModel;

import java.util.ArrayList;

public class recyclerCategoriesAdapter extends RecyclerView.Adapter<recyclerCategoriesAdapter.MyViewHolder>{

    private ArrayList<CategoriesModel> categories_list;


    public recyclerCategoriesAdapter(ArrayList<CategoriesModel> categories_list){
        this.categories_list=categories_list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{

        final private TextView category_name;
        final private TextView category_type;

        public MyViewHolder(final View view){
            super(view);
            category_name = view.findViewById(R.id.category_name);
            category_type = view.findViewById(R.id.type_category_text);
        }
    }

    public CategoriesModel getItem(int position){
        return this.categories_list.get(position);
    }


    @NonNull
    @Override
    public recyclerCategoriesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_categories,parent,false);
        return new MyViewHolder(item_view);
    }

    @Override
    public void onBindViewHolder(@NonNull recyclerCategoriesAdapter.MyViewHolder holder, int position) {
        // Set data
        holder.category_name.setText(categories_list.get(position).getName());
        holder.category_type.setText(categories_list.get(position).getType());
    }

        @Override
    public int getItemCount(){
        return categories_list.size();
    }

}