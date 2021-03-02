package com.example.guardiana.repository;

import com.example.guardiana.model.Address;

import java.util.List;

public class AddressResponse {
    private List<Address> addressList;
    private int statusCode;
    private String message;
    private int flag;

    public AddressResponse() {
    }

    public AddressResponse(List<Address> addressList, int statusCode, String message, int flag) {
        this.addressList = addressList;
        this.statusCode = statusCode;
        this.message = message;
        this.flag = flag;
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
