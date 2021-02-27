package com.example.guardiana.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.guardiana.model.Address;
import com.example.guardiana.services.WebElementService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;


public class ElementRepository {
    private static ElementRepository instance;
    private static MutableLiveData<List<Address>> addressesMutableLiveData = new MutableLiveData<>();
    private static WebElementService elementsApi;

    public static ElementRepository getInstance() {
        if (instance == null) {
            instance = new ElementRepository();
            addressesMutableLiveData.setValue(new ArrayList<>());
            elementsApi = new Retrofit.Builder()
                    .baseUrl(WebElementService.URL)
                    .addConverterFactory(JacksonConverterFactory.create()).build().create(WebElementService.class);
        }
        return instance;
    }

    private ElementRepository() {

    }

}
