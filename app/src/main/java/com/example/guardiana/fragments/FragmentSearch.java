package com.example.guardiana.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.guardiana.PreferencesManager;
import com.example.guardiana.R;
import com.example.guardiana.SignInActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FragmentSearch extends Fragment {

    private FloatingActionButton logout;
    private PreferencesManager manager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        manager = new PreferencesManager(getActivity().getApplicationContext());
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        view.findViewById(R.id.poweroffButt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(getActivity().getApplicationContext())
                        .addOnCompleteListener(task -> {
                            Log.i("TAG", "onComplete: ");
                            manager.setLoggedIn(false);
                            startActivity(new Intent(getActivity().getApplicationContext(), SignInActivity.class));
                        });
            }
        });

        return view;
    }
}
