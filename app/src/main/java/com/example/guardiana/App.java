package com.example.guardiana;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.android.libraries.places.api.Places;
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

        initializePlacesApiKey();
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

    private void initializePlacesApiKey() {
        try {
            String mapApiKey = getContext().getPackageManager().getApplicationInfo(getContext().getPackageName(),
                    PackageManager.GET_META_DATA).metaData.getString("com.google.android.geo.API_KEY");
            Places.initialize(getContext(), mapApiKey);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
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
