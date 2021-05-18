package com.example.guardiana.viewmodel;

import android.content.Context;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.guardiana.App;
import com.example.guardiana.clustermap.ReportClusterMarker;
import com.example.guardiana.customViews.AbstractBaseView;
import com.example.guardiana.customViews.resources.BottomSheetReportResource;
import com.example.guardiana.model.Element;
import com.example.guardiana.repository.ElementRepository;
import com.example.guardiana.repository.ElementResponse;
import com.example.guardiana.utility.DialogOptions;

import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ElementsViewModel extends ViewModel {

    private final ElementRepository elementRepository;
    private final MutableLiveData<Location> locationMutableLiveData = new MutableLiveData<>();

    public ElementsViewModel() {
        elementRepository = ElementRepository.getInstance();
        elementRepository.initMutableLiveData();
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

//    public LiveData<ElementResponse> getAllElements(String type, String value, String sortBy, String sortOrder, int page, int size) {
//        return elementRepository.getAllElementsByFilters(type, value, sortBy, sortOrder, page, size);
//    }


//    public LiveData<ElementResponse> deleteAll() {
//        return elementRepository.deleteAll();
//    }

    public LiveData<Location> getLocation() {
        return locationMutableLiveData;
    }

    public void setLocation(Location location) {
        locationMutableLiveData.postValue(location);
    }

    public void updateItem(Context activity, BottomSheetReportResource resources, int pos, ReportClusterMarker item) {
        Element updatedElement = item.getElement();
        Integer currentThreshold = (Integer) updatedElement.getElementAttribute().get("threshold");
        String userEmail = App.getUserId().replaceAll("\\.", "_");
        Integer reporterCount = (Integer) updatedElement.getElementAttribute().get(userEmail);
        String operation = resources.getResources().get(pos).getTextView().getText().toString();
        if (currentThreshold == null || reporterCount == null) return ;
        if (reporterCount == 0 && operation.equals(DialogOptions.BottomDialog.LIKE)) {
            // Increment
            updatedElement.getElementAttribute().put(userEmail, 1);
            updatedElement.getElementAttribute().put("threshold", currentThreshold + 1);
            update(updatedElement.getId(), updatedElement);
        } else if (reporterCount == 1 && operation.equals(DialogOptions.BottomDialog.DISLIKE)) {
            // Decrement
            updatedElement.getElementAttribute().put(userEmail, 0);
            updatedElement.getElementAttribute().put("threshold", currentThreshold - 1);
            update(updatedElement.getId(), updatedElement);
        } else {
            // bad operation
            new SweetAlertDialog(activity).setTitleText("Bad Operation").setContentText("You already said you " + operation + " it").show();
        }
    }
}
