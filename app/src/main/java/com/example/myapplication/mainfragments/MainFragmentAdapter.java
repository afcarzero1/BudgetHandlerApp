package com.example.myapplication.mainfragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainFragmentAdapter extends FragmentStateAdapter {

    public MainFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        // position = 0 : Initial view
        // position = 1 : Transaction view

        switch (position){
            case 0:
                return new InitialFragment();
            case 1:
                return new TransactionsFragment();
            case 2:
                return new CategoriesFragment();
            default:
                // Should never happen
                return new InitialFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
