package com.example.guardiana.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.guardiana.model.UserMarker;
import com.example.guardiana.repository.UserMarkerRepository;
import com.example.guardiana.repository.UserMarkerResponse;

import java.util.Map;

public class UserMarkerViewModel extends ViewModel {

    private final UserMarkerRepository userMarkerRepository;

    public UserMarkerViewModel() {
        userMarkerRepository = UserMarkerRepository.getInstance();
    }

    public LiveData<UserMarkerResponse> create(UserMarker userMarker) {
        return userMarkerRepository.create(userMarker);
    }

    public LiveData<UserMarkerResponse> update(String userEmail, UserMarker update) {
        return userMarkerRepository.update(userEmail, update);
    }

    public LiveData<UserMarkerResponse> getAllElementsByLocationFilters(
            Map<String, String> attr, int page, int size) {
        return userMarkerRepository.getAllUserMarkersByPerimeter(attr, page, size);
    }

}