package com.example.guardiana.customviews.concretecustomviews.bottommenu;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;

import com.example.guardiana.model.Location;
import com.example.guardiana.repository.AddressResponse;

public class BottomMenuService implements BottomMenuOperations {

    private final Context context;

    public BottomMenuService(Context context) {
        this.context = context;
    }

    @Override
    public void sendLocation(Location location) {
        String uri = "http://maps.google.com/maps?saddr=" + location.getLat() + "," + location.getLng();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String ShareSub = "Here is my location";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, ShareSub);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, uri);
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    @Override
    public void remove(LiveData<AddressResponse> liveData) {

    }

    @Override
    public void favorite() {

    }

    @Override
    public void drive() {

    }
}
