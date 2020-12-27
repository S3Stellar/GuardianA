package com.example.guardiana;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.guardiana.fragments.FragmentRoad;
import com.example.guardiana.fragments.FragmentSearch;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class HomeActivity extends AppCompatActivity {
    private ChipNavigationBar chipNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start app introduction activity - shows until permissions given
        //startActivity(new Intent(this, AppIntroduction.class));

        setContentView(R.layout.activity_home);

        chipNavigationBar = findViewById(R.id.bottom_nav_menu);
        chipNavigationBar.setItemSelected(R.id.bottom_nav_search, true);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentSearch()).commit();
        bottomMenu();
    }
    private void bottomMenu() {
        chipNavigationBar.setOnItemSelectedListener(id -> {
            Fragment fragment = null;
            if (R.id.bottom_nav_search == id) {
                fragment = new FragmentSearch();
            } else if(R.id.bottom_nav_map == id){
                fragment = new FragmentRoad();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        });
    }

    @Override
    public void onBackPressed() {
    }
}