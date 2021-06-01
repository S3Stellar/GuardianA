package com.example.guardiana.async;

import android.graphics.Color;
import android.os.AsyncTask;

import com.example.guardiana.R;
import com.example.guardiana.utility.DirectionsJSONParser;
import com.example.guardiana.utility.Utility;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A class to parse the Google Places in JSON format
 */
public class AsyncParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
    // Parsing the data in non-ui thread
    private GoogleMap googleMap;
    private static Polyline lastPolyLine;
    private static Marker targetMarker;
    public AsyncParserTask(GoogleMap googleMap) {
        this.googleMap = googleMap;

    }

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
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = new PolylineOptions();
        // Remove last drawn polyline (route)
        if (lastPolyLine != null)
            lastPolyLine.remove();
        if (targetMarker != null)
            targetMarker.remove();

        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
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
            int color = Utility.isDay() ? Color.DKGRAY : Color.WHITE;
            lineOptions.color(color);
            lineOptions.geodesic(true);

        }

        List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dot(), new Gap(3f));
        // Drawing polyline in the Google Map for the i-th route
        lastPolyLine = googleMap.addPolyline(lineOptions);

        lastPolyLine.setPattern(pattern);
        targetMarker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lastPolyLine.getPoints().get(0).latitude, lastPolyLine.getPoints().get(0).longitude))
                .title("Destination")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.flag)));
    }
}


