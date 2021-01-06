package com.example.guardiana.repository;


import com.example.guardiana.listeners.OnDataCompleteListener;
import com.example.guardiana.listeners.OnDataErrorListener;

public interface FirebaseRepository<T, ID> {

    void save(T content, OnDataCompleteListener<T> data, OnDataErrorListener err);

    void findById(ID id, OnDataCompleteListener<T> data, OnDataErrorListener err);

    void update(ID id, T content);

    void delete(ID id);

}
