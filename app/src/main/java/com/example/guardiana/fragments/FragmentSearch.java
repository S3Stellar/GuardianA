package com.example.guardiana.fragments;

import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guardiana.App;
import com.example.guardiana.PreferencesManager;
import com.example.guardiana.SignInActivity;
import com.example.guardiana.adapters.AddressAdapter;
import com.example.guardiana.databinding.FragmentSearchBinding;
import com.example.guardiana.model.Address;
import com.example.guardiana.model.Location;
import com.example.guardiana.repository.AddressResponse;
import com.example.guardiana.viewmodel.AddressViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class FragmentSearch extends Fragment {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 991;
    private FragmentSearchBinding fragmentSearchBinding;
    private AddressViewModel addressViewModel;
    private AddressAdapter addressAdapter;
    private PreferencesManager manager;
    private final int PAGE_SIZE = 5;
    private int currentPage = 0;
    private int offset = 0;
    private static int count = 0;
    private Runnable scrollRunnable;
    private Observer<AddressResponse> observer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragmentSearchBinding = FragmentSearchBinding.inflate(getLayoutInflater(), container, false);
        setupRecyclerView();

        addressViewModel = new ViewModelProvider(requireActivity()).get(AddressViewModel.class);

        observer = response -> {
            Log.i(TAG, "onCreateView: " + response.getStatusCode());
            if (response.getStatusCode() == 200) {
                scrollRunnable = response.getFlag() == 0 ? null : () -> fragmentSearchBinding.recyclerView.smoothScrollToPosition(0);
                addressAdapter.submitList(response.getAddressList(), scrollRunnable);
            } else {
                new SweetAlertDialog(getContext()).setTitleText("Error").setContentText(response.getMessage()).show();
            }
        };

        addressViewModel.getAllAddresses(App.getUserId(), "", "", "", "", currentPage, PAGE_SIZE, offset).observe(requireActivity(), observer);


        fragmentSearchBinding.addButton.setOnClickListener(v -> {
            Log.i(TAG, "onCreateView: " + App.getUserId());
            addressViewModel.create(
                    new Address(App.getUserId(), "afff", "lotus" + count++, new Date(), new Location(55, 32))).observe(requireActivity(),
                    observer);
        });


        fragmentSearchBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    currentPage = addressAdapter.getCurrentList().size() / PAGE_SIZE;
                    offset = addressAdapter.getCurrentList().size() % PAGE_SIZE; // num of addresses to delete from end
                    addressViewModel.getAllAddresses(App.getUserId(), "", "", "", "", currentPage, PAGE_SIZE, offset).observe(requireActivity(), observer);
                }
            }
        });

        fragmentSearchBinding.welcomeText.setText(String.format("Hello %s !", App.getUserId()));
        setupPowerOffButton();

        addressAdapter.setOnItemClickListener(position -> {
            Log.i("TAG", "onItemClick: " + position);
        });

        cardSwipeListener();
        fragmentSearchBinding.searchBar.setFocusable(false);
        fragmentSearchBinding.searchBar.setOnClickListener(this::startAutocompleteActivity);
        return fragmentSearchBinding.getRoot();
    }

    private void cardSwipeListener() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                int position = viewHolder.getAdapterPosition();
                Address undoSave = addressAdapter.getCurrentList().get(position);
                addressViewModel.delete(undoSave.getId(),position);
                //Snackbar.make(recyclerview, "Preference Deleted", LENGTH_LONG).setAction("Undo", new UndoListener(undoSave)).show();

            }
        }).attachToRecyclerView(fragmentSearchBinding.recyclerView);
    }

//    private class UndoListener implements View.OnClickListener {
//        private final SavedPref undoSave;
//
//        public UndoListener(SavedPref undoSave) {
//            this.undoSave = undoSave;
//        }
//
//        @Override
//        public void onClick(View view) {
//            myListViewModel.addSavedPref(undoSave);
//        }
//    }

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
                LatLng latLng = place.getLatLng();
                double myLat = latLng.latitude;
                double myLng = latLng.longitude;
                String cityName = "";
                try {
                    List<android.location.Address> addresses = new Geocoder(getContext(), Locale.getDefault()).getFromLocation(myLat, myLng, 1);
                    cityName = addresses.get(0).getLocality();
                } catch (IOException e) {
                    e.printStackTrace();

                }
                addressViewModel.create(
                        new Address(App.getUserId(), place.getName(), cityName, new Date(), new Location(myLat, myLng))).observe(requireActivity(),
                        observer);

             /*   addressFirebaseRepository.save(new Address(cityName, place.getName(), new Location(myLat, myLng),
                        new Date()), data1 -> {
                    addressList.addAll(0, data1);
                    addressAdapter.notifyDataSetChanged();
                }, e -> {

                });*/
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
        fragmentSearchBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fragmentSearchBinding.recyclerView.setHasFixedSize(true);
        addressAdapter = new AddressAdapter();
        fragmentSearchBinding.recyclerView.setAdapter(addressAdapter);
    }

    private void setupPowerOffButton() {
        manager = new PreferencesManager(App.getContext());
        fragmentSearchBinding.poweroffButt.setOnClickListener(v -> AuthUI.getInstance()
                .signOut(getContext())
                .addOnCompleteListener(task -> {
                    manager.setLoggedIn(false);
                    startActivity(new Intent(getContext(), SignInActivity.class));
                    getActivity().finish();
                    //addressFirebaseRepository.setLastResult(null);
                    //addressFirebaseRepository = AddressFirebaseRepository.getInstance();
                }));

    }
}
