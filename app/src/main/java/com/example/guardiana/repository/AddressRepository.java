package com.example.guardiana.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.guardiana.App;
import com.example.guardiana.model.Address;
import com.example.guardiana.services.WebAddressService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class AddressRepository {
    private static final MutableLiveData<List<Address>> addressesMutableLiveData = new MutableLiveData<>();
    private static AddressRepository instance;
    private static WebAddressService addressApi;

    public static synchronized AddressRepository getInstance() {
        if (instance == null) {
            instance = new AddressRepository();
//            addressesMutableLiveData.setValue(new ArrayList<>());
            addressApi = new Retrofit.Builder()
                    .baseUrl(WebAddressService.URL)
                    .addConverterFactory(JacksonConverterFactory.create()).build().create(WebAddressService.class);
        }
        return instance;
    }

    private AddressRepository() {

    }

    public MutableLiveData<List<Address>> getAllAddresses(String userId, String type, String value, String sortBy, String sortOrder, int page, int size) {
        addressApi.getAddressesByEmail(userId, type, value, sortBy, sortOrder, page, size).enqueue(new Callback<Address[]>() {
            @Override
            public void onResponse(@NotNull Call<Address[]> call, @NotNull Response<Address[]> response) {
                if (response.body() != null) {
                    addressesMutableLiveData.setValue(new ArrayList<>(Arrays.asList(response.body())));
                }
            }

            @Override
            public void onFailure(@NotNull Call<Address[]> call, @NotNull Throwable t) {

            }
        });
        return addressesMutableLiveData;
    }


    public void create(Address address) {
        addressApi.create(address).enqueue(new Callback<Address>() {
            @Override
            public void onResponse(@NotNull Call<Address> call, @NotNull Response<Address> response) {
                if (response.body() != null) {
                    List<Address> newList = new ArrayList<>(addressesMutableLiveData.getValue());
                    newList.add(0, response.body());
                    addressesMutableLiveData.setValue(newList);
                }
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                Log.d("TAG", "onFailure: create failed " + t.getMessage());
            }
        });
    }

    public LiveData<Call<Address[]>> getLive(String userId, String type, String value, String sortBy, String sortOrder, int page, int size) {
        return addressApi.getAddressesByEmail1(userId, type, value, sortBy, sortOrder, page, size);
    }
}
