package com.example.guardiana.model;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class Address {
    @DocumentId
    private String id;
    private String city;
    private String cityAddress;
    private Date date;

    public Address() {

    }
    public Address(String city, String cityAddress, Date date) {
        this.city = city;
        this.cityAddress = cityAddress;
        this.date = date;

    }
    public Address(String id, String city, String cityAddress, Date date) {
        this.id = id;
        this.city = city;
        this.cityAddress = cityAddress;
        this.date = date;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public String getCityAddress() {
        return cityAddress;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCityAddress(String cityAddress) {
        this.cityAddress = cityAddress;
    }

    @Override
    public String toString() {
        return "Address{" +
                "id='" + id + '\'' +
                ", city='" + city + '\'' +
                ", cityAddress='" + cityAddress + '\'' +
                ", date=" + date +
                '}';
    }
}
