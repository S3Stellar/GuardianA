package com.example.guardiana.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.sax.ElementListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.guardiana.App;
import com.example.guardiana.R;
import com.example.guardiana.clustermap.ClusterManagerRender;
import com.example.guardiana.clustermap.ReportClusterMarker;
import com.example.guardiana.model.Element;
import com.example.guardiana.model.ElementCreator;
import com.example.guardiana.repository.ElementResponse;
import com.example.guardiana.viewmodel.ElementsViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.clustering.ClusterManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class FragmentRoad extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private MapView mMapView;
    private ElementsViewModel elementsViewModel;
    private Observer<ElementResponse> observer;
    private Set<Element> displayedElements = new HashSet<>();
    private Location lastKnownLocation;

    private ClusterManager<ReportClusterMarker> clusterManager;
    private ClusterManagerRender clusterManagerRender;

    // The Fused Location Provider provides access to location APIs.
    private FusedLocationProviderClient fusedLocationClient;

    // Allows class to cancel the location request if it exits the activity.
    // Typically, you use one cancellation source per lifecycle.
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    private double currentMapZoom;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_road, container, false);

        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        elementsViewModel = new ViewModelProvider(requireActivity()).get(ElementsViewModel.class);

        setObserver();

        MapsInitializer.initialize(getActivity().getApplicationContext());

        FloatingActionButton fab = view.findViewById(R.id.reportButton);

        fab.setOnClickListener(this::showAlertDialogButtonClicked);

        mMapView.getMapAsync(this);

        return view;
    }

    private void setObserver() {
        observer = response -> {
            if (response.getStatusCode() == 200) {
                if (response.getFlag() == 0) {

                    Set<Element> oldElements = new HashSet<>(displayedElements);
                    displayedElements.addAll(response.getElementList());
                    displayedElements.removeAll(oldElements);

                    for (Element element : displayedElements) {
                        ReportClusterMarker recycleBinClusterMarker = new ReportClusterMarker("Snippet", element);
                        clusterManager.addItem(recycleBinClusterMarker);
                        clusterManager.cluster();
                    }
                    displayedElements.addAll(oldElements);
                } else if (response.getFlag() == 1  ) {
                    Toast.makeText(getContext(), "Status 200 - create", Toast.LENGTH_SHORT).show();
                }
            } else {
                new SweetAlertDialog(getContext()).setTitleText("Error").setContentText(response.getMessage()).show();
            }
        };
    }

    public void showAlertDialogButtonClicked(View view) {
        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Report");

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.report_layout, null);
        builder.setView(customLayout);

        // add a button
        builder.setPositiveButton("OK", (dialog, which) -> {
            // send data from the AlertDialog to the Activity
            //EditText editText = customLayout.findViewById(R.id.editText);
            //sendDialogDataToActivity(editText.getText().toString());
        });

        setReportButtonListeners(customLayout);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setReportButtonListeners(View customLayout) {
        customLayout.findViewById(R.id.accidentButton).setOnClickListener(v -> {
            Toast.makeText(getContext(), "accidentButton", Toast.LENGTH_SHORT).show();
            sendReport("ACCIDENT");
        });
        customLayout.findViewById(R.id.policeButton).setOnClickListener(v -> {
            Toast.makeText(getContext(), "policeButton", Toast.LENGTH_SHORT).show();
            sendReport("POLICE");
        });
        customLayout.findViewById(R.id.pumpButton);
        customLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "pumpButton", Toast.LENGTH_SHORT).show();
                sendReport("PUMP");
            }
        });
        customLayout.findViewById(R.id.protestButton).setOnClickListener(v -> {
            Toast.makeText(getContext(), "protestButton", Toast.LENGTH_SHORT).show();
            sendReport("PROTEST");
        });
    }

    private void sendReport(String reportType) {
        elementsViewModel.create(new Element(
                reportType,
                reportType,
                reportType,
                true,
                new ElementCreator(App.getUserId()),
                new com.example.guardiana.model.Location(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()),
                new HashMap<>())).observe(requireActivity(), observer);
    }


    // do something with the data coming from the AlertDialog
    private void sendDialogDataToActivity(String data) {
        Toast.makeText(getContext(), data, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
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
           /* //To add marker
            LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String locationProvider = String.valueOf(locationManager.getBestProvider(criteria, true));
            //String locationProvider = LocationManager.NETWORK_PROVIDER;
            // I suppressed the missing-permission warning because this wouldn't be executed in my
            // case without location services being enabled
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation == null) {
                locationManager.requestLocationUpdates(locationProvider, 1000, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        locationManager.removeUpdates(this::onLocationChanged);
                        initializeMyLocation(location);
                    }
                });
            } else
                initializeMyLocation(lastKnownLocation);*/
            initClusterManager();
            requestCurrentLocation();
            loadElementsFromServer();
            cameraMoveLoadElementsListener();
        });
    }

    private void initClusterManager() {
        if (googleMap != null) {
            if (clusterManager == null) {
                clusterManager = new ClusterManager<>(requireActivity(), googleMap);
            }
            if (clusterManagerRender == null) {
                clusterManagerRender = new ClusterManagerRender(getActivity(), googleMap, clusterManager);
            }
            googleMap.setOnCameraIdleListener(clusterManager);
            clusterManager.setRenderer(clusterManagerRender);
        }
    }

    @NotNull
    private LocationCallback getLocationCallback() {
        return new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        Log.i(TAG, "onLocationResult: lng:  "  + locationResult.getLastLocation().getLongitude() + ", lat: "+ locationResult.getLastLocation().getLatitude());

                        lastKnownLocation.setLongitude(locationResult.getLastLocation().getLongitude());
                        lastKnownLocation.setLatitude(locationResult.getLastLocation().getLatitude());

                    }
                };
    }

    private void requestCurrentLocation() {
        Log.d(TAG, "requestCurrentLocation()");
        // Request permission
        if (ActivityCompat.checkSelfPermission(
                getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            // Main code
            Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.getToken()
            );
            currentLocationTask.addOnCompleteListener((task -> {
                String result = "";
                if (task.isSuccessful()) {
                    // Task completed successfully
                    Location location = task.getResult();
                    result = "Location (success): " +
                            location.getLatitude() +
                            ", " +
                            location.getLongitude();
                    initializeMyLocation(location);

                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setInterval(5000);
                    locationRequest.setFastestInterval(0);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                    fusedLocationClient.requestLocationUpdates(locationRequest, getLocationCallback(), Looper.myLooper());
                } else {
                    // Task failed with an exception
                    Exception exception = task.getException();
                    result = "Exception thrown: " + exception;
                }
                Log.d(TAG, "getCurrentLocation() result: " + result);
            }));
        } else {
            // TODO: Request fine location permission
            Log.d(TAG, "Request fine location permission.");
        }
    }

    private void cameraMoveLoadElementsListener() {
        googleMap.setOnCameraIdleListener(() -> {
            Log.i("MOVE", "CAMERA MOVE!");
            loadElementsFromServer();
        });

        googleMap.setOnCameraMoveListener(() -> {
            CameraPosition cameraPosition = googleMap.getCameraPosition();
            currentMapZoom = cameraPosition.zoom;
            Log.i(TAG, "onCameraMove: " + currentMapZoom);
        });
    }

    private void onClusterItemClick() {
        clusterManager.setOnClusterItemClickListener(item -> {

            return false;
        });

    }

    private void initializeMyLocation(Location lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
        LatLng yourLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        // googleMap.addMarker(new MarkerOptions().position(yourLocation).title("Title").snippet("Marker Description"));
        // For zooming functionality
        CameraPosition cameraPosition = new CameraPosition.Builder().target(yourLocation).zoom(18).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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

        // @TODO Calculate size ratio of elements per map size (zoom)
        elementsViewModel.getAllElementsByLocationFilters(attrMap, "", "", 0, 20).observe(requireActivity(), observer);
    }
}