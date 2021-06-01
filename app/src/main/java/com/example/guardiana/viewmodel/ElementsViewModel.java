package com.example.guardiana.viewmodel;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.guardiana.App;
import com.example.guardiana.R;
import com.example.guardiana.clustermap.ReportClusterMarker;
import com.example.guardiana.customViews.resources.BottomSheetReportResource;
import com.example.guardiana.fragments.FragmentRoad;
import com.example.guardiana.model.Element;
import com.example.guardiana.repository.ElementRepository;
import com.example.guardiana.repository.ElementResponse;
import com.example.guardiana.async.AsyncDownloadTask;
import com.example.guardiana.utility.DialogOptions;
import com.example.guardiana.utility.Directions;
import com.example.guardiana.utility.Utility;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class ElementsViewModel extends AndroidViewModel {

    private final ElementRepository elementRepository;
    private final MutableLiveData<Location> locationMutableLiveData = new MutableLiveData<>();
    private Location lastKnownLocation;
    private Polyline lastPolyLine;
    private Marker lastMarker;
    public ElementsViewModel(@NonNull @NotNull Application application) {
        super(application);
        elementRepository = ElementRepository.getInstance();
        elementRepository.initMutableLiveData();
    }

//    public ElementsViewModel() {
//        elementRepository = ElementRepository.getInstance();
//        elementRepository.initMutableLiveData();
//    }

    public LiveData<ElementResponse> create(Element element) {
        return elementRepository.create(element);
    }

    public LiveData<ElementResponse> update(String elementId, Element update) {
        return elementRepository.update(elementId, update);
    }

    public LiveData<ElementResponse> getAllElementsByLocationFilters(Map<String, String> attr, String sortBy, String sortOrder, int page, int size) {
        return elementRepository.getAllElementsByLocationFilters(attr, sortBy, sortOrder, page, size);
    }

//    public LiveData<ElementResponse> getAllElements(String type, String value, String sortBy, String sortOrder, int page, int size) {
//        return elementRepository.getAllElementsByFilters(type, value, sortBy, sortOrder, page, size);
//    }


//    public LiveData<ElementResponse> deleteAll() {
//        return elementRepository.deleteAll();
//    }

    public LiveData<Location> getLocation() {
        return locationMutableLiveData;
    }

    public void setLocation(Location location) {
        locationMutableLiveData.postValue(location);
    }

    public void updateItem(Context activity, BottomSheetReportResource resources, int pos, ReportClusterMarker item) {
        Element updatedElement = item.getElement();
        Integer currentThreshold = (Integer) updatedElement.getElementAttribute().get("threshold");
        String userEmail = App.getUserId().replaceAll("\\.", "_");
        Integer reporterCount = (Integer) updatedElement.getElementAttribute().get(userEmail);
        String operation = resources.getResources().get(pos).getTextView().getText().toString();
        if (currentThreshold == null || reporterCount == null) return;
        if (reporterCount == 0 && operation.equals(DialogOptions.BottomDialog.LIKE)) {
            // Increment
            updatedElement.getElementAttribute().put(userEmail, 1);
            updatedElement.getElementAttribute().put("threshold", currentThreshold + 1);
            update(updatedElement.getId(), updatedElement);
        } else if (reporterCount == 1 && operation.equals(DialogOptions.BottomDialog.DISLIKE)) {
            // Decrement
            updatedElement.getElementAttribute().put(userEmail, 0);
            updatedElement.getElementAttribute().put("threshold", currentThreshold - 1);
            update(updatedElement.getId(), updatedElement);
        } else {
            // bad operation
            new SweetAlertDialog(activity).setTitleText("Bad Operation").setContentText("You already said you " + operation + " it").show();
        }
    }


    public void showRoute(FusedLocationProviderClient fusedLocationClient, CancellationTokenSource cancellationTokenSource, Bundle bundle, GoogleMap googleMap) {
        Log.i("TAG", "onStart: " + bundle);
        if (bundle != null && !bundle.isEmpty()) {
            if (ActivityCompat.checkSelfPermission(
                    getApplication(),
                    Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {

                Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(
                        PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.getToken());

                currentLocationTask.addOnCompleteListener((task -> {
                    if (task.isSuccessful()) {
                        // Task completed successfully
                        Location location = task.getResult();

                        com.example.guardiana.model.Location endLocation = ((com.example.guardiana.model.Location) bundle.getSerializable("location"));
                        // Getting URL to the Google Directions API
                        String url = Directions.getDirectionsUrl(new LatLng(endLocation.getLat(), endLocation.getLng()), new LatLng(location.getLatitude(), location.getLongitude()));

                        // Start downloading json data from Google Directions API
                        new AsyncDownloadTask(googleMap).execute(url);

                        if (lastMarker != null)
                            lastMarker.remove();
                        Log.d(TAG, "onHiddenChanged: " + endLocation.getLat() + " " + endLocation.getLat());

                    } else {
                        // Task failed with an exception
                        Exception exception = task.getException();
                        Log.d(TAG, "Exception thrown: " + exception);

                    }
                }));
            }
        }
    }

    public void getCurrentLocation(GoogleMap googleMap, FusedLocationProviderClient fusedLocationClient, CancellationTokenSource cancellationTokenSource, LocationCallback locationCallback) {
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.getToken());

            currentLocationTask.addOnCompleteListener((task -> {
                if (task.isSuccessful()) {
                    // Task completed successfully
                    Location location = task.getResult();
                    Log.i(TAG, "getCurrentLocation: " + location);
                    initializeMyLocation(googleMap, location);
                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setInterval(5000);
                    locationRequest.setFastestInterval(0);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    if (locationCallback != null)
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                } else {
                    // Task failed with an exception
                    Exception exception = task.getException();
                    Log.d(TAG, "Exception thrown: " + exception);
                }
            }));
        }
    }

    public void initializeMyLocation(GoogleMap googleMap, Location location) {
        FragmentRoad.lastKnownLocation = location;
        LatLng yourLocation = new LatLng(location.getLatitude(), location.getLongitude());
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
                            getApplication(), style));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void removeAllPolyline() {
        if (lastPolyLine != null)
            lastPolyLine.remove();
    }

}
