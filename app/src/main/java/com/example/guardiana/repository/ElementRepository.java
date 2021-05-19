package com.example.guardiana.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.guardiana.model.Address;
import com.example.guardiana.model.Element;
import com.example.guardiana.services.WebElementService;
import com.example.guardiana.utility.StatusCode;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;


public class ElementRepository {
    private MutableLiveData<ElementResponse> elementsMutableLiveData;
    private static ElementRepository instance;
    private static WebElementService elementsApi;

    private ElementRepository() {
    }

    public static synchronized ElementRepository getInstance() {
        if (instance == null) {
            instance = new ElementRepository();
            elementsApi = new Retrofit.Builder()
                    .baseUrl(WebElementService.URL)
                    .addConverterFactory(JacksonConverterFactory.create()).build().create(WebElementService.class);
        }
        return instance;
    }

    public void initMutableLiveData() {
        elementsMutableLiveData = new MutableLiveData<>();
        elementsMutableLiveData.setValue(new ElementResponse());
    }

    public LiveData<ElementResponse> getAllElementsByLocationFilters(Map<String, String> attr, String sortBy, String sortOrder, int page, int size) {
        elementsApi.getAllElementsByLocationFilters(attr, sortBy, sortOrder, page, size).enqueue(new Callback<Element[]>() {
            @Override
            public void onResponse(@NotNull Call<Element[]> call, @NotNull Response<Element[]> response) {
                if (response.body() != null && response.body().length > 0) {
                    // Create new element response
                    ElementResponse elementResponse = new ElementResponse();

                    // Set the flag to get in the response
                    elementResponse.setFlag(ElementResponse.flagTypes.GET.ordinal());

                    // Set the status - should be status 20[0-9]
                    elementResponse.setStatusCode(response.code());

                    // Create new list for the response and add all the items from the database
                    elementResponse.setElementList(new ArrayList<>());
                    List<Element> responseList = Arrays.stream(response.body()).collect(Collectors.toList());
                    elementResponse.getElementList().addAll(responseList);

                    // Trigger the observers
                    elementsMutableLiveData.setValue(elementResponse);
                }
            }

            @Override
            public void onFailure(@NotNull Call<Element[]> call, Throwable t) {
                setFailureResponse(t);
            }
        });
        return elementsMutableLiveData;
    }

    public LiveData<ElementResponse> create(Element element) {
        elementsApi.create(element).enqueue(new Callback<Element>() {
            @Override
            public void onResponse(@NotNull Call<Element> call, @NotNull Response<Element> response) {
                if (response.body() != null) {
                    trigger(ElementResponse.flagTypes.CREATE.ordinal(), response.code());
                }
            }

            @Override
            public void onFailure(@NotNull Call<Element> call, @NotNull Throwable t) {
                setFailureResponse(t);
            }
        });
        return elementsMutableLiveData;
    }

    public LiveData<ElementResponse> update(String elementId, Element update) {
        elementsApi.updateElement(elementId, update).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                trigger(ElementResponse.flagTypes.UPDATE.ordinal(), response.code());
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, Throwable t) {
                setFailureResponse(t);
            }
        });
        return elementsMutableLiveData;
    }

    private void trigger(int type, int code) {
        // Create new element response
        ElementResponse elementResponse = new ElementResponse();

        // Set the flag type as update
        elementResponse.setFlag(type);

        // Set the status - should be status 20[0-9]
        elementResponse.setStatusCode(code);

        // Trigger observers
        elementsMutableLiveData.setValue(elementResponse);
    }

    private void setFailureResponse(Throwable t) {
        // Create element response for failure
        ElementResponse elementResponse = new ElementResponse();

        // Set the response as internal server error
        elementResponse.setStatusCode(StatusCode.INTERNAL_SERVER_ERROR);

        // Set the message from the server
        elementResponse.setMessage(t.getMessage());

        // Trigger the observers
        elementsMutableLiveData.setValue(elementResponse);
    }


//    public LiveData<ElementResponse> getAllElementsByFilters(String type, String value, String sortBy, String sortOrder, int page, int size) {
//        elementsApi.getAllElementsByFilters(type, value, sortBy, sortOrder, page, size).enqueue(new Callback<Element[]>() {
//            @Override
//            public void onResponse(@NotNull Call<Element[]> call, @NotNull Response<Element[]> response) {
//                if (response.body() != null && response.body().length > 0) {
//                    ElementResponse elementResponse = new ElementResponse();
//                    elementResponse.setFlag(3);
//                    elementResponse.setStatusCode(response.code());
//
//                    elementResponse.setElementList(new ArrayList<>(elementsMutableLiveData.getValue().getElementList()));
//
//                    List<Element> responseList = Arrays.stream(response.body()).collect(Collectors.toList());
//                    elementResponse.getElementList().addAll(responseList);
//                    elementsMutableLiveData.setValue(elementResponse);
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<Element[]> call,
//                                  Throwable t) {
//                ElementResponse elementResponse = new ElementResponse();
//                elementResponse.setStatusCode(500);
//                elementResponse.setMessage(t.getMessage());
//                elementsMutableLiveData.setValue(elementResponse);
//            }
//        });
//        return elementsMutableLiveData;
//    }
//
//    public LiveData<ElementResponse> deleteAll() {
//        elementsApi.deleteAll().enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
//                ElementResponse elementResponse = new ElementResponse();
//                elementResponse.setFlag(4);
//                elementResponse.setStatusCode(response.code());
//                elementsMutableLiveData.getValue().getElementList().clear();
//                elementResponse.setElementList(new ArrayList<>(elementsMutableLiveData.getValue().getElementList()));
//                elementsMutableLiveData.setValue(elementResponse);
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
//                ElementResponse elementResponse = new ElementResponse();
//                elementResponse.setStatusCode(500);
//                elementResponse.setMessage(t.getMessage());
//                elementsMutableLiveData.setValue(elementResponse);
//            }
//        });
//        return elementsMutableLiveData;
//    }


}
