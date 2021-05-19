package com.example.guardiana.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
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
import com.example.guardiana.utility.DirectionsJSONParser;
import com.example.guardiana.utility.StatusCode;
import com.example.guardiana.utility.Utility;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.clustering.ClusterManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class FragmentRoad extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    // The Fused Location Provider provides access to location APIs.
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;

    private ElementsViewModel elementsViewModel;
    private final Set<Element> displayedElements = new HashSet<>();
    private Observer<ElementResponse> elementResponseObserver;
    private ClusterManager<ReportClusterMarker> reportClusterManager;
    private ClusterManagerRender clusterManagerRender;

    // Allows class to cancel the location request if it exits the activity.
    // Typically, you use one cancellation source per lifecycle.
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    private Polyline lastPolyLine;
    private Marker lastMarker;

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
            initClusterManager();
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
                .build();

        bottomSheetDialog.setOnCustomViewClickEvent(pos -> {
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
        return new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                lastKnownLocation.setLongitude(locationResult.getLastLocation().getLongitude());
                lastKnownLocation.setLatitude(locationResult.getLastLocation().getLatitude());

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
    }


    private void cameraMoveLoadElementsListener() {
        googleMap.setOnCameraIdleListener(this::loadElementsFromServer);
    }

    private void initializeMyLocation(Location lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
        LatLng yourLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        // googleMap.addMarker(new MarkerOptions().position(yourLocation).title("Title").snippet("Marker Description"));
        // For zooming functionality
//        CameraPosition currentposition = googleMap.getCameraPosition();
        CameraPosition cameraPosition = new CameraPosition
                .Builder()
                .target(yourLocation)
                .zoom(40)
//                .bearing(currentposition.bearing)
                .tilt(30)
                .build();
        try {
            int style = Utility.isDay() ? R.raw.day : R.raw.night;
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            requireActivity(), style));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    private void requestCurrentLocation() {
        // Request permission
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.getToken());

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

    // Method called when Drive button is clicked and route has to be shown
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            //do when hidden
        } else {
            Bundle bundle = getArguments();
            Log.i("TAG", "onStart: " + bundle);
            if (bundle != null && !bundle.isEmpty()) {
                if (ActivityCompat.checkSelfPermission(
                        getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

                    Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(
                            PRIORITY_HIGH_ACCURACY,
                            cancellationTokenSource.getToken());

                    currentLocationTask.addOnCompleteListener((task -> {
                        String result;
                        if (task.isSuccessful()) {
                            // Task completed successfully
                            Location location = task.getResult();
                            result = "Location (success): " +
                                    location.getLatitude() +
                                    ", " +
                                    location.getLongitude();
                            com.example.guardiana.model.Location endLocation = ((com.example.guardiana.model.Location) bundle.getSerializable("location"));
                            // Getting URL to the Google Directions API
                            String url = getDirectionsUrl(new LatLng(endLocation.getLat(), endLocation.getLng()), new LatLng(location.getLatitude(), location.getLongitude()));

                            // Start downloading json data from Google Directions API
                            new DownloadTask().execute(url);

                            if (lastMarker != null)
                                lastMarker.remove();
                            Log.i(TAG, "onHiddenChanged: " + endLocation.getLat() + " " + endLocation.getLat());
                            lastMarker = googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(endLocation.getLat(), endLocation.getLng()))
                                    .title("Destination")
                                    .icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable.flag)));

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
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = new PolylineOptions();

            // Remove last drawn polyline (route)
            if (lastPolyLine != null)
                lastPolyLine.remove();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.DKGRAY);
                lineOptions.geodesic(true);
            }

            // Drawing polyline in the Google Map for the i-th route
            lastPolyLine = googleMap.addPolyline(lineOptions);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=true";
        String mode = "mode=bicycling";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&key=API_KEY_HERE\n";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


}
