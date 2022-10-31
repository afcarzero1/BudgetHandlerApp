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

    private ArrayList<CategoriesModel> mCategoriesList;


    public recyclerCategoriesAdapter(ArrayList<CategoriesModel> categories_list){
        this.mCategoriesList =categories_list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{

        final private TextView mCategoryName;
        final private TextView mCategoryType;
        final private TextView mParentCategoryname;

        public MyViewHolder(final View view){
            super(view);
            mCategoryName = view.findViewById(R.id.category_name);
            mCategoryType = view.findViewById(R.id.type_category_text);
            mParentCategoryname = view.findViewById(R.id.category_parent_text_view);
        }
    }

    public CategoriesModel getItem(int position){
        return this.mCategoriesList.get(position);
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
        holder.mCategoryName.setText(mCategoriesList.get(position).getName());
        holder.mCategoryType.setText(mCategoriesList.get(position).getType());
        String parentName;
        if(mCategoriesList.get(position).getParentName()==null){holder.mParentCategoryname.setText("");}else{
            holder.mParentCategoryname.setText(mCategoriesList.get(position).getParentName());
        }
    }

        @Override
    public int getItemCount(){
        return mCategoriesList.size();
    }

}
