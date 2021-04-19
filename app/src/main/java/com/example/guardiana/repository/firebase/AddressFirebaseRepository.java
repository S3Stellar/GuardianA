package com.example.guardiana.repository.firebase;

import com.example.guardiana.listeners.OnDataCompleteListener;
import com.example.guardiana.listeners.OnDataErrorListener;
import com.example.guardiana.model.Address;
import com.example.guardiana.pageable.Pageable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.guardiana.App.getUserId;

public class AddressFirebaseRepository implements FirebasePagingAndSortingRepository<Address, String> {

    private static final String USERS_ADDRESS_MAPPING = "UsersAddressMapping";
    private static final String ADDRESSES = "Addresses";
    private static AddressFirebaseRepository addressFirebaseRepository;
    private final CollectionReference collectionReference;
    private static DocumentSnapshot lastResult;

    private AddressFirebaseRepository() {
        collectionReference = FirebaseFirestore
                .getInstance()
                .collection(USERS_ADDRESS_MAPPING)
                .document(getUserId())
                .collection(ADDRESSES);
    }

    public static AddressFirebaseRepository getInstance() {
        if (addressFirebaseRepository == null || lastResult == null) {
            addressFirebaseRepository = new AddressFirebaseRepository();
        }
        return addressFirebaseRepository;
    }

    @Override
    public void save(Address content, OnDataCompleteListener<Address> data, OnDataErrorListener err) {
        collectionReference.document().set(content)
                .addOnCompleteListener((task) -> data.onDataCompleteListener(Collections.singletonList(content)))
                .addOnFailureListener(err::onError);
    }


    @Override
    public void findById(String id, OnDataCompleteListener<Address> onComplete, OnDataErrorListener err) {
        collectionReference.document(id).get()
                .addOnCompleteListener(task -> onComplete
                        .onDataCompleteListener(Collections.singletonList(task.getResult().toObject(Address.class))))
                .addOnFailureListener(err::onError);
    }

    @Override
    public void update(String id, Address content) {
        collectionReference.document(id)
                .update(updateData(content));
    }

    @Override
    public void findAll(Pageable pageable, OnDataCompleteListener<Address> onComplete, OnDataErrorListener err) {
        Query query = lastResult == null ?
                collectionReference.orderBy(pageable.getSort().getSortBy(), pageable.getSort().getDirection()).limit(pageable.getPageSize()) :
                collectionReference.orderBy(pageable.getSort().getSortBy(), pageable.getSort().getDirection()).startAfter(lastResult).limit(pageable.getPageSize());
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().size() > 0) {
                onComplete.onDataCompleteListener(task.getResult().getDocuments().stream().map(e -> e.toObject(Address.class)).collect(Collectors.toList()));
                lastResult = task.getResult().getDocuments().get(task.getResult().getDocuments().size() - 1);
            }
        }).addOnFailureListener(err::onError);
    }

    @Override
    public void delete(String id) {
        collectionReference.document(id).delete();
    }

    public void setLastResult(DocumentSnapshot lastResult) {
        this.lastResult = lastResult;
    }

    private Map<String, Object> updateData(Address address) {
        Map<String, Object> map = new HashMap<>();
        if (address.getCityAddress() != null && !address.getCityAddress().isEmpty()) {
            map.put("cityAddress", address.getCityAddress());
        }

        if (address.getCityName() != null && !address.getCityName().isEmpty()) {
            map.put("cityName", address.getCityName());
        }

        if (address.getLocation() != null) {
            map.put("location", address.getLocation());
        }

        return map;
    }
}
