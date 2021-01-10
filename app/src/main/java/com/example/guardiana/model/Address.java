package com.example.guardiana.model;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;
import java.util.Objects;

public class Address {
    @DocumentId
    private String id;
    private String cityName;
    private String cityAddress;
    private Location location;
    private Date date;

    public Address() {

    }

    public Address(String cityName, String cityAddress, Location location, Date date) {
        this.cityName = cityName;
        this.cityAddress = cityAddress;
        this.location = location;
        this.date = date;
    }

    public Address(String id, String cityName, String cityAddress, Location location, Date date) {
        this.id = id;
        this.cityName = cityName;
        this.cityAddress = cityAddress;
        this.location = location;
        this.date = date;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(cityName, address.cityName) &&
                Objects.equals(cityAddress, address.cityAddress) &&
                Objects.equals(location, address.location) &&
                Objects.equals(date, address.date);
    }

    @Override
    public String toString() {
        return "Address{" +
                "id='" + id + '\'' +
                ", cityName='" + cityName + '\'' +
                ", cityAddress='" + cityAddress + '\'' +
                ", location=" + location +
                ", date=" + date +
                '}';
    }
}
