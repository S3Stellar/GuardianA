package com.example.guardiana.repository;

import com.example.guardiana.model.Address;
import com.example.guardiana.utility.StatusCode;

import java.util.ArrayList;
import java.util.List;

public class AddressResponse {
    private List<Address> addressList;
    private int statusCode;
    private String message;
    private int flag;

    public AddressResponse() {
        addressList = new ArrayList<>();
    }

    public AddressResponse(List<Address> addressList, String message, int statusCode, int flag) {
        this.addressList = new ArrayList<>(addressList);
        this.message = message;
        this.statusCode = statusCode;
        this.flag = flag;
    }

    public AddressResponse(int statusCode, String message){
        this.statusCode = statusCode;
        this.message = message;
    }

    public List<Address> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<Address> addressList) {
        this.addressList = addressList;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
