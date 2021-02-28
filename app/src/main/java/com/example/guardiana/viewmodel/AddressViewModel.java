package com.example.guardiana.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.guardiana.model.Address;
import com.example.guardiana.repository.AddressRepository;

import java.util.List;

import retrofit2.Call;

public class AddressViewModel extends ViewModel {

    private final AddressRepository addressRepository;

    public AddressViewModel() {

        addressRepository = AddressRepository.getInstance();
    }

    public void create(Address address) {
        addressRepository.create(address);
    }

    public LiveData<List<Address>> getAllAddresses(String userId, String type, String value, String sortBy, String sortOrder, int page, int size) {
        return addressRepository.getAllAddresses(userId, type, value, sortBy, sortOrder, page, size);
    }
    public LiveData<Call<Address[]>> getLive(String userId, String type, String value, String sortBy, String sortOrder, int page, int size) {
        return addressRepository.getLive(userId, type, value, sortBy, sortOrder, page, size);
    }
}
