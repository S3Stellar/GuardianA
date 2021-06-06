package com.example.guardiana.services;

import com.example.guardiana.model.UserMarker;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface WebUserMarkerService {
    //Emulator IP: 10.0.2.2
    //public static final String URL = "http://10.0.2.2:8089/markers/";
    String URL = "http://10.0.0.1:8089/markers/";


    @POST(".")
    Call<UserMarker> create(@Body UserMarker userMarker);

    @PUT("{userEmail}")
    Call<Void> updateMarker(@Path("userEmail") String userEmail, @Body UserMarker userMarker);

    @GET("markers")
    Call<UserMarker[]> getAllUserMarkersByPerimeter(@QueryMap Map<String, String> attr,
                                                    @Query("page") int page,
                                                    @Query("size") int size);
}
