package com.example.guardiana;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class App extends Application {

    private static Context context;
    private PreferencesManager manager;
    private static String userId = "";

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        manager = new PreferencesManager(this);

        checkLoggedIn();
    }

    private void checkLoggedIn() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getEmail() != null && !currentUser.getEmail().isEmpty() ? currentUser.getEmail() : currentUser.getPhoneNumber();

            manager.setLoggedIn(true);
        } else {
            manager.setLoggedIn(false);
        }
    }

    public static void setUserId(String userId) {
        App.userId = userId;
    }

    public static Context getContext() {
        return context;
    }

    public static String getUserId() {
        return userId;
    }
}
