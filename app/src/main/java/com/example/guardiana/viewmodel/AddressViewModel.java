package com.example.guardiana.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.guardiana.model.Address;
import com.example.guardiana.repository.AddressRepository;
import com.example.guardiana.repository.AddressResponse;

public class AddressViewModel extends ViewModel {

    private final AddressRepository addressRepository;

    public AddressViewModel() {

        addressRepository = AddressRepository.getInstance();
    }

    public LiveData<AddressResponse> create(Address address) {
        return addressRepository.create(address);
    }

    public LiveData<AddressResponse> getAllAddresses(String userId, String type, String value, String sortBy, String sortOrder, int page, int size, int offset) {
        return addressRepository.getAllAddresses(userId, type, value, sortBy, sortOrder, page, size, offset);
    }

}
