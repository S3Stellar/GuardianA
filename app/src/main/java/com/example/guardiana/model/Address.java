package com.example.guardiana.model;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class Address {
    @DocumentId
    private String id;
    private String cityName;
    private String cityAddress;

    private double lat;
    private double lng;
    private Date date;

    public Address() {

    }

    public Address(String cityName, String cityAddress, double lat, double lng, Date date) {
        this.cityName = cityName;
        this.cityAddress = cityAddress;
        this.lat = lat;
        this.lng = lng;
        this.date = date;
    }

    public Address(String id, String cityName, String cityAddress, double lat, double lng, Date date) {
        this.id = id;
        this.cityName = cityName;
        this.cityAddress = cityAddress;
        this.lat = lat;
        this.lng = lng;
        this.date = date;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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


    public void setCityAddress(String cityAddress) {
        this.cityAddress = cityAddress;
    }

    @Override
    public String toString() {
        return "Address{" +
                "id='" + id + '\'' +
                ", cityName='" + cityName + '\'' +
                ", cityAddress='" + cityAddress + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", date=" + date +
                '}';
    }
}
