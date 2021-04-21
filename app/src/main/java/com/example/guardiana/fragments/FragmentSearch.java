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
import com.example.guardiana.customViews.concretecustomviews.bottommenu.BottomMenuService;
import com.example.guardiana.customViews.resources.BottomSheetAddressMenuResource;
import com.example.guardiana.customViews.resources.BottomSheetFavoriteResource;
import com.example.guardiana.databinding.FragmentSearchBinding;
import com.example.guardiana.dialogs.BottomSheetMenuDialog;
import com.example.guardiana.model.Address;
import com.example.guardiana.model.Location;
import com.example.guardiana.repository.AddressResponse;
import com.example.guardiana.utility.StatusCode;
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
    private final int PAGE_SIZE = 3;
    private int currentPage = 0, offset = 0;
    private static int count = 0;
    private Runnable scrollRunnable;
    private Observer<AddressResponse> observer;
    private LinearLayoutManager layoutManager;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Bind the view to the fragment
        fragmentSearchBinding = FragmentSearchBinding.inflate(getLayoutInflater(), container, false);

        // Setup views
        setupRecyclerView();
        setupPowerOffButton();
        setupRemoveAddress();
        setupSearchBar();
        setWelcomeHeader();

        // Setup and initialize the view model to the current fragment
        initViewModel();

        // Set observer
        observer = setObserver();

        // Initialize the first address page
        initAddressPage();

        // Initialize scroll listener
        initScrollListener();

        //TODO: only for testing
        testAddPage();

        setupSelectionAction();
        return fragmentSearchBinding.getRoot();
    }

    /**
     * Initialize the first page address and observes to changes
     */
    private void initAddressPage() {
        Log.i(TAG, "initAddressPage: " + App.getUserId());
        addressViewModel.getAllAddressesByPriority(App.getUserId(), currentPage, PAGE_SIZE, offset, "0").observe(requireActivity(), observer);
    }

    /**
     * Initialize the scroll listener which return the next available page
     */
    private void initScrollListener() {
        fragmentSearchBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    currentPage = addressAdapter.getCurrentList().size() / PAGE_SIZE;
                    offset = addressAdapter.getCurrentList().size() % PAGE_SIZE; // num of addresses to delete from end
                    addressViewModel.getAllAddressesByPriority(App.getUserId(), currentPage, PAGE_SIZE, offset, "0").observe(requireActivity(), observer);
                }
            }
        });

    }

    /**
     * Setup swipe listener which triggered when the user swipe a cell and remove the item from the list
     */
    private void setupRemoveAddress() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    addressViewModel.delete(addressAdapter.getAddressPositionAt(position)).observe(requireActivity(), observer);
                }
            }
        }).attachToRecyclerView(fragmentSearchBinding.recyclerView);
    }


    /**
     * Set an observer which gets triggered when an action(add, remove, scroll) occurred on the recycler view
     *
     * @return Observer<AddressResponse>
     */
    private Observer<AddressResponse> setObserver() {
        return (response) -> {
            fragmentSearchBinding.recyclerView.getRecycledViewPool().clear();
            if (String.valueOf(response.getStatusCode()).matches("20[0-9]")) {
                Log.i(TAG, "setObserver: ");
//                layoutManager.scrollToPositionWithOffset(0,0);
                scrollRunnable = response.getFlag() == 0 ? null : () ->layoutManager.scrollToPositionWithOffset(0,0); /*fragmentSearchBinding.recyclerView.smoothScrollToPosition(0);*/
                addressAdapter.submitList(response.getAddressList(), scrollRunnable);
            } else {
                new SweetAlertDialog(getContext()).setTitleText("Error").setContentText(response.getMessage()).show();
            }
        };
    }

    /**
     * Initialize the view model and attached it to the lifecycle of the current fragment
     */
    private void initViewModel() {
        addressViewModel = new ViewModelProvider(requireActivity()).get(AddressViewModel.class);
    }

    /**
     * Setup the Welcome header with the current active user
     */
    private void setWelcomeHeader() {
        fragmentSearchBinding.welcomeText.setText(String.format("Hello %s !", App.getUserId()));
    }

    /**
     * setup the search bar which open an new intent for search address
     */
    private void setupSearchBar() {
        fragmentSearchBinding.searchBar.setFocusable(false);
        fragmentSearchBinding.searchBar.setOnClickListener(this::startAutocompleteActivity);
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
                        new Address(App.getUserId(), place.getName(), cityName, new Date(), new Location(myLat, myLng), 7)).observe(requireActivity(),
                        observer);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, "onActivityResult: " + status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    /**
     * Setup and config the recycler view
     */
    private void setupRecyclerView() {
        fragmentSearchBinding.recyclerView.setLayoutManager(layoutManager = new LinearLayoutManager(getContext()));
        fragmentSearchBinding.recyclerView.setHasFixedSize(true);
        addressAdapter = new AddressAdapter();
        fragmentSearchBinding.recyclerView.setAdapter(addressAdapter);
    }

    /**
     * Setup the power off button which close the current user session
     */
    private void setupPowerOffButton() {
        manager = new PreferencesManager(App.getContext());
        fragmentSearchBinding.poweroffButt.setOnClickListener(v -> AuthUI.getInstance()
                .signOut(getContext())
                .addOnCompleteListener(task -> {
                    manager.setLoggedIn(false);
                    startActivity(new Intent(getContext(), SignInActivity.class));
                    Log.i(TAG, "setupPowerOffButton: " + getActivity());
                    getActivity().finish();
                }));

    }

    private void setupSelectionAction() {
        fragmentSearchBinding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    //TODO: only for testing
    private void testAddPage() {
        fragmentSearchBinding.addButton.setOnClickListener(v -> {
            addressViewModel.create(
                    new Address(App.getUserId(), "address " + count, "lotus" + count++, new Date(), new Location(55, 32), 7)).observe(requireActivity(),
                    observer);
        });

        addressAdapter.setOnItemClickListener(position -> {
            BottomSheetMenuDialog bottomSheetDialog = new BottomSheetMenuDialog
                    .Builder()
                    .setHeader(addressAdapter.getCurrentList().get(position).getCityName())
                    .setNumberRows(1)
                    .setNumberCols(4)
                    .setResources(new BottomSheetAddressMenuResource(getActivity()))
                    .build();

            bottomSheetDialog.show(getParentFragmentManager(), "bottomSheetDialog");
            bottomSheetDialog.setOnCustomViewClickEvent((pos) -> {

                if (pos == 0) {
                    addressViewModel.delete(addressAdapter.getAddressPositionAt(position)).observe(requireActivity(), observer);
                    bottomSheetDialog.dismiss();
                }
                if (pos == 1) {
                    BottomMenuService b = new BottomMenuService(getActivity());

                    //new FragmentSearchLogic(getActivity()).sendLocation(addressAdapter.getCurrentList().get(position).getLocation());
                }
                if (pos == 2) {
                    bottomSheetDialog.dismiss();
                    BottomSheetMenuDialog favSheetDialog = new BottomSheetMenuDialog
                            .Builder()
                            .setHeader(addressAdapter.getCurrentList().get(position).getCityName())
                            .setNumberRows(2)
                            .setNumberCols(4)
                            .setResources(new BottomSheetFavoriteResource(getActivity()))
                            .build();


                    favSheetDialog.setOnCustomViewClickEvent(priority -> {
                        // Create copy the current address
                        Address update = new Address(addressAdapter.getCurrentList().get(position));
                        update.setPriority(priority);
                        addressViewModel.updateAddress(update, update.getId()).observe(requireActivity(), observer);
                        favSheetDialog.dismiss();

                    });
                    favSheetDialog.show(getParentFragmentManager(), "favSheetDialog");
                    ;
                }
            });
        });
    }

}
