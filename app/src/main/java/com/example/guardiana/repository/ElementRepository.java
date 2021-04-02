package com.example.guardiana.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.guardiana.model.Address;
import com.example.guardiana.model.Element;
import com.example.guardiana.services.WebElementService;

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
    private static final MutableLiveData<ElementResponse> elementsMutableLiveData = new MutableLiveData<>();
    private static ElementRepository instance;
    private static WebElementService elementsApi;

    private ElementRepository() {
    }

    public static synchronized ElementRepository getInstance() {
        if (instance == null) {
            instance = new ElementRepository();
            elementsMutableLiveData.setValue(new ElementResponse(200));
            elementsApi = new Retrofit.Builder()
                    .baseUrl(WebElementService.URL)
                    .addConverterFactory(JacksonConverterFactory.create()).build().create(WebElementService.class);
        }
        return instance;
    }

    public LiveData<ElementResponse> getAllElementsByLocationFilters(Map<String, String> attr, String sortBy, String sortOrder, int page, int size) {
        elementsApi.getAllElementsByLocationFilters(attr, sortBy, sortOrder, page, size).enqueue(new Callback<Element[]>() {
            @Override
            public void onResponse(@NotNull Call<Element[]> call, @NotNull Response<Element[]> response) {
                if (response.body() != null && response.body().length > 0) {
                    ElementResponse elementResponse = new ElementResponse();
                    elementResponse.setFlag(0);
                    elementResponse.setStatusCode(response.code());

                    elementResponse.setElementList(new ArrayList<>());

                    List<Element> responseList = Arrays.stream(response.body()).collect(Collectors.toList());
                    elementResponse.getElementList().addAll(responseList);
                    elementsMutableLiveData.setValue(elementResponse);
                }
            }

            @Override
            public void onFailure(@NotNull Call<Element[]> call,
                                  Throwable t) {
                ElementResponse elementResponse = new ElementResponse();
                elementResponse.setStatusCode(500);
                elementResponse.setMessage(t.getMessage());
                elementsMutableLiveData.setValue(elementResponse);
            }
        });
        return elementsMutableLiveData;
    }

    public LiveData<ElementResponse> create(Element element) {
        elementsApi.create(element).enqueue(new Callback<Element>() {
            @Override
            public void onResponse(@NotNull Call<Element> call, @NotNull Response<Element> response) {
                if (response.body() != null) {
                    ElementResponse elementResponse = new ElementResponse();
                    elementResponse.setFlag(1);
                    elementResponse.setStatusCode(response.code());
                    elementResponse.setElementList(new ArrayList<>(elementsMutableLiveData.getValue().getElementList()));
                    elementResponse.getElementList().add(0, response.body());
                    elementsMutableLiveData.setValue(elementResponse);
                }
            }

            @Override
            public void onFailure(@NotNull Call<Element> call, @NotNull Throwable t) {
                ElementResponse elementResponse = new ElementResponse();
                elementResponse.setStatusCode(500);
                elementResponse.setMessage(t.getMessage());
                elementsMutableLiveData.setValue(elementResponse);
            }
        });
        return elementsMutableLiveData;
    }

    public LiveData<ElementResponse> update(String elementId, Element update) {
        elementsApi.updateElement(elementId, update).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                ElementResponse elementResponse = new ElementResponse();
                elementResponse.setFlag(2);
                elementResponse.setStatusCode(response.code());
                elementResponse.setElementList(new ArrayList<>(elementsMutableLiveData.getValue().getElementList()));

                Element oldElement = elementResponse.getElementList().stream()
                        .filter(element -> elementId.equals(element.getId()))
                        .findAny()
                        .orElse(null);
     /*           Predicate<Element> condition = element -> element.getId().equals(update.getId());
                elementResponse.getElementList().removeIf(condition);
                elementResponse.getElementList().add(0, update);*/
                compareFieldsAndUpdate(oldElement, update);
                elementsMutableLiveData.setValue(elementResponse);
            }

            private void compareFieldsAndUpdate(Element oldElement, Element update) {
                if(!empty(update.getIcon()))
                    oldElement.setType(update.getType());
                if(!empty(update.getName()))
                    oldElement.setName(update.getName());
                if(update.getActive() != null)
                    oldElement.setActive(update.getActive());
                if(update.getLocation() != null)
                    oldElement.setLocation(update.getLocation());
                if(update.getElementAttribute() != null && !update.getElementAttribute().isEmpty())
                    oldElement.setElementAttribute(update.getElementAttribute());
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, Throwable t) {
                ElementResponse elementResponse = new ElementResponse();
                elementResponse.setStatusCode(500);
                elementResponse.setMessage(t.getMessage());
                elementsMutableLiveData.setValue(elementResponse);
            }
        });
        return elementsMutableLiveData;
    }

    public LiveData<ElementResponse> getAllElementsByFilters(String type, String value, String sortBy, String sortOrder, int page, int size) {
        elementsApi.getAllElementsByFilters(type, value, sortBy, sortOrder, page, size).enqueue(new Callback<Element[]>() {
            @Override
            public void onResponse(@NotNull Call<Element[]> call, @NotNull Response<Element[]> response) {
                if (response.body() != null && response.body().length > 0) {
                    ElementResponse elementResponse = new ElementResponse();
                    elementResponse.setFlag(3);
                    elementResponse.setStatusCode(response.code());

                    elementResponse.setElementList(new ArrayList<>(elementsMutableLiveData.getValue().getElementList()));

                    List<Element> responseList = Arrays.stream(response.body()).collect(Collectors.toList());
                    elementResponse.getElementList().addAll(responseList);
                    elementsMutableLiveData.setValue(elementResponse);
                }
            }

            @Override
            public void onFailure(@NotNull Call<Element[]> call,
                                  Throwable t) {
                ElementResponse elementResponse = new ElementResponse();
                elementResponse.setStatusCode(500);
                elementResponse.setMessage(t.getMessage());
                elementsMutableLiveData.setValue(elementResponse);
            }
        });
        return elementsMutableLiveData;
    }

    public LiveData<ElementResponse> deleteAll() {
        elementsApi.deleteAll().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                    ElementResponse elementResponse = new ElementResponse();
                    elementResponse.setFlag(4);
                    elementResponse.setStatusCode(response.code());
                    elementsMutableLiveData.getValue().getElementList().clear();
                    elementResponse.setElementList(new ArrayList<>(elementsMutableLiveData.getValue().getElementList()));
                    elementsMutableLiveData.setValue(elementResponse);
            }

            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                ElementResponse elementResponse = new ElementResponse();
                elementResponse.setStatusCode(500);
                elementResponse.setMessage(t.getMessage());
                elementsMutableLiveData.setValue(elementResponse);
            }
        });
        return elementsMutableLiveData;
    }

    public static boolean empty(final String s ) {
        return s == null || s.trim().isEmpty();
    }
}
