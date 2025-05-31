package com.nnthienphuc.bookdownloaderapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nnthienphuc.bookdownloaderapp.fragments.BooksFragment;
import com.nnthienphuc.bookdownloaderapp.fragments.DownloadsFragment;
import com.nnthienphuc.bookdownloaderapp.fragments.ProfileFragment;

public class MainPagerAdapter extends FragmentStateAdapter {
    public MainPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new BooksFragment();
            case 1: return new DownloadsFragment();
            default: return new ProfileFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
