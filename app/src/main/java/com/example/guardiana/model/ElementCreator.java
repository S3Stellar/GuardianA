package com.example.guardiana.model;

public class ElementCreator {
    private String userEmail;

    public ElementCreator() {
    }

    public ElementCreator(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    @Override
    public String toString() {
        return "User [userEmail=" + userEmail + "]";
    }
}
