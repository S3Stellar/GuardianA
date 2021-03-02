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
            addressesMutableLiveData.setValue(new AddressResponse(new ArrayList<>()));
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
                    addressResponse.setAddressList(new ArrayList<>(addressesMutableLiveData.getValue().getAddressList()));
                    addressResponse.getAddressList().add(0, response.body());
                    addressesMutableLiveData.setValue(addressResponse);
                }
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                Log.d("TAG", "onFailure: create failed " + t.getMessage());
            }
        });
        return addressesMutableLiveData;
    }
}

/*

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
    private static final MutableLiveData<List<Address>> addressesMutableLiveData = new MutableLiveData<>();
    private static AddressRepository instance;
    private static WebAddressService addressApi;

    public static synchronized AddressRepository getInstance() {
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

    public MutableLiveData<List<Address>> getAllAddresses(String userId, String type, String value, String sortBy, String sortOrder, int page, int size, int offset) {
        addressApi.getAddressesByEmail(userId, type, value, sortBy, sortOrder, page, size).enqueue(new Callback<Address[]>() {
            @Override
            public void onResponse(@NotNull Call<Address[]> call, @NotNull Response<Address[]> response) {
                if (response.body() != null && response.body().length > 0) {
                    ArrayList<Address> arrayList = new ArrayList<>(addressesMutableLiveData.getValue());

                    if(arrayList.size() >= offset){
                        arrayList.subList(arrayList.size() - offset, arrayList.size()).clear();
                    }

                    List<Address> responseList = Arrays.stream(response.body()).collect(Collectors.toList());
                    // responseList.subList(0, Math.min(offset, responseList.size())).clear();
                    arrayList.addAll(responseList);
                    addressesMutableLiveData.setValue(arrayList);
                }
            }

            @Override
            public void onFailure(@NotNull Call<Address[]> call, @NotNull Throwable t) {

            }
        });
        return addressesMutableLiveData;
    }



    public LiveData<List<Address>> create(Address address) {
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
        return addressesMutableLiveData;
    }

    public LiveData<Call<Address[]>> getLive(String userId, String type, String value, String sortBy, String sortOrder, int page, int size) {
        return addressApi.getAddressesByEmail1(userId, type, value, sortBy, sortOrder, page, size);
    }
    private MutableLiveData<Call> response = new MutableLiveData<>();
    */
/*
        Map<>
        Call
     *//*

//    public void trigger(RequestAPI page) {
//        response.setValue(page);
//    }
    public void doSomething() {

        Call w = addressApi.getSomething();
        addressApi.getSomething().enqueue(new Callback<Address[]>() {
            @Override
            public void onResponse(Call<Address[]> call, Response<Address[]> response) {

            }

            @Override
            public void onFailure(Call<Address[]> call, Throwable t) {

            }
        });
    }
}
*/
