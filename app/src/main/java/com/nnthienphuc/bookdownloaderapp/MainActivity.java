package com.nnthienphuc.bookdownloaderapp;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nnthienphuc.bookdownloaderapp.adapters.MainPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        int themeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        bottomNav = findViewById(R.id.bottomNav);

        viewPager.setAdapter(new MainPagerAdapter(this));

        int defaultTab = getIntent().getIntExtra("start_tab", 0);
        viewPager.setCurrentItem(defaultTab, false);

        // Bỏ listener tạm
        bottomNav.setOnItemSelectedListener(null);

        // Gán icon đúng tab
        if (defaultTab == 0) bottomNav.setSelectedItemId(R.id.nav_all_books);
        else if (defaultTab == 1) bottomNav.setSelectedItemId(R.id.nav_downloaded);
        else if (defaultTab == 2) bottomNav.setSelectedItemId(R.id.nav_profile);

        // Gắn lại listener sau khi sync
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_all_books:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.nav_downloaded:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.nav_profile:
                    viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        });

//        bottomNav.setOnItemSelectedListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.nav_all_books:
//                    viewPager.setCurrentItem(0);
//                    return true;
//                case R.id.nav_downloaded:
//                    viewPager.setCurrentItem(1);
//                    return true;
//                case R.id.nav_profile:
//                    viewPager.setCurrentItem(2);
//                    return true;
//            }
//            return false;
//        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNav.setSelectedItemId(R.id.nav_all_books);
                        break;
                    case 1:
                        bottomNav.setSelectedItemId(R.id.nav_downloaded);
                        break;
                    case 2:
                        bottomNav.setSelectedItemId(R.id.nav_profile);
                        break;
                }
            }
        });
    }
}
