package com.example.guardiana.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.guardiana.App;
import com.example.guardiana.R;
import com.example.guardiana.clustermap.ClusterManagerRender;
import com.example.guardiana.clustermap.ReportClusterMarker;
import com.example.guardiana.customViews.resources.BottomSheetReportMenuResource;
import com.example.guardiana.customViews.resources.BottomSheetReportResource;
import com.example.guardiana.dialogs.BottomSheetMenuDialog;
import com.example.guardiana.model.Element;
import com.example.guardiana.model.ElementCreator;
import com.example.guardiana.repository.ElementResponse;
import com.example.guardiana.utility.StatusCode;
import com.example.guardiana.viewmodel.ElementsViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.clustering.ClusterManager;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.ContentValues.TAG;

public class FragmentRoad extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    // The Fused Location Provider provides access to location APIs.
    private FusedLocationProviderClient fusedLocationClient;
    public static Location lastKnownLocation;

    private ElementsViewModel elementsViewModel;
    private final Set<Element> displayedElements = new HashSet<>();
    private Observer<ElementResponse> elementResponseObserver;
    private ClusterManager<ReportClusterMarker> reportClusterManager;
    private ClusterManagerRender clusterManagerRender;

    // Allows class to cancel the location request if it exits the activity.
    // Typically, you use one cancellation source per lifecycle.
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    private LocationCallback locationCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_road, container, false);

        MapView mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        elementsViewModel = new ViewModelProvider(requireActivity()).get(ElementsViewModel.class);

        setElementResponseObserver();

        MapsInitializer.initialize(requireActivity());

        FloatingActionButton fab = view.findViewById(R.id.reportButton);

        fab.setOnClickListener(v -> createReportSelectionView());

        mMapView.getMapAsync(this);

        return view;
    }

    private void setElementResponseObserver() {
        elementResponseObserver = response -> {
            if (response.getStatusCode() == StatusCode.OK) {
                if (response.getFlag() == ElementResponse.flagTypes.GET.ordinal()) {
                    clear();
                    displayedElements.addAll(response.getElementList());
                    displayedElements.forEach(element -> {
                        ReportClusterMarker reportClusterMarker = new ReportClusterMarker("Snippet", element);
                        reportClusterManager.addItem(reportClusterMarker);
                        reportClusterManager.cluster();
                    });
                } else if (response.getFlag() == ElementResponse.flagTypes.CREATE.ordinal() || response.getFlag() == ElementResponse.flagTypes.UPDATE.ordinal()) {
                    clear();
                    loadElementsFromServer();
                }
            } else {
                new SweetAlertDialog(requireActivity()).setTitleText("Error").setContentText(response.getMessage()).show();
            }
        };
    }

    private void clear() {
        reportClusterManager.clearItems();
        reportClusterManager.cluster();
        displayedElements.clear();
    }

    private void createReportSelectionView() {
        BottomSheetReportMenuResource resources = new BottomSheetReportMenuResource(getActivity());
        BottomSheetMenuDialog bottomSheetDialog = new BottomSheetMenuDialog
                .Builder().setHeader("Do you want to set new alert ?")
                .setNumberRows(1).setNumberCols(4)
                .setResources(resources)
                .setOnClickEvent(pos -> sendReport(resources.getResources().get(pos).getTextView().getText().toString().toUpperCase()))
                .build();
        bottomSheetDialog.show(getParentFragmentManager(), "bottomSheetDialog");
    }


    private void sendReport(String reportType) {
        elementsViewModel.create(new Element(
                reportType,
                reportType,
                reportType,
                true,
                new ElementCreator(App.getUserId()),
                new com.example.guardiana.model.Location(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()),
                new HashMap<>())).observe(requireActivity(), elementResponseObserver);
    }


    // do something with the data coming from the AlertDialog
    private void sendDialogDataToActivity(String data) {
        Toast.makeText(getContext(), data, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setOnMapLoadedCallback(() -> {
            googleMap.setMyLocationEnabled(true);
            initClusterManager();
            getLocationCallback();
            requestCurrentLocation();
            loadElementsFromServer();
            cameraMoveLoadElementsListener();
        });
    }


    private void initClusterManager() {
        if (googleMap != null) {
            if (reportClusterManager == null) {
                reportClusterManager = new ClusterManager<>(requireActivity(), googleMap);
            }
            if (clusterManagerRender == null) {
                clusterManagerRender = new ClusterManagerRender(getActivity(), googleMap, reportClusterManager);
            }
            googleMap.setOnCameraIdleListener(reportClusterManager);
            reportClusterManager.setRenderer(clusterManagerRender);
            reportClusterManager.setOnClusterItemClickListener(item -> {
                createDialog(item);
                return false;
            });
        }
    }

    private void createDialog(ReportClusterMarker item) {
        BottomSheetReportResource resources = new BottomSheetReportResource(getActivity());
        BottomSheetMenuDialog bottomSheetDialog = new BottomSheetMenuDialog
                .Builder()
                .setHeader("Is it still there ?")
                .setNumberRows(1)
                .setNumberCols(2)
                .setResources(resources)
                .setOnClickEvent(pos -> {
                    elementsViewModel.updateItem(requireActivity(), resources, pos, item);
                })
                .build();

        bottomSheetDialog.show(getParentFragmentManager(), "bottomSheetDialog");
    }

    private void loadElementsFromServer() {
        Map<String, String> attrMap = new HashMap<>();
        VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();
        LatLng northeast = visibleRegion.latLngBounds.northeast;
        LatLng southwest = visibleRegion.latLngBounds.southwest;
        attrMap.put("minLat", southwest.latitude + "");
        attrMap.put("maxLat", northeast.latitude + "");
        attrMap.put("minLng", southwest.longitude + "");
        attrMap.put("maxLng", northeast.longitude + "");
        elementsViewModel.getAllElementsByLocationFilters(attrMap, "", "", 0, 20).observe(requireActivity(), elementResponseObserver);
    }

    @NotNull
    private LocationCallback getLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(lastKnownLocation != null){
                    lastKnownLocation.setLongitude(locationResult.getLastLocation().getLongitude());
                    lastKnownLocation.setLatitude(locationResult.getLastLocation().getLatitude());
                    Log.i(TAG, "onLocationResult: " + lastKnownLocation);

                }
                // @TODO
                // Send to server my updated location
/*
                userMarkerViewModel.create(new UserMarker(App.getUserId(),
                        new com.example.guardiana.model.Location(lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude()), true, true, R.drawable.bottom_sheet_bicycle));
*/

  /*              userMarkerViewModel.update(App.getUserId(), new UserMarker(App.getUserId(),
                        new com.example.guardiana.model.Location(lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude()), true, true, R.drawable.bottom_sheet_bicycle));

                // Request new locations of other users to update map
                loadUserMarkersFromServer();*/
            }
        };
        return locationCallback;
    }


    private void cameraMoveLoadElementsListener() {
        googleMap.setOnCameraIdleListener(this::loadElementsFromServer);
    }

//    private void initializeMyLocation(Location lastKnownLocation) {
//        this.lastKnownLocation = lastKnownLocation;
//        elementsViewModel.initializeMyLocation(lastKnownLocation, googleMap);
//    }


    private void requestCurrentLocation() {
        elementsViewModel.getCurrentLocation(googleMap, fusedLocationClient, cancellationTokenSource, locationCallback);
        // Request permission

    }

    // Method called when Drive button is clicked and route has to be shown
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Bundle bundle = getArguments();
        elementsViewModel.showRoute(fusedLocationClient, cancellationTokenSource, bundle, googleMap);
    }
}
