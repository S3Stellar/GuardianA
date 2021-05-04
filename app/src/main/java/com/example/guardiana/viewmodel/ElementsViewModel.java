package com.example.guardiana.viewmodel;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.guardiana.model.Element;
import com.example.guardiana.repository.ElementRepository;
import com.example.guardiana.repository.ElementResponse;

import java.util.Map;

public class ElementsViewModel extends ViewModel {

    private final ElementRepository elementRepository;
    private MutableLiveData<Location> locationMutableLiveData = new MutableLiveData<>();

    public ElementsViewModel() {
        elementRepository = ElementRepository.getInstance();
    }

    public LiveData<ElementResponse> create(Element element) {
        return elementRepository.create(element);
    }

    public LiveData<ElementResponse> update(String elementId, Element update) {
        return elementRepository.update(elementId, update);
    }

    public LiveData<ElementResponse> getAllElementsByLocationFilters(Map<String, String> attr, String sortBy, String sortOrder, int page, int size) {
        return elementRepository.getAllElementsByLocationFilters(attr, sortBy, sortOrder, page, size);
    }

    public LiveData<ElementResponse> getAllElements(String type, String value, String sortBy, String sortOrder, int page, int size) {
        return elementRepository.getAllElementsByFilters(type, value, sortBy, sortOrder, page, size);
    }


    public LiveData<ElementResponse> deleteAll() {
        return elementRepository.deleteAll();
    }

    public LiveData<Location> getLocation(){
        return locationMutableLiveData;
    }

    public void setLocation(Location location) {
        locationMutableLiveData.postValue(location);
    }
}
