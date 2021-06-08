package com.example.guardiana.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.guardiana.model.Address;
import com.example.guardiana.model.Location;
import com.example.guardiana.repository.AddressRepository;
import com.example.guardiana.repository.AddressResponse;
import com.example.guardiana.services.AddressOptions;

public class AddressViewModel extends AndroidViewModel {

    private final AddressRepository addressRepository;

    public AddressViewModel(@NonNull Application application) {
        super(application);
        addressRepository = AddressRepository.getInstance();
        addressRepository.initMutableLiveData();
    }

    public LiveData<AddressResponse> create(Address address) {
        return addressRepository.create(address);
    }

    public MutableLiveData<AddressResponse> getAllAddressesByPriority(String userId, int page, int size, int offset, String priority) {
        return addressRepository.getAllAddressesByPriority(userId, AddressOptions.byPriority.name(), priority, "", "", page, size, offset);
    }

    public LiveData<AddressResponse> updateAddress(Address address, String addressId) {
        return addressRepository.updateAddress(address, addressId);
    }

    public LiveData<AddressResponse> delete(Address address) {
        return addressRepository.delete(address);
    }

    public void sendLocation(Context context, Location location) {
        String uri = "http://maps.google.com/maps?saddr=" + location.getLat() + "," + location.getLng();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String ShareSub = "Here is my location";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, ShareSub);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, uri);
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}
