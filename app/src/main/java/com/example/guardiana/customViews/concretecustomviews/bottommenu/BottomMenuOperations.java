package com.example.guardiana.customViews.concretecustomviews.bottommenu;

import androidx.lifecycle.LiveData;

import com.example.guardiana.model.Location;
import com.example.guardiana.repository.AddressResponse;

public interface BottomMenuOperations {

    public void sendLocation(Location location);

    public void remove(LiveData<AddressResponse> liveData);

    public void favorite();

    public void drive();
}
