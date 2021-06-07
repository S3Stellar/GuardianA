package com.example.guardiana.utility;

import android.content.pm.PackageManager;
import android.util.Log;

import com.example.guardiana.App;
import com.google.android.gms.maps.model.LatLng;

public class Directions {

    public static String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=true";
        String mode = "mode=bicycling";
        // Building the parameters to the web service
        String mapApiKey="";
        try {
             mapApiKey = App.getContext().getPackageManager().getApplicationInfo(App.getContext().getPackageName(),
                    PackageManager.GET_META_DATA).metaData.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&key="+mapApiKey+"\n";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }
}
