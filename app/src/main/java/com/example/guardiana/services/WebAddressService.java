package com.example.guardiana.services;

import androidx.lifecycle.LiveData;

import com.example.guardiana.model.Address;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WebAddressService {
    public static final String URL = "http://10.0.2.2:8088/addresses/";


    @POST(".")
    Call<Address> create(@Body Address address);

    @GET("{addressId}")
    Call<Address> getSpecificAddress(@Path("addressId") String addressId);

    @PUT("{addressId}")
    Call<Void> updateAddress(@Path("addressId") String addressId, @Body Address address);

    @DELETE(".")
    Call<Void> deleteAll();

    @GET("byEmail/{user}")
    Call<Address[]> getAddressesByEmail(@Path("user") String user,
                                      @Query("filterType") String type,
                                      @Query("filterValue") String value,
                                      @Query("sortBy") String sortBy,
                                      @Query("sortOrder") String sortOrder,
                                      @Query("page") int page,
                                      @Query("size") int size);

    @GET("{addressId}")
    LiveData<Call<Address[]>> getAddressesByEmail1(@Path("user") String user,
                                        @Query("filterType") String type,
                                        @Query("filterValue") String value,
                                        @Query("sortBy") String sortBy,
                                        @Query("sortOrder") String sortOrder,
                                        @Query("page") int page,
                                        @Query("size") int size);

    Call<Address[]> getSomething();
}

