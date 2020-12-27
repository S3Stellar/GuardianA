package com.example.guardiana.model;

public class Address {
    private String city;
    private String streetAddress;
    private String state;
    private String zipCode;

    public Address() {

    }
    public Address(String city, String streetAddress){
        this.city = city;
        this.streetAddress = streetAddress;

    }
    public Address( String city, String streetAddress, String state, String zipCode){
        this(city, streetAddress);
        this.state = state;
        this.zipCode = zipCode;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
