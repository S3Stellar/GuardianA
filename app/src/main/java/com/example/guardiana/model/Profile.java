package com.example.guardiana.model;


import java.util.Date;
import java.util.Objects;


public class Profile {

    private String email;

    private String name;

    private String phoneNumber;

    private int icon;

    private Date creationDate;

    public Profile() {
    }

    public Profile(Profile other) {
        this(other.email, other.name, other.phoneNumber, other.icon, other.creationDate);
    }

    public Profile(String email, String name, String phoneNumber, int icon, Date creationDate) {
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.icon = icon;
        this.creationDate = creationDate;
    }


    public Profile(String phoneNumber, int icon, Date creationDate) {
        this.phoneNumber = phoneNumber;
        this.icon = icon;
        this.creationDate = creationDate;
    }

    public Profile(String email, String name, int icon, Date creationDate) {
        this.email = email;
        this.name = name;
        this.icon = icon;
        this.creationDate = creationDate;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return icon == profile.icon &&
                email.equals(profile.email) &&
                Objects.equals(name, profile.name) &&
                Objects.equals(phoneNumber, profile.phoneNumber) &&
                Objects.equals(creationDate, profile.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, name, phoneNumber, icon, creationDate);
    }

    @Override
    public String toString() {
        return "Profile{" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", creationDate=" + creationDate +
                ", icon=" + icon +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
