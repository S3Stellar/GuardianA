package com.example.guardiana.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.guardiana.customviews.resources.BottomSheetReportMenuResource;
import com.example.guardiana.customviews.resources.BottomSheetReportResource;
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
import static android.content.Context.VIBRATOR_SERVICE;

public class FragmentRoad extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    // The Fused Location Provider provides access to location APIs.
    private FusedLocationProviderClient fusedLocationClient;
    public static Location lastKnownLocation;
    public static boolean isRouteShown;

    private ElementsViewModel elementsViewModel;
    private final Set<Element> displayedElements = new HashSet<>();
    private Observer<ElementResponse> elementResponseObserver;
    private ClusterManager<ReportClusterMarker> reportClusterManager;
    private ClusterManagerRender clusterManagerRender;

    // Allows class to cancel the location request if it exits the activity.
    // Typically, you use one cancellation source per lifecycle.
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    private LocationCallback locationCallback;
    private FloatingActionButton fab;
    private Vibrator mVibrator;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_road, container, false);

        MapView mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mVibrator = (Vibrator) requireActivity().getSystemService(VIBRATOR_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        elementsViewModel = new ViewModelProvider(requireActivity()).get(ElementsViewModel.class);

        setElementResponseObserver();

        MapsInitializer.initialize(requireActivity());

        fab = view.findViewById(R.id.reportButton);
        fab.hide();
        fab.setOnClickListener(v -> {
            mVibrator.vibrate(80);
            createReportSelectionView();
        });

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
                .build();
        bottomSheetDialog.setOnCustomViewClickEvent(
                pos -> {
                    mVibrator.vibrate(80);
                    sendReport(resources.getResources().get(pos).getTextView().getText().toString().toUpperCase());
                    bottomSheetDialog.dismiss();
                });
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

    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setOnMapLoadedCallback(() -> {
            googleMap.setMyLocationEnabled(true);
            initClusterManager();
            getLocationCallback();
            requestCurrentLocation();
            loadElementsFromServer();
            cameraMoveLoadElementsListener();
            fab.show();
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
                mVibrator.vibrate(80);
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
                .build();
        bottomSheetDialog.setOnCustomViewClickEvent(pos -> {
            mVibrator.vibrate(80);
            elementsViewModel.updateItem(requireActivity(), resources, pos, item);
            bottomSheetDialog.dismiss();
        });
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
                if (lastKnownLocation != null) {
                    lastKnownLocation.setLongitude(locationResult.getLastLocation().getLongitude());
                    lastKnownLocation.setLatitude(locationResult.getLastLocation().getLatitude());
                    Log.i(TAG, "onLocationResult: " + lastKnownLocation);

                }
                // @TODO Send to server my updated location for user' markers sharing
/*                userMarkerViewModel.create(new UserMarker(App.getUserId(),
                        new com.example.guardiana.model.Location(lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude()), true, true, R.drawable.bottom_sheet_bicycle));

                userMarkerViewModel.update(App.getUserId(), new UserMarker(App.getUserId(),
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


    private void requestCurrentLocation() {
        elementsViewModel.getCurrentLocation(googleMap, fusedLocationClient, cancellationTokenSource, locationCallback);
    }

    // Method called when Drive button is clicked and route has to be shown
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (isRouteShown) {
            Bundle bundle = getArguments();
            elementsViewModel.showRoute(fusedLocationClient, cancellationTokenSource, bundle, googleMap);
        }
    }
}
