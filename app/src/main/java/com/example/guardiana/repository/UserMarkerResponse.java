package com.example.guardiana.repository;

import com.example.guardiana.model.UserMarker;

import java.util.ArrayList;
import java.util.List;

public class UserMarkerResponse {
    private List<UserMarker> userMarkerList;
    private int statusCode;
    private String message;
    private int flag;

    public UserMarkerResponse() {
        userMarkerList = new ArrayList<>();
    }

    public UserMarkerResponse(int statusCode) {
        this();
        this.statusCode = statusCode;
    }

    public UserMarkerResponse(List<UserMarker> userMarkerList, int statusCode, String message, int flag) {
        this.userMarkerList = userMarkerList;
        this.statusCode = statusCode;
        this.message = message;
        this.flag = flag;
    }

    public List<UserMarker> getUserMarkerList() {
        return userMarkerList;
    }

    public void setUserMarkerList(List<UserMarker> userMarkerList) {
        this.userMarkerList = userMarkerList;
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

    @Override
    public String toString() {
        return "ElementResponse{" +
                "elementList=" + userMarkerList +
                ", statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", flag=" + flag +
                '}';
    }
}
