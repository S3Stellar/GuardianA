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
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class AddressRepository {
    private static final MutableLiveData<AddressResponse> addressesMutableLiveData = new MutableLiveData<>();
    private static AddressRepository instance;
    private static WebAddressService addressApi;
    private AddressRepository() { }

    public static synchronized AddressRepository getInstance() {
        if (instance == null) {
            instance = new AddressRepository();
            addressesMutableLiveData.setValue(new AddressResponse(200));
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
                    AddressResponse addressResponse = new AddressResponse();
                    addressResponse.setFlag(0);
                    addressResponse.setStatusCode(response.code());
                    addressResponse.setAddressList(new ArrayList<>(addressesMutableLiveData.getValue().getAddressList()));

                    if (addressResponse.getAddressList().size() >= offset) {
                        addressResponse.getAddressList().subList(addressResponse.getAddressList().size() - offset, addressResponse.getAddressList().size()).clear();
                    }

                    List<Address> responseList = Arrays.stream(response.body()).collect(Collectors.toList());
                    addressResponse.getAddressList().addAll(responseList);
                    addressesMutableLiveData.setValue(addressResponse);
                }
            }

            @Override
            public void onFailure(@NotNull Call<Address[]> call, @NotNull Throwable t) {
                AddressResponse addressResponse = new AddressResponse();
                addressResponse.setStatusCode(500);
                addressResponse.setMessage(t.getMessage());
                addressesMutableLiveData.setValue(addressResponse);
            }
        });
        return addressesMutableLiveData;
    }


    public LiveData<AddressResponse> create(Address address) {
        addressApi.create(address).enqueue(new Callback<Address>() {
            @Override
            public void onResponse(@NotNull Call<Address> call, @NotNull Response<Address> response) {
                if (response.body() != null) {
                    AddressResponse addressResponse = new AddressResponse();
                    addressResponse.setFlag(1);
                    addressResponse.setStatusCode(response.code());
                    addressResponse.setAddressList(new ArrayList<>(addressesMutableLiveData.getValue().getAddressList()));
                    addressResponse.getAddressList().add(0, response.body());
                    addressesMutableLiveData.setValue(addressResponse);
                }
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                AddressResponse addressResponse = new AddressResponse();
                addressResponse.setStatusCode(500);
                addressResponse.setMessage(t.getMessage());
                addressesMutableLiveData.setValue(addressResponse);
            }
        });
        return addressesMutableLiveData;
    }

    public LiveData<AddressResponse> delete(String addressId, int position){
        addressApi.delete(addressId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

              //  AddressResponse addressResponse = new AddressResponse(response.code());
                addressesMutableLiveData.getValue().getAddressList().remove(position);
                AddressResponse addressResponse = new AddressResponse(new ArrayList<>(addressesMutableLiveData.getValue().getAddressList()));
                addressResponse.setStatusCode(200);
                addressesMutableLiveData.setValue(addressResponse);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                AddressResponse addressResponse = new AddressResponse(500);
                addressesMutableLiveData.setValue(addressResponse);
            }
        });
        return addressesMutableLiveData;
    }
}
