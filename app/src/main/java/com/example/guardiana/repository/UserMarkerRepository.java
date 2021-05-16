package com.example.guardiana.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.guardiana.model.UserMarker;
import com.example.guardiana.services.WebUserMarkerService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class UserMarkerRepository {
    private static final MutableLiveData<UserMarkerResponse> userMarkerResponseMutableLiveData = new MutableLiveData<>();
    private static UserMarkerRepository instance;
    private static WebUserMarkerService userMarkersApi;

    private UserMarkerRepository() {
    }

    public static synchronized UserMarkerRepository getInstance() {
        if (instance == null) {
            instance = new UserMarkerRepository();
            userMarkerResponseMutableLiveData.setValue(new UserMarkerResponse(200));
            userMarkersApi = new Retrofit.Builder()
                    .baseUrl(WebUserMarkerService.URL)
                    .addConverterFactory(JacksonConverterFactory.create()).build().create(WebUserMarkerService.class);
        }
        return instance;
    }

    public LiveData<UserMarkerResponse> getAllUserMarkersByPerimeter(Map<String, String> attr, int page, int size) {
        userMarkersApi.getAllUserMarkersByPerimeter(attr, page, size).enqueue(new Callback<UserMarker[]>() {
            @Override
            public void onResponse(@NotNull Call<UserMarker[]> call, @NotNull Response<UserMarker[]> response) {
                if (response.body() != null && response.body().length > 0) {
                    UserMarkerResponse userMarkerResponse = new UserMarkerResponse();
                    userMarkerResponse.setFlag(0);
                    userMarkerResponse.setStatusCode(response.code());

                    userMarkerResponse.setUserMarkerList(new ArrayList<>());

                    List<UserMarker> responseList = Arrays.stream(response.body()).collect(Collectors.toList());
                    userMarkerResponse.getUserMarkerList().addAll(responseList);
                    userMarkerResponseMutableLiveData.setValue(userMarkerResponse);
                }
            }

            @Override
            public void onFailure(@NotNull Call<UserMarker[]> call,
                                  Throwable t) {
                UserMarkerResponse userMarkerResponse = new UserMarkerResponse();
                userMarkerResponse.setFlag(-1);
                userMarkerResponse.setStatusCode(500);
                userMarkerResponse.setMessage(t.getMessage());
                userMarkerResponseMutableLiveData.setValue(userMarkerResponse);
            }
        });
        return userMarkerResponseMutableLiveData;
    }

    public LiveData<UserMarkerResponse> create(UserMarker userMarker) {
        userMarkersApi.create(userMarker).enqueue(new Callback<UserMarker>() {
            @Override
            public void onResponse(@NotNull Call<UserMarker> call, @NotNull Response<UserMarker> response) {
                if (response.body() != null) {
                    UserMarkerResponse userMarkerResponse = new UserMarkerResponse();
                    userMarkerResponse.setFlag(1);
                    userMarkerResponse.setStatusCode(response.code());
                    userMarkerResponse.setUserMarkerList(new ArrayList<>(userMarkerResponseMutableLiveData.getValue().getUserMarkerList()));
                    userMarkerResponseMutableLiveData.setValue(userMarkerResponse);
                }
            }

            @Override
            public void onFailure(@NotNull Call<UserMarker> call, @NotNull Throwable t) {
                UserMarkerResponse userMarkerResponse = new UserMarkerResponse();
                userMarkerResponse.setStatusCode(500);
                userMarkerResponse.setMessage(t.getMessage());
                userMarkerResponseMutableLiveData.setValue(userMarkerResponse);
            }
        });
        return userMarkerResponseMutableLiveData;
    }

    public LiveData<UserMarkerResponse> update(String userEmail, UserMarker update) {
        userMarkersApi.updateMarker(userEmail, update).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                UserMarkerResponse userMarkerResponse = new UserMarkerResponse();
                userMarkerResponse.setFlag(2);
                userMarkerResponse.setStatusCode(response.code());
                userMarkerResponse.setUserMarkerList(new ArrayList<>(userMarkerResponseMutableLiveData.getValue().getUserMarkerList()));


            }

            @Override
            public void onFailure(@NotNull Call<Void> call, Throwable t) {
                UserMarkerResponse userMarkerResponse = new UserMarkerResponse();
                userMarkerResponse.setStatusCode(500);
                userMarkerResponse.setMessage(t.getMessage());
                userMarkerResponseMutableLiveData.setValue(userMarkerResponse);
            }
        });
        return userMarkerResponseMutableLiveData;
    }


}