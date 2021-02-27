package com.example.guardiana.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.guardiana.model.Address;
import com.example.guardiana.repository.AddressRepository;

import java.util.List;

public class AddressViewModel extends AndroidViewModel {

    private final AddressRepository addressRepository;

    public AddressViewModel(@NonNull Application application) {
        super(application);
        addressRepository = AddressRepository.getInstance();
    }

    public void create(Address address) {
        addressRepository.create(address);
    }

    public LiveData<List<Address>> getAllAddresses(String type, String value, String sortBy, String sortOrder, int page, int size) {
        return addressRepository.getAllAddresses(type, value, sortBy, sortOrder, page, size);
    }
}
