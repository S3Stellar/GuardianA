package com.example.guardiana.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guardiana.App;
import com.example.guardiana.PreferencesManager;
import com.example.guardiana.R;
import com.example.guardiana.SignInActivity;
import com.example.guardiana.adapters.AddressAdapter;
import com.example.guardiana.model.Address;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class FragmentSearch extends Fragment {

    private View view;
    private FloatingActionButton logout;
    private PreferencesManager manager;
    private RecyclerView recyclerView;
    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);
        setupPowerOffButton();
        setupRecyclerView();
        for(int i = 0; i < 35; i++){
            addressList.add(new Address("Heifa", "Some street address " + i));
        }
        addressAdapter.notifyDataSetChanged();
        addressAdapter.setOnItemClickListener(new AddressAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //Toast.makeText(getContext(), "Item at position: " + position + " pressed", Toast.LENGTH_SHORT).show();
                Log.i("TAG", "onItemClick: " + position);
            }
        });
        return view;

    }

    private void setupRecyclerView() {
        recyclerView = view.findViewById(R.id.fragment_search_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(addressAdapter = new AddressAdapter(addressList = new ArrayList<>()));
        addressAdapter.notifyDataSetChanged();
    }
    private void setupPowerOffButton() {
        manager = new PreferencesManager(App.getContext());
        view.findViewById(R.id.poweroffButt).setOnClickListener(v -> AuthUI.getInstance()
                .signOut(getActivity().getApplicationContext())
                .addOnCompleteListener(task -> {
                    Log.i("TAG", "onComplete: ");
                    manager.setLoggedIn(false);
                    startActivity(new Intent(getActivity().getApplicationContext(), SignInActivity.class));
                }));

    }
}
