package com.example.guardiana.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.example.guardiana.App;
import com.example.guardiana.PreferencesManager;
import com.example.guardiana.R;
import com.example.guardiana.SignInActivity;
import com.example.guardiana.adapters.AddressAdapter;
import com.example.guardiana.listeners.OnDataCompleteListener;
import com.example.guardiana.listeners.OnDataErrorListener;
import com.example.guardiana.model.Address;
import com.example.guardiana.pageable.Direction;
import com.example.guardiana.pageable.PageRequest;
import com.example.guardiana.repository.AddressFirebaseRepository;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class FragmentSearch extends Fragment {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 991;
    private View view;
    private FloatingActionButton logout;
    private PreferencesManager manager;
    private RecyclerView recyclerView;
    private AddressAdapter addressAdapter;
    private List<com.example.guardiana.model.Address> addressList;
    private EditText mSearchText;
    private AddressFirebaseRepository addressFirebaseRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);
        mSearchText = view.findViewById(R.id.search_bar);
        addressFirebaseRepository = AddressFirebaseRepository.getInstance();

        setupPowerOffButton();
        setupRecyclerView();
        loadAddressesToRecycleView();
        initializePlacesApiKey();

        addressAdapter.setOnItemClickListener(position -> {
            //Toast.makeText(getContext(), "Item at position: " + position + " pressed", Toast.LENGTH_SHORT).show();
            Log.i("TAG", "onItemClick: " + position);
        });

        mSearchText.setFocusable(false);
        mSearchText.setOnClickListener(this::startAutocompleteActivity);
        return view;

    }

    private void loadAddressesToRecycleView() {
        addressFirebaseRepository.findAll(PageRequest.of(5, "date", Query.Direction.DESCENDING), data -> {
            addressList.addAll(data);
            addressAdapter.notifyDataSetChanged();
        }, e -> {
        });
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

    public void startAutocompleteActivity(View view) {
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY,
                Arrays.asList(Place.Field.ID, Place.Field.NAME,
                        Place.Field.ADDRESS, Place.Field.LAT_LNG))
                .setTypeFilter(TypeFilter.ADDRESS)
                .setLocationBias(RectangularBounds.newInstance(
                        new LatLng(29.2545, 34.5737),
                        new LatLng(33.4055, 35.8481)
                ))
                .setCountries(Collections.singletonList("IL"))
                .build(getContext());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "onActivityResult: Place: " + place.getName() + ", address: " + place.getAddress());

                LatLng latLng = place.getLatLng();
                double myLat = latLng.latitude;
                double myLng = latLng.longitude;
                String cityName = "";
                try {
                    List<android.location.Address> addresses = new Geocoder(getContext(), Locale.getDefault()).getFromLocation(myLat, myLng, 1);
                    cityName = addresses.get(0).getLocality();
                    Log.i(TAG, "cityName = " + cityName);
                } catch (IOException e) {
                    e.printStackTrace();

                }
                addressFirebaseRepository.save(new Address(cityName, place.getName(), myLat, myLng, new Date()), data1 -> {
                    //loadAddressesToRecycleView();
                    addressList.addAll(0, data1);
                    addressAdapter.notifyDataSetChanged();
                }, e -> {

                });
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, "onActivityResult: " + status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void setupRecyclerView() {
        recyclerView = view.findViewById(R.id.fragment_search_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(addressAdapter = new AddressAdapter(addressList = new ArrayList<>()));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("-----","end");
                    loadAddressesToRecycleView();
                }
            }
        });

        addressAdapter.notifyDataSetChanged();
    }

    private void setupPowerOffButton() {
        manager = new PreferencesManager(App.getContext());
        view.findViewById(R.id.poweroffButt).setOnClickListener(v -> AuthUI.getInstance()
                .signOut(getContext())
                .addOnCompleteListener(task -> {
                    Log.i("TAG", "onComplete: ");
                    manager.setLoggedIn(false);
                    startActivity(new Intent(getContext(), SignInActivity.class));
                }));

    }
}
