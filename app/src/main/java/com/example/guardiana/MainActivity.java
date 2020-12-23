package com.example.guardiana;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private PreferencesManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start app introduction activity - shows until permissions given
        startActivity(new Intent(this, AppIntroduction.class));

        setContentView(R.layout.activity_main);
        manager = new PreferencesManager(this);
        Button logout = findViewById(R.id.logoutButt);


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(getApplicationContext())
                        .addOnCompleteListener(task -> {
                            Log.i("TAG", "onComplete: ");
                            manager.setLoggedIn(false);
                            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                        });
            }
        });
    }

    @Override
    public void onBackPressed() {
    }
}