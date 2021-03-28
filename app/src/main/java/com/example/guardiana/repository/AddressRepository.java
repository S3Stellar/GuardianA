package com.example.guardiana.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.guardiana.model.Address;
import com.example.guardiana.services.WebAddressService;
import com.example.guardiana.utility.StatusCode;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class AddressRepository {
    private static MutableLiveData<AddressResponse> addressesMutableLiveData;
    private static AddressRepository instance;
    private static WebAddressService addressApi;

    private AddressRepository() {
    }

    public static synchronized AddressRepository getInstance() {
        if (instance == null) {
            addressesMutableLiveData = new MutableLiveData<>();
            instance = new AddressRepository();
            addressesMutableLiveData.setValue(new AddressResponse());
            addressApi = new Retrofit.Builder()
                    .baseUrl(WebAddressService.URL)
                    .addConverterFactory(JacksonConverterFactory.create()).build().create(WebAddressService.class);
        }
        return instance;
    }

    public MutableLiveData<AddressResponse> getAllAddresses(String userId, String type, String value, String sortBy, String sortOrder, int page, int size, int offset) {
        addressApi.getAddressesByEmail(userId, type, value, sortBy, sortOrder, page, size).enqueue(new Callback<Address[]>() {
            @Override
            public void onResponse(@NotNull Call<Address[]> call, @NotNull Response<Address[]> response) {
                if (response.body() != null && response.body().length > 0) {
                    // Create new AddressResponse which hold the current list and the response status
                    AddressResponse addressResponse = new AddressResponse(addressesMutableLiveData.getValue().getAddressList(), response.message(), response.code(), 0);

                    /* This if statement checks two edge cases:
                     At first we check whether the offset is differ from 0, which means that we have a partial page
                     so we have to remove the addresses in that partial page.
                     The second check if to handle sublist operation on an list which is less than offset size.
                    */
                    if (offset != 0 && addressResponse.getAddressList().size() >= offset) {
                        addressResponse.getAddressList().subList(addressResponse.getAddressList().size() - offset, addressResponse.getAddressList().size()).clear();
                    }

                    // Add the response to the current collection.
                    addressResponse.getAddressList().addAll(Arrays.stream(response.body()).collect(Collectors.toList()));

                    // Call the observers with the updated data.
                    addressesMutableLiveData.setValue(addressResponse);
                }
            }

            @Override
            public void onFailure(@NotNull Call<Address[]> call, @NotNull Throwable t) {
                addressesMutableLiveData.setValue(new AddressResponse(StatusCode.INTERNAL_SERVER_ERROR, t.getMessage()));
            }
        });
        return addressesMutableLiveData;
    }

    public LiveData<AddressResponse> create(Address address) {

        addressApi.create(address).enqueue(new Callback<Address>() {
            @Override
            public void onResponse(@NotNull Call<Address> call, @NotNull Response<Address> response) {
                if (response.body() != null) {
                    // Create new AddressResponse
                    AddressResponse addressResponse = new AddressResponse(
                            new ArrayList<>(addressesMutableLiveData.getValue().getAddressList()), response.message(), response.code(),1
                    );

                    // Add the result at the front of the list
                    addressResponse.getAddressList().add(0, response.body());

                    // Call the observers with the updated data
                    addressesMutableLiveData.setValue(addressResponse);
                }
            }

            @Override
            public void onFailure(@NotNull Call<Address> call, @NotNull Throwable t) {
                addressesMutableLiveData.setValue(new AddressResponse(StatusCode.INTERNAL_SERVER_ERROR, t.getMessage()));
            }
        });
        return addressesMutableLiveData;
    }

    public LiveData<AddressResponse> delete(Address address) {
        addressApi.delete(address.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                AddressResponse addressResponse = new AddressResponse(new ArrayList<>(addressesMutableLiveData.getValue().getAddressList()), response.message(), response.code(), 0);
                addressResponse.getAddressList().remove(address);
                addressesMutableLiveData.setValue(addressResponse);

            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                addressesMutableLiveData.setValue(new AddressResponse(StatusCode.INTERNAL_SERVER_ERROR, t.getMessage()));

            }
        });
        return addressesMutableLiveData;
    }
}
