package com.example.guardiana;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.guardiana.fragments.FragmentRoad;
import com.example.guardiana.fragments.FragmentSearch;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class HomeActivity extends AppCompatActivity {
    private ChipNavigationBar chipNavigationBar;

    private FragmentSearch fragmentSearch;
    private FragmentRoad fragmentRoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Start app introduction activity - shows until permissions given
        startActivity(new Intent(this, AppIntroduction.class));

        setContentView(R.layout.activity_home);
        fragmentRoad = new FragmentRoad();
        fragmentSearch = new FragmentSearch();

        addFragments();

        chipNavigationBar = findViewById(R.id.bottom_nav_menu);
        chipNavigationBar.setItemSelected(R.id.bottom_nav_search, true);

        bottomMenu();
    }

    private void addFragments() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragmentRoad)
                .add(R.id.fragment_container, fragmentSearch)
                .hide(fragmentRoad)
                .show(fragmentSearch).commit();
    }

    private void bottomMenu() {
        chipNavigationBar.setOnItemSelectedListener(id -> {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (R.id.bottom_nav_search == id) {
                 ft.setCustomAnimations(
                         R.anim.slide_in,
                         R.anim.slide_out
                 );
                ft.show(fragmentSearch);
                ft.hide(fragmentRoad);
            } else if (R.id.bottom_nav_map == id) {
                ft.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
                ft.show(fragmentRoad);
                ft.hide(fragmentSearch);
            }
            ft.commit();
        });
    }

    @Override
    public void onBackPressed() {
    }
}