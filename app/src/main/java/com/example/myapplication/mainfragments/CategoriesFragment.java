package com.example.myapplication.mainfragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.RecyclerItemClickListener;
import com.example.myapplication.datahandlers.TransactionHandler;
import com.example.myapplication.datahandlers.models.CategoriesModel;
import com.example.myapplication.datahandlers.adapters.recyclerCategoriesAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoriesFragment extends Fragment {

    private RecyclerView categoriesListRecyclerView;
    private List<CategoriesModel> categories;


    public CategoriesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CategoriesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoriesFragment newInstance(String param1, String param2) {
        CategoriesFragment fragment = new CategoriesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.categoriesListRecyclerView = view.findViewById(R.id.recycler_view_categories);
        configureDeleteFunctionality();
        updateCategoriesView();
    }


    protected void configureDeleteFunctionality(){
        categoriesListRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), categoriesListRecyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {

                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // Get the item id
                        recyclerCategoriesAdapter ra = (recyclerCategoriesAdapter) categoriesListRecyclerView.getAdapter();
                        if (ra != null) {
                            CategoriesModel model = ra.getItem(position);

                            final String categoryName = model.getName();
                            final String categoryType = model.getType();
                            // When long click is pressed delete the transaction
                            new AlertDialog.Builder(getActivity())
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Deleting Category")
                                    .setMessage("Are you sure you want to delete this category?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            TransactionHandler db = new TransactionHandler(getActivity());
                                            try {
                                                final boolean success = db.deleteCategory(categoryName,categoryType);
                                                if(success){
                                                    Toast.makeText(getActivity(),"Successfully deleted",Toast.LENGTH_SHORT).show();
                                                }else{Toast.makeText(getActivity(),"Deletion Failed",Toast.LENGTH_LONG).show();}
                                            }catch (Exception e){
                                                Toast.makeText(getActivity(),"Deletion Failed",Toast.LENGTH_LONG).show();
                                            }
                                            updateCategoriesView();
                                        }
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        }

                    }
                })
        );
    }

    protected void updateCategoriesView(){
        this.dataInitialize();
        this.setTransactionAdapter(this.categories);
    }

    public void setTransactionAdapter(List<CategoriesModel> categories){
        recyclerCategoriesAdapter adapter = new recyclerCategoriesAdapter(new ArrayList<CategoriesModel>(categories));

        categoriesListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        categoriesListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        categoriesListRecyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
    }

    protected void dataInitialize(){
        TransactionHandler ch = new TransactionHandler(getActivity());
        this.categories = ch.getAllCategories(true);
    }
}