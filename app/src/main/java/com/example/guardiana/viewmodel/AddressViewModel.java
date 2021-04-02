package com.example.guardiana.viewmodel;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.guardiana.model.Address;
import com.example.guardiana.model.Location;
import com.example.guardiana.repository.AddressRepository;
import com.example.guardiana.repository.AddressResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressViewModel extends AndroidViewModel {

    private final AddressRepository addressRepository;

    public AddressViewModel(@NonNull Application application) {
        super(application);
        addressRepository = AddressRepository.getInstance();
        addressRepository.initMutableLiveData();
    }

//    public AddressViewModel() {
//        addressRepository = AddressRepository.getInstance();
//        addressRepository.initMutableLiveData();
//    }

    public LiveData<AddressResponse> create(Address address) {
        return addressRepository.create(address);
    }

    public LiveData<AddressResponse> getAllAddresses(String userId, String type, String value, String sortBy, String sortOrder, int page, int size, int offset) {
        return addressRepository.getAllAddresses(userId, type, value, sortBy, sortOrder, page, size, offset);
    }

    public MutableLiveData<AddressResponse> getAllAddressesByPriority(String userId, int page, int size, int offset, String priority) {
        return addressRepository.getAllAddresses(userId, "byPriority", priority, "", "", page, size, offset);
    }

    public LiveData<AddressResponse> updateAddress(Address address, String addressId) {
        return addressRepository.updateAddress(address, addressId);
    }

    public LiveData<AddressResponse> delete(Address address) {
        return addressRepository.delete(address);
    }



}
