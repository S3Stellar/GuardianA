package com.example.guardiana.model;

import java.util.Date;
import java.util.Objects;

public class Address {

    private String id;
    private String user;
    private String cityAddress;
    private String cityName;
    private Date createdTimestamp;
    private Location location;

    public Address() {

    }

    public Address(String id, String user, String cityAddress, String cityName, Date createdTimestamp, Location location) {
        this.id = id;
        this.user = user;
        this.cityAddress = cityAddress;
        this.cityName = cityName;
        this.createdTimestamp = createdTimestamp;
        this.location = location;
    }

    public Address(String user, String cityAddress, String cityName, Date createdTimestamp, Location location) {
        this.user = user;
        this.cityAddress = cityAddress;
        this.cityName = cityName;
        this.createdTimestamp = createdTimestamp;
        this.location = location;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public void setCityAddress(String cityAddress) {
        this.cityAddress = cityAddress;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(id, address.id) &&
                Objects.equals(user, address.user) &&
                Objects.equals(cityAddress, address.cityAddress) &&
                Objects.equals(cityName, address.cityName) &&
                Objects.equals(createdTimestamp, address.createdTimestamp) &&
                Objects.equals(location, address.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, cityAddress, cityName, createdTimestamp, location);
    }

//    @Override
//    public String toString() {
//        return "Address{" +
//                "id='" + id + '\'' +
//                ", user='" + user + '\'' +
//                ", cityAddress='" + cityAddress + '\'' +
//                ", cityName='" + cityName + '\'' +
//                ", createdTimestamp=" + createdTimestamp +
//                ", location=" + location +
//                '}';
//    }
@Override
public String toString() {
    return "Address{" +
            "id='" + id + '\'' +
            ", cityName='" + cityName + '\'' +
           '}';
}
}
