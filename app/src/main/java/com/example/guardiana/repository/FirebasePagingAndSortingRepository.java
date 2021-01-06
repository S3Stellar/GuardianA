package com.example.guardiana.repository;


import com.example.guardiana.listeners.OnDataCompleteListener;
import com.example.guardiana.listeners.OnDataErrorListener;
import com.example.guardiana.pageable.Pageable;

public interface FirebasePagingAndSortingRepository<T,ID>  extends FirebaseRepository<T,ID> {

    void findAll(Pageable pageable, OnDataCompleteListener<T> data, OnDataErrorListener err);


}
