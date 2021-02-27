package com.example.guardiana.services;

import com.example.guardiana.model.Element;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WebElementService {

    public static final String URL = "http://10.0.2.2:8087/elements/";


    @POST(".")
    Call<Element> create(@Body Element element);

    @GET("{elementId}")
    Call<Element> getSpecificElement(@Path("elementId") String elementId);

    @PUT("{elementId}")
    Call<Void> updateElement(@Path("elementId") String elementId, @Body Element element);

    @DELETE(".")
    Call<Void> deleteAll();

    @GET("location")
    Call<Element[]> getAllElementsByLocationFilters(@Query("attrMap") Map<String, String> attr,
                                                    @Query("sortBy") String sortBy,
                                                    @Query("sortOrder") String sortOrder,
                                                    @Query("page") int page,
                                                    @Query("size") int size);

    @GET(".")
    Call<Element[]> getAllElementsByFilters(@Query("type") String type,
                                            @Query("value") String value,
                                            @Query("sortBy") String sortBy,
                                            @Query("sortOrder") String sortOrder,
                                            @Query("page") int page,
                                            @Query("size") int size);
}
