package com.example.guardiana.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.guardiana.App;
import com.example.guardiana.model.Address;
import com.example.guardiana.services.WebAddressService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class AddressRepository {
    private static AddressRepository instance;
    private static MutableLiveData<List<Address>> addressesMutableLiveData = new MutableLiveData<>();
    private static WebAddressService addressApi;

    public static AddressRepository getInstance() {
        if (instance == null) {
            instance = new AddressRepository();
            addressesMutableLiveData.setValue(new ArrayList<>());
            addressApi = new Retrofit.Builder()
                    .baseUrl(WebAddressService.URL)
                    .addConverterFactory(JacksonConverterFactory.create()).build().create(WebAddressService.class);
        }
        return instance;
    }

    private AddressRepository() {

    }

    public MutableLiveData<List<Address>> getAllAddresses(String type, String value, String sortBy, String sortOrder, int page, int size) {
        addressApi.getAddressesByEmail(App.getUserId(), type, value, sortBy, sortOrder, page, size).enqueue(new Callback<Address[]>() {
            @Override
            public void onResponse(Call<Address[]> call, Response<Address[]> response) {
                if(response != null) {
                    addressesMutableLiveData.setValue(new ArrayList<>(Arrays.asList(response.body())));
                    Log.i("TAG", "onResponseGetAll: ");
                }
            }

            @Override
            public void onFailure(Call<Address[]> call, Throwable t) {

            }
        });
        return addressesMutableLiveData;
    }

    public void create(Address address){
        addressApi.create(address).enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {
                if (response != null) {
                    List<Address> newList = addressesMutableLiveData.getValue();
                    newList.add(0, response.body());
                    Log.i("TAG", "onResponse: " + newList);
                    addressesMutableLiveData.setValue(newList);
                }
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                Log.d("TAG", "onFailure: create failed " + t.getMessage());
            }
        });
    }
}
